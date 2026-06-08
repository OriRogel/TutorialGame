package com.example.tutorialgame.engine.ui.customviews.buttons;

import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.MotionEvent;

import com.example.tutorialgame.R;
import com.example.tutorialgame.engine.audio.SoundManager;
import com.example.tutorialgame.engine.ui.customviews.buttons.circles.CircleImages;
import com.example.tutorialgame.ui.base.BaseActivity;

public abstract class GameButton {
    public interface OnClickListener {
        void onClick(GameButton button);
    }

    public enum PressType {ON_DOWN, ON_UP}

    private static GameButton exclusiveOwner = null;
    protected final RectF hitbox;
    protected boolean pushed, enabled = true;
    protected int pointerId = -1;
    protected boolean vibe, multitouch;
    protected PressType pressType = PressType.ON_UP; // ברירת מחדל

    // 2. הוספת שדה לאחסון המאזין
    private OnClickListener onClickListener;

    public GameButton(RectF hitbox, boolean multitouch) {
        this.hitbox = hitbox;
        vibe = true;
        this.multitouch = multitouch;
    }

    public GameButton(PointF centerPos, CircleImages circleImage, boolean multitouch) {
        float w = circleImage.getWidth();
        float h = circleImage.getHeight();
        hitbox = new RectF(
                centerPos.x - w / 2f,
                centerPos.y - h / 2f,
                centerPos.x + w / 2f,
                centerPos.y + h / 2f
        );
        vibe = false;
        this.multitouch = multitouch;
        pressType = circleImage.getPressType();
    }

    // 3. הוספת מתודה להגדרת המאזין
    public void setOnClickListener(OnClickListener listener) {
        this.onClickListener = listener;
    }

    public static void releaseExclusiveOwner() {
        if (exclusiveOwner != null) {
            exclusiveOwner.pushed = false;
            exclusiveOwner.pointerId = -1;
            exclusiveOwner = null;
        }
    }

    public boolean isPushed(int pointerId) {
        if (this.pointerId != pointerId) return false;
        return pushed;
    }

    public void setPushed(boolean pushed, int pointerId) {
        if (this.pushed && pushed) return;
        this.pushed = pushed;
        this.pointerId = pointerId;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    protected abstract boolean isIn(float x, float y);

    protected abstract void draw(Canvas c);

    public boolean eventHandler(MotionEvent event) {
        if (!enabled) return false;

        int action = event.getActionMasked();
        int actionIndex = event.getActionIndex();
        int pid = event.getPointerId(actionIndex);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN: {
                float x = event.getX(actionIndex), y = event.getY(actionIndex);

                if (!this.multitouch && exclusiveOwner != null && exclusiveOwner != this) {
                    return false;
                }

                if (isIn(x, y)) {
                    if (!this.multitouch && exclusiveOwner == null) {
                        exclusiveOwner = this;
                    }
                    setPushed(true, pid);

                    // --- שינוי: פידבק פיזי קורה תמיד בלחיצה --- 
                    if (vibe) BaseActivity.ButtonPressVibe();

                    if (pressType == PressType.ON_DOWN) {
                        // --- הפעלה לוגית ---
                        if (onClickListener != null) {
                            onClickListener.onClick(this);
                            SoundManager.getInstance(BaseActivity.getContext()).playSfx(R.raw.sfx_button_pressed);
                        }
                        return true;
                    }
                }
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                if (pointerId != -1) {
                    int idx = findPointerIndex(event, pointerId);
                    if (idx >= 0) {
                        if(!isIn(event.getX(idx), event.getY(idx))) pushed = false;
                    } else {
                        pushed = false;
                        if (exclusiveOwner == this) exclusiveOwner = null;
                        pointerId = -1;
                    }
                }
                break;
            }

            case MotionEvent.ACTION_CANCEL: {
                pushed = false;
                if (exclusiveOwner == this) exclusiveOwner = null;
                pointerId = -1;
                break;
            }

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP: {
                float ux = event.getX(actionIndex), uy = event.getY(actionIndex);
                boolean wasPushed = (pointerId == pid) && pushed && isIn(ux, uy);

                if (wasPushed && pressType == PressType.ON_UP) {
                    // --- הפעלה לוגית בלבד. הפידבק הפיזי כבר קרה ---
                    if (onClickListener != null) {
                        onClickListener.onClick(this);
                        SoundManager.getInstance(BaseActivity.getContext()).playSfx(R.raw.sfx_button_pressed);
                    }
                }

                if (pointerId == pid) {
                    pushed = false;
                    pointerId = -1;
                }

                if (exclusiveOwner == this) exclusiveOwner = null;

                if (pressType == PressType.ON_UP) return wasPushed;
                else return false;
            }
        }
        return false;
    }

    private int findPointerIndex(MotionEvent e, int pointerId) {
        for (int i = 0; i < e.getPointerCount(); i++)
            if (e.getPointerId(i) == pointerId) return i;
        return -1;
    }

    protected RectF getHitbox() {
        return hitbox;
    }
}
