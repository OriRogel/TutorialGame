package com.example.tutorialgame.gamestates.cutscenes;

import android.graphics.Canvas;
import android.view.MotionEvent;
import com.example.tutorialgame.engine.core.Game;
import com.example.tutorialgame.gamestates.BaseState;
import com.example.tutorialgame.gamestates.cutscenes.scenes.GettingSword;
import com.example.tutorialgame.gamestates.cutscenes.scenes.Intro;
import com.example.tutorialgame.gamestates.cutscenes.scenes.SkeletonArise;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the sequencing and execution of cinematic cutscenes.
 * Orchestrates the transition between gameplay and narrative moments
 * based on the player's progress (Checkpoints).
 */
public class SceneManager extends BaseState {
    private final List<BaseScene> scenePlaylist = new ArrayList<>();
    private BaseScene currentScene = null;
    private boolean isStartingDialogueAfter;

    public SceneManager(Game game) {
        super(game);
        buildPlaylist();
    }

    /**
     * Builds the global sequence of story events.
     * New scenes should be added here in chronological order.
     */
    private void buildPlaylist() {
        scenePlaylist.clear();
        scenePlaylist.add(new Intro(game, this));
        scenePlaylist.add(new GettingSword(game, this));
        scenePlaylist.add(new SkeletonArise(game, this));
    }

    /**
     * Searches for the first uncompleted scene in the playlist and starts it.
     * If all scenes are completed, returns control to the gameplay state.
     */
    public void playNextRelevantScene() {
        if (currentScene != null) return; // Guard: Don't interrupt active scene

        for (BaseScene scene : scenePlaylist) {
            if (!scene.checkCheckPoint()) { 
                this.currentScene = scene;
                this.currentScene.onEnter();
                return; 
            }
        }

        // Fallback: If no scenes are pending, resume normal gameplay
        Game.setNextGameState(Game.GameState.PLAYING);
    }

    /**
     * Callback triggered by a BaseScene when it finishes its execution.
     */
    public void onSceneFinished() {
        if (currentScene != null) {
            isStartingDialogueAfter = currentScene.isDialogueAfter();
        }
        this.currentScene = null;
        Game.setNextGameState(Game.GameState.PLAYING);
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