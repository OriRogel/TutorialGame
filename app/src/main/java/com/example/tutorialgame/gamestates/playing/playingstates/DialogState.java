package com.example.tutorialgame.gamestates.playing.playingstates;

import static com.example.tutorialgame.engine.core.GameConstants.View.SCREEN_HEIGHT;
import static com.example.tutorialgame.engine.core.GameConstants.View.SCREEN_WIDTH;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.view.MotionEvent;

import com.example.tutorialgame.MyApp;
import com.example.tutorialgame.components.DialogueComponent;
import com.example.tutorialgame.engine.audio.MusicManager;
import com.example.tutorialgame.entities.characters.Character;
import com.example.tutorialgame.engine.core.Game;
import com.example.tutorialgame.engine.ui.DialoguePanel;
import com.example.tutorialgame.gamestates.GameState;
import com.example.tutorialgame.gamestates.playing.PlayingManager;
import com.example.tutorialgame.managers.CameraManager;
import com.example.tutorialgame.managers.DialogueManager;

import java.util.List;

/**
 * Manages the state of the game during dialogues.
 * Handles cinematic camera movements, zooming, letterboxing, and audio ducking.
 */
public class DialogState extends GameState {
    private final PlayingManager playingManager;
    private final DialoguePanel dialoguePanel;
    private Character lastSpeaker;
    private final PointF focusPoint = new PointF();
    private final Paint letterboxPaint = new Paint();
    private static final float MAX_CINEMATIC_ZOOM = 2.2f;

    private float elapsedTime = 0f;
    private static final float ZOOM_DURATION = 0.45f;

    private DialogueComponent currentDialogue;
    private static boolean isEnding = false;

    public DialogState(Game game, PlayingManager playingManager) {
        super(game);
        this.playingManager = playingManager;
        this.dialoguePanel = new DialoguePanel(this);
        letterboxPaint.setColor(Color.BLACK);
    }

    @Override
    public void onEnter() {
        super.onEnter();
        MusicManager.getInstance(context).duck();
        isEnding = false;
        elapsedTime = 0f;
    }

    @Override
    public void onExit() {
        super.onExit();
        MusicManager.getInstance(context).unDuck();
        CameraManager.setTempZoom(CameraManager.getFinalZoom());
    }

    @Override
    public void update(double delta) {
        dialoguePanel.update(delta);
        updateFocusPoint();
        CameraManager.lookAt(focusPoint.x, focusPoint.y, delta);

        float startZoom = CameraManager.getFinalZoom();
        float deltaZoom = MAX_CINEMATIC_ZOOM - startZoom;

        if (!isEnding) {
            elapsedTime = Math.min(ZOOM_DURATION, elapsedTime + (float) delta);
        } else {
            elapsedTime = Math.max(0f, elapsedTime - (float) delta);
            if (elapsedTime <= 0f) {
                playingManager.setCurrentPlayingState(PlayingManager.PlayingState.OVER_WORLD);
            }
        }

        float currentZoom = easeInOutCubic(elapsedTime, startZoom, deltaZoom, ZOOM_DURATION);
        CameraManager.setTempZoom(currentZoom);

        float alphaProgress = easeInOutCubic(elapsedTime, 0, 220, ZOOM_DURATION);
        letterboxPaint.setAlpha((int) alphaProgress);
    }

    private void updateFocusPoint() {
        if (lastSpeaker == null) return;
        Character player = playingManager.getOverWorld().getPlayer();
        focusPoint.x = (player.getHitBox().centerX() + lastSpeaker.getHitBox().centerX()) / 2f;
        focusPoint.y = (player.getHitBox().centerY() + lastSpeaker.getHitBox().centerY()) / 2f;
    }

    @Override
    public void render(Canvas c) {
        playingManager.getOverWorld().renderWithoutUi(c);

        if (letterboxPaint.getAlpha() > 0) {
            float barHeight = easeInOutCubic(elapsedTime, 0, SCREEN_HEIGHT * 0.12f, ZOOM_DURATION);
            c.drawRect(0, 0, SCREEN_WIDTH, barHeight, letterboxPaint);
            c.drawRect(0, SCREEN_HEIGHT - barHeight, SCREEN_WIDTH, SCREEN_HEIGHT, letterboxPaint);
        }

        dialoguePanel.draw(c);
    }

    @Override
    public void touchEvents(MotionEvent event) {
        if (!isEnding) {
            dialoguePanel.eventHandler(event);
        }
    }

    public void setCurrentSpeaker(Character speaker) {
        setCurrentSpeaker(speaker, speaker.getGameCharType().name());
    }

    public void setCurrentSpeaker(Character speaker, String dialogueId) {
        prepareSpeaker(speaker);
        List<String> lines = DialogueManager.resolveDialogue(dialogueId);
        speaker.getDialogueComponent().updateLines(lines);
        this.currentDialogue = speaker.getDialogueComponent();
        dialoguePanel.startDialogue(speaker);
    }

    public void setCurrentSpeakerWithLines(Character speaker, List<String> lines) {
        prepareSpeaker(speaker);
        speaker.getDialogueComponent().updateLines(lines);
        this.currentDialogue = speaker.getDialogueComponent();
        dialoguePanel.startDialogue(speaker);
    }

    private void prepareSpeaker(Character speaker) {
        lastSpeaker = speaker;
        Character player = playingManager.getOverWorld().getPlayer();
        if (speaker != player) {
            player.turnTowardsTarget(speaker);
            speaker.turnTowardsTarget(player);
        }
        speaker.resetAnimation();
        speaker.setAttacking(false);
    }

    public void continueLastDialog() {
        if (lastSpeaker != null) {
            setCurrentSpeaker(lastSpeaker);
        }
    }

    public static void endDialogue(String speakerName) {
        isEnding = true;
        MyApp.getProgress().registerEncounter(speakerName);
    }

    public String getNextDialogueLine() {
        return (currentDialogue != null) ? currentDialogue.getNextLine() : null;
    }

    private float easeInOutCubic(float t, float b, float c, float d) {
        t /= d / 2;
        if (t < 1) return c / 2 * t * t * t + b;
        t -= 2;
        return c / 2 * (t * t * t + 2) + b;
    }

    public PlayingManager getPlayingManager() { return playingManager; }
}
