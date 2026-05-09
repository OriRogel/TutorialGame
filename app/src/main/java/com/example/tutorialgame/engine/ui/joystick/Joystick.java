package com.example.tutorialgame.engine.ui.joystick;

import static com.example.tutorialgame.engine.core.GameConstants.Sprite.SCALE_MULTIPLIER;

import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.view.MotionEvent;

import com.example.tutorialgame.utils.CollisionUtils;
import com.example.tutorialgame.utils.Other;

/**
 * A virtual analogue joystick for player movement.
 * Encapsulates touch logic, boundary calculations, and rendering.
 */
public class Joystick {
    private final PointF centerPos, knobPos;
    private final float radius;

    private final Paint paintOuter, paintBg, paintKnob, paintShadow;

    private int pointerId = -1;
    private boolean pushed;

    // The normalized movement vector (values between -1 and 1)
    private final PointF movementVector = new PointF(0, 0);

    public Joystick(float x, float y, float radius) {
        this.centerPos = new PointF(x, y);
        this.knobPos = new PointF(x, y);
        this.radius = radius;

        paintOuter = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintOuter.setColor(Color.RED);
        paintOuter.setStrokeWidth(SCALE_MULTIPLIER);
        paintOuter.setStyle(Paint.Style.STROKE);

        paintBg = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintBg.setColor(Color.argb(100, 186, 0, 0));
        paintBg.setStyle(Paint.Style.FILL);

        paintKnob = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintKnob.setColor(Color.WHITE);
        paintKnob.setStyle(Paint.Style.FILL);

        paintShadow = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintShadow.setStyle(Paint.Style.FILL);
        paintShadow.setColor(0x88000000);
        // שינוי כאן: ה-Blur מחושב לפי SCALE_MULTIPLIER כדי להיות עקבי בכל רזולוציה
        paintShadow.setMaskFilter(new BlurMaskFilter(5 * SCALE_MULTIPLIER, BlurMaskFilter.Blur.NORMAL));
    }

    public void draw(Canvas c) {
        // Draw base
        c.drawCircle(centerPos.x, centerPos.y, radius, paintBg);
        c.drawCircle(centerPos.x, centerPos.y, radius, paintOuter);

        // Draw knob shadow and knob
        // רדיוס הצל מעט גדול יותר מהכפתור
        c.drawCircle(knobPos.x, knobPos.y, radius / 3 + SCALE_MULTIPLIER, paintShadow);
        c.drawCircle(knobPos.x, knobPos.y, radius / 3, paintKnob);
    }

    public void eventHandler(MotionEvent event) {
        final int action = event.getActionMasked();
        final int actionIndex = event.getActionIndex();
        final int pid = event.getPointerId(actionIndex);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                if (isInside(event.getX(actionIndex), event.getY(actionIndex))) {
                    pushed = true;
                    pointerId = pid;
                    updatePositions(event.getX(actionIndex), event.getY(actionIndex));
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (pushed) {
                    for (int i = 0; i < event.getPointerCount(); i++) {
                        if (event.getPointerId(i) == pointerId) {
                            updatePositions(event.getX(i), event.getY(i));
                            break;
                        }
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL:
                if (pid == pointerId) {
                    reset();
                }
                break;
        }
    }

    private void updatePositions(float touchX, float touchY) {
        float dx = touchX - centerPos.x;
        float dy = touchY - centerPos.y;
        float distance = (float) Math.hypot(dx, dy);

        if (distance <= radius) {
            knobPos.set(touchX, touchY);
        } else {
            // Keep knob on the edge of the circle
            Other.IntersectCircle(centerPos.x, centerPos.y, radius, touchX, touchY, knobPos);
        }

        // Calculate normalized movement vector
        movementVector.set(dx / radius, dy / radius);
        // Clamp vector to unit circle length
        float mag = (float) Math.hypot(movementVector.x, movementVector.y);
        if (mag > 1.0f) {
            movementVector.x /= mag;
            movementVector.y /= mag;
        }
    }

    public void reset() {
        pushed = false;
        pointerId = -1;
        knobPos.set(centerPos.x, centerPos.y);
        movementVector.set(0, 0);
    }

    private boolean isInside(float x, float y) {
        return CollisionUtils.isWithinRange(x, y, centerPos.x, centerPos.y, radius);
    }

    public PointF getMovementVector() {
        return movementVector;
    }

    public boolean isPushed() {
        return pushed;
    }
}
