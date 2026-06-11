package com.example.tutorialgame.engine.core;

import com.example.tutorialgame.gamestates.GameState;
import com.example.tutorialgame.gamestates.State;
import com.example.tutorialgame.engine.ui.customviews.buttons.GameButton;

import java.util.EnumMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Manages game states and transitions in a thread-safe manner.
 */
public class StateMachine {
    private final Map<State, GameState> states = new EnumMap<>(State.class);
    private volatile GameState currentStateObj;
    private volatile State currentState;
    private final Queue<State> transitionQueue = new ConcurrentLinkedQueue<>();

    public void registerState(State state, GameState instance) {
        states.put(state, instance);
    }

    /**
     * Queues a state transition to be processed on the next update cycle.
     */
    public void queueTransition(State newState) {
        if (newState != null) {
            transitionQueue.add(newState);
        }
    }

    /**
     * Processes any pending state transitions.
     * This should be called at the beginning of the update loop on the game thread.
     */
    public void processTransitions() {
        State next = transitionQueue.poll();
        // Clear the queue if multiple transitions were queued, we only care about the latest one
        while (!transitionQueue.isEmpty()) {
            next = transitionQueue.poll();
        }

        if (next != null && next != currentState) {
            if (currentStateObj != null) {
                currentStateObj.onExit();
            }

            currentState = next;
            currentStateObj = states.get(next);

            if (currentStateObj != null) {
                currentStateObj.onEnter();
            }

            GameButton.releaseExclusiveOwner();
        }
    }

    public GameState getCurrentStateObj() {
        return currentStateObj;
    }

    public State getCurrentState() {
        return currentState;
    }
    
    public void setInitialState(State state) {
        this.currentState = state;
        this.currentStateObj = states.get(state);
        if (this.currentStateObj != null) {
            this.currentStateObj.onEnter();
        }
    }
}
