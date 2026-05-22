package com.example.tutorialgame.managers;

import static com.example.tutorialgame.engine.core.GameConstants.Sprite.TILE_SIZE;
import com.example.tutorialgame.MyApp;
import com.example.tutorialgame.cloud.document.WorldStateDoc;
import com.example.tutorialgame.entities.characters.GameCharacters;
import com.example.tutorialgame.entities.characters.nonenemies.neutral.BlackKnight;
import com.example.tutorialgame.entities.characters.nonenemies.neutral.Father;
import com.example.tutorialgame.entities.characters.nonenemies.neutral.WhiteKnight;
import com.example.tutorialgame.managers.worldactions.WorldActions;
import com.example.tutorialgame.managers.worldactions.WorldAction;
import com.example.tutorialgame.R;
import java.util.HashMap;
import java.util.Map;

public class WorldEventManager {
    private static final Map<String, WorldAction> events = new HashMap<>();

    static {
        // TALKED_TO_FRIEND_FIRST_TIME
        events.put("TALKED_TO_FRIEND_FIRST_TIME", () -> 
            new WorldActions.SetHome("BEST_FRIEND", "village.tmx", -TILE_SIZE * 2, TILE_SIZE * 0.3f, true).execute());

        // RECEIVED_SWORD_FROM_BLACKSMITH
        events.put("RECEIVED_SWORD_FROM_BLACKSMITH", () -> {
            new WorldActions.MoveNpc("BEST_FRIEND", "village.tmx", "weapon_store.tmx", TILE_SIZE * 2.2f, TILE_SIZE * 8.4f).execute();
            new WorldActions.SetDoor("weapon_store.tmx", "weapon_store_to_village", false).execute();
            new WorldActions.UpdateWeapon().execute();
        });

        // TALKED_TO_FRIEND_IN_SHOP
        events.put("TALKED_TO_FRIEND_IN_SHOP", () -> {
            new WorldActions.SetDoor("weapon_store.tmx", "weapon_store_to_village", true).execute();
            new WorldActions.MoveNpc("BEST_FRIEND", "weapon_store.tmx", "village.tmx", TILE_SIZE * 25, TILE_SIZE * 17).execute();
        });

        // ENTER_MAZE
        events.put("ENTER_MAZE", () -> 
            new WorldActions.SetDialogue(GameCharacters.PLAYER.name()).execute());

        // RUNWAY
        events.put("RUNWAY", () -> {
            new WorldActions.SetDoor("maze.tmx", "maze_to_chief_home", true).execute();
            new WorldActions.ChangeMusic("maze.tmx", R.raw.music_runaway).execute();
            new WorldActions.SpawnMonsters("maze.tmx", "SKELETON", 50, 5).execute();
        });
    }

    public static void triggerEvent(String eventId) {
        WorldAction event = events.get(eventId);
        if (event != null) {
            event.execute();
        }

        // Special cases that need custom logic (like Knights or Father teleport)
        switch (eventId) {
            case "RECEIVED_SWORD_FROM_BLACKSMITH":
            case "TALKED_TO_WHITE_KNIGHT":
                Father father = (Father) MapManager.getCharacterById("FATHER", "chief_home.tmx");
                if (father != null) father.teleportTo(90 * TILE_SIZE, 90 * TILE_SIZE);
                
                if ("TALKED_TO_WHITE_KNIGHT".equals(eventId)) {
                    WhiteKnight wk = (WhiteKnight) MapManager.getCharacterById("WHITE_KNIGHT", "village.tmx");
                    BlackKnight bk = (BlackKnight) MapManager.getCharacterById("BLACK_KNIGHT", "village.tmx");
                    if (wk != null) wk.setAngry(true);
                    if (bk != null) bk.setAngry(true);
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
            triggerEvent("TALKED_TO_FRIEND_IN_SHOP");
            if (doc.getCheckPoint("event_player_talkedToWhiteKnight")) {
                triggerEvent("TALKED_TO_WHITE_KNIGHT");
            }
        } else if (doc.getCheckPoint("event_player_receivedWeapon")) {
            triggerEvent("RECEIVED_SWORD_FROM_BLACKSMITH");
        } else if (doc.getCheckPoint("event_player_talkedToFriend")) {
            triggerEvent("TALKED_TO_FRIEND_FIRST_TIME");
        }
    }

    public static void setDoorState(String mapName, String doorName, boolean active) {
        new WorldActions.SetDoor(mapName, doorName, active).execute();
    }
}
