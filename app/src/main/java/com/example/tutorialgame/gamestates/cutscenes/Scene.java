package com.example.tutorialgame.gamestates.cutscenes;

import static com.example.tutorialgame.engine.core.GameConstants.Sprite.SCALE_MULTIPLIER;
import static com.example.tutorialgame.engine.core.GameConstants.Sprite.TILE_SIZE;
import static com.example.tutorialgame.engine.core.GameConstants.View.SCREEN_HEIGHT;
import static com.example.tutorialgame.engine.core.GameConstants.View.SCREEN_WIDTH;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.MotionEvent;

import com.example.tutorialgame.R;
import com.example.tutorialgame.engine.audio.MusicManager;
import com.example.tutorialgame.engine.core.Game;
import com.example.tutorialgame.engine.renderer.TextRenderer;
import com.example.tutorialgame.gamestates.GameState;
import com.example.tutorialgame.managers.WorldEventManager;

/**
 * Scene class for cinematic sequences.
 * Handles frame sequencing, fade effects, and narrative progression.
 */
public class Scene extends GameState {
    protected final SceneManager sceneManager;
    private double frameTimer;
    private double inputGuardTimer;

    private final TextRenderer nextLabel;
    private final Bitmap[] frames;
    private int currentFrameIndex;

    private final String checkPointKey;
    private final boolean hasDialogueAfter;
    private final int musicRes;
    private final String onExitEvent;

    private boolean isSkipping = false;
    private final Rect screenRect = new Rect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

    private static final float SECONDS_PER_FRAME = 4.5f;
    private static final float INPUT_DELAY_SEC = 0.5f;

    public Scene(Game game, SceneManager sceneManager, Scenes sceneData) {
        super(game);
        this.sceneManager = sceneManager;
        this.frames = sceneData.getFrameArr();
        this.checkPointKey = sceneData.getCheckPoint();
        this.musicRes = sceneData.getMusicRes();
        this.hasDialogueAfter = sceneData.getDialogueAfter();
        this.onExitEvent = sceneData.getOnExitEvent();

        nextLabel = new TextRenderer(SCALE_MULTIPLIER * 13, R.color.magnolia_white);
        nextLabel.setShadowOffset(SCALE_MULTIPLIER, SCALE_MULTIPLIER);
        nextLabel.setPosition(SCREEN_WIDTH - TILE_SIZE * 3.5f, SCREEN_HEIGHT - TILE_SIZE);
        nextLabel.setFilterBitmap(true);
    }

    @Override
    public void onEnter() {
        super.onEnter();

        // 1. Skip if already completed
        if (checkCheckPoint()) {
            isSkipping = true;
            onExit();
            return;
        }

        // 2. Setup audio
        MusicManager.getInstance(context).play(musicRes);

        // 3. Reset state
        this.currentFrameIndex = 0;
        this.frameTimer = 0;
        this.inputGuardTimer = 0;
        this.nextLabel.setAlpha(0);
    }

    @Override
    public void onExit() {
        super.onExit();
        if (onExitEvent != null) {
            WorldEventManager.triggerEvent(onExitEvent);
        }
        sceneManager.onSceneFinished();
    }

    @Override
    public void update(double delta) {
        if (isSkipping) return;

        frameTimer += delta;
        inputGuardTimer += delta;

        // Visual Fade-in logic for the "Next" prompt
        int currentAlpha = nextLabel.getAlpha();
        if (currentAlpha < 255) {
            int newAlpha = currentAlpha + (int) (255 * delta * 1.5);
            nextLabel.setAlpha(Math.min(newAlpha, 255));
        }

        // Automatic frame progression
        if (frameTimer >= SECONDS_PER_FRAME) {
            if (currentFrameIndex < frames.length - 1) {
                advanceFrame();
            } else {
                finishScene();
            }
        }
    }

    private void advanceFrame() {
        currentFrameIndex++;
        frameTimer = 0;
        nextLabel.setAlpha(0);
    }

    private void finishScene() {
        // Mark as completed in cloud storage
        if (checkPointKey != null && !checkPointKey.isEmpty()) {
            userRepository.getCloudManager().saveGame(checkPointKey);
        }
        onExit();
    }

    @Override
    public void render(Canvas c) {
        if (isSkipping || frames == null || frames.length == 0) return;

        // Draw current cinematic frame scaled to screen
        c.drawBitmap(frames[currentFrameIndex], null, screenRect, nextLabel);

        // Draw "Next" prompt if applicable
        if (currentFrameIndex < frames.length - 1) {
            nextLabel.drawWithShadow(context.getString(R.string.next), c);
        }
    }

    @Override
    public void touchEvents(MotionEvent event) {
        if (isSkipping) return;

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            // Prevent accidental double-taps using input guard
            if (inputGuardTimer > INPUT_DELAY_SEC) {
                if (currentFrameIndex < frames.length - 1) {
                    advanceFrame();
                    inputGuardTimer = 0; // Reset guard for next frame
                } else finishScene();
            }
        }
    }

    public boolean checkCheckPoint() {
        return userRepository.getWorldStateDoc().getCheckPoint(checkPointKey);
    }

    public boolean isDialogueAfter() {
        return hasDialogueAfter;
    }
}