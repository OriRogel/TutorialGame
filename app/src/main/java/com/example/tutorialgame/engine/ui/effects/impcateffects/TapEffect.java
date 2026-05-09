package com.example.tutorialgame.engine.ui.effects.impcateffects;

import static com.example.tutorialgame.engine.core.GameConstants.Sprite.SCALE_MULTIPLIER;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import com.example.tutorialgame.engine.interfaces.BitmapMethods;

public class TapEffect implements BitmapMethods {
    private static final long TOTAL_DURATION_MS = 250;
    private ImpactEffectType tapType;
    private long startTime = -1;
    private long elapsed;

    public TapEffect(ImpactEffectType tapType) {
        this.tapType = tapType;
    }

    public void show(Canvas c, float x, float y) {
        if (startTime < 0 || tapType == null) return;

        elapsed = System.currentTimeMillis() - startTime;
        if (elapsed > TOTAL_DURATION_MS) {
            startTime = -1;
            return;
        }

        int index = (int) ((elapsed * tapType.getFrameCount()) / TOTAL_DURATION_MS);
        if (index >= tapType.getFrameCount()) index = tapType.getFrameCount() - 1;
        Bitmap sprite = tapType.getSprite(index);
        c.drawBitmap(sprite,
                x - (16 * SCALE_MULTIPLIER),
                y - (16 * SCALE_MULTIPLIER),
                null);
    }


    public void resetEffect() {
        this.startTime = System.currentTimeMillis();
    }

    public void setTapType(ImpactEffectType tapType) {
        this.tapType = tapType;
    }

    public long getElapsed() {
        return elapsed;
    }
}
