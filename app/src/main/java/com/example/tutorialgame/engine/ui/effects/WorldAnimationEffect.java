package com.example.tutorialgame.engine.ui.effects;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import com.example.tutorialgame.components.AnimationComponent;
import com.example.tutorialgame.engine.ui.effects.impcateffects.ImpactEffectType;

/**
 * אובייקט המנהל אנימציה חד-פעמית בעולם המשחק.
 * מסתיים באופן טבעי ברגע שהאנימציה (AnimationComponent) מסיימת מחזור שלם.
 */
public class WorldAnimationEffect {
    private ImpactEffectType type;
    private final PointF pos = new PointF();
    private final AnimationComponent animator;
    private final Matrix matrix = new Matrix();
    private boolean active;
    private float rotation;

    public WorldAnimationEffect() {
        this.animator = new AnimationComponent(0, 0);
    }

    public void init(ImpactEffectType type, float x, float y) {
        init(type, x, y, 0);
    }

    public void init(ImpactEffectType type, float x, float y, float rotation) {
        this.type = type;
        this.pos.set(x, y);
        this.rotation = rotation;
        this.animator.setSpeed(type.getAnimSpeed());
        this.animator.setFrameCount(type.getFrameCount());
        this.animator.resetAnimation();
        this.active = true;
    }

    public void update() {
        if (!active) return;
        
        // animator.update() מחזיר true כשהאנימציה מסיימת מחזור שלם
        if (animator.update()) {
            active = false;
        }
    }

    public void draw(Canvas c) {
        if (!active || type == null) return;

        Bitmap sprite = type.getSprite(animator.getAniIndex());
        if (sprite != null) {
            matrix.reset();
            matrix.postTranslate(-sprite.getWidth() / 2f, -sprite.getHeight() / 2f);
            if (rotation != 0) {
                matrix.postRotate(rotation);
            }
            matrix.postTranslate(pos.x, pos.y);
            c.drawBitmap(sprite, matrix, null);
        }
    }

    public void setPos(float x, float y) {
        this.pos.set(x, y);
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    public boolean isActive() {
        return active;
    }
}