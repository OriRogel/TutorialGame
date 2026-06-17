package com.example.tutorialgame.managers.worldactions;

import android.util.Log;

import com.example.tutorialgame.cloud.UserRepository;
import com.example.tutorialgame.engine.interfaces.StateSwitcher;
import com.example.tutorialgame.ui.base.BaseActivity;

import java.util.Map;

/**
 * Factory class to create WorldAction instances from XML data.
 * Updated to use the consolidated WorldActions container and strict validation.
 */
public class ActionFactory {
    private static final String TAG = "ActionFactory";
    private final WorldActions worldActions;
    private final UserRepository userRepository;

    public ActionFactory(StateSwitcher switcher, UserRepository repo) {
        this.worldActions = new WorldActions(switcher);
        this.userRepository = repo;
    }

    public WorldAction createAction(String type, Map<String, String> params) {
        if (type == null) return null;
        
        String upperType = type.toUpperCase();
        switch (upperType) {
            case "MOVE_NPC":
                if (!validateParams(upperType, params, "char", "from", "to", "x", "y")) return null;
                return new WorldActions.MoveNpc(
                        params.get("char"),
                        params.get("from"),
                        params.get("to"),
                        getFloat(params, "x", 0),
                        getFloat(params, "y", 0)
                );
            case "SET_DOOR":
                if (!validateParams(upperType, params, "map", "door", "active")) return null;
                return new WorldActions.SetDoor(
                        params.get("map"),
                        params.get("door"),
                        Boolean.parseBoolean(params.get("active"))
                );
            case "TELEPORT":
                if (!validateParams(upperType, params, "char", "map", "x", "y")) return null;
                return new WorldActions.Teleport(
                        params.get("char"),
                        params.get("map"),
                        getFloat(params, "x", 0),
                        getFloat(params, "y", 0)
                );
            case "SET_HOME":
                if (!validateParams(upperType, params, "char", "map", "x", "y", "relative")) return null;
                return new WorldActions.SetHome(
                        params.get("char"),
                        params.get("map"),
                        getFloat(params, "x", 0),
                        getFloat(params, "y", 0),
                        Boolean.parseBoolean(params.get("relative"))
                );
            case "SET_DIALOGUE":
                if (!validateParams(upperType, params, "speakerType")) return null;
                return new WorldActions.SetDialogue(
                        params.get("speakerType"),
                        userRepository != null ? userRepository.getWorldStateDoc() : null
                );
            case "SPAWN_MONSTERS":
                if (!validateParams(upperType, params, "map", "spawnType", "minCount", "immediateCount")) return null;
                return new WorldActions.SpawnMonsters(
                        params.get("map"),
                        params.get("spawnType"),
                        getInt(params, "minCount", 0),
                        getInt(params, "immediateCount", 0)
                );
            case "CHANGE_MUSIC":
                if (!validateParams(upperType, params, "map", "music")) return null;
                int resId = getInt(params, "musicRes", 0);
                if (resId == 0) {
                    String musicName = params.get("music");
                    resId = BaseActivity.getContext().getResources().getIdentifier(
                            musicName, "raw", BaseActivity.getContext().getPackageName());
                }
                return new WorldActions.ChangeMusic(params.get("map"), resId);
            case "UPDATE_WEAPON":
            case "GIVE_WEAPON":
                if (!validateParams(upperType, params, "weapon")) return null;
                return new WorldActions.UpdateWeapon(params.get("weapon"), userRepository);
            case "CHANGE_STATE":
                if (!validateParams(upperType, params, "state")) return null;
                return worldActions.new ChangeState(params.get("state"));
            default:
                Log.w(TAG, "Unknown action type: " + type);
                return null;
        }
    }

    private static boolean validateParams(String type, Map<String, String> params, String... required) {
        for (String req : required) {
            if (!params.containsKey(req) || params.get(req) == null || params.get(req).isEmpty()) {
                Log.e(TAG, "MISSING PARAMETER: Action '" + type + "' requires '" + req + "' but it was not found in quests.xml!");
                return false;
            }
        }
        return true;
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
