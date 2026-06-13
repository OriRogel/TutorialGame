package com.example.tutorialgame.managers.worldactions;

import com.example.tutorialgame.MyApp;
import com.example.tutorialgame.engine.audio.MusicManager;
import com.example.tutorialgame.engine.core.GamePanel;
import com.example.tutorialgame.entities.characters.Character;
import com.example.tutorialgame.entities.characters.Player;
import com.example.tutorialgame.environments.GameMap;
import com.example.tutorialgame.gamestates.State;
import com.example.tutorialgame.managers.DialogueManager;
import com.example.tutorialgame.managers.MapManager;
import com.example.tutorialgame.ui.base.BaseActivity;

import java.util.List;

/**
 * A container for all atomic world changes.
 * Consolidating actions into one file for easier navigation and parameter lookup.
 */
public class WorldActions {

    /** Moves a character to a different map. */
    public static class MoveNpc implements WorldAction {
        private final String charId, fromMap, toMap;
        private final float x, y;

        public MoveNpc(String charId, String fromMap, String toMap, float x, float y) {
            this.charId = charId; this.fromMap = fromMap; this.toMap = toMap;
            this.x = x; this.y = y;
        }

        @Override
        public void execute() {
            Character c = MapManager.getCharacterById(charId, toMap);
            if (c == null) c = MapManager.getCharacterById(charId, fromMap);
            if (c != null) c.moveToMap(toMap, x, y);
        }
    }

    /** Enables or disables a doorway. */
    public static class SetDoor implements WorldAction {
        private final String mapName, doorName;
        private final boolean active;

        public SetDoor(String mapName, String doorName, boolean active) {
            this.mapName = mapName; this.doorName = doorName; this.active = active;
        }

        @Override
        public void execute() {
            GameMap map = MapManager.getMapByName(mapName);
            if (map.getDoorwayByName(doorName) != null) {
                map.getDoorwayByName(doorName).setDoorwayActive(active);
            }
        }
    }

    /** Instantly teleports a character on its current map. */
    public static class Teleport implements WorldAction {
        private final String charId, mapName;
        private final float x, y;

        public Teleport(String charId, String mapName, float x, float y) {
            this.charId = charId; this.mapName = mapName; this.x = x; this.y = y;
        }

        @Override
        public void execute() {
            Character c = MapManager.getCharacterById(charId, mapName);
            if (c != null) {
                c.teleportTo(x, y);
            }
        }
    }

    /** Changes a character's home (idle) position. */
    public static class SetHome implements WorldAction {
        private final String charId, mapName;
        private final float x, y;
        private final boolean relative;

        public SetHome(String charId, String mapName, float x, float y, boolean relative) {
            this.charId = charId; this.mapName = mapName; this.x = x; this.y = y; this.relative = relative;
        }

        @Override
        public void execute() {
            Character c = MapManager.getCharacterById(charId, mapName);
            if (c != null) {
                float fx = relative ? c.getHitBox().left + x : x;
                float fy = relative ? c.getHitBox().top + y : y;
                c.setHome(fx, fy);
            }
        }
    }

    /** Triggers thoughts/interior dialogue for the player. */
    public static class SetDialogue implements WorldAction {
        private final String speakerType;

        public SetDialogue(String speakerType) { this.speakerType = speakerType; }

        @Override
        public void execute() {
            Player p = MapManager.getCurrentMap().getPlayer();
            if (p != null) {
                List<String> lines = DialogueManager.resolveDialogue(speakerType);
                p.setInteriorDialogue(lines);
            }
        }
    }

    /** Configures monster spawning for a map. */
    public static class SpawnMonsters implements WorldAction {
        private final String mapName, spawnType;
        private final int minCount, immediateCount;

        public SpawnMonsters(String mapName, String spawnType, int minCount, int immediateCount) {
            this.mapName = mapName; this.spawnType = spawnType;
            this.minCount = minCount; this.immediateCount = immediateCount;
        }

        @Override
        public void execute() {
            GameMap map = MapManager.getMapByName(mapName);
            map.setSpawnType(spawnType);
            map.setMinMonsters(minCount);
            if (immediateCount > 0) map.spawnMonsterOnPlayer(immediateCount);
        }
    }

    /** Changes music for a specific map. */
    public static class ChangeMusic implements WorldAction {
        private final String mapName;
        private final int musicRes;

        public ChangeMusic(String mapName, int musicRes) {
            this.mapName = mapName; this.musicRes = musicRes;
        }

        @Override
        public void execute() {
            GameMap map = MapManager.getMapByName(mapName);
            map.setMusicRes(musicRes);
            if (MapManager.getCurrentMap() == map) {
                MusicManager.getInstance(BaseActivity.getContext()).play(musicRes);
            }
        }
    }

    /** Updates the player's weapon. If weaponId is provided, it sets it in the save state first. */
    public static class UpdateWeapon implements WorldAction {
        private final String weaponId;

        public UpdateWeapon() { this(null); }
        public UpdateWeapon(String weaponId) { this.weaponId = weaponId; }

        @Override
        public void execute() {
            if (weaponId != null && !weaponId.isEmpty()) {
                MyApp.getWorldStateDoc().setCurrentWeapon(weaponId);
            }
            
            Player p = MapManager.getCurrentMap().getPlayer();
            if (p != null) p.setWeapon(MyApp.getWorldStateDoc().getCurrentWeapon());
        }
    }

    /** Changes the overall Game State (e.g., to CUTSCENE). */
    public static class ChangeState implements WorldAction {
        private final String stateName;
        public ChangeState(String stateName) { this.stateName = stateName; }

        @Override
        public void execute() {
            try {
                GamePanel panel = com.example.tutorialgame.MyApp.getGamePanel();
                if (panel != null && panel.getGame() != null) {
                    panel.getGame().changeState(State.valueOf(stateName.toUpperCase()));
                }
            } catch (Exception ignored) {}
        }
    }
}
