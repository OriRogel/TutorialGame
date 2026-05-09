package com.example.tutorialgame.engine.audio;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;

public class MusicManager {
    private static final String TAG = "MusicManager";
    private static final long SAVE_DELAY_MS = 500L;
    private static MusicManager instance;

    private final Context appContext;
    private final SharedPreferences sp;

    // --- שינוי מרכזי: שני נגנים למעבר חלק ---
    private MediaPlayer activePlayer;
    private MediaPlayer fadingPlayer;
    private int activeResId = -1;
    private int lastResId = -1;

    private float musicVolume;
    private boolean isLooping = true;

    // --- Handler לניהול שמירה ו-Fading ---
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable saveRunnable;
    private Runnable fadeRunnable;

    private boolean isDucked = false;
    private static final float DUCK_VOLUME_MULTIPLIER = 0.3f;

    private MusicManager(Context context) {
        this.appContext = context.getApplicationContext();
        this.sp = appContext.getSharedPreferences("settings", Context.MODE_PRIVATE);
        this.musicVolume = clamp(sp.getFloat("music_volume", 0.3f));
        saveRunnable = () -> sp.edit().putFloat("music_volume", musicVolume).apply();

        // יצירת הנגנים
        activePlayer = createMediaPlayer();
        fadingPlayer = createMediaPlayer();
    }

    public static synchronized MusicManager getInstance(Context context) {
        if (instance == null) {
            instance = new MusicManager(context);
        }
        return instance;
    }

    private MediaPlayer createMediaPlayer() {
        MediaPlayer mp = new MediaPlayer();
        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();
        mp.setAudioAttributes(attrs);
        return mp;
    }

    /**
     * מנגן את קטע המוזיקה המבוקש עם אפקט Fade-in/Fade-out.
     * @param resId ה-ID של קובץ המוזיקה.
     */
    public void play(int resId) {
        // אם השיר כבר מתנגן - אין מה לעשות
        if (resId == activeResId && activePlayer != null && activePlayer.isPlaying()) {
            return;
        }

        // --- זה השינוי המרכזי למניעת התחלה מחדש ---
        // אם השיר המבוקש הוא השיר הנוכחי אבל הוא פשוט בהשהיה (Paused)
        if (resId == activeResId && activePlayer != null) {
            activePlayer.start();
            // אם היה fade שנעצר, אפשר לחדש אותו או פשוט לתת לווליום לחזור
            setVolume(musicVolume, false);
            return;
        }

        // --- מכאן והלאה: טיפול בשיר חדש באמת ---

        if (fadeRunnable != null) {
            handler.removeCallbacks(fadeRunnable);
        }

        // אם אין נגן ישן, פשוט הפעל חדש
        if (activePlayer == null || !activePlayer.isPlaying()) {
            setupAndPlayNewTrack(resId, musicVolume);
            return;
        }

        // בצע Crossfade לשיר החדש
        MediaPlayer temp = fadingPlayer;
        fadingPlayer = activePlayer;
        activePlayer = temp;

        lastResId = activeResId;
        setupAndPlayNewTrack(resId, 0f);
        startCrossfade();
    }

    // מתודה חדשה שמבודדת את לוגיקת האתחול של טראק חדש
    private void setupAndPlayNewTrack(int resId, float initialVolume) {
        try {
            activeResId = resId;
            activePlayer.reset();
            AssetFileDescriptor afd = appContext.getResources().openRawResourceFd(resId);
            activePlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();

            activePlayer.setLooping(isLooping);
            activePlayer.setVolume(initialVolume, initialVolume);
            activePlayer.prepare();
            activePlayer.start();
        } catch (IOException | IllegalStateException e) {
            Log.e(TAG, "Error setting up new track: resId = " + resId, e);
        }
    }

