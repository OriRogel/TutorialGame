package com.example.tutorialgame.engine.core;

import com.example.tutorialgame.gamestates.State;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A bridge to allow static access to state transitions, 
 * maintaining compatibility with existing code while transitioning 
 * to the new architecture.
 */
public class StaticGameStateBridge {
    private static final AtomicReference<State> nextState = new AtomicReference<>(null);

    public static void setNextState(State state) {
        nextState.set(state);
    }

    public static State getAndClearNextState() {
        return nextState.getAndSet(null);
    }
}
