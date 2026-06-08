package com.example.tutorialgame.engine.ui.customviews.buttons.circles;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.RectF;

import com.example.tutorialgame.engine.ui.customviews.buttons.GameButton;


public class CircleButton extends GameButton {
    private final Bitmap normal, pressed, disabled;

    public CircleButton(PointF centerPos, CircleImages circleImage, boolean multitouch) {
        super(centerPos, circleImage, multitouch);
        normal = circleImage.getNormal();
        pressed = circleImage.getPressed();
        disabled = circleImage.getDisabled();
    }

    private Bitmap getImg() {
        if (!enabled) return disabled;
        return isPushed(this.pointerId) ? pressed : normal;
    }

    public RectF getHitbox() {
        return hitbox;
    }

    // כעת מימוש נכון: בודק לפי מרכז ה-hitbox
    @Override
    protected boolean isIn(float x, float y) {
        float cx = hitbox.centerX();
        float cy = hitbox.centerY();
        float dx = x - cx;
        float dy = y - cy;
        float dist = (float) Math.hypot(dx, dy);
        float radius = Math.min(hitbox.width(), hitbox.height()) / 2f;
        return dist <= radius;
    }

    @Override
    public void draw(Canvas c) {
        c.drawBitmap(getImg(), hitbox.left, hitbox.top, null);
    }
}
