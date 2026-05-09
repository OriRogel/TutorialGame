package com.example.tutorialgame.managers;

import static com.example.tutorialgame.engine.core.GameConstants.Sprite.TILE_SIZE;
import com.example.tutorialgame.MyApp;
import com.example.tutorialgame.cloud.document.WorldStateDoc;
import com.example.tutorialgame.engine.audio.MusicManager;
import com.example.tutorialgame.entities.characters.Character;
import com.example.tutorialgame.entities.characters.GameCharacters;
import com.example.tutorialgame.entities.characters.Player;
import com.example.tutorialgame.entities.characters.nonenemies.neutral.BlackKnight;
import com.example.tutorialgame.entities.characters.nonenemies.neutral.Father;
import com.example.tutorialgame.entities.characters.nonenemies.neutral.WhiteKnight;
import com.example.tutorialgame.environments.GameMap;
import com.example.tutorialgame.ui.base.BaseActivity;
import com.example.tutorialgame.R;
import java.util.List;
import java.util.Objects;

public class WorldEventManager {

    public static void triggerEvent(String eventId) {
        switch (eventId) {
            case "TALKED_TO_FRIEND_FIRST_TIME":
                Character friend = MapManager.getCharacterById("BEST_FRIEND", "village.tmx");
                if (friend != null) {
                    friend.setHome(friend.getHitBox().left - TILE_SIZE * 2, friend.getHitBox().top + TILE_SIZE*0.3f);
                }
                break;

            case "RECEIVED_SWORD_FROM_BLACKSMITH":
                moveCharacterToMap("BEST_FRIEND", "village.tmx", "weapon_store.tmx", TILE_SIZE * 2.2f, TILE_SIZE * 8.4f);
                Father father = (Father) MapManager.getCharacterById("FATHER", "chief_home.tmx");
                Objects.requireNonNull(father).teleportTo(90*TILE_SIZE, 90*TILE_SIZE);
                setDoorState("weapon_store.tmx", "weapon_store_to_village", false);
                MapManager.getCurrentMap().getPlayer().setWeapon(MyApp.getWorldStateDoc().getCurrentWeapon());
                break;

            case "TALKED_TO_FRIEND_IN_SHOP":
                setDoorState("weapon_store.tmx", "weapon_store_to_village", true);
                moveCharacterToMap("BEST_FRIEND", "weapon_store.tmx", "village.tmx", TILE_SIZE * 25, TILE_SIZE * 17);
                break;

            case "TALKED_TO_WHITE_KNIGHT":
                WhiteKnight wk = (WhiteKnight) MapManager.getCharacterById("WHITE_KNIGHT", "village.tmx");
                BlackKnight bk = (BlackKnight) MapManager.getCharacterById("BLACK_KNIGHT", "village.tmx");
                if (wk != null) wk.setAngry(true);
                if (bk != null) bk.setAngry(true);
                father = (Father) MapManager.getCharacterById("FATHER", "chief_home.tmx");
                Objects.requireNonNull(father).teleportTo(90*TILE_SIZE, 90*TILE_SIZE);
                break;
            case "ENTER_MAZE":
                Player p = MapManager.getCurrentMap().getPlayer();
                List<String> lines = DialogueManager.resolveDialogue(GameCharacters.PLAYER.name());
                p.setInteriorDialogue(lines);
                break;
            case "RUNWAY":
                setDoorState("maze.tmx", "maze_to_chief_home", true);
                GameMap maze = MapManager.getMapByName("maze.tmx");
                maze.setMusicRes(R.raw.music_runaway); // עדכון המוזיקה במפה
                maze.setSpawnType("SKELETON");
                maze.setMinMonsters(50);
                
                // אם אנחנו כבר במבוך, נפעיל את המוזיקה מיד
                if (MapManager.getCurrentMap() == maze) {
                    MusicManager.getInstance(BaseActivity.getContext()).play(R.raw.music_runaway);
                    maze.spawnMonsterOnPlayer(5);
                }
                break;
        }
    }

    public static void refreshWorldState() {
        WorldStateDoc doc = MyApp.getWorldStateDoc();

        if (doc.getCheckPoint("seen_cutscene_skeletonArise")) {
            triggerEvent("RUNWAY");
        }
        else if (doc.getCheckPoint("event_enter_maze")) {
            setDoorState("maze.tmx", "maze_to_chief_home", false);
        } else if (doc.getCheckPoint("event_player_talkedToFriend2")) {
            moveCharacterToMap("BEST_FRIEND", "weapon_store.tmx", "village.tmx", TILE_SIZE * 25, TILE_SIZE * 17);
            if (doc.getCheckPoint("event_player_talkedToWhiteKnight")) {
                triggerEvent("TALKED_TO_WHITE_KNIGHT");
            }
        } else if (doc.getCheckPoint("event_player_receivedWeapon")) {
            triggerEvent("RECEIVED_SWORD_FROM_BLACKSMITH");
        } else if (doc.getCheckPoint("event_player_talkedToFriend")) {
            triggerEvent("TALKED_TO_FRIEND_FIRST_TIME");
        }
    }

    private static void moveCharacterToMap(String charId, String defaultMap, String targetMapName, float x, float y) {
        Character c = MapManager.getCharacterById(charId, targetMapName);
        if (c == null) {
            c = MapManager.getCharacterById(charId, defaultMap);
        }
        if (c != null) {
            if (MapManager.getCurrentMap().getFileName().equals(targetMapName) && c.getHitBox().left == x && c.getHitBox().top == y) {
                return;
            }
            c.moveToMap(targetMapName, x, y);
        }
    }

    public static void setDoorState(String mapName, String doorName, boolean active) {
        MapManager.getMapByName(mapName).getDoorwayByName(doorName).setDoorwayActive(active);
    }
}
