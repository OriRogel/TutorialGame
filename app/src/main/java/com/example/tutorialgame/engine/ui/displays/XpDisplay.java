package com.example.tutorialgame.engine.ui.displays;

import static com.example.tutorialgame.engine.core.GameConstants.Sprite.SCALE_MULTIPLIER;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import com.example.tutorialgame.MyApp;
import com.example.tutorialgame.R;
import com.example.tutorialgame.engine.interfaces.BitmapMethods;
import com.example.tutorialgame.engine.renderer.TextRenderer;
import com.example.tutorialgame.managers.BitmapManager;

/**
 * A HUD component that visualizes the player's current XP and Level.
 * Uses a clipped progress bar to represent the experience gap.
 */
public class XpDisplay implements BitmapMethods {
    private static final double SCALE_RATIO = 0.53125;
    
    // Static assets to ensure minimal memory footprint
    private static final Bitmap xpImg, containerBg, containerFrame, progress;

    static {
        xpImg = BitmapManager.getBitmap(R.drawable.xp_img, SCALE_RATIO, false);
        containerBg = BitmapManager.getBitmap(R.drawable.xp_container, SCALE_RATIO, false);
        containerFrame = BitmapManager.getBitmap(R.drawable.xp_bg_frame, SCALE_RATIO, false);
        progress = BitmapManager.getBitmap(R.drawable.xp_progress, SCALE_RATIO, false);
    }

    private final Rect srcRect, dstRect;
    private final float x, y, containerOffset;
    private final TextRenderer lv;
    private double currentXP = -1;
    private final int progWidth, progHeight, progressOffset;

    public XpDisplay(float x, float y) {
        this.x = x;
        this.y = y;

        this.srcRect = new Rect();
        this.dstRect = new Rect();

        lv = new TextRenderer(xpImg.getWidth() / 2f, R.color.magnolia_white);
        lv.setShadowColor(R.color.black);

        float offsetX = x + (xpImg.getWidth() - lv.measureText(String.valueOf(MyApp.getProgress().getLevel())) + 0.5f * SCALE_MULTIPLIER) / 2f;
        float offsetY = y + lv.getTextSize() * 1.3f;

        progWidth = progress.getWidth();
        progHeight = progress.getHeight();
        containerOffset = xpImg.getWidth() / 1.6f + x;
        progressOffset = (int) (containerOffset + 6 * SCALE_MULTIPLIER);

        lv.setPosition(offsetX, offsetY);
        lv.setShadowOffset(0, 0.5f * SCALE_MULTIPLIER);

        updateProgress();
    }

    public void draw(Canvas c) {
        drawContainer(c);
        drawXpImg(c);
    }

    private void drawXpImg(Canvas c) {
        c.drawBitmap(xpImg, x, y, null);
        String levelStr = String.valueOf(MyApp.getProgress().getLevel());
        lv.drawWithShadow(levelStr, c);
    }

    private void drawContainer(Canvas c) {
        updateProgress();
        c.drawBitmap(containerBg, containerOffset, y, null);
        c.drawBitmap(progress, srcRect, dstRect, null);
        c.drawBitmap(containerFrame, containerOffset, y, null);
    }

    /**
     * Updates the clipping rectangles based on current XP progress.
     */
    private void updateProgress() {
        double xp = MyApp.getProgress().getXp();
        if (xp == currentXP) return;
        
        currentXP = xp;
        float needed = MyApp.getProgress().neededXpForLevelUp();
        float pct = (float) (currentXP / needed);
        
        int cutW = Math.round(progWidth * Math.min(1f, pct));

        srcRect.set(0, 0, cutW, progHeight);
        dstRect.set(progressOffset, (int) y, progressOffset + cutW, (int) (y + progHeight));
    }
}
