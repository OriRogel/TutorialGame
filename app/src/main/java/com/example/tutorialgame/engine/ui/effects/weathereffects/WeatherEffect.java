package com.example.tutorialgame.engine.ui.effects.weathereffects;

import static com.example.tutorialgame.engine.core.GameConstants.Sprite.TILE_SIZE;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.example.tutorialgame.MyApp;

/**
 * Scene class for all environmental weather effects.
 */
public abstract class WeatherEffect {
    // שמות ברורים שמתארים איפה האפקט מצויר ביחס לעולם
    public enum RenderOrder { BELOW_ENTITIES, ABOVE_ENTITIES }

    protected final Paint paint = new Paint();
    
    protected float x, y;
    protected float speedX, speedY;
    protected int alpha = 255;
    
    protected boolean fading, dead;
    protected static final java.util.Random random = MyApp.getRandom();

    public WeatherEffect(float startX, float startY) {
        this.x = startX;
        this.y = startY;
    }

    /**
     * מחזיר את סדר הציור של האפקט (מתחת או מעל הישויות).
     */
    public abstract RenderOrder getRenderOrder();

    protected abstract void init(float startX, float startY);

    public abstract void update(double delta);

    public abstract void draw(Canvas c);

    protected void applyMovement(double delta) {
        x += (float) (speedX * (delta * 0.625*TILE_SIZE));
        y += (float) (speedY * (delta * 0.625*TILE_SIZE));
    }

    public boolean isDead() {
        return dead;
    }

    public void respawn(float worldX, float worldY) {
        init(worldX, worldY);
    }
}
