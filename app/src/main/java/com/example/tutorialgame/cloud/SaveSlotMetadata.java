package com.example.tutorialgame.cloud;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;
import java.util.Map;

/**
 * מנהל את המידע התמציתי על 3 הסלוטים בתוך מסמך המשתמש הראשי.
 * מאפשר עדכון שדות ספציפיים (Level, XP, Checkpoint) כדי לשמור על סנכרון עם התקדמות המשחק.
 */
public class SaveSlotMetadata extends BaseDocument {
    // Database Constants
    public static final String DOC_NAME = "slots_metadata";
    public static final String KEY_ROOT = DOC_NAME + ".";

    // Field Names
    public static final String F_NAME = "slotName";
    public static final String F_CHECKPOINT = "checkpointKey";
    public static final String F_LEVEL = "level";
    public static final String F_FRAMES_COUNT = "framesCount";

    public static class Slot {
        public String slotName = "Empty Slot";
        public String checkpointKey = "START";
        private int level = 1;
        public int framesCount = 1;
        public boolean exists = false;
        public int getLevel() { return level; }
    }

    private final Map<Integer, Slot> slots = new HashMap<>();

    public SaveSlotMetadata(DocumentReference userRef, Runnable onFinishedLoading) {
        super(userRef, onFinishedLoading);
        for (int i = 1; i <= 3; i++) {
            slots.put(i, new Slot());
        }
    }

    @NonNull
    @Override
    protected String getDocName() {
        return DOC_NAME;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void parseData(@NonNull Map<String, Object> data) {
        for (int i = 1; i <= 3; i++) {
            Object rawSlot = data.get(String.valueOf(i));
            if (rawSlot instanceof Map) {
                Map<String, Object> slotData = (Map<String, Object>) rawSlot;
                Slot slot = new Slot();
                slot.slotName = (String) slotData.getOrDefault(F_NAME, "Adventure " + i);
                slot.checkpointKey = (String) slotData.getOrDefault(F_CHECKPOINT, "START");
                slot.level = ((Number) slotData.getOrDefault(F_LEVEL, 1)).intValue();
                slot.framesCount = ((Number) slotData.getOrDefault(F_FRAMES_COUNT, 1)).intValue();
                slot.exists = true;
                slots.put(i, slot);
            } else {
                slots.put(i, new Slot());
            }
        }
    }

    /**
     * מתודה גנרית לעדכון שדה ספציפי בסלוט כולל עדכון זמן השמירה האחרון.
     */
    private void updateField(int slotId, String field, Object value) {
        Slot s = slots.get(slotId);
        if (s == null) return;
        
        s.exists = true;

        String slotPath = KEY_ROOT + slotId + ".";
        Map<String, Object> updates = new HashMap<>();
        updates.put(slotPath + field, value);

        docRef.update(updates);
    }

    public void updateLevel(int slotId, int level) {
        Slot s = slots.get(slotId);
        if (s != null) s.level = level;
        updateField(slotId, F_LEVEL, level);
    }

    public void updateFramesCount(int slotId, int count) {
        Slot s = slots.get(slotId);
        if (s != null) s.framesCount += count;
        updateField(slotId, F_FRAMES_COUNT, count);
    }

    public void updateCheckpoint(int slotId, String checkpointKey) {
        Slot s = slots.get(slotId);
        if (s != null) s.checkpointKey = checkpointKey;
        updateField(slotId, F_CHECKPOINT, checkpointKey);
    }

    public void updateSlotName(int slotId, String name) {
        Slot s = slots.get(slotId);
        if (s != null) s.slotName = name;
        updateField(slotId, F_NAME, name);
    }

    /**
     * עדכון מרוכז של נתוני השמירה (לשימוש בסיום סשן או שמירה ידנית).
     */
    public void updateSlotMetadata(int slotId, int level, int framesCount) {
        Slot s = slots.get(slotId);
        if (s == null) return;

        s.level = level;
        s.framesCount = framesCount;
        s.exists = true;

        String path = KEY_ROOT + slotId;
        Map<String, Object> update = new HashMap<>();
        update.put(path + "." + F_LEVEL, level);
        update.put(path + "." + F_FRAMES_COUNT, framesCount);

        docRef.update(update);
    }
    public void resetSlotMetadata(int slotId) {
        Slot s = slots.get(slotId);
        if (s == null) return;

        // 1. איפוס מקומי (Cache)
        s.exists = false;
        s.level = 0;
        s.framesCount = 0;
        s.checkpointKey = "START";
        s.slotName = "Empty Slot";

        // 2. מחיקת השדה הספציפי מהמפה במסמך הראשי בענן
        Map<String, Object> updates = new HashMap<>();
        updates.put(KEY_ROOT + slotId, FieldValue.delete());
        docRef.update(updates);
    }

    public Slot getSlot(int id) {
        return slots.getOrDefault(id, new Slot());
    }
}
