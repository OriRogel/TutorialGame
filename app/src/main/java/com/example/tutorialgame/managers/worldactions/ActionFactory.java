package com.example.tutorialgame.managers.worldactions;

import java.util.Map;

/**
 * Factory class to create WorldAction instances from XML data.
 * Updated to use the consolidated WorldActions container.
 */
public class ActionFactory {

    public static WorldAction createAction(String type, Map<String, String> params) {
        if (type == null) return null;
        
        switch (type.toUpperCase()) {
            case "MOVE_NPC":
                return new WorldActions.MoveNpc(
                        params.get("char"),
                        params.get("from"),
                        params.get("to"),
                        getFloat(params, "x", 0),
                        getFloat(params, "y", 0)
                );
            case "SET_DOOR":
                return new WorldActions.SetDoor(
                        params.get("map"),
                        params.get("door"),
                        Boolean.parseBoolean(params.get("active"))
                );
            case "TELEPORT":
                return new WorldActions.Teleport(
                        params.get("char"),
                        params.get("map"),
                        getFloat(params, "x", 0),
                        getFloat(params, "y", 0)
                );
            case "SET_HOME":
                return new WorldActions.SetHome(
                        params.get("char"),
                        params.get("map"),
                        getFloat(params, "x", 0),
                        getFloat(params, "y", 0),
                        Boolean.parseBoolean(params.get("relative"))
                );
            case "SET_DIALOGUE":
                return new WorldActions.SetDialogue(
                        params.get("speakerType")
                );
            case "SPAWN_MONSTERS":
                return new WorldActions.SpawnMonsters(
                        params.get("map"),
                        params.get("spawnType"),
                        getInt(params, "minCount", 0),
                        getInt(params, "immediateCount", 0)
                );
            case "CHANGE_MUSIC":
                return new WorldActions.ChangeMusic(
                        params.get("map"),
                        getInt(params, "musicRes", 0)
                );
            case "UPDATE_WEAPON":
            case "GIVE_WEAPON":
                return new WorldActions.UpdateWeapon(params.get("weapon"));
            case "CHANGE_STATE":
                return new WorldActions.ChangeState(params.get("state"));
            default:
                return null;
        }
    }

    private static float getFloat(Map<String, String> params, String key, float defaultValue) {
        String val = params.get(key);
        try {
            return val != null ? Float.parseFloat(val) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static int getInt(Map<String, String> params, String key, int defaultValue) {
        String val = params.get(key);
        try {
            return val != null ? Integer.parseInt(val) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
