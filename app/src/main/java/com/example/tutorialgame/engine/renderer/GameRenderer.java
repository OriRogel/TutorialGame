package com.example.tutorialgame.engine.renderer;

import android.graphics.Canvas;
import android.graphics.Color;
import android.view.SurfaceHolder;

import com.example.tutorialgame.engine.core.StateMachine;
import com.example.tutorialgame.engine.ui.HUDManager;
import com.example.tutorialgame.gamestates.GameState;
import com.example.tutorialgame.gamestates.State;
import com.example.tutorialgame.gamestates.playing.PlayingManager;

/**
 * Handles the main rendering pipeline for the game.
 */
public class GameRenderer {
    private final SurfaceHolder holder;
    private final StateMachine stateMachine;
    private final PlayingManager playingManager;
    private final HUDManager hudManager;

    public GameRenderer(SurfaceHolder holder, StateMachine stateMachine, PlayingManager playingManager, HUDManager hudManager) {
        this.holder = holder;
        this.stateMachine = stateMachine;
        this.playingManager = playingManager;
        this.hudManager = hudManager;
    }

    public void render(boolean isResetting, int fps) {
        if (isResetting) return;

        Canvas c = null;
        try {
            c = holder.lockCanvas();
            if (c != null) {
                synchronized (holder) {
                    c.drawColor(Color.BLACK);

                    State currentState = stateMachine.getCurrentState();
                    GameState currentStateObj = stateMachine.getCurrentStateObj();

                    // Render background if playing or transparent
                    if (currentState == State.PLAYING || (currentState != null && currentState.isTransparent())) {
                        playingManager.render(c);
                    }

                    // Render current state if not playing
                    if (currentStateObj != null && currentState != State.PLAYING) {
                        currentStateObj.render(c);
                    }

                    hudManager.render(c, fps);
                }
            }
        } finally {
            if (c != null) {
                holder.unlockCanvasAndPost(c);
            }
        }
    }
}
