package com.example.tutorialgame.gamestates.cutscenes;

import android.graphics.Canvas;
import android.view.MotionEvent;
import com.example.tutorialgame.engine.core.Game;
import com.example.tutorialgame.gamestates.GameState;
import com.example.tutorialgame.gamestates.State;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the sequencing and execution of cinematic cutscenes.
 * Orchestrates the transition between gameplay and narrative moments
 * based on the player's progress (Checkpoints).
 */
public class SceneManager extends GameState {
    private final List<Scene> scenePlaylist = new ArrayList<>();
    private Scene currentScene = null;
    private boolean isStartingDialogueAfter;

    public SceneManager(Game game) {
        super(game);
        buildPlaylist();
    }

    /**
     * Builds the global sequence of story events.
     * New scenes are added here automatically from the Scenes enum.
     */
    private void buildPlaylist() {
        scenePlaylist.clear();
        for (Scenes scene : Scenes.values()) {
            scenePlaylist.add(new Scene(game, this, scene));
        }
    }

    /**
     * Searches for the first uncompleted scene in the playlist and starts it.
     * If all scenes are completed, returns control to the gameplay state.
     */
    public void playNextRelevantScene() {
        if (currentScene != null) return; // Guard: Don't interrupt active scene

        for (Scene scene : scenePlaylist) {
            if (!scene.checkCheckPoint()) { 
                this.currentScene = scene;
                this.currentScene.onEnter();
                return; 
            }
        }

        // Fallback: If no scenes are pending, resume normal gameplay
        Game.setNextGameState(State.PLAYING);
    }

    /**
     * Callback triggered by a Scene when it finishes its execution.
     */
    public void onSceneFinished() {
        if (currentScene != null) {
            isStartingDialogueAfter = currentScene.isDialogueAfter();
        }
        this.currentScene = null;
        Game.setNextGameState(State.PLAYING);
    }

    @Override
    public void onEnter() {
        super.onEnter();
        playNextRelevantScene();
    }

    @Override
    public void onExit() {
        super.onExit();
        // Trigger post-scene dialogue if the completed scene required it
        if (isStartingDialogueAfter) {
            game.getPlayingManager().continueDialog();
            isStartingDialogueAfter = false;
        }
    }

    @Override
    public void update(double delta) {
        if (currentScene != null) {
            currentScene.update(delta);
        }
    }

    @Override
    public void render(Canvas c) {
        if (currentScene != null) {
            currentScene.render(c);
        }
    }

    @Override
    public void touchEvents(MotionEvent event) {
        if (currentScene != null) {
            currentScene.touchEvents(event);
        }
    }
}