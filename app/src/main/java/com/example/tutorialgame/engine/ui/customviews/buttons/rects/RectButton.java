package com.example.tutorialgame.engine.ui.customviews.buttons.rects;

import static com.example.tutorialgame.engine.core.GameConstants.Sprite.SCALE_MULTIPLIER;

import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.drawable.NinePatchDrawable;

import androidx.appcompat.content.res.AppCompatResources;

import com.example.tutorialgame.R;
import com.example.tutorialgame.engine.renderer.TextRenderer;
import com.example.tutorialgame.engine.ui.customviews.buttons.GameButton;
import com.example.tutorialgame.ui.base.BaseActivity;

import java.util.Objects;

public class RectButton extends GameButton {
    private NinePatchDrawable normal, pressed;
    private RectImages rectImages;
    private final TextRenderer textPaint;
    private String overrideText;

    public RectButton(float x, float y, float width, float height, boolean multitouch) {
        super(new RectF(x, y, x + width, y + height), multitouch);
        this.textPaint = new TextRenderer(SCALE_MULTIPLIER * 12f, R.color.floral_white);
        this.textPaint.setShadowColor(BaseActivity.getContext().getColor(R.color.dark_charcoal));
        this.textPaint.setShadowOffset(0, SCALE_MULTIPLIER * 1.5f);
    }

    public RectButton(float x, float y, float width, float height, RectImages img, boolean multitouch) {
        super(new RectF(x, y, x + width, y + height), multitouch);

        // Load fresh, high-quality NinePatch instances for EACH button
        this.normal = (NinePatchDrawable) Objects.requireNonNull(AppCompatResources.getDrawable(BaseActivity.getContext(), R.drawable.btn_menu_base)).mutate();
        this.normal.setTint(img.getBtnColor());
        this.normal.setTintMode(PorterDuff.Mode.MULTIPLY);
        this.normal.setFilterBitmap(false); // Sharp edges for Pixel Art
        this.normal.setBounds((int) getHitbox().left, (int) getHitbox().top, (int) getHitbox().right, (int) getHitbox().bottom);

        this.pressed = (NinePatchDrawable) Objects.requireNonNull(AppCompatResources.getDrawable(BaseActivity.getContext(), R.drawable.btn_menu_pressed)).mutate();
        this.pressed.setTint(img.getBtnColor());
        this.pressed.setTintMode(PorterDuff.Mode.MULTIPLY);
        this.pressed.setFilterBitmap(false); // Sharp edges for Pixel Art
        this.pressed.setBounds((int) getHitbox().left, (int) getHitbox().top, (int) getHitbox().right, (int) getHitbox().bottom);

        this.textPaint = new TextRenderer(SCALE_MULTIPLIER * 12f, R.color.floral_white);
        this.textPaint.setShadowColor(BaseActivity.getContext().getColor(R.color.dark_charcoal));
        this.textPaint.setShadowOffset(0, SCALE_MULTIPLIER * 1.5f);

        rectImages = img;
    }

    private NinePatchDrawable getBtnImg() {
        return pushed ? pressed : normal;
    }

    public RectF getHitbox() {
        return super.getHitbox();
    }

    public void setOverrideText(String text) {
        this.overrideText = text;
    }

    @Override
    public void draw(Canvas c) {
        setTextPos(pushed);

        this.getBtnImg().draw(c);

        String text = overrideText != null ? overrideText : rectImages.getText();
        textPaint.drawWithShadow(text, c);
    }

    private void setTextPos(boolean isPushed) {
        String text = overrideText != null ? overrideText : rectImages.getText();
        float xOffset = textPaint.measureText(text) / 2;
        if (isPushed)
            textPaint.setPosition(pressed.getBounds().centerX() - xOffset, pressed.getBounds().centerY() + SCALE_MULTIPLIER * 4f);
        else
            textPaint.setPosition(pressed.getBounds().centerX() - xOffset, pressed.getBounds().centerY() + SCALE_MULTIPLIER * 2f);
    }

    @Override
    protected boolean isIn(float x, float y) {
        return hitbox.contains(x, y);
    }
}
