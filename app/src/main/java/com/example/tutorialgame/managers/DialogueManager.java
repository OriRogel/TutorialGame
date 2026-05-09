package com.example.tutorialgame.managers;

import android.util.Log;
import com.example.tutorialgame.MyApp;
import com.example.tutorialgame.cloud.document.WorldStateDoc;
import com.example.tutorialgame.ui.base.BaseActivity;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * High-performance engine for parsing and managing game dialogues.
 * Uses a thread-safe cache and JSON branching logic based on world states.
 */
public final class DialogueManager {
    private static final String TAG = "DialogueManager";
    private static final Gson gson = new Gson();
    private static final Type STRING_LIST_TYPE = new TypeToken<List<String>>(){}.getType();

    // Thread-safe cache to avoid repeated disk reads
    private static final Map<String, JsonElement> dialogueCache = new ConcurrentHashMap<>();
    private static volatile String loadedLanguage = "";

    private DialogueManager() {}

    /**
     * Synchronizes the dialogue cache with the currently selected language.
     */
    public static void checkLanguageSync() {
        String currentLang = BaseActivity.getLang();
        if (!currentLang.equals(loadedLanguage)) {
            synchronized (DialogueManager.class) {
                if (!currentLang.equals(loadedLanguage)) {
                    dialogueCache.clear();
                    loadedLanguage = currentLang;
                    Log.i(TAG, "Dialogue cache cleared. Switched to language: " + currentLang);
                }
            }
        }
    }

    /**
     * Main entry point: retrieves the appropriate dialogue lines for a character
     * based on the current game progress and language.
     */
    public static List<String> resolveDialogue(String characterId) {
        checkLanguageSync();
        String lang = loadedLanguage;

        // ComputeIfAbsent handles "get from cache or load if missing" atomically
        JsonElement element = dialogueCache.computeIfAbsent(characterId,
                id -> loadCharacterFile(id, lang));

        if (element == null || element.isJsonNull()) return Collections.emptyList();

        return parseDialogueElement(element);
    }

    private static JsonElement loadCharacterFile(String characterId, String lang) {
        String filePath = "dialogues/" + lang + "/" + characterId + ".json";
        try (Reader reader = new InputStreamReader(MyApp.getAppContext().getAssets().open(filePath))) {
            return gson.fromJson(reader, JsonElement.class);
        } catch (Exception e) {
            Log.w(TAG, "Dialogue file not found: " + filePath);
            return null;
        }
    }

    private static List<String> parseDialogueElement(JsonElement element) {
        // Case 1: Simple array of strings
        if (element.isJsonArray()) {
            return gson.fromJson(element, STRING_LIST_TYPE);
        }

        // Case 2: Object with branching logic
        if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
            if (obj.has("branches") && obj.get("branches").isJsonArray()) {
                JsonArray branches = obj.get("branches").getAsJsonArray();
                for (JsonElement branchElement : branches) {
                    if (!branchElement.isJsonObject()) continue;
                    JsonObject branch = branchElement.getAsJsonObject();

                    // Find the first branch where the condition is met
                    if (isConditionMet(branch.get("condition"))) {
                        return gson.fromJson(branch.get("lines"), STRING_LIST_TYPE);
                    }
                }
            }
        }
        return Collections.emptyList();
    }

    private static boolean isConditionMet(JsonElement conditionElement) {
        // If no condition is defined, the branch is always valid (e.g., fallback branch)
        if (conditionElement == null || conditionElement.isJsonNull()) return true;

        JsonObject condition = conditionElement.getAsJsonObject();
        WorldStateDoc worldState = MyApp.getWorldStateDoc();

        if (condition.has("flag")) {
            String flagName = condition.get("flag").getAsString();
            boolean flagValue = worldState.getCheckPoint(flagName);

            if (condition.has("equals")) {
                return flagValue == condition.get("equals").getAsBoolean();
            }
            return flagValue; // Defaults to checking if flag is true
        }
        return false;
    }
}
