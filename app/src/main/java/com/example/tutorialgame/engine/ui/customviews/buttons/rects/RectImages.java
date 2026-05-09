package com.example.tutorialgame.engine.ui.customviews.buttons.rects;

import static com.example.tutorialgame.engine.core.GameConstants.Sprite.SCALE_MULTIPLIER;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.NinePatchDrawable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import com.example.tutorialgame.R;
import com.example.tutorialgame.engine.interfaces.BitmapMethods;
import com.example.tutorialgame.engine.renderer.TextRenderer;
import com.example.tutorialgame.ui.base.BaseActivity;
import java.util.Objects;

public enum RectImages implements BitmapMethods {
    REPLAY(Color.RED, R.string.replay),
    EXIT_DEATH(Color.RED, R.string.back_launcher),
    EXIT_MENU(Color.WHITE, R.string.save_quit),
    BACK(Color.WHITE, R.string.back),
    RETURN(Color.WHITE, R.string.return_),
    OPTIONS(Color.WHITE, R.string.options),
    DONE(Color.WHITE, R.string.done),
    VIDEO_SETTINGS(Color.WHITE, R.string.video_settings),
    MUSIC_SOUNDS(Color.WHITE, R.string.music_sounds),
    LANGUAGE(Color.WHITE, R.string.language),
    CREDITS(Color.WHITE, R.string.credits);

    private final NinePatchDrawable normal, pressed;
    private final @StringRes int textRes;
    private final TextRenderer textPaint;

    RectImages(int btnColor, @StringRes int textRes) {
        this.normal = (NinePatchDrawable) ContextCompat.getDrawable(BaseActivity.getContext(), R.drawable.btn_menu_base);
        Objects.requireNonNull(this.normal).setTint(btnColor);
        this.normal.setTintMode(PorterDuff.Mode.MULTIPLY);

        this.pressed = (NinePatchDrawable) ContextCompat.getDrawable(BaseActivity.getContext(), R.drawable.btn_menu_pressed);
        Objects.requireNonNull(this.pressed).setTint(btnColor);
        this.pressed.setTintMode(PorterDuff.Mode.MULTIPLY);

        textPaint = new TextRenderer(SCALE_MULTIPLIER * 12f, R.color.floral_white);
        textPaint.setShadowColor(BaseActivity.getContext().getColor(R.color.dark_charcoal));
        textPaint.setShadowOffset(0,SCALE_MULTIPLIER*1.5f);

        this.textRes = textRes;
    }

    public NinePatchDrawable getNormal() {
        return normal;
    }
    public NinePatchDrawable getPressed() {
        return pressed;
    }

    public String getText() {
        return BaseActivity.getContext().getString(textRes);
    }

    public void setTextPos(boolean isPushed) {
        float xOffset = getTextPaint().measureText(getText()) / 2;
        if (isPushed)
            textPaint.setPosition(pressed.getBounds().centerX() - xOffset, pressed.getBounds().centerY() + SCALE_MULTIPLIER * 4f);
        else
            textPaint.setPosition(pressed.getBounds().centerX() - xOffset, pressed.getBounds().centerY() + SCALE_MULTIPLIER * 2f);
    }

    public TextRenderer getTextPaint() {
        return textPaint;
    }
}
