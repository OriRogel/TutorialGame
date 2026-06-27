package com.example.tutorialgame.cloud;

import com.example.tutorialgame.MyApp;
import com.example.tutorialgame.cloud.document.ProfileDoc;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

/**
 * מנהל העל של נתוני הענן.
 */
public class CloudManager {
    private final DocumentReference userDoc;
    private final ProfileDoc profileDoc;
    private final SaveSlotMetadata slotsMetadata;
    private UserDataManager activeSlot;
    private int activeSlotId = -1; // שמירת ה-ID של הסלוט הפעיל

    public CloudManager(String uid) {
        this.userDoc = FirebaseFirestore.getInstance().collection("users").document(uid);
        this.profileDoc = new ProfileDoc(userDoc, null);
        this.slotsMetadata = new SaveSlotMetadata(userDoc, null);
    }

    public void loadAccountData(UserDataManager.OnDataLoadedListener listener) {
        profileDoc.loadAndCache(new UserDataManager.OnDataLoadedListener() {
            @Override
            public void onDataLoadSuccess() {
                slotsMetadata.loadAndCache(listener);
            }
            @Override
            public void onDataLoadFailed() {
                listener.onDataLoadFailed();
            }
        });
    }

    public void selectSlot(int slotId, UserDataManager.OnDataLoadedListener listener) {
        com.example.tutorialgame.managers.MapManager.clearCache(); // איפוס המפות לפני טעינת סלוט חדש
        this.activeSlotId = slotId;
        DocumentReference slotRef = userDoc.collection("slots").document("slot_" + slotId);
        activeSlot = new UserDataManager(slotRef);
        activeSlot.loadAndCache(listener);
    }

    public void createNewSlot(int slotId, UserDataManager.OnDataLoadedListener listener) {
        com.example.tutorialgame.managers.MapManager.clearCache(); // איפוס המפות לפני יצירת סלוט חדש
        this.activeSlotId = slotId;
        DocumentReference slotRef = userDoc.collection("slots").document("slot_" + slotId);
        activeSlot = new UserDataManager(slotRef);
        activeSlot.initializeNewSlotData();
        
        slotsMetadata.updateSlotMetadata(slotId, 1, 1);
        
        if (listener != null) listener.onDataLoadSuccess();
    }

    /**
     * מוחק סלוט ספציפי ובתנאי שהוא לא הסלוט שמשחקים בו כרגע.
     */
    public void deleteInactiveSlot(int targetSlotId, UserDataManager.OnDataLoadedListener listener) {
        // הגנה: מונע מחיקה של הסלוט הפעיל
        if (targetSlotId == this.activeSlotId) {
            if (listener != null) listener.onDataLoadFailed();
            return;
        }

        // 1. ניגשים למסמך של הסלוט שאנחנו רוצים למחוק
        DocumentReference targetSlotRef = userDoc.collection("slots").document("slot_" + targetSlotId);

        // 2. מוחקים את מסמך הסלוט בענן (אין צורך לטעון אותו קודם!)
        targetSlotRef.delete().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // 3. מסירים את הרישום שלו מהמטא-דאטה הגלובלי
                slotsMetadata.resetSlotMetadata(targetSlotId);

                if (listener != null) listener.onDataLoadSuccess();
            } else {
                if (listener != null) listener.onDataLoadFailed();
            }
        });
    }

    public void saveGame(String flagName) {
        activeSlot.getWorldStateDoc().saveWorldState();
        activeSlot.getWorldStateDoc().setCheckPoint(flagName);
        if (activeSlotId != -1) {
            slotsMetadata.updateSlotMetadata(
                    activeSlotId,
//                        getVisualCheckpointKey(), // המפתח לתמונה
                    Objects.requireNonNull(MyApp.getProgress()).getLevel(),
                    activeSlot.getCosmeticDoc().getAvailableFrames().size()
            );
        }
    }

    public ProfileDoc getProfile() { return profileDoc; }
    public SaveSlotMetadata getSlotsMetadata() { return slotsMetadata; }
    public UserDataManager getActiveSlot() { return activeSlot; }
    public DocumentReference getUserDoc() { return userDoc; }
    public int getActiveSlotId() { return activeSlotId; }
}
