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

    private final int btnColor;
    private final @StringRes int textRes;

    RectImages(int btnColor, @StringRes int textRes) {
        this.btnColor = btnColor;
        this.textRes = textRes;
    }

    public int getBtnColor() {
        return btnColor;
    }

    public String getText() {
        return BaseActivity.getContext().getString(textRes);
    }
}
