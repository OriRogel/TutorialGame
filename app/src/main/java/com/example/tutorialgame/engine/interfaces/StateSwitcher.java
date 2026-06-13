package com.example.tutorialgame.engine.interfaces;

import com.example.tutorialgame.gamestates.State;

/**
 * Interface for components that need to request a game state transition.
 * Decouples game components from the core Game class or static methods.
 */
public interface StateSwitcher {
    /**
     * Requests a transition to a new game state.
     * @param newState The state to transition to.
     */
    void changeState(State newState);
}
