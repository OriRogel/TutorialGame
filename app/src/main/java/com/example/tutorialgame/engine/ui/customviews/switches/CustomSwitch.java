package com.example.tutorialgame.engine.ui.customviews.switches;

import static com.example.tutorialgame.engine.core.GameConstants.Sprite.SCALE_MULTIPLIER;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.view.MotionEvent;

import com.example.tutorialgame.R;
import com.example.tutorialgame.engine.audio.SoundManager;
import com.example.tutorialgame.engine.renderer.TextRenderer;
import com.example.tutorialgame.ui.base.BaseActivity;

public class CustomSwitch {

    /**
     * Interface definition for a callback to be invoked when the checked state of a switch changes.
     */
    public interface OnCheckedChangeListener {
        void onCheckedChanged(CustomSwitch customSwitch, boolean isChecked);
    }

    private OnCheckedChangeListener listener;
    private final RectF hitbox;
    private final String key;
    private final SwitchList sw;
    private final TextRenderer paint;
    private boolean checked;

    // ANIMATION FIELDS
    private int baseColorFrom, baseColorTo, shadowColorFrom, shadowColorTo;
    private long colorAnimStart = 0L;
    private boolean colorAnimating;

    public CustomSwitch(float x, float y, SwitchList sw) {
        this.sw = sw;

        hitbox = new RectF(x, y, sw.getChecked().getWidth() + x, sw.getChecked().getHeight() + y);
        float textX = hitbox.right + 3 * SCALE_MULTIPLIER;

        paint = new TextRenderer(sw.getHeight() / 2f, R.color.dark_charcoal);
        paint.setShadowColor(R.color.dark_moon);
        paint.setPosition(textX, hitbox.centerY() + paint.getTextSize()/4);
        paint.setShadowOffset(0.5f * SCALE_MULTIPLIER, 0.5f * SCALE_MULTIPLIER);

        this.key = sw.getKey();
        this.checked = BaseActivity.getSpSettings().getBoolean(key, sw.getDefValue());

        applyColorsImmediate(checked);
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        this.listener = listener;
    }

    private Bitmap getImg() {
        return checked ? sw.getChecked() : sw.getUnChecked();
    }

    public void draw(Canvas c) {
        updateAnimatedColors();

        c.drawBitmap(getImg(), hitbox.left, hitbox.top, null);
        paint.drawWithShadow(sw.getName(), c);
    }

    public boolean eventHandler(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            if (isIn(event)) {
                checked = !checked;

                // Determine target colors
                int newBase = checked ?
                        BaseActivity.getContext().getColor(R.color.text_color_pressed) :
                        BaseActivity.getContext().getColor(R.color.dark_charcoal);

                int newShadow = checked ?
                        BaseActivity.getContext().getColor(R.color.black) :
                        BaseActivity.getContext().getColor(R.color.dark_moon);

                startColorTransition(newBase, newShadow);

                // Save state and provide feedback
                BaseActivity.getSpSettings().edit().putBoolean(key, checked).apply();
                SoundManager.getInstance(BaseActivity.getContext()).playSfx(R.raw.sfx_bloop);
                BaseActivity.ButtonPressVibe();

                // Notify listener
                if (listener != null) {
                    listener.onCheckedChanged(this, checked);
                }
            }
        }
        return checked;
    }

    private boolean isIn(MotionEvent event) {
        return hitbox.contains(event.getX(), event.getY());
    }

    private void applyColorsImmediate(boolean checkedState) {
        int base, shadow;
        if (checkedState) {
            base = BaseActivity.getContext().getColor(R.color.text_color_pressed);
            shadow = BaseActivity.getContext().getColor(R.color.black);
        } else {
            base = BaseActivity.getContext().getColor(R.color.dark_charcoal);
            shadow = BaseActivity.getContext().getColor(R.color.dark_moon);
        }
        paint.setColor(base);
        paint.setShadowColor(shadow);
        baseColorFrom = baseColorTo = base;
        shadowColorFrom = shadowColorTo = shadow;
        colorAnimating = false;
    }

    private void startColorTransition(int newBaseColor, int newShadowColor) {
        baseColorFrom = paint.getColor();
        shadowColorFrom = paint.getShadowColor();
        baseColorTo = newBaseColor;
        shadowColorTo = newShadowColor;
        colorAnimStart = System.currentTimeMillis();
        colorAnimating = true;
    }

    private void updateAnimatedColors() {
        if (!colorAnimating) return;

        long now = System.currentTimeMillis();
        long colorAnimDuration = 180L;
        float t = (now - colorAnimStart) / (float) colorAnimDuration;

        if (t >= 1f) {
            paint.setColor(baseColorTo);
            paint.setShadowColor(shadowColorTo);
            colorAnimating = false;
            return;
        }

        float smoothT = t * t * (3f - 2f * t);
        paint.setColor(lerpColor(baseColorFrom, baseColorTo, smoothT));
        paint.setShadowColor(lerpColor(shadowColorFrom, shadowColorTo, smoothT));
    }

    private int lerpColor(int fromColor, int toColor, float t) {
        int a1 = (fromColor >> 24) & 0xff, r1 = (fromColor >> 16) & 0xff, g1 = (fromColor >> 8) & 0xff, b1 = fromColor & 0xff;
        int a2 = (toColor >> 24) & 0xff, r2 = (toColor >> 16) & 0xff, g2 = (toColor >> 8) & 0xff, b2 = toColor & 0xff;
        return (Math.round(a1 + (a2 - a1) * t) & 0xff) << 24 | (Math.round(r1 + (r2 - r1) * t) & 0xff) << 16 | (Math.round(g1 + (g2 - g1) * t) & 0xff) << 8 | (Math.round(b1 + (b2 - b1) * t) & 0xff);
    }
}
