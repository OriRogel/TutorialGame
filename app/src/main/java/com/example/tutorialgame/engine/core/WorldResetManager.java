package com.example.tutorialgame.engine.core;

import android.util.Log;
import com.example.tutorialgame.gamestates.State;
import com.example.tutorialgame.gamestates.playing.PlayingManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Handles the background logic for resetting the game world.
 */
public class WorldResetManager {
    private static final String TAG = "WorldResetManager";
    private final AtomicBoolean isResetting = new AtomicBoolean(false);
    private final ExecutorService backgroundExecutor = Executors.newSingleThreadExecutor();
    private final PlayingManager playingManager;
    private final StateMachine stateMachine;

    public WorldResetManager(PlayingManager playingManager, StateMachine stateMachine) {
        this.playingManager = playingManager;
        this.stateMachine = stateMachine;
    }

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
                        stateMachine.queueTransition(State.PLAYING);
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

    public boolean isResetting() {
        return isResetting.get();
    }

    public void shutdown() {
        backgroundExecutor.shutdownNow();
    }
}
