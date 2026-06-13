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
import com.example.tutorialgame.engine.core.GamePanel;
import com.example.tutorialgame.managers.objectpool.ObjectPoolManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Random;

public class MyApp extends Application {
    private static Context appContext;
    private static CloudManager cloudManager;
    final public static Random RND = new Random();

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = getApplicationContext();
        
        int[] allSfx = {
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

        SoundManager.getInstance(this).loadAllSfx(allSfx);
    }

    public static void initializeCloudManager() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            cloudManager = new CloudManager(currentUser.getUid());
        } else {
            cloudManager = null;
        }
    }

    public static void startLoadingAccountData(UserDataManager.OnDataLoadedListener listener) {
        if (cloudManager != null) {
            cloudManager.loadAccountData(listener);
        }
    }

    public static void clearCloudManager() {
        cloudManager = null;
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (level >= ComponentCallbacks2.TRIM_MEMORY_COMPLETE) {
            SoundManager.getInstance(this).release();
            MusicManager.getInstance(this).release();
        }
        ObjectPoolManager.clearAllPools();
    }

    public static Context getAppContext() { return appContext; }
    public static CloudManager getCloudManager() { return cloudManager; }
    
    // נתוני חשבון גלובליים
    public static ProfileDoc getProfile() { return cloudManager.getProfile(); }
    
    // קיצורי דרך ישירים לנתוני הסלוט הפעיל
    public static StatsDoc getPlayerStats() { 
        return cloudManager.getActiveSlot().getPlayerStats();
    }
    
    public static ProgressDoc getProgress() { 
        return cloudManager.getActiveSlot().getProgress();
    }
    
    public static CosmeticDoc getCosmetic() { 
        return cloudManager.getActiveSlot().getCosmeticDoc();
    }
    
    public static WorldStateDoc getWorldStateDoc() { 
        return cloudManager.getActiveSlot().getWorldStateDoc();
    }
}