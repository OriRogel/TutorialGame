package com.example.tutorialgame;

import android.app.Application;
import android.content.ComponentCallbacks2;
import android.content.Context;
import com.example.tutorialgame.cloud.CloudManager;
import com.example.tutorialgame.cloud.UserDataManager;
import com.example.tutorialgame.cloud.document.CosmeticDoc;
import com.example.tutorialgame.cloud.document.ProfileDoc;
import com.example.tutorialgame.cloud.document.ProgressDoc;
import com.example.tutorialgame.cloud.document.WorldStateDoc;
import com.example.tutorialgame.engine.audio.MusicManager;
import com.example.tutorialgame.engine.audio.SoundManager;
import com.example.tutorialgame.cloud.document.StatsDoc;
import com.example.tutorialgame.managers.objectpool.ObjectPoolManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Main Application class for TutorialGame.
 * Manages global application state, sound loading, and cloud data synchronization.
 */
public class MyApp extends Application {
    private static MyApp instance;
    private static CloudManager cloudManager;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        
        // Asynchronously load sound effects to prevent blocking the main thread during startup.
        Executors.newSingleThreadExecutor().execute(() -> SoundManager.getInstance(this).preloadDefaultSfx());
    }

    /**
     * @return The singleton instance of the application.
     */
    public static MyApp getInstance() {
        return instance;
    }

    /**
     * @return The application context.
     */
    public static Context getAppContext() {
        return instance != null ? instance.getApplicationContext() : null;
    }

    /**
     * Initializes the CloudManager if a user is currently logged in.
     */
    public static void initializeCloudManager() {
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
    public static void startLoadingAccountData(UserDataManager.OnDataLoadedListener listener) {
        if (cloudManager != null) {
            cloudManager.loadAccountData(listener);
        } else if (listener != null) {
            listener.onDataLoadFailed();
        }
    }

    public static void clearCloudManager() {
        cloudManager = null;
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        // Release heavy resources if memory is low.
        if (level >= ComponentCallbacks2.TRIM_MEMORY_COMPLETE) {
            SoundManager.getInstance(this).release();
            MusicManager.getInstance(this).release();
        }
        ObjectPoolManager.clearAllPools();
    }

    public static CloudManager getCloudManager() {
        return cloudManager;
    }

    // --- Global Account Data Getters with Null Safety ---

    public static ProfileDoc getProfile() {
        return (cloudManager != null) ? cloudManager.getProfile() : null;
    }

    // --- Active Slot Shortcuts ---

    private static UserDataManager getActiveSlot() {
        return (cloudManager != null) ? cloudManager.getActiveSlot() : null;
    }

    public static StatsDoc getPlayerStats() {
        UserDataManager slot = getActiveSlot();
        return (slot != null) ? slot.getPlayerStats() : null;
    }

    public static ProgressDoc getProgress() {
        UserDataManager slot = getActiveSlot();
        return (slot != null) ? slot.getProgress() : null;
    }

    public static CosmeticDoc getCosmetic() {
        UserDataManager slot = getActiveSlot();
        return (slot != null) ? slot.getCosmeticDoc() : null;
    }

    public static WorldStateDoc getWorldStateDoc() {
        UserDataManager slot = getActiveSlot();
        return (slot != null) ? slot.getWorldStateDoc() : null;
    }

    /**
     * @return A thread-safe random instance for the current thread.
     * Use this instead of creating new Random() instances.
     */
    public static ThreadLocalRandom getRandom() {
        return ThreadLocalRandom.current();
    }
}