    // שכתוב של לוגיקת ה-fade להיות פשוטה ואמינה יותר
    private void startCrossfade() {
        final int FADE_DURATION_MS = 1000; // נקצר קצת את משך המעבר
        final int FADE_INTERVAL_MS = 50;
        final int NUMBER_OF_STEPS = FADE_DURATION_MS / FADE_INTERVAL_MS;
        final float STEP_SIZE = 1.0f / NUMBER_OF_STEPS;
        final float[] progress = {0.0f};

        fadeRunnable = new Runnable() {
            @Override
            public void run() {
                progress[0] += STEP_SIZE;
                if (progress[0] > 1.0f) {
                    progress[0] = 1.0f;
                }

                float targetVolume = isDucked ? musicVolume * DUCK_VOLUME_MULTIPLIER : musicVolume;

                // הגבר את הנגן החדש
                if (activePlayer.isPlaying()) {
                    activePlayer.setVolume(progress[0] * targetVolume, progress[0] * targetVolume);
                }

                // הנמך את הנגן הישן
                if (fadingPlayer.isPlaying()) {
                    fadingPlayer.setVolume((1.0f - progress[0]) * targetVolume, (1.0f - progress[0]) * targetVolume);
                }

                if (progress[0] < 1.0f) {
                    handler.postDelayed(this, FADE_INTERVAL_MS);
                } else {
                    // סיום ה-fade
                    if (fadingPlayer.isPlaying()) {
                        fadingPlayer.stop();
                    }
                    fadeRunnable = null; // אפשר להתחיל fade חדש
                }
            }
        };
        handler.post(fadeRunnable);
    }

    // --- הוספת מתודות Ducking ---

    /**
     * מנמיך את המוזיקה בעדינות (למשל, בכניסה לדיאלוג).
     */
    public void duck() {
        if (isDucked) return;
        isDucked = true;
        setVolume(musicVolume, false); // מפעיל עדכון ווליום ללא שמירה
    }

    /**
     * מחזיר את המוזיקה לווליום המלא (למשל, ביציאה מדיאלוג).
     */
    public void unDuck() {
        if (!isDucked) return;
        isDucked = false;
        setVolume(musicVolume, false); // מפעיל עדכון ווליום ללא שמירה
    }

    public void pause() {
        if (activePlayer != null && activePlayer.isPlaying()) {
            activePlayer.pause();
        }
        handler.removeCallbacks(fadeRunnable); // עצור fade אם קיים
    }

    public void stop() {
        if (activePlayer != null && activePlayer.isPlaying()) {
            activePlayer.stop();
        }
        if (fadingPlayer != null && fadingPlayer.isPlaying()) {
            fadingPlayer.stop();
        }
        activeResId = -1;
        handler.removeCallbacks(fadeRunnable);
    }

    // --- מתודות נוספות (כמעט ללא שינוי, רק מתייחסות ל-activePlayer) ---

    public int getCurrentResId() {
        return activeResId;
    }
    public int getLastResId() {
        return lastResId;
    }

    public boolean isPlaying() {
        return activePlayer != null && activePlayer.isPlaying();
    }

    // --- עדכון מתודת setVolume ---
    public void setVolume(float volume, boolean persist) {
        float v = clamp(volume);
        // שמור תמיד את הווליום שהמשתמש בחר, לא את הווליום המונמך
        if (!isDucked || persist) {
            this.musicVolume = v;
        }

        // חשב את הווליום האמיתי שצריך להיות כרגע
        float actualVolume = isDucked ? v * DUCK_VOLUME_MULTIPLIER : v;

        if (activePlayer != null) {
            try {
                activePlayer.setVolume(actualVolume, actualVolume);
            } catch (Exception e) {
                Log.w(TAG, "Failed to set media player volume", e);
            }
        }

        // ... לוגיקת השמירה נשארת זהה
        if (persist) {
            handler.removeCallbacks(saveRunnable);
            saveRunnable.run();
        } else {
            handler.removeCallbacks(saveRunnable);
            handler.postDelayed(saveRunnable, SAVE_DELAY_MS);
        }
    }

    public float getVolume() { return musicVolume; }
    public void setVolume(float volume) { setVolume(volume, false); }

    public void setLooping(boolean looping) {
        isLooping = looping;
        if (activePlayer != null) activePlayer.setLooping(looping);
    }

    public void release() {
        persistVolumeNow();
        if (activePlayer != null) {
            activePlayer.release();
            activePlayer = null;
        }
        if (fadingPlayer != null) {
            fadingPlayer.release();
            fadingPlayer = null;
        }
        handler.removeCallbacksAndMessages(null); // נקה את כל ה-Runnables
        instance = null;
    }

    public void persistVolumeNow() {
        handler.removeCallbacks(saveRunnable);
        saveRunnable.run();
    }

    private float clamp(float v) {
        if (Float.isNaN(v)) return 0f;
        return Math.max(0f, Math.min(1f, v));
    }
}
