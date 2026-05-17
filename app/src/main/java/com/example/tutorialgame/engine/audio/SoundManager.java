package com.example.tutorialgame.engine.audio;

import static com.example.tutorialgame.engine.core.GameConstants.View.SCREEN_WIDTH;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.tutorialgame.MyApp;
import com.example.tutorialgame.managers.CameraManager;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
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

    private static final long SAVE_DELAY_MS = 500L;
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
    private final Set<Integer> loadedSoundIds = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private final float minRate = 0.9f;
    private final float maxRate = 1.1f;
    private final Handler saveHandler = new Handler(Looper.getMainLooper());
    private SharedPreferences sp;
    private float soundVolume;
    private final Runnable saveRunnable = () -> sp.edit().putFloat("sound_volume", soundVolume).apply();

    // --- Sound Throttling System ---
    private final Map<Integer, Long> throttledSounds = new ConcurrentHashMap<>();
    private final Map<String, SoundThrottleGroup> soundGroups = new ConcurrentHashMap<>();

    private static class SoundThrottleGroup {
        int count;
        long lastReset;
        final int limit;
        final long interval;

        SoundThrottleGroup(int limit, long interval) {
            this.limit = limit;
            this.interval = interval;
            this.lastReset = System.currentTimeMillis();
        }

        boolean canPlay() {
            long now = System.currentTimeMillis();
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

    private SoundManager(Context context) {
        this.appContext = context.getApplicationContext();

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(13)
                .setAudioAttributes(audioAttributes)
                .build();

        soundPool.setOnLoadCompleteListener((pool, sampleId, status) -> {
            if (status == 0) {
                loadedSoundIds.add(sampleId);
            } else {
                Log.w(TAG, "Failed to load soundId = " + sampleId);
            }
        });

        sp = appContext.getSharedPreferences("settings", Context.MODE_PRIVATE);
        this.soundVolume = clamp(sp.getFloat("sound_volume", 0.5f));
    }

    public static synchronized SoundManager getInstance(Context context) {
        if (instance == null) {
            instance = new SoundManager(context);
        }
        return instance;
    }

    /**
     * Loads multiple SFX resources into the SoundPool.
     */
    public void loadAllSfx(int[] resIds) {
        for (int resId : resIds) {
            if (!resIdToSoundId.containsKey(resId)) {
                int soundId = soundPool.load(appContext, resId, 1);
                resIdToSoundId.put(resId, soundId);
            }
        }
    }

    /**
     * Checks if a sound resource is loaded and ready to be played.
     */
    public boolean isLoaded(int resId) {
        Integer soundId = resIdToSoundId.get(resId);
        return (soundId != null && loadedSoundIds.contains(soundId));
    }

    /**
     * Plays a sound effect with a slightly randomized pitch.
     */
    public void playRndPitchSfx(int resId) {
        if (!isLoaded(resId)) return;
        float rate = minRate + MyApp.RND.nextFloat() * (maxRate - minRate);
        soundPool.play(Objects.requireNonNull(resIdToSoundId.get(resId)), soundVolume, soundVolume, 1, 0, rate);
    }

    /**
     * Plays a sound effect with spatial characteristics, with optional throttling.
     *
     * @param resId     The raw resource ID.
     * @param sourceX   The world X coordinate.
     * @param groupName Optional group name for shared throttling.
     * @param limit     Max sounds per interval.
     * @param interval  The interval in MS.
     */
    public void playSpatialSfxThrottled(int resId, float sourceX, String groupName, int limit, long interval) {
        SoundThrottleGroup group = soundGroups.computeIfAbsent(groupName, k -> new SoundThrottleGroup(limit, interval));
        if (group.canPlay()) {
            playSpatialSfx(resId, sourceX);
        }
    }

    /**
     * Plays a spatial sound effect with a per-resource throttle.
     */
    public void playSpatialSfxThrottled(int resId, float sourceX, long minInterval) {
        long now = System.currentTimeMillis();
        Long lastTime = throttledSounds.get(resId);
        if (lastTime == null || now - lastTime > minInterval) {
            throttledSounds.put(resId, now);
            playSpatialSfx(resId, sourceX);
        }
    }

    /**
     * Plays a sound effect with spatial characteristics (volume and panning) based on the source's X position
     * relative to the camera's focus point.
     *
     * @param resId   The raw resource ID of the sound.
     * @param sourceX The world X coordinate of the sound source.
     */
    public void playSpatialSfx(int resId, float sourceX) {
        if (!isLoaded(resId)) return;

        int screenWidth = SCREEN_WIDTH;
        if (screenWidth == 0) {
            playSfx(resId);
            return;
        }

        float listenerX = CameraManager.getLookAtX();
        float distanceX = Math.abs(sourceX - listenerX);
        float maxHearingDistance = screenWidth * 1.5f;

        float attenuationFactor = 1.0f - (distanceX / maxHearingDistance);
        float distanceVolume = Math.max(0, attenuationFactor);

        float finalEffectiveVolume = soundVolume * distanceVolume;
        float pan = (sourceX - listenerX) / (screenWidth / 2.0f);
        pan = Math.max(-1.0f, Math.min(1.0f, pan));

        float finalLeftVolume;
        float finalRightVolume;
        if (pan > 0) { // Source is to the right
            finalRightVolume = finalEffectiveVolume;
            finalLeftVolume = finalEffectiveVolume * (1.0f - pan);
        } else { // Source is to the left or center
            finalLeftVolume = finalEffectiveVolume;
            finalRightVolume = finalEffectiveVolume * (1.0f + pan);
        }

        float rate = minRate + MyApp.RND.nextFloat() * (maxRate - minRate);
        soundPool.play(Objects.requireNonNull(resIdToSoundId.get(resId)), finalLeftVolume, finalRightVolume, 1, 0, rate);
    }

    /**
     * Plays a sound effect at the current global sound volume.
     */
    public void playSfx(int resId) {
        if (!isLoaded(resId)) return;
        soundPool.play(Objects.requireNonNull(resIdToSoundId.get(resId)), soundVolume, soundVolume, 1, 0, 1f);
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
        instance = null;
    }

    private float clamp(float v) {
        if (Float.isNaN(v)) return 0f;
        return Math.max(0f, Math.min(1f, v));
    }
}