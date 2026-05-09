package com.example.tutorialgame.ui.activities;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.view.WindowMetrics;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.tutorialgame.MyApp;
import com.example.tutorialgame.R;
import com.example.tutorialgame.cloud.UserDataManager;
import com.example.tutorialgame.engine.audio.MusicManager;
import com.example.tutorialgame.engine.core.GameConstants;
import com.example.tutorialgame.managers.DialogueManager;
import com.example.tutorialgame.managers.MapManager;
import com.example.tutorialgame.ui.activities.authentication.LoginActivity;
import com.example.tutorialgame.ui.base.BaseActivity;
import com.example.tutorialgame.ui.dialogs.AlertDialogUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * The entry point of the application.
 * Handles initial setup, branding animations, and automatic user authentication.
 */
@SuppressLint("CustomSplashScreen")
public class SplashActivity extends BaseActivity {
    private static final int SPLASH_DELAY = 4000; // Minimum time to show the splash screen

    private ImageView ivLogo;
    private TextView tvAppName;
    private ObjectAnimator logoAnimator;
    private float lastVol;

    // Handler and Runnable members for safe lifecycle management
    private final Handler splashHandler = new Handler(Looper.getMainLooper());
    private final Runnable statusCheckRunnable = this::checkUserStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        setupMusic();
        initViews();
        initDimensions();
        
        startTransition();
        startFadeInSlideUp();

        // Ensure localization is synced before any UI or Data load
        DialogueManager.checkLanguageSync();

        // Schedule the transition to the next screen
        splashHandler.postDelayed(statusCheckRunnable, SPLASH_DELAY);
    }

    /**
     * Initializes views and basic components.
     */
    private void initViews() {
        ivLogo = findViewById(R.id.ivLogo);
        tvAppName = findViewById(R.id.tvAppName);
    }

    /**
     * Configures the Splash Screen music and saves previous volume settings.
     */
    private void setupMusic() {
        MusicManager music = MusicManager.getInstance(this);
        lastVol = music.getVolume();
        music.setVolume(1.0f);
        music.play(R.raw.music_monkey_splash);
        music.setLooping(true);
    }

    /**
     * Calculates screen dimensions and sets up global game constants for scaling.
     */
    private void initDimensions() {
        DisplayMetrics dm = new DisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            WindowMetrics windowMetrics = wm.getCurrentWindowMetrics();
            Rect bounds = windowMetrics.getBounds();
            dm.heightPixels = bounds.height();
            dm.widthPixels = bounds.width();
        } else {
            getWindowManager().getDefaultDisplay().getRealMetrics(dm);
        }

        int screenHeight = dm.heightPixels;
        // Logic for game-specific aspect ratio scaling (16:9 aspect ratio)
        int screenWidth = (int) (screenHeight / 0.5625);

        // Notify game engine of screen dimensions for coordinate mapping
        GameConstants.View.initScreenDimensions(screenWidth, screenHeight);
    }

    /**
     * Starts the sprite-based logo animation and horizontal translation.
     */
    public void startTransition() {
        ivLogo.setBackgroundResource(R.drawable.logo_animation);
        AnimationDrawable animationDrawable = (AnimationDrawable) ivLogo.getBackground();
        animationDrawable.start();

        int width = getResources().getDisplayMetrics().widthPixels;
        logoAnimator = ObjectAnimator.ofFloat(ivLogo, "translationX", -width / 3f, width);
        logoAnimator.setDuration(2300);
        logoAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        logoAnimator.setRepeatMode(ObjectAnimator.RESTART);
        logoAnimator.start();
    }

    /**
     * Animates the application name with a fade-in and slide-up effect.
     */
    private void startFadeInSlideUp() {
        Animation slideUpFadeIn = AnimationUtils.loadAnimation(this, R.anim.slide_up_fade_in);
        tvAppName.startAnimation(slideUpFadeIn);
    }

    /**
     * Validates the user's authentication state and begins the data loading chain.
     */
    private void checkUserStatus() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null || !currentUser.isEmailVerified()) {
            navigateToLogin();
            return;
        }

        if (MyApp.getCloudManager() == null) {
            MyApp.initializeCloudManager();
        }

        // Deep Loading Chain: Profile -> Slot -> Map -> Launcher
        MyApp.startLoadingAccountData(new UserDataManager.OnDataLoadedListener() {
            @Override
            public void onDataLoadSuccess() {
                int savedSlotId = MyApp.getProfile().getLastSelectedSlot();
                
                MyApp.getCloudManager().selectSlot(savedSlotId, new UserDataManager.OnDataLoadedListener() {
                    @Override
                    public void onDataLoadSuccess() {
                        // Preload map on a background thread to prevent UI stutter
                        new Thread(() -> {
                            MapManager.initStartingWorld();
                            runOnUiThread(SplashActivity.this::navigateToMain);
                        }).start();
                    }

                    @Override
                    public void onDataLoadFailed() { handleLoadError(); }
                });
            }

            @Override
            public void onDataLoadFailed() { handleLoadError(); }
        });
    }

    /**
     * Handles fatal loading errors by showing a restart prompt.
     */
    private void handleLoadError() {
        if (!isFinishing())
            AlertDialogUtils.showErrorDialogAndRestart(this);
    }

    private void navigateToMain() {
        restoreVolume();
        Intent intent = new Intent(this, LauncherActivity.class);
        startActivityWithClearStack(intent);
    }

    private void navigateToLogin() {
        restoreVolume();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivityWithClearStack(intent);
    }

    /**
     * Navigates to a new activity while clearing the backstack for security and flow.
     */
    private void startActivityWithClearStack(Intent intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void restoreVolume() {
        MusicManager.getInstance(this).setVolume(lastVol);
    }

    @Override
    protected void onDestroy() {
        // Cleanup resources to prevent memory leaks and orphaned animations
        splashHandler.removeCallbacks(statusCheckRunnable);
        if (logoAnimator != null) {
            logoAnimator.cancel();
        }
        super.onDestroy();
    }
}