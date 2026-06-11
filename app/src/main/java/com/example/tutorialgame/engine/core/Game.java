package com.example.tutorialgame.engine.core;

import static android.content.Context.MODE_PRIVATE;
import static com.example.tutorialgame.engine.core.GameConstants.Sprite.SCALE_MULTIPLIER;
import static com.example.tutorialgame.engine.core.GameConstants.Sprite.TILE_SIZE;
import static com.example.tutorialgame.engine.core.GameConstants.View.SCREEN_HEIGHT;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.tutorialgame.MyApp;
import com.example.tutorialgame.engine.renderer.TextRenderer;
import com.example.tutorialgame.engine.ui.customviews.buttons.GameButton;
import com.example.tutorialgame.engine.ui.effects.impcateffects.ImpactEffectType;
import com.example.tutorialgame.engine.ui.effects.impcateffects.TapEffect;
import com.example.tutorialgame.gamestates.DeathScreen;
import com.example.tutorialgame.gamestates.GameState;
import com.example.tutorialgame.gamestates.State;
import com.example.tutorialgame.gamestates.UpgradeState;
import com.example.tutorialgame.gamestates.cutscenes.SceneManager;
import com.example.tutorialgame.gamestates.menu.MenuManager;
import com.example.tutorialgame.gamestates.playing.PlayingManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The core Game class that manages the game loop, states, and rendering.
 */
public class Game {
    private static final String TAG = "Game";

    private final SurfaceHolder holder;
    private final GameLoop gameLoop;
    private final TapEffect tapEffect = new TapEffect(null);
    private final PointF lastTouch = new PointF();

    // Game States - Finalized as they are initialized in the constructor
    private final MenuManager menuManager;
    private final PlayingManager playingManager;
    private final DeathScreen deathScreen;
    private final SceneManager sceneManager;
    private final UpgradeState upgradeState;

    private volatile State currentState;
    private volatile GameState currentStateObj; // Volatile for thread safety

    private final SharedPreferences spSettings;
    private final Context context;
    private final TextRenderer fpsRendered = new TextRenderer(TILE_SIZE / 2f);
    private boolean showFPS;

    /**
     * atomic lock to prevent multiple simultaneous restarts.
     */
    private final AtomicBoolean isResetting = new AtomicBoolean(false);

    private final ExecutorService backgroundExecutor = Executors.newSingleThreadExecutor();

    private static final AtomicReference<State> nextState = new AtomicReference<>(null);

    public Game(@NonNull SurfaceHolder holder, @NonNull Context context) {
        this.holder = holder;
        this.context = context;

        // Initialize managers
        this.menuManager = new MenuManager(this);
        this.playingManager = new PlayingManager(this);
        this.deathScreen = new DeathScreen(this);
        this.upgradeState = new UpgradeState(this);
        this.sceneManager = new SceneManager(this);

        this.gameLoop = new GameLoop(this);
        this.spSettings = context.getSharedPreferences("settings", MODE_PRIVATE);

        initInitialState();
        initTapEffect();
        initFPS();
    }

    private void initInitialState() {
        if (!MyApp.getWorldStateDoc().getCheckPoint("seen_cutscene_coldOpening")) {
            currentState = State.CUTSCENE;
        } else {
            currentState = State.PLAYING;
        }
        currentStateObj = getStateInstance(currentState);
        if (currentStateObj != null) {
            currentStateObj.onEnter();
        }
    }

    public void update(double delta) {
        if (isResetting.get() || currentState == null) return;

        handleStateChange();

        // Re-capture to local variable for consistent use in update
        GameState currentObj = currentStateObj;
        if (currentObj == null) return;

        // Dynamic Background Update Logic
        double backgroundSpeed = currentState.getBackgroundUpdateSpeed();
        if (backgroundSpeed > 0 && currentState != State.PLAYING) {
            playingManager.update(delta * backgroundSpeed);
        }

        // Current State Update
        currentObj.update(delta);

        if (showFPS) {
            fpsRendered.updateColorBasedOnValue(gameLoop.getFPS(), 0, 60);
        }
    }

