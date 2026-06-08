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

import com.example.tutorialgame.MyApp;
import com.example.tutorialgame.engine.renderer.TextRenderer;
import com.example.tutorialgame.engine.ui.customviews.buttons.GameButton;
import com.example.tutorialgame.engine.ui.effects.impcateffects.TapEffect;
import com.example.tutorialgame.engine.ui.effects.impcateffects.ImpactEffectType;
import com.example.tutorialgame.gamestates.DeathScreen;
import com.example.tutorialgame.gamestates.cutscenes.SceneManager;
import com.example.tutorialgame.gamestates.menu.MenuManager;
import com.example.tutorialgame.gamestates.playing.PlayingManager;
import com.example.tutorialgame.gamestates.UpgradeState;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Game {
    final private SurfaceHolder holder;
    final private GameLoop gameLoop;
    private final TapEffect tapEffect = new TapEffect(null);
    private final PointF lastTouch = new PointF();
    private MenuManager menuManager;
    private PlayingManager playingManager;
    private DeathScreen deathScreen;
    private SceneManager sceneManager;
    private UpgradeState upgradeState;
    private volatile GameState currentGameState;
    private final SharedPreferences spSettings;
    private final Context context;
    private final TextRenderer fpsRendered = new TextRenderer(TILE_SIZE / 2f);
    private boolean showFPS;
    private volatile boolean isResetting;

    // שיפור: שימוש ב-Executor לביצוע פעולות רקע כבדות (כמו ריסטרט)
    private final ExecutorService backgroundExecutor = Executors.newSingleThreadExecutor();

    private static volatile GameState nextGameState = null;

    public Game(SurfaceHolder holder, Context context) {
        this.holder = holder;
        this.context = context;

        gameLoop = new GameLoop(this);
        initGameStates();

        spSettings = context.getSharedPreferences("settings", MODE_PRIVATE);
        initTapEffect();
        initFPS();
    }

    public void update(double delta) {
        if (isResetting || currentGameState == null) return;

        handleStateChange();

        com.example.tutorialgame.gamestates.GameState currentStateObj = getStateInstance(currentGameState);

        if (currentGameState == Game.GameState.UPGRADE_STATE && playingManager != null)
            playingManager.update(delta*0.8);
        if (currentStateObj != null)
            currentStateObj.update(delta);

        if (showFPS) {
            fpsRendered.updateColorBasedOnValue(gameLoop.getFPS(), 0, 60);
        }
    }


    public void render() {
        if (isResetting) return;

        Canvas c = null;
        try {
            c = holder.lockCanvas();
            if (c != null) {
                synchronized (holder) {
                    c.drawColor(Color.BLACK);

                    if (currentGameState != Game.GameState.CUTSCENE && playingManager != null) {
                        playingManager.render(c);
                    }

                    com.example.tutorialgame.gamestates.GameState state = getStateInstance(currentGameState);
                    if (state != null && state != playingManager) state.render(c);

                    drawFPS(c);
                    tapEffect.show(c, lastTouch.x, lastTouch.y);
                }
            }
        } finally {
            if (c != null) holder.unlockCanvasAndPost(c);
        }
    }

    private void initGameStates() {
        menuManager = new MenuManager(this);
        playingManager = new PlayingManager(this);
        deathScreen = new DeathScreen(this);
        upgradeState = new UpgradeState(this);
        sceneManager = new SceneManager(this);

        if (!MyApp.getWorldStateDoc().getCheckPoint("seen_cutscene_coldOpening")) {
            currentGameState = Game.GameState.CUTSCENE;
            sceneManager.onEnter();
        } else {
            currentGameState = Game.GameState.PLAYING;
            playingManager.onEnter();
        }
    }

    public boolean touchEvent(MotionEvent event) {
        if (currentGameState == null || isResetting) return false;
        switch (currentGameState) {
            case MENU: menuManager.touchEvents(event); break;
            case PLAYING: if (playingManager != null) playingManager.touchEvents(event); break;
            case DEATH_SCREEN: deathScreen.touchEvents(event); break;
            case UPGRADE_STATE: upgradeState.touchEvents(event); break;
            case CUTSCENE: sceneManager.touchEvents(event); break;
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            tapEffect.resetEffect();
            lastTouch.set(event.getX(), event.getY());
        }
        return true;
    }

    
    public void restartGame() {
        if (isResetting) return;
        isResetting = true;
        Log.d("GameRestart", "Restart Initiated - Moving to background thread");

        // שיפור 2: העברת הריסטרט ל-Thread רקע כדי למנוע ANR ב-UI Thread
        backgroundExecutor.execute(() -> {
            try {
                if (playingManager != null && playingManager.getOverWorld() != null) {
                    playingManager.getOverWorld().resetWorld(() -> {
                        nextGameState = null;
                        this.currentGameState = Game.GameState.PLAYING;

                        if (playingManager != null) {
                            playingManager.onEnter();
                        }

                        this.isResetting = false;
                        Log.d("GameRestart", "World Reset Successfully - Resume Rendering.");
                    });
                }
            } catch (Exception e) {
                Log.e("GameRestart", "Error during restart", e);
                isResetting = false;
            }
        });
    }

    public static void setNextGameState(GameState newState) {
        nextGameState = newState;
    }

    private void handleStateChange() {
        if (nextGameState == null || isResetting) return;

        if (currentGameState != nextGameState) {
            com.example.tutorialgame.gamestates.GameState old = getStateInstance(currentGameState);
            if (old != null) old.onExit();

            currentGameState = nextGameState;

            com.example.tutorialgame.gamestates.GameState now = getStateInstance(currentGameState);
            if (now != null) now.onEnter();

            GameButton.releaseExclusiveOwner();
        }
        nextGameState = null;
    }

    private void initTapEffect() {
        int index = spSettings.getInt("tapEffect", ImpactEffectType.SPARK.ordinal());
        tapEffect.setTapType(ImpactEffectType.values()[index]);
    }

    private void initFPS() {
        showFPS = spSettings.getBoolean("fps", false);
        fpsRendered.setPosition(SCALE_MULTIPLIER, SCREEN_HEIGHT - fpsRendered.getTextSize() / 3);
    }

    private void drawFPS(Canvas c) {
        if (showFPS) fpsRendered.drawText("FPS: " + gameLoop.getFPS(), c);
    }

    public void startGameLoop() { gameLoop.startGameLoop(); }
    public void stopGameLoop() { gameLoop.stopGameLoop(); }
    public GameState getCurrentGameState() { return currentGameState; }

    public com.example.tutorialgame.gamestates.GameState getStateInstance(GameState s) {
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
        backgroundExecutor.shutdownNow();
    }

    public enum GameState { MENU, PLAYING, DEATH_SCREEN, UPGRADE_STATE, CUTSCENE }
}