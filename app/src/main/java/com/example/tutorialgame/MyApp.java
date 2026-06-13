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

import java.util.Random;
import java.util.concurrent.Executors;

/**
 * Main Application class for TutorialGame.
 * Manages global application state, sound loading, and cloud data synchronization.
 */
public class MyApp extends Application {
    private static MyApp instance;
    private static CloudManager cloudManager;
    
    // Using a single Random instance is fine for a game, but consider ThreadLocalRandom for high-concurrency needs
    public static final Random RND = new Random();

    private static final int[] ALL_SFX = {
            R.raw.sfx_bloop, R.raw.sfx_impact_enemy2, R.raw.sfx_impact_enemy1,
            R.raw.sfx_impact_player, R.raw.sfx_slash, R.raw.sfx_whoosh,
            R.raw.sfx_slash3, R.raw.sfx_slash2, R.raw.sfx_impact3,
            R.raw.sfx_error, R.raw.sfx_jump, R.raw.sfx_coin_drop,
            R.raw.sfx_coin_collected, R.raw.sfx_success4, R.raw.sfx_voice_player,
            R.raw.sfx_voice_bestfriend, R.raw.sfx_voice_black_knight,
            R.raw.sfx_voice_white_knight, R.raw.sfx_voice_blacksmith,
            R.raw.sfx_elemental_grass, R.raw.sfx_elemental_dirt,
            R.raw.sfx_elemental_stone, R.raw.sfx_unlock, R.raw.sfx_iris_close,
            R.raw.sfx_iris_open, R.raw.sfx_explosion1, R.raw.sfx_explosion3,
            R.raw.sfx_explosion5, R.raw.sfx_pop, R.raw.sfx_scarry1,
            R.raw.sfx_scarry2, R.raw.sfx_scarry3, R.raw.sfx_landing
    };

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        
        // Asynchronously load sound effects to prevent blocking the main thread during startup.
        Executors.newSingleThreadExecutor().execute(() -> SoundManager.getInstance(this).loadAllSfx(ALL_SFX));
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
}