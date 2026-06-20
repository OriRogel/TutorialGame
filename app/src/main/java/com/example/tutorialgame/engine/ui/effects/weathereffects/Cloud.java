package com.example.tutorialgame.engine.ui.effects.weathereffects;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import com.example.tutorialgame.R;
import com.example.tutorialgame.managers.BitmapManager;

/**
 * Represents a floating cloud effect.
 * Inherits from WeatherEffect and uses static resource loading for performance.
 */
public class Cloud extends WeatherEffect {
    private static final Bitmap staticCloudBmp;
    private static final PorterDuffColorFilter cloudFilter;
    private long birthTime, lifeDuration;

    static {
        staticCloudBmp = BitmapManager.getBitmap(R.drawable.particle_clouds);
        int tintColor = Color.argb(255, 70, 70, 70);
        cloudFilter = new PorterDuffColorFilter(tintColor, PorterDuff.Mode.SRC_ATOP);
    }

    public Cloud(float startX, float startY) {
        super(startX, startY);
        this.paint.setColorFilter(cloudFilter);
    }

    @Override
    public RenderOrder getRenderOrder() {
        return RenderOrder.BELOW_ENTITIES;
    }

    @Override
    protected void init(float startX, float startY) {
        this.x = startX;
        this.y = startY;
        this.speedX = (0.5f + random.nextFloat() * 0.5f);
        this.speedY = (0.2f + random.nextFloat() * 0.5f);
        
        this.birthTime = System.currentTimeMillis();
        this.lifeDuration = 7000 + random.nextInt(7001);
        
        this.alpha = 0;
        this.fading = false;
        this.dead = false;
    }

    @Override
    public void update(double delta) {
        if (dead) return;

        applyMovement(delta);

        if (!fading) {
            if (alpha < 60) alpha++;
            if (System.currentTimeMillis() - birthTime >= lifeDuration) fading = true;
        } else {
            alpha -= (int) (5 * delta * 60);
            if (alpha <= 0) {
                alpha = 0;
                dead = true;
            }
        }
        paint.setAlpha(alpha);
    }

    @Override
    public void draw(Canvas c) {
        if (dead) return;
        c.drawBitmap(staticCloudBmp, x, y, paint);
    }
}
