package com.example.tutorialgame.engine.ui;

import static com.example.tutorialgame.engine.core.GameConstants.Sprite.SCALE_MULTIPLIER;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import com.example.tutorialgame.R;
import com.example.tutorialgame.entities.characters.Character;
import com.example.tutorialgame.managers.BitmapManager;

/**
 * A specialized mini-health bar rendered above NPCs.
 * Features a dynamic progress fill and a "damage flash" effect to visualize health loss.
 */
public class NpcHealthBar {
    // Static resources shared across all NPC instances to minimize memory footprint
    private static final Bitmap lifeBarEmpty, lifeBarProgressFull, frame;
    private static final int barWidth, barHeight;
    private static final Paint flashPaint;

    static {
        lifeBarEmpty = BitmapManager.getBitmap(R.drawable.lifebar_mini_under);
        lifeBarProgressFull = BitmapManager.getBitmap(R.drawable.lifebar_mini_progress);
        frame = BitmapManager.getBitmap(R.drawable.lifebar_mini_frame);

        barWidth = lifeBarProgressFull.getWidth();
        barHeight = lifeBarProgressFull.getHeight();

        flashPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // Soft pink/white tint for the damage flash effect
        flashPaint.setColorFilter(new PorterDuffColorFilter(0xFFFFC8DC, PorterDuff.Mode.SRC_ATOP));
    }

    private final Character npc;
    private final Rect srcRect = new Rect();
    private final Rect dstRect = new Rect();
    private final Rect flashSrcRect = new Rect();
    private final Rect flashDstRect = new Rect();

    public NpcHealthBar(Character npc) {
        this.npc = npc;
    }

    /**
     * Renders the health bar relative to the NPC's overhead position.
     */
    public void drawBar(Canvas c) {
        if (npc.isDead() || !npc.isActive()) return;

        // Position the bar above the NPC's head
        float y = npc.getProjectedHitBox().top - 8 * SCALE_MULTIPLIER;
        float x = npc.getProjectedHitBox().left - 2.5f * SCALE_MULTIPLIER;

        // 1. Draw the background track
        c.drawBitmap(lifeBarEmpty, x, y, null);

        // 2. Calculate and draw the current health progress
        float currentHealth = npc.getCurrentHealth();
        int maxHealth = npc.getMaxHealth();
        float pct = currentHealth / maxHealth;
        int cutW = Math.round(barWidth * Math.max(0, Math.min(1, pct)));

        if (cutW > 0) {
            srcRect.set(0, 0, cutW, barHeight);
            dstRect.set((int) x, (int) y, (int) (x + cutW), (int) (y + barHeight));
            c.drawBitmap(lifeBarProgressFull, srcRect, dstRect, null);
        }

        // 3. Render damage flash and the containing frame
        drawFlash(c, x, y, cutW, currentHealth, maxHealth);
        c.drawBitmap(frame, x, y, null);
    }

    private void drawFlash(Canvas c, float x, float y, int currentCutW, float currentHealth, int maxHealth) {
        float alpha = npc.getHealthComponent().getFlashAlpha();
        if (alpha <= 0) return;

        int prevHealth = npc.getHealthComponent().getPrevHealth();
        if (prevHealth <= currentHealth) return;

        // Calculate the maximum width of the health lost section
        float diffPct = (prevHealth - currentHealth) / maxHealth;
        int maxFlashWidth = (int) (diffPct * barWidth);

        // Animate the flash width based on alpha to create a "draining" effect
        int animatedFlashWidth = (int) (maxFlashWidth * alpha);

        if (animatedFlashWidth > 0) {
            // The flash section starts at the current health edge and extends rightwards, shrinking over time
            flashSrcRect.set(currentCutW, 0, currentCutW + animatedFlashWidth, barHeight);
            flashDstRect.set((int) (x + currentCutW), (int) y, (int) (x + currentCutW + animatedFlashWidth), (int) (y + barHeight));

            flashPaint.setAlpha((int) (alpha * 255));
            c.drawBitmap(lifeBarProgressFull, flashSrcRect, flashDstRect, flashPaint);
        }
    }
}