package com.example.tutorialgame.engine.audio;

import static com.example.tutorialgame.engine.core.GameConstants.View.SCREEN_WIDTH;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import com.example.tutorialgame.MyApp;
import com.example.tutorialgame.R;
import com.example.tutorialgame.managers.CameraManager;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages sound effects (SFX) for the game using {@link SoundPool}.
 *
 * <p>The SoundManager provides centralized control over sound effects, including
 * volume management, spatial audio (panning and attenuation based on distance),
 * and random pitch variation. It utilizes a Singleton pattern for global access
 * and handles asynchronous sound loading.</p>
 */
public class SoundManager {
    private static final String TAG = "SoundManager";

    // Constants
    private static final String
            PREFS_NAME = "settings",
            KEY_SOUND_VOLUME = "sound_volume";
    private static final long SAVE_DELAY_MS = 500L;
    private static final int MAX_STREAMS = 16;
    private static final float
            MIN_RATE = 0.9f,
            MAX_RATE = 1.1f,
            DEFAULT_VOLUME = 0.5f;
    private static final float SPATIAL_DISTANCE_MULTIPLIER = 1.5f;

    private static SoundManager instance;
    private final SoundPool soundPool;
    private final Context appContext;

    /**
     * Maps raw resource IDs to SoundPool sound IDs.
     */
    private final Map<Integer, Integer> resIdToSoundId = new ConcurrentHashMap<>();

    /**
     * Tracks successfully loaded sound IDs.
     */
    private final Set<Integer> loadedSoundIds = ConcurrentHashMap.newKeySet();

    private final Handler saveHandler = new Handler(Looper.getMainLooper());
    private final SharedPreferences sp;
    private float soundVolume;
    private final Runnable saveRunnable;

    // --- Sound Throttling System ---
    private final Map<String, SoundThrottleGroup> soundGroups = new ConcurrentHashMap<>();

    private static class SoundThrottleGroup {
        private int count;
        private long lastReset;
        private final int limit;
        private final long interval;

        SoundThrottleGroup(int limit, long interval) {
            this.limit = limit;
            this.interval = interval;
            this.lastReset = SystemClock.elapsedRealtime();
        }

        synchronized boolean canPlay() {
            long now = SystemClock.elapsedRealtime();
            if (now - lastReset > interval) {
                count = 0;
                lastReset = now;
            }
            if (count < limit) {
                count++;
                return true;
            }
            return false;
        }
    }

