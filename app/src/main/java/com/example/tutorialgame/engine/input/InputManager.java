package com.example.tutorialgame.engine.input;

import android.view.MotionEvent;
import com.example.tutorialgame.engine.core.StateMachine;
import com.example.tutorialgame.engine.ui.HUDManager;
import com.example.tutorialgame.gamestates.GameState;

/**
 * Handles and delegates input events.
 */
public class InputManager {
    private final StateMachine stateMachine;
    private final HUDManager hudManager;

    public InputManager(StateMachine stateMachine, HUDManager hudManager) {
        this.stateMachine = stateMachine;
        this.hudManager = hudManager;
    }

    public boolean handleTouchEvent(MotionEvent event, boolean isResetting) {
        GameState currentStateObj = stateMachine.getCurrentStateObj();
        if (isResetting || currentStateObj == null) return false;

        currentStateObj.touchEvents(event);
        hudManager.handleInput(event);
        
        return true;
    }
}