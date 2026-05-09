package com.example.tutorialgame.components;

import static com.example.tutorialgame.engine.core.GameConstants.Sprite.SCALE_MULTIPLIER;
import android.graphics.Canvas;
import android.graphics.Matrix;
import com.example.tutorialgame.engine.ui.Emotes;
import com.example.tutorialgame.entities.characters.Character;

/**
 * Manages emotional reaction bubbles (Emotes) displayed above characters.
 * Handles timing, position interpolation, and visual pop-in effects.
 */
public class EmoteComponent {
    private Emotes activeEmote;
    private float lifeTimer;
    private float displayDuration; // In seconds
    private final Character character;
    
    private float currentScale = 0f;
    private float offsetY;
    private final float offsetX, targetOffsetY;
    
    private final Matrix drawMatrix = new Matrix();

    public EmoteComponent(Character character) {
        this.character = character;
        // Centering offset relative to character width
        this.offsetX = (character.getHitBox().width() - (14 * SCALE_MULTIPLIER)) / 2f;
        this.targetOffsetY = 12 * SCALE_MULTIPLIER;
    }

    public void update(double delta) {
        if (activeEmote == null) return;

        lifeTimer += (float) delta;

        if (lifeTimer >= displayDuration) {
            activeEmote = null;
        } else {
            // Smooth pop-in animation using scaling
            if (currentScale < 1.0f) {
                currentScale = Math.min(1.0f, currentScale + (float) (delta * 8.0));
            }
            
            // Gentle floating movement
            offsetY = targetOffsetY + (float) Math.sin(lifeTimer * 4.0) * (2 * SCALE_MULTIPLIER);
        }
    }

    /**
     * Activates a new emote above the character.
     * @param emote The emote type from the Emotes enum.
     * @param durationMillis Display time in milliseconds.
     */
    public void showEmote(Emotes emote, long durationMillis) {
        this.activeEmote = emote;
        this.lifeTimer = 0;
        this.displayDuration = durationMillis / 1000f;
        this.currentScale = 0f; // Reset for pop-in effect
        this.offsetY = targetOffsetY;
    }

    public boolean isActive() {
        return activeEmote != null;
    }

    public void drawEmote(Canvas c) {
        if (!isActive() || activeEmote.getEmote() == null) return;

        float drawX = character.getHitBox().left + offsetX;
        float drawY = character.getHitBox().top - offsetY - character.getElevation() - SCALE_MULTIPLIER*3;

        drawMatrix.reset();
        // Scale from the center of the emote
        drawMatrix.postScale(currentScale, currentScale, 
                activeEmote.getEmote().getWidth() / 2f, 
                activeEmote.getEmote().getHeight() / 2f);
        drawMatrix.postTranslate(drawX, drawY);

        c.drawBitmap(activeEmote.getEmote(), drawMatrix, null);
    }
}
