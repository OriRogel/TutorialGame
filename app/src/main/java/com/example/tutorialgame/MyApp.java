package com.example.tutorialgame;

import android.app.Application;
import android.content.ComponentCallbacks2;
import android.content.Context;

import com.example.tutorialgame.cloud.CloudManager;
import com.example.tutorialgame.cloud.UserDataManager;
import com.example.tutorialgame.cloud.UserRepository;
import com.example.tutorialgame.cloud.document.CosmeticDoc;
import com.example.tutorialgame.cloud.document.ProfileDoc;
import com.example.tutorialgame.cloud.document.ProgressDoc;
import com.example.tutorialgame.cloud.document.StatsDoc;
import com.example.tutorialgame.cloud.document.WorldStateDoc;
import com.example.tutorialgame.engine.audio.MusicManager;
import com.example.tutorialgame.engine.audio.SoundManager;
import com.example.tutorialgame.managers.objectpool.ObjectPoolManager;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

import dagger.hilt.EntryPoint;
import dagger.hilt.EntryPoints;
import dagger.hilt.InstallIn;
import dagger.hilt.android.HiltAndroidApp;
import dagger.hilt.components.SingletonComponent;

/**
 * Main Application class for TutorialGame.
 * Manages global application state, sound loading, and cloud data synchronization.
 */
@HiltAndroidApp
public class MyApp extends Application {
    private static MyApp instance;

    @EntryPoint
    @InstallIn(SingletonComponent.class)
    public interface MyAppEntryPoint {
        UserRepository userRepository();
        SoundManager soundManager();
        MusicManager musicManager();
        ThreadLocalRandom threadLocalRandom();
    }

    private static UserRepository getUserRepository() {
        return EntryPoints.get(instance, MyAppEntryPoint.class).userRepository();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        
        // Asynchronously load sound effects to prevent blocking the main thread during startup.
        Executors.newSingleThreadExecutor().execute(() -> 
            EntryPoints.get(instance, MyAppEntryPoint.class).soundManager().preloadDefaultSfx());
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
        getUserRepository().initializeFromAuth();
    }

    /**
     * Starts loading account data from the cloud.
     */
    public static void startLoadingAccountData(UserDataManager.OnDataLoadedListener listener) {
        CloudManager cloudManager = getUserRepository().getCloudManager();
        if (cloudManager != null) {
            cloudManager.loadAccountData(listener);
        } else if (listener != null) {
            listener.onDataLoadFailed();
        }
    }

    public static void clearCloudManager() {
        getUserRepository().clear();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        // Release heavy resources if memory is low.
        if (level >= ComponentCallbacks2.TRIM_MEMORY_COMPLETE) {
            EntryPoints.get(instance, MyAppEntryPoint.class).soundManager().release();
            EntryPoints.get(instance, MyAppEntryPoint.class).musicManager().release();
        }
        ObjectPoolManager.clearAllPools();
    }

    public static CloudManager getCloudManager() {
        return getUserRepository().getCloudManager();
    }

    // --- Global Account Data Getters with Null Safety ---

    public static ProfileDoc getProfile() {
        return getUserRepository().getProfile();
    }

    // --- Active Slot Shortcuts ---

    public static StatsDoc getPlayerStats() {
        return getUserRepository().getPlayerStats();
    }

    public static ProgressDoc getProgress() {
        return getUserRepository().getProgress();
    }

    public static CosmeticDoc getCosmetic() {
        return getUserRepository().getCosmetic();
    }

    public static WorldStateDoc getWorldStateDoc() {
        return getUserRepository().getWorldStateDoc();
    }

    /**
     * @return A thread-safe random instance for the current thread.
     * Use this instead of creating new Random() instances.
     */
    public static ThreadLocalRandom getRandom() {
        return EntryPoints.get(instance, MyAppEntryPoint.class).threadLocalRandom();
    }
}