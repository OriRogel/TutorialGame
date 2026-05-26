package com.example.tutorialgame.engine.ui.effects;

import static com.example.tutorialgame.engine.core.GameConstants.Sprite.SCALE_MULTIPLIER;
import static com.example.tutorialgame.engine.core.GameConstants.Sprite.TILE_SIZE;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;

import com.example.tutorialgame.R;
import com.example.tutorialgame.engine.renderer.TextRenderer;
import com.example.tutorialgame.managers.BitmapManager;
import com.example.tutorialgame.managers.objectpool.ObjectPoolManager;

public class XpEffect {
    private Bitmap xpImg;
    private int value;
    private final PointF pos = new PointF();
    private long lifeTime, creationTime;
    private float floatSpeed;
    private boolean isActive = true;

    private TextRenderer textRenderer;
    private float offsetY;

    public XpEffect(int value, float x, float y) {
        init(value, x, y);
    }

    public void init(int value, float x, float y) {
        this.value = Math.max(1, value);
        
        // Slightly smaller scale
        double scale = (0.5 + (this.value / 200.0));
        this.xpImg = BitmapManager.getBitmap(R.drawable.xp_img, scale, false);
        if (this.xpImg == null) {
            this.isActive = false;
            return;
        }

        // Center the bitmap over the death location
        this.pos.set(x - xpImg.getWidth()/2f, y - xpImg.getHeight());

        creationTime = System.currentTimeMillis();
        lifeTime = 1000;
        floatSpeed = TILE_SIZE * 0.75f;
        this.isActive = true;

        // Smaller font size
        float fontSize = 8f * SCALE_MULTIPLIER;
        textRenderer = new TextRenderer(fontSize, R.color.floral_white);

        // Calculate centering
        float textWidth = textRenderer.measureText("+" + this.value);
        float centerX = pos.x + xpImg.getWidth() / 2f;
        float textX = centerX - textWidth / 2f;
        
        // Vertically center text relative to image
        // textRenderer draws from baseline, so we adjust by font size
        offsetY = (xpImg.getHeight() + fontSize * 0.6f) / 2f;

        textRenderer.setPosition(textX, this.pos.y + offsetY);
        // Correctly set shadow offset as a small relative value
        textRenderer.setShadowOffset(0.5f * SCALE_MULTIPLIER, 0.5f * SCALE_MULTIPLIER);
        textRenderer.setShadowColor(R.color.dark_moon);
    }

    public boolean isActive() {
        return isActive;
    }

    public void update(double delta) {
        if (!isActive) return;
        long now = System.currentTimeMillis();
        if (now - creationTime > lifeTime) {
            isActive = false;
            ObjectPoolManager.releaseXpEffect(this);
            return;
        }

        textRenderer.setAlpha(Math.round(255 * (1-((float) (now - creationTime) / lifeTime))));
        pos.y -= (float) (floatSpeed * delta);
        textRenderer.setY(pos.y + offsetY);
    }

    public void draw(Canvas c) {
        if (!isActive) return;
        c.drawBitmap(xpImg, pos.x, pos.y, textRenderer);
        textRenderer.drawWithShadow("+" + value, c);
    }
}