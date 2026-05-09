package com.example.tutorialgame.engine.ui.customviews;

import static com.example.tutorialgame.engine.core.GameConstants.Sprite.SCALE_MULTIPLIER;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.drawable.NinePatchDrawable;
import android.view.MotionEvent;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import com.example.tutorialgame.R;
import com.example.tutorialgame.engine.audio.SoundManager;
import com.example.tutorialgame.engine.renderer.TextRenderer;
import com.example.tutorialgame.ui.base.BaseActivity;
import java.util.Objects;

/**
 * A custom UI seek bar component designed for the game engine.
 *
 * <p>This class provides a graphical slider that allows users to select a value
 * by dragging a progress bar. It supports 9-patch background and progress drawables,
 * dynamic text rendering for labels and percentages, and provides haptic and
 * audio feedback during interaction.</p>
 *
 * <p>Key features include:</p>
 * <ul>
 *   <li>Visual feedback for "pushed" versus "normal" states.</li>
 *   <li>Normalized progress handling (0.0 to 1.0).</li>
 *   <li>Integrated {@link TextRenderer} for displaying the seek bar's name and current percentage.</li>
 *   <li>Listener interface to observe progress changes in real-time.</li>
 * </ul>
 */
public class CustomSeekBar {

    /**
     * Interface definition for a callback to be invoked when the progress level is changed.
     */
    public interface OnProgressChangedListener {
        void onProgressChanged(CustomSeekBar seekBar, float progress);
    }

    private OnProgressChangedListener listener;

    private final RectF bgHitbox;
    private final NinePatchDrawable normal, pressed, background;
    private final TextRenderer percentRenderer, nameRenderer;
    private final float percentY, nameX, nameY;
    private final String name;
    private boolean pushed;
    private float progress;
    private String percentText;
    private float percentX;
    private int currentPercent, lastPercent;

    // Cached colors to improve performance during drawing
    private final int colorPushedText, colorPushedShadow;
    private final int colorNormalText, colorNormalShadow;

    public CustomSeekBar(float x, float y, float width, float height, @StringRes int name) {
        Context ctx = BaseActivity.getContext();
        bgHitbox = new RectF(x, y, x + width, y + height);

        background = (NinePatchDrawable) ContextCompat.getDrawable(ctx, R.drawable.interior_background);
        Objects.requireNonNull(background).setBounds((int) bgHitbox.left, (int) bgHitbox.top, (int) bgHitbox.right, (int) bgHitbox.bottom);

        this.normal = (NinePatchDrawable) ContextCompat.getDrawable(ctx, R.drawable.slider_progress_normal);
        this.pressed = (NinePatchDrawable) ContextCompat.getDrawable(ctx, R.drawable.slider_progress_pressed);
        this.name = ctx.getString(name);

        // Pre-caching colors
        colorPushedText = ContextCompat.getColor(ctx, R.color.floral_white);
        colorPushedShadow = ContextCompat.getColor(ctx, R.color.gray);
        colorNormalText = Color.WHITE;
        colorNormalShadow = ContextCompat.getColor(ctx, R.color.light_gray);

        // Initialize Renderers
        float percentSize = bgHitbox.height() - 5 * SCALE_MULTIPLIER;
        percentRenderer = new TextRenderer(percentSize);
        nameRenderer = new TextRenderer(percentSize / 2);

        // Set shadow offset
        float shadowOffset = SCALE_MULTIPLIER * 0.5f;
        percentRenderer.setShadowOffset(shadowOffset, shadowOffset);
        nameRenderer.setShadowOffset(shadowOffset, shadowOffset);

        percentY = bgHitbox.top + percentRenderer.getTextSize();
        nameY = bgHitbox.bottom - nameRenderer.getTextSize() / 2;
        nameX = bgHitbox.left + bgHitbox.bottom - nameY;

        updateDrawableBoundsFromProgress();
        updatePercentCache();
    }