    public void render() {
        if (isResetting.get()) return;

        Canvas c = null;
        try {
            c = holder.lockCanvas();
            if (c != null) {
                synchronized (holder) {
                    c.drawColor(Color.BLACK);

                    // Capture current states to local variables for thread-safe access within render
                    State current = currentState;
                    GameState currentObj = currentStateObj;

                    // Dynamic Background Rendering Logic
                    if (current == State.PLAYING || current.isTransparent()) {
                        playingManager.render(c);
                    }

                    // Current State Rendering (if different from playingManager)
                    if (currentObj != null && current != State.PLAYING) {
                        currentObj.render(c);
                    }

                    drawFPS(c);
                    tapEffect.show(c, lastTouch.x, lastTouch.y);
                }
            }
        } finally {
            if (c != null) {
                holder.unlockCanvasAndPost(c);
            }
        }
    }

    public boolean touchEvent(MotionEvent event) {
        GameState currentObj = currentStateObj;
        if (isResetting.get() || currentObj == null) return false;

        currentObj.touchEvents(event);

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            tapEffect.resetEffect();
            lastTouch.set(event.getX(), event.getY());
        }
        return true;
    }

    /**
     * Resets the game world and returns to the playing state.
     * The reset is performed on a background thread to avoid blocking the UI/Game thread.
     */
    public void restartGame() {
        if (!isResetting.compareAndSet(false, true)) {
            Log.d(TAG, "Restart already in progress. Ignoring request.");
            return;
        }

        Log.d(TAG, "Restart Initiated - Moving to background thread");

        backgroundExecutor.execute(() -> {
            try {
                if (playingManager.getOverWorld() != null) {
                    playingManager.getOverWorld().resetWorld(() -> {
                        // Safe state transition via handleStateChange in the next update loop
                        setNextGameState(State.PLAYING);
                        isResetting.set(false);
                        Log.d(TAG, "World Reset Successfully.");
                    });
                } else {
                    isResetting.set(false);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error during restart", e);
                isResetting.set(false);
            }
        });
    }

    public static void setNextGameState(State newState) {
        nextState.set(newState);
    }

    private void handleStateChange() {
        // Only consume the nextState if we are not resetting
        if (isResetting.get()) return;

        State next = nextState.getAndSet(null);
        if (next == null) return;

        if (currentState != next) {
            if (currentStateObj != null) {
                currentStateObj.onExit();
            }

            currentState = next;
            currentStateObj = getStateInstance(next);

            if (currentStateObj != null) {
                currentStateObj.onEnter();
            }

            GameButton.releaseExclusiveOwner();
        }
    }

    private void initTapEffect() {
        int index = spSettings.getInt("tapEffect", ImpactEffectType.SPARK.ordinal());
        ImpactEffectType[] types = ImpactEffectType.values();
        if (index >= 0 && index < types.length) {
            tapEffect.setTapType(types[index]);
        } else {
            tapEffect.setTapType(ImpactEffectType.SPARK);
        }
    }

    private void initFPS() {
        showFPS = spSettings.getBoolean("fps", false);
        fpsRendered.setPosition(SCALE_MULTIPLIER, SCREEN_HEIGHT - fpsRendered.getTextSize() / 3);
    }

    private void drawFPS(Canvas c) {
        if (showFPS) {
            fpsRendered.drawText("FPS: " + gameLoop.getFPS(), c);
        }
    }

    public void startGameLoop() { gameLoop.startGameLoop(); }
    public void stopGameLoop() { gameLoop.stopGameLoop(); }
    public State getCurrentGameState() { return currentState; }

    @Nullable
    public GameState getStateInstance(State s) {
        if (s == null) return null;
        switch (s) {
            case MENU: return menuManager;
            case PLAYING: return playingManager;
            case DEATH_SCREEN: return deathScreen;
            case UPGRADE_STATE: return upgradeState;
            case CUTSCENE: return sceneManager;
            default: return null;
        }
    }

    public void setTapEffectType(ImpactEffectType tapType) {
        this.tapEffect.setTapType(tapType);
        spSettings.edit().putInt("tapEffect", tapType.ordinal()).apply();
    }

    public void setShowFPS(boolean showFPS) { this.showFPS = showFPS; }
    public Context getContext() { return context; }
    public PlayingManager getPlayingManager() { return playingManager; }

    /**
     * Refreshes the UI components of the game to reflect language changes.
     */
    public void refreshUI() {
        if (menuManager != null) {
            menuManager.refreshMenus();
        }
    }

    public void onDestroy() {
        stopGameLoop();
        backgroundExecutor.shutdownNow();
    }
}