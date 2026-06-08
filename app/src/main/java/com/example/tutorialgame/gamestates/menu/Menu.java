package com.example.tutorialgame.gamestates.menu;

import static com.example.tutorialgame.engine.core.GameConstants.Sprite.SCALE_MULTIPLIER;
import static com.example.tutorialgame.engine.core.GameConstants.Sprite.TILE_SIZE;
import static com.example.tutorialgame.engine.core.GameConstants.View.SCREEN_HEIGHT;
import static com.example.tutorialgame.engine.core.GameConstants.View.SCREEN_WIDTH;

import android.graphics.Canvas;
import android.graphics.drawable.NinePatchDrawable;

import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import com.example.tutorialgame.R;
import com.example.tutorialgame.engine.core.Game;
import com.example.tutorialgame.engine.renderer.TextRenderer;
import com.example.tutorialgame.gamestates.GameState;

import java.util.Objects;

/**
 * An abstract base class for all menu states.
 * Provides a consistent visual framework including a common background, 
 * frame, and titled layout using the TextRenderer system.
 */
public abstract class Menu extends GameState {
    protected final NinePatchDrawable background, frame;
    protected final TextRenderer titlePaint;
    protected final MenuManager menuManager;
    private int lastRes = -1;

    public Menu(Game game, MenuManager menuManager) {
        super(game);
        this.menuManager = menuManager;

        // Visual Assets setup
        background = (NinePatchDrawable) ContextCompat.getDrawable(context, R.drawable.menu_background);
        Objects.requireNonNull(background).setBounds(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        background.setAlpha(220);

        frame = (NinePatchDrawable) ContextCompat.getDrawable(context, R.drawable.menu_frame);
        Objects.requireNonNull(frame).setBounds(background.getBounds());

        // Centralized Title Rendering logic
        titlePaint = new TextRenderer(TILE_SIZE * 2, R.color.floral_white);
        titlePaint.setShadowColor(context.getColor(R.color.dark_moon));
        titlePaint.setShadowOffset(SCALE_MULTIPLIER * 2.5f, SCALE_MULTIPLIER * 2.5f);
    }

    /**
     * Renders the common menu infrastructure.
     * @param c The canvas to draw on.
     * @param stringResId The resource ID of the title text.
     */
    protected void drawBackground(Canvas c, @StringRes int stringResId) {
        if (frame != null) frame.draw(c);
        if (background != null) background.draw(c);

        if (lastRes != stringResId) {
            lastRes = stringResId;
            updateTitlePosition(stringResId);
        }
        // Single call handles both text and its shadow using the custom TextRenderer
        titlePaint.drawWithShadow(context.getString(stringResId), c);
    }

    private void updateTitlePosition(@StringRes int stringResId) {
        float titleWidth = titlePaint.measureText(context.getString(stringResId));
        titlePaint.setPosition(SCREEN_WIDTH / 2f - titleWidth / 2f, SCREEN_HEIGHT / 5f);
    }

    /**
     * Resets the cached title resource to force a re-measure and re-draw 
     * in the current language.
     */
    public void refreshStrings() {
        if (lastRes != -1) {
            updateTitlePosition(lastRes);
        }
    }


}
