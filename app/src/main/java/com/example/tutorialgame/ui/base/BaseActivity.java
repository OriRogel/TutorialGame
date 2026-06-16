package com.example.tutorialgame.ui.base;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import com.example.tutorialgame.MyApp;
import com.example.tutorialgame.R;
import com.example.tutorialgame.cloud.UserRepository;
import com.example.tutorialgame.engine.audio.MusicManager;
import com.example.tutorialgame.engine.audio.SoundManager;
import com.example.tutorialgame.managers.DialogueManager;
import com.google.android.material.snackbar.BaseTransientBottomBar;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public abstract class BaseActivity extends AppCompatActivity {
    protected static SharedPreferences spSettings;
    private static String lang;
    private Toast lastToast;
    private Paint errorPaint;
    private long lastBackPress;
    private int backPressCount;
    private static boolean DOES_VIBE;

    private static final Map<String, Locale> localeCache = new HashMap<>();

    // משתנה זמני השומר את ה-Activity הפעילה כדי לספק Context עם השפה הנכונה
    private static Context currentActivityContext;

    @Inject protected MusicManager musicManager;
    @Inject protected SoundManager soundManager;
    @Inject protected UserRepository userRepository;

    /**
     * יוצר Context חדש המוגדר לשפה המבוקשת.
     */
    public static Context updateLocale(Context context, String langCode) {
        Locale locale = localeCache.get(langCode);
        if (locale == null) {
            locale = new Locale(langCode);
            localeCache.put(langCode, locale);
        }
        Locale.setDefault(locale);

        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());
        config.setLocale(locale);

        Context newContext = context.createConfigurationContext(config);

        // זה חשוב: ה-Manager ינקה את המטמון אם השפה השתנתה
        DialogueManager.checkLanguageSync();

        return newContext;
    }

    /**
     * מחזיר Context שמותאם לשפה הנוכחית.
     * בשימוש בתוך לולאת המשחק - יעיל מאוד ולא מייצר אובייקטים חדשים.
     */
    public static Context getContext() {
        if (currentActivityContext != null) {
            return currentActivityContext;
        }
        return MyApp.getAppContext();
    }

    // בתוך BaseActivity.java

    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences sp = newBase.getSharedPreferences("app_language", Context.MODE_PRIVATE);
        // שינוי: ברירת מחדל חייבת להיות "en" (בלי dialogues/)
        lang = sp.getString("app_language", "en");

        super.attachBaseContext(updateLocale(newBase, lang));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentActivityContext = this; // רישום ה-Activity הנוכחית

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        hideSystemUI();

        spSettings = this.getSharedPreferences("settings", MODE_PRIVATE);

        errorPaint = new Paint();
        errorPaint.setColorFilter(new PorterDuffColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY));

        setupBackPressHandler();
        DOES_VIBE = spSettings.getBoolean("haptics", true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        currentActivityContext = this; // וודוא שהרפרנס מעודכן

        MusicManager mgr = MusicManager.getInstance(this);
        int track = mgr.getCurrentResId();
        if (track != -1 && !mgr.isPlaying()) {
            mgr.play(track);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        MusicManager.getInstance(this).pause();
    }

    @Override
    protected void onDestroy() {
        // ניקוי הרפרנס כדי למנוע Memory Leak
        if (currentActivityContext == this) {
            currentActivityContext = null;
        }
        super.onDestroy();
    }

    public static void ButtonPressVibe() {
        if (!DOES_VIBE) return;

        Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);

        if (vibrator == null || !vibrator.hasVibrator()) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK));
        else vibrator.vibrate(VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE));
    }
    public static void setDoesVibe(boolean flag) {
        DOES_VIBE = flag;
    }

    public static SharedPreferences getSpSettings() {
        return spSettings;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) hideSystemUI();
    }

    private void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
    }

    public void showToast(String message, @BaseTransientBottomBar.Duration int duration) {
        if (lastToast != null) lastToast.cancel();
        lastToast = Toast.makeText(this, message, duration);
        View layout = LayoutInflater.from(this)
                .inflate(R.layout.custom_toast, new FrameLayout(this), false);
        TextView tv = layout.findViewById(R.id.tvToast);
        tv.setText(message);
        lastToast.setView(layout);
        lastToast.show();
    }

    protected void setError(EditText et, String error, int drawable) {
        Drawable icon = AppCompatResources.getDrawable(this, drawable);
        if (icon != null) {
            icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
            icon.setColorFilter(errorPaint.getColorFilter());
        }
        et.setError(error, icon);
    }

    protected void setupBackPressHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleExitWithDoubleClick();
            }
        });
    }

    protected void handleExitWithDoubleClick() {
        backPressCount++;
        if (System.currentTimeMillis() - lastBackPress >= 2000) {
            backPressCount = 1;
            lastBackPress = System.currentTimeMillis();
        }

        if (backPressCount >= 2) {
            if (lastToast != null) lastToast.cancel();
            finish();
        } else {
            showToast(getString(R.string.press_to_exit), Toast.LENGTH_SHORT);
        }
    }

    public static String getLang() {
        return lang;
    }

    /**
     * Updates the application's locale on-the-fly without recreating the activity.
     * This is useful for in-game language switching.
     */
    public void updateLanguageOnTheFly(String langCode) {
        // 1. Update SharedPreferences
        getSharedPreferences("app_language", Context.MODE_PRIVATE).edit()
                .putString("app_language", langCode).apply();

        // 2. Update Configuration of the current activity
        Resources res = getResources();
        Configuration config = res.getConfiguration();
        
        Locale locale = localeCache.get(langCode);
        if (locale == null) {
            locale = new Locale(langCode);
            localeCache.put(langCode, locale);
        }

        Locale.setDefault(locale);
        config.setLocale(locale);

        // Update the configuration
        res.updateConfiguration(config, res.getDisplayMetrics());

        // 3. Update the static lang variable
        lang = langCode;

        // 4. Synchronize dialogues
        DialogueManager.checkLanguageSync();
    }
}