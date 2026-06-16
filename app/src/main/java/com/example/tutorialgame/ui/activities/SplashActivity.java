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

import androidx.core.splashscreen.SplashScreen;

import com.example.tutorialgame.R;
import com.example.tutorialgame.cloud.CloudManager;
import com.example.tutorialgame.cloud.UserDataManager;
import com.example.tutorialgame.engine.core.GameConstants;
import com.example.tutorialgame.managers.DialogueManager;
import com.example.tutorialgame.managers.MapManager;
import com.example.tutorialgame.ui.activities.authentication.LoginActivity;
import com.example.tutorialgame.ui.base.BaseActivity;
import com.example.tutorialgame.ui.dialogs.AlertDialogUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import dagger.hilt.android.AndroidEntryPoint;

/**
 * The entry point of the application.
 * Handles initial setup, branding animations, and automatic user authentication.
 */
@AndroidEntryPoint
@SuppressLint("CustomSplashScreen")
public class SplashActivity extends BaseActivity {
    private static final int MIN_SPLASH_TIME = 2000; // Minimum time to show our custom branding

    private ImageView ivLogo;
    private TextView tvAppName;
    private ObjectAnimator logoAnimator;
    private float lastVol;
    private boolean isInitialized = false; // Controls system splash dismissal
    private long startTime;

    // Handler for timing logic
    private final Handler splashHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 1. Install Splash Screen API before super.onCreate()
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        startTime = System.currentTimeMillis();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // 2. Dismiss the system splash screen as soon as our Activity is ready
        splashScreen.setKeepOnScreenCondition(() -> !isInitialized);
        
        // Mark as initialized to let the system splash go and reveal our custom layout
        isInitialized = true;

        setupMusic();
        initViews();
        initDimensions();

        // Ensure localization is synced before any UI or Data load
        DialogueManager.checkLanguageSync();

        // 3. Start our custom branding animations immediately (once layout is ready)
        startTransition();
        startFadeInSlideUp();

        // 4. Begin the loading process
        checkUserStatus();
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
        lastVol = musicManager.getVolume();
        musicManager.setVolume(1.0f);
        musicManager.play(R.raw.music_monkey_splash);
        musicManager.setLooping(true);
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
            finalizeLoading(this::navigateToLogin);
            return;
        }

        if (userRepository.getCloudManager() == null) {
            userRepository.initializeFromAuth();
        }

        // Deep Loading Chain: Profile -> Slot -> Map -> Launcher
        CloudManager cloudManager = userRepository.getCloudManager();
        if (cloudManager != null) {
            cloudManager.loadAccountData(new UserDataManager.OnDataLoadedListener() {
                @Override
                public void onDataLoadSuccess() {
                    int savedSlotId = userRepository.getProfile().getLastSelectedSlot();
                    
                    userRepository.getCloudManager().selectSlot(savedSlotId, new UserDataManager.OnDataLoadedListener() {
                        @Override
                        public void onDataLoadSuccess() {
                            // Preload map on a background thread to prevent UI stutter
                            new Thread(() -> {
                                MapManager.initStartingWorld();
                                runOnUiThread(() -> finalizeLoading(SplashActivity.this::navigateToMain));
                            }).start();
                        }

                        @Override
                        public void onDataLoadFailed() { handleLoadError(); }
                    });
                }

                @Override
                public void onDataLoadFailed() { handleLoadError(); }
            });
        } else {
            handleLoadError();
        }
    }

    /**
     * Ensures minimum splash duration for branding before proceeding.
     */
    private void finalizeLoading(Runnable navigationTask) {
        long elapsed = System.currentTimeMillis() - startTime;
        long remaining = MIN_SPLASH_TIME - elapsed;

        if (remaining > 0) {
            splashHandler.postDelayed(navigationTask, remaining);
        } else {
            navigationTask.run();
        }
    }

    /**
     * Handles fatal loading errors by showing a restart prompt.
     */
    private void handleLoadError() {
        if (!isFinishing()) {
            AlertDialogUtils.showErrorDialogAndRestart(this);
        }
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
        musicManager.setVolume(lastVol);
    }

    @Override
    protected void onDestroy() {
        // Cleanup resources to prevent memory leaks and orphaned animations
        if (logoAnimator != null) {
            logoAnimator.cancel();
        }
        super.onDestroy();
    }
}