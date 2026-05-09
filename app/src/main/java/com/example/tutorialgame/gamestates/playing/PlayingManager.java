package com.example.tutorialgame.gamestates.playing;

import android.graphics.Canvas;
import android.view.MotionEvent;
import com.example.tutorialgame.entities.characters.Character;
import com.example.tutorialgame.engine.core.Game;
import com.example.tutorialgame.gamestates.BaseState;
import com.example.tutorialgame.gamestates.playing.playingstates.DialogState;
import com.example.tutorialgame.gamestates.playing.playingstates.OverWorld;
import com.example.tutorialgame.managers.WorldEventManager;

import java.util.List;

public class PlayingManager extends BaseState {
    private OverWorld overWorld;
    private DialogState dialogState;
    private PlayingState currentPlayingState = PlayingState.OVER_WORLD;

    @Override
    public void onEnter() {
        switch (currentPlayingState) {
            case OVER_WORLD:
                overWorld.onEnter();
                break;
            case DIALOG:
                dialogState.onEnter();
                break;
        }
    }

    @Override
    public void onExit() {
        switch (currentPlayingState) {
            case OVER_WORLD:
                overWorld.onExit();
                break;
            case DIALOG:
                dialogState.onExit();
                break;
        }
    }

    public PlayingManager(Game game) {
        super(game);
        initPlayingState();
    }

    @Override
    public void update(double delta) {
        switch (currentPlayingState) {
            case OVER_WORLD:
                overWorld.update(delta);
                break;
            case DIALOG:
                overWorld.updateEffects(delta);
                dialogState.update(delta);
                break;
        }
    }

    @Override
    public void render(Canvas c) {
        switch (currentPlayingState) {
            case OVER_WORLD:
                overWorld.render(c);
                break;
            case DIALOG:
                dialogState.render(c);
                break;
        }
    }

    @Override
    public void touchEvents(MotionEvent event) {
        switch (currentPlayingState) {
            case OVER_WORLD:
                overWorld.touchEvents(event);
                break;
            case DIALOG:
                dialogState.touchEvents(event);
                break;
        }
    }

    private void initPlayingState() {
        overWorld = new OverWorld(game, this);
        dialogState = new DialogState(game, this);
    }

    public void setCurrentPlayingState(PlayingState currentPlayingState) {
        onExit();
        this.currentPlayingState = currentPlayingState;
        onEnter();
    }

    public void setDialogState(Character speaker) {
        setCurrentPlayingState(PlayingState.DIALOG);
        dialogState.setCurrentSpeaker(speaker);
        speaker.onDialogue();
    }

    /**
     * פותח דיאלוג עם שורות טקסט ספציפיות (שימושי למחשבות של השחקן).
     */
    public void setCustomDialogState(Character speaker, List<String> lines) {
        setCurrentPlayingState(PlayingState.DIALOG);
        dialogState.setCurrentSpeakerWithLines(speaker, lines);
    }

    public void continueDialog() {
        setCurrentPlayingState(PlayingState.DIALOG);
        dialogState.continueLastDialog();
    }

    public OverWorld getOverWorld() {
        return overWorld;
    }

    public enum PlayingState {
        OVER_WORLD, DIALOG
    }
}
