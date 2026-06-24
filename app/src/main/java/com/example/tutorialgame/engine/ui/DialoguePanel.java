package com.example.tutorialgame.engine.ui;

import static com.example.tutorialgame.engine.core.GameConstants.Sprite.SCALE_MULTIPLIER;
import static com.example.tutorialgame.engine.core.GameConstants.Sprite.TILE_SIZE;
import static com.example.tutorialgame.engine.core.GameConstants.View.SCREEN_HEIGHT;
import static com.example.tutorialgame.engine.core.GameConstants.View.SCREEN_WIDTH;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.text.Layout;
import android.text.StaticLayout;
import android.view.MotionEvent;

import com.example.tutorialgame.R;
import com.example.tutorialgame.engine.audio.SoundManager;
import com.example.tutorialgame.engine.renderer.TextRenderer;
import com.example.tutorialgame.engine.ui.customviews.buttons.circles.CircleButton;
import com.example.tutorialgame.engine.ui.customviews.buttons.circles.CircleImages;
import com.example.tutorialgame.entities.characters.Character;
import com.example.tutorialgame.entities.characters.GameCharacters;
import com.example.tutorialgame.gamestates.playing.playingstates.DialogState;
import com.example.tutorialgame.managers.BitmapManager;
import com.example.tutorialgame.ui.base.BaseActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Manages the visual dialogue interface using a state machine and optimized rendering.
 */
public class DialoguePanel {

    private enum PanelState { HIDDEN, ENTERING, TYPING, WAITING, EXITING }
    private PanelState currentState = PanelState.HIDDEN;

    private List<String> currentPages = new ArrayList<>();
    private int pageIndex = 0;
    private final int dialogueTextHeight;

    private Character currentSpeaker;
    private Bitmap faceSet;
    private final Bitmap dialogBox; // Changed from static
    private String speakerName;
    private final TextRenderer namePaint, textRenderer;

    private final float targetY, dialogBoxX, faceSetX, faceSetY;
    private static final float SLIDE_SPEED = TILE_SIZE * 12;
    private static final float EXIT_SPEED_MULTIPLIER = 1.5f;
    private float offsetY;

    private final CircleButton btnNext; // Changed from static
    private final DialogState dialogState;
    private final SoundManager soundManager;

    // Typewriter & Layout optimization
    private char[] fullLineChars;
    private android.text.StaticLayout cachedLayout;
    private int charIndex;
    private float timeSinceLastChar;

    private static final float TIME_PER_CHAR = 0.04f;
    private static final float SHORT_BREAK = 0.15f;
    private static final float LONG_BREAK = 0.45f;

    private final float dialogueTextX, dialogueTextY;
    private final int dialogueTextWidth;
    private int voiceRes = -1;

    public DialoguePanel(DialogState dialogState) {
        this.dialogState = dialogState;
        this.soundManager = SoundManager.getInstance(BaseActivity.getContext().getApplicationContext());

        // Initialize resources here instead of static block
        this.dialogBox = BitmapManager.getBitmap(R.drawable.dialogbox_faceset, 1.0, false);
        this.btnNext = new CircleButton(
                new PointF(SCREEN_WIDTH - 2.1f * TILE_SIZE, 2.7f * TILE_SIZE),
                CircleImages.NEXT,
                true
        );

        this.targetY = SCREEN_HEIGHT - Objects.requireNonNull(dialogBox).getHeight();
        this.offsetY = SCREEN_HEIGHT;
        this.dialogBoxX = SCALE_MULTIPLIER;
        this.faceSetX = 6 * SCALE_MULTIPLIER;
        this.faceSetY = 14 * SCALE_MULTIPLIER;

        namePaint = new TextRenderer(9 * SCALE_MULTIPLIER, R.color.floral_white);
        namePaint.setPosition(9 * SCALE_MULTIPLIER, 8.5f * SCALE_MULTIPLIER);

        textRenderer = new TextRenderer(8 * SCALE_MULTIPLIER, R.color.black);
        dialogueTextX = 62 * SCALE_MULTIPLIER;
        dialogueTextY = 19 * SCALE_MULTIPLIER;

        dialogueTextWidth = (int) (dialogBox.getWidth() - dialogueTextX - (12 * SCALE_MULTIPLIER));
        dialogueTextHeight = (int) (dialogBox.getHeight() - dialogueTextY);
    }

    public void update(double delta) {
        switch (currentState) {
            case ENTERING:
                offsetY -= (float) (SLIDE_SPEED * delta);
                if (offsetY <= targetY) {
                    offsetY = targetY;
                    currentState = PanelState.TYPING;
                }
                break;

            case TYPING:
                updateTypewriter(delta);
                break;

            case EXITING:
                offsetY += (float) (SLIDE_SPEED * delta * EXIT_SPEED_MULTIPLIER);
                if (offsetY >= SCREEN_HEIGHT) {
                    offsetY = SCREEN_HEIGHT;
                    currentState = PanelState.HIDDEN;
                }
                break;
        }
    }

