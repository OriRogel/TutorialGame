package com.example.tutorialgame.engine.ui.effects.lighting;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;

import com.example.tutorialgame.MyApp;


/**
 * Represents a light source in the game world.
 * Updated to handle "pure white" as a neutral light that only affects ambient darkness alpha.
 */
public class LightSource {

    public enum LightType {
        STATIC,
        FLICKER,
        PULSE
    }

    private final PointF pos;
    private final float baseRadius;
    private float currentRadius;

    private final LightType type;
    private float animationTime;
    private float jitter;

    // Filters are pre-calculated for performance
    private final PorterDuffColorFilter outerFilter;
    private final PorterDuffColorFilter innerFilter;
    private final boolean isPureWhite;

    public LightSource(PointF pos, float radius, int color, LightType type) {
        this.pos = pos;
        this.baseRadius = radius;
        this.currentRadius = radius;
        this.type = type;

        // Check if color is pure white (ignoring original alpha, focusing on RGB)
        this.isPureWhite = (Color.red(color) >= 250 && Color.green(color) >= 250 && Color.blue(color) >= 250);

        if (isPureWhite) {
            // For pure white, we use BLACK with SRC_IN.
            // This makes the mask pixels (0,0,0) in the light bitmap but keeps their alpha.
            // In the ADD pass, adding (0,0,0) does nothing, fulfilling the "alpha only" requirement.
            this.outerFilter = new PorterDuffColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
            this.innerFilter = new PorterDuffColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
        } else {
            int innerColor = calculateInnerColor(color);
            this.outerFilter = new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN);
            this.innerFilter = new PorterDuffColorFilter(innerColor, PorterDuff.Mode.SRC_IN);
        }
    }

    private int calculateInnerColor(int baseColor) {
        float[] hsv = new float[3];
        Color.colorToHSV(baseColor, hsv);
        // Boost saturation and value for the inner core
        hsv[1] = Math.min(1f, hsv[1] + 0.3f);
        hsv[2] = Math.min(1f, hsv[2] + 0.2f);
        return Color.HSVToColor(Color.alpha(baseColor), hsv);
    }

    public void update(double delta) {
        animationTime += (float) delta;

        switch (type) {
            case FLICKER:
                if (MyApp.RND.nextFloat() > 0.85f) {
                    jitter = (MyApp.RND.nextFloat() - 0.5f) * (baseRadius * 0.12f);
                }
                currentRadius = baseRadius + (float) Math.sin(animationTime * 20) * (baseRadius * 0.02f) + jitter;
                break;

            case PULSE:
                float pulse = (float) (Math.sin(animationTime * 2.5) * 0.6 + Math.sin(animationTime * 1.5) * 0.4);
                currentRadius = baseRadius + (pulse * (baseRadius * 0.12f));
                break;

            default:
                currentRadius = baseRadius;
        }
    }

    public PointF getPos() { return pos; }
    public float getRadius() { return currentRadius; }
    public PorterDuffColorFilter getOuterFilter() { return outerFilter; }
    public PorterDuffColorFilter getInnerFilter() { return innerFilter; }
    public boolean isPureWhite() { return isPureWhite; }
}
