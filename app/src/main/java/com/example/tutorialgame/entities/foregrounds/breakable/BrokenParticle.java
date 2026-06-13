package com.example.tutorialgame.entities.foregrounds.breakable;

import static com.example.tutorialgame.engine.core.GameConstants.Sprite.SCALE_MULTIPLIER;
import static com.example.tutorialgame.engine.core.GameConstants.Sprite.TILE_SIZE;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import com.example.tutorialgame.MyApp;


/**
 * A reusable particle representing a piece of a broken entity.
 * Physics and fading are calculated based on delta time and screen scale.
 */
public class BrokenParticle {
    private float x, y;
    private float velX, velY, rotation, rotVel;
    private float alpha, fadeSpeed;
    private Bitmap img;
    private boolean active;
    private float lifeTimer, timeBeforeFade;
    
    private final Paint paint = new Paint();
    private static final float GRAVITY = 18 * TILE_SIZE;

    public BrokenParticle() {
        this.active = false;
    }

    /**
     * Initializes or resets the particle state for reuse.
     * All movement values are scaled by SCALE_MULTIPLIER for resolution independence.
     */
    public void init(float startX, float startY, Bitmap img) {
        this.x = startX;
        this.y = startY;
        this.img = img;
        this.active = true;
        this.alpha = 255;
        this.rotation = MyApp.getRandom().nextFloat() * 360;
        
        // Randomized velocities scaled by the engine's multiplier
        this.velX = (MyApp.getRandom().nextFloat() - 0.5f) * 100f * SCALE_MULTIPLIER;
        this.velY = (MyApp.getRandom().nextFloat() - 0.8f) * 100f * SCALE_MULTIPLIER;
        this.rotVel = (MyApp.getRandom().nextFloat() - 0.5f) * 720f;
        
        this.fadeSpeed = 300f + MyApp.getRandom().nextInt(200);
        this.timeBeforeFade = 0.15f + MyApp.getRandom().nextFloat() * 0.1f;
        this.lifeTimer = 0;
        this.paint.setAlpha(255);
    }

    public void update(double delta) {
        if (!active) return;

        float dt = (float) delta;
        lifeTimer += dt;

        // Physics update (Frame-rate independent)
        x += velX * dt;
        y += velY * dt;
        velY += GRAVITY * dt; 
        rotation += rotVel * dt;

        // Fade logic
        if (lifeTimer > timeBeforeFade) {
            alpha -= fadeSpeed * dt;
            if (alpha <= 0) {
                alpha = 0;
                active = false;
            }
            paint.setAlpha((int) alpha);
        }
    }

    public void draw(Canvas c) {
        if (!active || img == null) return;
        c.save();
        c.translate(x, y);
        c.rotate(rotation);
        c.drawBitmap(img, -img.getWidth() / 2f, -img.getHeight() / 2f, paint);
        c.restore();
    }

    public boolean isActive() { return active; }
}