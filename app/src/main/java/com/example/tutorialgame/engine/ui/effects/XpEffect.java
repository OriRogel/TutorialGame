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

import java.util.Objects;

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
        this.value = value;
        this.xpImg = BitmapManager.getBitmap(R.drawable.xp_img, value*0.9/32, false);
        this.pos.set(x, y - Objects.requireNonNull(xpImg).getHeight()/2f);

        creationTime = System.currentTimeMillis();
        lifeTime = 800;
        floatSpeed = TILE_SIZE/2f;
        this.isActive = true;

        textRenderer = new TextRenderer(xpImg.getWidth()/2.5f, R.color.floral_white);

        float x1 = x + (xpImg.getWidth() - textRenderer.measureText("+" + value)) / 2f;
        offsetY = textRenderer.getTextSize()*1.5f;

        textRenderer.setPosition(x1, this.pos.y + offsetY);
        textRenderer.setShadowOffset(x1, this.pos.y + offsetY + 0.3f*SCALE_MULTIPLIER);
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