    private static final int[] STARTUP_SFX = {
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

    private SoundManager(Context context) {
        this.appContext = context.getApplicationContext();

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(MAX_STREAMS)
                .setAudioAttributes(audioAttributes)
                .build();

        soundPool.setOnLoadCompleteListener((pool, sampleId, status) -> {
            if (status == 0) {
                loadedSoundIds.add(sampleId);
            } else {
                Log.w(TAG, "Failed to load soundId = " + sampleId);
            }
        });

        this.sp = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.soundVolume = clamp(sp.getFloat(KEY_SOUND_VOLUME, DEFAULT_VOLUME));
        this.saveRunnable = () -> sp.edit().putFloat(KEY_SOUND_VOLUME, soundVolume).apply();
    }

    public static synchronized SoundManager getInstance(Context context) {
        if (instance == null) {
            instance = new SoundManager(context);
        }
        return instance;
    }

    /**
     * Loads a single SFX resource.
     */
    public void loadSfx(int resId) {
        if (resId <= 0) {
            Log.w(TAG, "Attempted to load sound with invalid resId: " + resId);
            return;
        }
        if (!resIdToSoundId.containsKey(resId)) {
            int soundId = soundPool.load(appContext, resId, 1);
            resIdToSoundId.put(resId, soundId);
        }
    }

    /**
     * Unloads an SFX resource.
     */
    public void unloadSfx(int resId) {
        Integer soundId = resIdToSoundId.remove(resId);
        if (soundId != null) {
            soundPool.unload(soundId);
            loadedSoundIds.remove(soundId);
        }
    }

    /**
     * Gets the SoundPool ID for a resource if it is loaded and ready.
     * @return soundId or -1 if not ready.
     */
    private int getLoadedSoundId(int resId) {
        Integer soundId = resIdToSoundId.get(resId);
        if (soundId != null && loadedSoundIds.contains(soundId)) {
            return soundId;
        }
        return -1;
    }

    /**
     * @return A random pitch rate between MIN_RATE and MAX_RATE.
     */
    private float getRandomRate() {
        return MIN_RATE + MyApp.getRandom().nextFloat() * (MAX_RATE - MIN_RATE);
    }

    /**
     * Plays a sound effect with a slightly randomized pitch.
     */
    public void playRndPitchSfx(int resId) {
        int soundId = getLoadedSoundId(resId);
        if (soundId != -1) {
            soundPool.play(soundId, soundVolume, soundVolume, 1, 0, getRandomRate());
        }
    }

    /**
     * Plays a sound effect with spatial characteristics, with optional throttling.
     */
    public void playSpatialSfxThrottled(int resId, float sourceX, String groupName, int limit, long interval) {
        SoundThrottleGroup group = soundGroups.computeIfAbsent(groupName, k -> new SoundThrottleGroup(limit, interval));
        if (group.canPlay()) {
            playSpatialSfx(resId, sourceX);
        }
    }



    /**
     * Plays a sound effect with spatial characteristics (volume and panning) based on the source's X position
     * relative to the camera's focus point.
     */
    public void playSpatialSfx(int resId, float sourceX) {
        int soundId = getLoadedSoundId(resId);
        if (soundId == -1) return;

        int screenWidth = SCREEN_WIDTH;
        if (screenWidth <= 0) {
            playSfx(resId);
            return;
        }

        float listenerX = CameraManager.getLookAtX();
        float distanceX = Math.abs(sourceX - listenerX);
        float maxHearingDistance = screenWidth * SPATIAL_DISTANCE_MULTIPLIER;

        float attenuationFactor = 1.0f - (distanceX / maxHearingDistance);
        float distanceVolume = Math.max(0, attenuationFactor);

        float finalEffectiveVolume = soundVolume * distanceVolume;
        float rawPan = (sourceX - listenerX) / (screenWidth / 2.0f);
        float pan = Math.max(-1.0f, Math.min(1.0f, rawPan));

        float finalLeftVolume;
        float finalRightVolume;
        if (pan > 0) { // Source is to the right
            finalRightVolume = finalEffectiveVolume;
            finalLeftVolume = finalEffectiveVolume * (1.0f - pan);
        } else { // Source is to the left or center
            finalLeftVolume = finalEffectiveVolume;
            finalRightVolume = finalEffectiveVolume * (1.0f + pan);
        }

        soundPool.play(soundId, finalLeftVolume, finalRightVolume, 1, 0, getRandomRate());
    }

    /**
     * Plays a sound effect at the current global sound volume.
     */
    public void playSfx(int resId) {
        int soundId = getLoadedSoundId(resId);
        if (soundId != -1) {
            soundPool.play(soundId, soundVolume, soundVolume, 1, 0, 1.0f);
        }
    }

    /**
     * Sets the global SFX volume.
     * @param volume  The volume level (0.0 to 1.0).
     * @param persist If true, the volume is saved immediately to SharedPreferences.
     */
    public void setVolume(float volume, boolean persist) {
        this.soundVolume = clamp(volume);
        saveHandler.removeCallbacks(saveRunnable);
        if (persist) {
            saveRunnable.run();
        } else {
            saveHandler.postDelayed(saveRunnable, SAVE_DELAY_MS);
        }
    }

    public void setVolume(float volume) {
        setVolume(volume, false);
    }

    public float getVolume() {
        return soundVolume;
    }

    public void persistVolumeNow() {
        saveHandler.removeCallbacks(saveRunnable);
        saveRunnable.run();
    }

    /**
     * Releases SoundPool resources. Should be called when the manager is no longer needed.
     */
    public void release() {
        persistVolumeNow();
        soundPool.release();
        resIdToSoundId.clear();
        loadedSoundIds.clear();
        instance = null;
    }

    private float clamp(float v) {
        if (Float.isNaN(v)) return 0f;
        return Math.max(0f, Math.min(1f, v));
    }

    public void preloadDefaultSfx() {
        for (int resId : STARTUP_SFX) {
            loadSfx(resId);
        }
    }
}