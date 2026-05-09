package com.example.tutorialgame.engine.ui.customviews.buttons.rects;

import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.drawable.NinePatchDrawable;

import com.example.tutorialgame.engine.ui.customviews.buttons.BaseButton;

import java.util.Objects;

public class RectButton extends BaseButton {
    private NinePatchDrawable normal, pressed;
    private RectImages rectImages;

    public RectButton(float x, float y, float width, float height, boolean multitouch) {
        super(new RectF(x, y, x + width, y + height), multitouch);

    }

    public RectButton(float x, float y, float width, float height, RectImages img, boolean multitouch) {
        super(new RectF(x, y, x + width, y + height), multitouch);

        normal = img.getNormal();
        Objects.requireNonNull(normal).setBounds((int) getHitbox().left, (int) getHitbox().top, (int) getHitbox().right, (int) getHitbox().bottom);

        this.pressed = img.getPressed();
        Objects.requireNonNull(pressed).setBounds((int) getHitbox().left, (int) getHitbox().top, (int) getHitbox().right, (int) getHitbox().bottom);

        rectImages = img;
    }

    private NinePatchDrawable getBtnImg() {
        return pushed ? pressed : normal;
    }

    public RectF getHitbox() {
        return super.getHitbox();
    }

    @Override
    public void draw(Canvas c) {
        rectImages.setTextPos(pushed);

        this.getBtnImg().draw(c);

        rectImages.getTextPaint().drawWithShadow(rectImages.getText(), c);
    }

    @Override
    protected boolean isIn(float x, float y) {
        return hitbox.contains(x, y);
    }
}