    /**
     * Sets a listener to receive progress change events.
     */
    public void setOnProgressChangedListener(OnProgressChangedListener listener) {
        this.listener = listener;
    }

    private NinePatchDrawable getSeekBarImg() {
        if (pushed) {
            percentRenderer.setColor(colorPushedText);
            percentRenderer.setShadowColor(colorPushedShadow);

            nameRenderer.setColor(colorPushedText);
            nameRenderer.setShadowColor(colorPushedShadow);
            return pressed;
        } else {
            percentRenderer.setColor(colorNormalText);
            percentRenderer.setShadowColor(colorNormalShadow);

            nameRenderer.setColor(colorNormalText);
            nameRenderer.setShadowColor(colorNormalShadow);
            return normal;
        }
    }

    public void setProgressNormalized(float p) {
        float clamped = Math.max(0f, Math.min(1f, p));
        if (Math.abs(clamped - this.progress) < 0.0005f) return;

        this.progress = clamped;
        updateDrawableBoundsFromProgress();
        updatePercentCache();

        // Notify listener of the change
        if (listener != null) {
            listener.onProgressChanged(this, this.progress);
        }
    }

    private void seekBarEffects() {
        if (currentPercent != lastPercent) {
            if (currentPercent == 0 || currentPercent == 100) BaseActivity.ButtonPressVibe();
            if (currentPercent % 5 == 0)
                SoundManager.getInstance(BaseActivity.getContext()).playSfx(R.raw.sfx_bloop);

            lastPercent = currentPercent;
        }
    }

    private void setProgressFromX(float x) {
        if (bgHitbox.width() <= 0) return;
        float p = (x - bgHitbox.left) / bgHitbox.width();
        setProgressNormalized(p);
    }

    private void updateDrawableBoundsFromProgress() {
        int left = (int) bgHitbox.left;
        int right = (int) Math.max(left + 1, bgHitbox.left + progress * bgHitbox.width());
        normal.setBounds(left, (int) bgHitbox.top, right, (int) bgHitbox.bottom);
        pressed.setBounds(normal.getBounds());
    }

    private void updatePercentCache() {
        currentPercent = Math.round(progress * 100f);
        percentText = currentPercent + "%";
        percentX = bgHitbox.centerX() - percentRenderer.measureText(percentText) / 2f;

        percentRenderer.setPosition(percentX, percentY);
        nameRenderer.setPosition(nameX, nameY);
    }

    public float eventHandler(MotionEvent event) {
        int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (isIn(event)) {
                    pushed = true;
                    BaseActivity.ButtonPressVibe();
                    setProgressFromX(event.getX());
                } else pushed = false;
                break;
            case MotionEvent.ACTION_MOVE:
                if (pushed) {
                    setProgressFromX(event.getX());
                    seekBarEffects();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                pushed = false;
                break;
        }
        return progress;
    }

    public void drawSeekBar(Canvas c) {
        background.draw(c);
        this.getSeekBarImg().draw(c);
        drawText(c);
    }

    private void drawText(Canvas c) {
        float filledRight = bgHitbox.left + (progress * bgHitbox.width());
        drawSeekBarName(c, filledRight);
        drawPercentsText(c, filledRight);
    }

    private void drawSeekBarName(Canvas c, float filledRight) {
        if (filledRight > nameX) {
            c.save();
            c.clipRect(nameX, bgHitbox.top, filledRight, bgHitbox.bottom);
            nameRenderer.drawWithShadow(name, c);
            c.restore();
        }
        nameRenderer.drawText(name, c);
    }

    private void drawPercentsText(Canvas c, float filledRight) {
        if (filledRight > percentX) {
            c.save();
            c.clipRect(percentX, bgHitbox.top, filledRight, bgHitbox.bottom);
            percentRenderer.drawWithShadow(percentText, c);
            c.restore();
        }
        percentRenderer.drawText(percentText, c);
    }

    private boolean isIn(MotionEvent e) {
        return this.bgHitbox.contains(e.getX(), e.getY());
    }
}