package com.example.tutorialgame.cloud;

import com.example.tutorialgame.cloud.document.CosmeticDoc;
import com.example.tutorialgame.cloud.document.ProfileDoc;
import com.example.tutorialgame.cloud.document.ProgressDoc;
import com.example.tutorialgame.cloud.document.StatsDoc;
import com.example.tutorialgame.cloud.document.WorldStateDoc;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class UserRepository {
    private CloudManager cloudManager;

    @Inject
    public UserRepository() {
        initializeFromAuth();
    }

    /**
     * Initializes the CloudManager if a user is currently logged in.
     */
    public void initializeFromAuth() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            cloudManager = new CloudManager(currentUser.getUid());
        } else {
            cloudManager = null;
        }
    }

    /**
     * Starts loading account data from the cloud.
     */
    public void startLoadingAccountData(UserDataManager.OnDataLoadedListener listener) {
        if (cloudManager != null) {
            cloudManager.loadAccountData(listener);
        } else if (listener != null) {
            listener.onDataLoadFailed();
        }
    }

    public void clear() {
        cloudManager = null;
    }

    public CloudManager getCloudManager() {
        return cloudManager;
    }

    public ProfileDoc getProfile() {
        return (cloudManager != null) ? cloudManager.getProfile() : null;
    }

    public SaveSlotMetadata getSlotsMetadata() {
        return (cloudManager != null) ? cloudManager.getSlotsMetadata() : null;
    }

    public UserDataManager getActiveSlot() {
        return (cloudManager != null) ? cloudManager.getActiveSlot() : null;
    }

    public int getActiveSlotId() {
        return (cloudManager != null) ? cloudManager.getActiveSlotId() : -1;
    }

    public DocumentReference getUserDoc() {
        return (cloudManager != null) ? cloudManager.getUserDoc() : null;
    }

    // --- Shortcuts to active slot documents ---

    public StatsDoc getPlayerStats() {
        UserDataManager slot = getActiveSlot();
        return (slot != null) ? slot.getPlayerStats() : null;
    }

    public ProgressDoc getProgress() {
        UserDataManager slot = getActiveSlot();
        return (slot != null) ? slot.getProgress() : null;
    }

    public CosmeticDoc getCosmetic() {
        UserDataManager slot = getActiveSlot();
        return (slot != null) ? slot.getCosmeticDoc() : null;
    }

    public WorldStateDoc getWorldStateDoc() {
        UserDataManager slot = getActiveSlot();
        return (slot != null) ? slot.getWorldStateDoc() : null;
    }
}
