package com.example.tutorialgame.engine.core;

import android.content.Context;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import androidx.annotation.NonNull;

import com.example.tutorialgame.MyApp;
import com.example.tutorialgame.engine.input.InputManager;
import com.example.tutorialgame.engine.renderer.GameRenderer;
import com.example.tutorialgame.engine.ui.HUDManager;
import com.example.tutorialgame.engine.ui.effects.impcateffects.ImpactEffectType;
import com.example.tutorialgame.gamestates.DeathScreen;
import com.example.tutorialgame.gamestates.GameState;
import com.example.tutorialgame.gamestates.State;
import com.example.tutorialgame.gamestates.UpgradeState;
import com.example.tutorialgame.gamestates.cutscenes.SceneManager;
import com.example.tutorialgame.gamestates.menu.MenuManager;
import com.example.tutorialgame.gamestates.playing.PlayingManager;

/**
 * The core Game class that coordinates the game loop, states, and rendering.
 * Refactored to delegate responsibilities to specialized components.
 */
public class Game {
    private final GameLoop gameLoop;
    private final Context context;

    // Components
    private final StateMachine stateMachine;
    private final HUDManager hudManager;
    private final GameRenderer renderer;
    private final InputManager inputManager;
    private final WorldResetManager resetManager;

    // Managers (State Instances)
    private final MenuManager menuManager;
    private final PlayingManager playingManager;
    private final DeathScreen deathScreen;
    private final SceneManager sceneManager;
    private final UpgradeState upgradeState;

    public Game(@NonNull SurfaceHolder holder, @NonNull Context context) {
        this.context = context;

        // 1. Initialize State Machine
        this.stateMachine = new StateMachine();

        // 2. Initialize State Managers
        this.menuManager = new MenuManager(this);
        this.playingManager = new PlayingManager(this);
        this.deathScreen = new DeathScreen(this);
        this.upgradeState = new UpgradeState(this);
        this.sceneManager = new SceneManager(this);

        // 3. Register States
        stateMachine.registerState(State.MENU, menuManager);
        stateMachine.registerState(State.PLAYING, playingManager);
        stateMachine.registerState(State.DEATH_SCREEN, deathScreen);
        stateMachine.registerState(State.UPGRADE_STATE, upgradeState);
        stateMachine.registerState(State.CUTSCENE, sceneManager);

        // 4. Initialize Core Components
        this.hudManager = new HUDManager(context);
        this.renderer = new GameRenderer(holder, stateMachine, playingManager, hudManager);
        this.inputManager = new InputManager(stateMachine, hudManager);
        this.resetManager = new WorldResetManager(playingManager, stateMachine);
        
        this.gameLoop = new GameLoop(this);

        initInitialState();
    }

    private void initInitialState() {
        State initialState = MyApp.getWorldStateDoc().getCheckPoint("seen_cutscene_coldOpening") 
                ? State.PLAYING : State.CUTSCENE;
        stateMachine.setInitialState(initialState);
    }

    public void update(double delta) {
        stateMachine.processTransitions();

        if (resetManager.isResetting()) return;

        State current = stateMachine.getCurrentState();
        GameState currentObj = stateMachine.getCurrentStateObj();
        if (currentObj == null) return;

        // Dynamic Background Update Logic
        double backgroundSpeed = current.getBackgroundUpdateSpeed();
        if (backgroundSpeed > 0 && current != State.PLAYING) {
            playingManager.update(delta * backgroundSpeed);
        }

        // Current State Update
        currentObj.update(delta);

        hudManager.update(gameLoop.getFPS());
    }

    public void render() {
        renderer.render(resetManager.isResetting(), gameLoop.getFPS());
    }

    public boolean touchEvent(MotionEvent event) {
        return inputManager.handleTouchEvent(event, resetManager.isResetting());
    }

    public void restartGame() {
        resetManager.restartGame();
    }

    public static void setNextGameState(State newState) {
        // This static method is still used by various parts of the game
        // We'll need a way to access the current Game instance or StateMachine
        // For now, we can use a static reference or find a better way to decouple
        // But to keep it working with existing code:
        StaticGameStateBridge.setNextState(newState);
    }

    // Bridge for handleStateChange in the next update
    public void processNextState() {
        State next = StaticGameStateBridge.getAndClearNextState();
        if (next != null) {
            stateMachine.queueTransition(next);
        }
    }

    // Accessors for Managers
    public Context getContext() { return context; }
    public PlayingManager getPlayingManager() { return playingManager; }
    public State getCurrentGameState() { return stateMachine.getCurrentState(); }

    // Lifecycle
    public void startGameLoop() { gameLoop.startGameLoop(); }
    public void stopGameLoop() { gameLoop.stopGameLoop(); }
    
    public void onDestroy() {
        stopGameLoop();
        resetManager.shutdown();
    }

    // UI Configuration
    public void setTapEffectType(ImpactEffectType tapType) {
        hudManager.setTapEffectType(tapType);
    }

    public void setShowFPS(boolean showFPS) {
        hudManager.setShowFPS(showFPS);
    }

    public void refreshUI() {
        if (menuManager != null) {
            menuManager.refreshMenus();
        }
    }
}