    private void updateTypewriter(double delta) {
        if (fullLineChars == null || charIndex >= fullLineChars.length) {
            currentState = PanelState.WAITING;
            return;
        }

        timeSinceLastChar += (float) delta;
        float requiredDelay = getDelayForChar(fullLineChars[charIndex > 0 ? charIndex - 1 : 0]);

        if (timeSinceLastChar >= requiredDelay) {
            timeSinceLastChar -= requiredDelay;
            charIndex++;

            // Rebuild layout only when text changes
            updateCachedLayout();

            char current = fullLineChars[charIndex - 1];
            if (current != ' ' && !isPunctuation(current)) {
                soundManager.playSfx(voiceRes);
            }
        }
    }

    private void updateCachedLayout() {
        if (fullLineChars == null) return;
        // Use StaticLayout.Builder to avoid creating intermediate strings
        cachedLayout = StaticLayout.Builder.obtain(
                        new String(fullLineChars, 0, charIndex),
                        0, charIndex, textRenderer, dialogueTextWidth)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(0f, 1.0f)
                .setIncludePad(false)
                .build();
    }

    private float getDelayForChar(char c) {
        switch (c) {
            case '.': case '?': case '!': return LONG_BREAK;
            case ',': return SHORT_BREAK;
            default: return TIME_PER_CHAR;
        }
    }

    private boolean isPunctuation(char c) {
        return ".,!? ".indexOf(c) != -1;
    }

    public void draw(Canvas c) {
        if (currentState == PanelState.HIDDEN) return;

        c.save();
        c.translate(0, offsetY);

        c.drawBitmap(dialogBox, dialogBoxX, 0, null);
        if (faceSet != null) c.drawBitmap(faceSet, faceSetX, faceSetY, null);
        if (speakerName != null) namePaint.drawText(speakerName, c);

        if (cachedLayout != null) {
            c.save();
            // Using the cached layout directly for performance
            c.translate(dialogueTextX - 6 * SCALE_MULTIPLIER, dialogueTextY - 3 * SCALE_MULTIPLIER);
            cachedLayout.draw(c);
            c.restore();
        }

        btnNext.draw(c);
        c.restore();
    }

    public boolean eventHandler(MotionEvent event) {
        if (currentState == PanelState.HIDDEN || currentState == PanelState.EXITING) return false;

        // Translate touch coordinates to local panel space
        event.offsetLocation(0, -offsetY);

        try {
            if (btnNext.eventHandler(event)) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (currentState == PanelState.TYPING) finishTyping();
                    else if (currentState == PanelState.WAITING) advanceDialogue();
                }
                return true;
            }
            return true; // Consume all other touches to prevent interacting with background
        } finally {
            event.offsetLocation(0, offsetY);
        }
    }

    private void startNewLine(String text) {
        if (text.startsWith("> ")) {
            setSpeakerData(dialogState.getPlayingManager().getOverWorld().getPlayer());
            text = text.substring(2);
        } else {
            setSpeakerData(currentSpeaker);
        }

        this.currentPages = textRenderer.splitTextIntoPages(text, dialogueTextWidth, dialogueTextHeight);
        this.pageIndex = 0;
        displayCurrentPage();
    }

    private void displayCurrentPage() {
        if (pageIndex < currentPages.size()) {
            this.fullLineChars = currentPages.get(pageIndex).toCharArray();
            this.charIndex = 0;
            this.timeSinceLastChar = 0;
            this.cachedLayout = null;
            this.currentState = (currentState == PanelState.ENTERING) ? PanelState.ENTERING : PanelState.TYPING;
        }
    }

    private void advanceDialogue() {
        if (pageIndex < currentPages.size() - 1) {
            pageIndex++;
            displayCurrentPage();
        } else {
            String nextLine = dialogState.getNextDialogueLine();
            if (nextLine != null) {
                startNewLine(nextLine);
            } else {
                String speakerId = currentSpeaker.getGameCharType().name();
                dialogState.getPlayingManager().getQuestManager().onDialogueFinished(speakerId);
                DialogState.endDialogue(currentSpeaker);
                currentState = PanelState.EXITING;
            }
        }
    }

    public void startDialogue(Character speaker) {
        this.currentSpeaker = speaker;
        this.offsetY = SCREEN_HEIGHT;
        this.currentState = PanelState.ENTERING;

        String firstLine = dialogState.getNextDialogueLine();
        if (firstLine != null) startNewLine(firstLine);
        else currentState = PanelState.EXITING;
    }

    private void setSpeakerData(Character speaker) {
        GameCharacters data = speaker.getGameCharType();
        this.faceSet = data.getFaceSet();
        this.speakerName = speaker.getName();
        this.voiceRes = data.getVoiceRes();
    }

    private void finishTyping() {
        if (fullLineChars != null) {
            charIndex = fullLineChars.length;
            updateCachedLayout();
            currentState = PanelState.WAITING;
        }
    }
}