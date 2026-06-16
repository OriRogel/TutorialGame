package com.example.tutorialgame.managers;

import static com.example.tutorialgame.engine.core.GameConstants.Sprite.TILE_SIZE;
import com.example.tutorialgame.cloud.UserRepository;
import com.example.tutorialgame.cloud.document.WorldStateDoc;
import com.example.tutorialgame.engine.interfaces.StateSwitcher;
import com.example.tutorialgame.entities.characters.GameCharacters;
import com.example.tutorialgame.entities.characters.nonenemies.neutral.BlackKnight;
import com.example.tutorialgame.entities.characters.nonenemies.neutral.Father;
import com.example.tutorialgame.entities.characters.nonenemies.neutral.WhiteKnight;
import com.example.tutorialgame.managers.worldactions.ActionFactory;
import com.example.tutorialgame.managers.worldactions.WorldAction;
import java.util.HashMap;
import java.util.Map;

public class WorldEventManager {
    private static final Map<String, WorldAction> events = new HashMap<>();
    private static ActionFactory actionFactory;
    private static UserRepository userRepository;

    public static void init(StateSwitcher switcher, UserRepository repo) {
        userRepository = repo;
        actionFactory = new ActionFactory(switcher, repo);
        setupEvents();
    }

    private static void setupEvents() {
        events.clear();
        // TALKED_TO_FRIEND_FIRST_TIME
        events.put("TALKED_TO_FRIEND_FIRST_TIME", actionFactory.createAction("SET_HOME", createParams("char", "BEST_FRIEND", "map", "village.tmx", "x", String.valueOf(-TILE_SIZE * 2), "y", String.valueOf(TILE_SIZE * 0.3f), "relative", "true")));

        // RECEIVED_SWORD_FROM_BLACKSMITH
        events.put("RECEIVED_SWORD_FROM_BLACKSMITH", () -> {
            actionFactory.createAction("MOVE_NPC", createParams("char", "BEST_FRIEND", "from", "village.tmx", "to", "weapon_store.tmx", "x", String.valueOf(TILE_SIZE * 2.2f), "y", String.valueOf(TILE_SIZE * 8.4f))).execute();
            actionFactory.createAction("SET_DOOR", createParams("map", "weapon_store.tmx", "door", "weapon_store_to_village", "active", "false")).execute();
            actionFactory.createAction("UPDATE_WEAPON", createParams("weapon", "")).execute();
        });

        // TALKED_TO_FRIEND_IN_SHOP
        events.put("TALKED_TO_FRIEND_IN_SHOP", () -> {
            actionFactory.createAction("SET_DOOR", createParams("map", "weapon_store.tmx", "door", "weapon_store_to_village", "active", "true")).execute();
            actionFactory.createAction("MOVE_NPC", createParams("char", "BEST_FRIEND", "from", "weapon_store.tmx", "to", "village.tmx", "x", String.valueOf(TILE_SIZE * 25), "y", String.valueOf(TILE_SIZE * 17))).execute();
        });

        // ENTER_MAZE
        events.put("ENTER_MAZE", () -> 
            actionFactory.createAction("SET_DIALOGUE", createParams("speakerType", GameCharacters.PLAYER.name())).execute());

        // RUNWAY
        events.put("RUNWAY", () -> {
            actionFactory.createAction("SET_DOOR", createParams("map", "maze.tmx", "door", "maze_to_chief_home", "active", "true")).execute();
            actionFactory.createAction("CHANGE_MUSIC", createParams("map", "maze.tmx", "music", "music_runaway")).execute();
            actionFactory.createAction("SPAWN_MONSTERS", createParams("map", "maze.tmx", "spawnType", "SKELETON", "minCount", "50", "immediateCount", "5")).execute();
        });
    }

    private static Map<String, String> createParams(String... kvs) {
        Map<String, String> params = new HashMap<>();
        for (int i = 0; i < kvs.length; i += 2) {
            params.put(kvs[i], kvs[i+1]);
        }
        return params;
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
        if (userRepository == null) return;
        WorldStateDoc doc = userRepository.getWorldStateDoc();

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
        if (actionFactory != null) {
            actionFactory.createAction("SET_DOOR", createParams("map", mapName, "door", doorName, "active", String.valueOf(active))).execute();
        }
    }
}