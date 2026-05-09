package com.example.tutorialgame.gamestates;

import static com.example.tutorialgame.engine.core.GameConstants.Sprite.SCALE_MULTIPLIER;
import static com.example.tutorialgame.engine.core.GameConstants.Sprite.TILE_SIZE;
import static com.example.tutorialgame.engine.core.GameConstants.View.SCREEN_HEIGHT;
import static com.example.tutorialgame.engine.core.GameConstants.View.SCREEN_WIDTH;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.NinePatchDrawable;
import android.view.MotionEvent;
import androidx.core.content.ContextCompat;
import com.example.tutorialgame.engine.core.Game;
import com.example.tutorialgame.engine.renderer.TextRenderer;
import com.example.tutorialgame.engine.ui.customviews.buttons.BaseButton;
import com.example.tutorialgame.engine.ui.customviews.buttons.rects.RectImages;
import com.example.tutorialgame.engine.ui.effects.impcateffects.TapEffect;
import com.example.tutorialgame.engine.ui.effects.impcateffects.ImpactEffectType;
import com.example.tutorialgame.engine.audio.MusicManager;
import com.example.tutorialgame.engine.ui.customviews.buttons.rects.RectButton;
import com.example.tutorialgame.R;
import com.example.tutorialgame.ui.base.BaseActivity;
import java.util.Objects;

/**
 * Manages the screen displayed upon player death.
 * Uses a state machine to coordinate opening animations, music, and UI interaction.
 */
public class DeathScreen extends BaseState implements BaseButton.OnClickListener {

    private final RectButton btnReplay, btnExit;
    private final NinePatchDrawable background, frame;
    private int left, top, right, bottom;

    private final TapEffect tapLT, tapRT, tapLB, tapRB;
    private final int effectOffset = 10 * SCALE_MULTIPLIER;
    private final TextRenderer titlePaint;
    private String currentLang = BaseActivity.getLang();
    private boolean isMusicPlaying, isAnimationDone;

    public DeathScreen(Game game) {
        super(game);

        btnReplay = new RectButton((int) (SCREEN_WIDTH * 0.2), (int) (SCREEN_HEIGHT * 0.4), (int) (SCREEN_WIDTH * 0.6), (int) (SCREEN_HEIGHT * 0.15), RectImages.REPLAY, false);
        btnExit = new RectButton((int) (SCREEN_WIDTH * 0.2), (int) (SCREEN_HEIGHT * 0.6), (int) (SCREEN_WIDTH * 0.6), (int) (SCREEN_HEIGHT * 0.15), RectImages.EXIT_DEATH, false);

        btnExit.setOnClickListener(this);
        btnReplay.setOnClickListener(this);

        frame = (NinePatchDrawable) ContextCompat.getDrawable(BaseActivity.getContext(), R.drawable.menu_frame);
        background = (NinePatchDrawable) ContextCompat.getDrawable(BaseActivity.getContext(), R.drawable.menu_background);
        if (background != null) background.setAlpha(180);

        tapLT = new TapEffect(ImpactEffectType.SPARK);
        tapRT = new TapEffect(ImpactEffectType.SPARK);
        tapLB = new TapEffect(ImpactEffectType.SPARK);
        tapRB = new TapEffect(ImpactEffectType.SPARK);

        titlePaint = new TextRenderer(TILE_SIZE * 2, R.color.floral_white);
        titlePaint.setShadowColor(R.color.dark_charcoal);
        titlePaint.setShadowOffset(SCALE_MULTIPLIER * 2.5f, SCALE_MULTIPLIER * 2.5f);
        updateTitleMeasurements();

        resetAnimation();
    }

    private void updateTitleMeasurements() {
        float titleWidth = titlePaint.measureText(getTitle());
        titlePaint.setPosition((SCREEN_WIDTH - titleWidth) / 2f, SCREEN_HEIGHT / 5f + SCALE_MULTIPLIER * 2.5f);
    }

    @Override
    public void update(double delta) {
        updateSparkLoop();

        if (!isAnimationDone) updateOpeningAnimation(delta);
        else {
            if (!isMusicPlaying) {
                MusicManager.getInstance(context).play(R.raw.music_deathscreen);
                MusicManager.getInstance(context).setLooping(true);
                isMusicPlaying = true;
            }
        }
    }

    private void updateOpeningAnimation(double delta) {
        int deltaPos = (int) (delta * 7.29 * TILE_SIZE);
        top = Math.max(0, top - deltaPos);
        bottom = Math.min(SCREEN_HEIGHT, bottom + deltaPos);

        if (top == 0 && bottom == SCREEN_HEIGHT) {
            int deltaX = deltaPos * 2;
            left = Math.max(0, left - deltaX);
            right = Math.min(SCREEN_WIDTH, right + deltaX);
        }

        // עדכון גבולות הציור לאחר חישוב המיקומים החדשים
        if (background != null) background.setBounds(left, top, right, bottom);
        if (frame != null) frame.setBounds(left, top, right, bottom);

        if (top == 0 && bottom == SCREEN_HEIGHT && left == 0 && right == SCREEN_WIDTH)
            isAnimationDone = true;
    }

    private void updateSparkLoop() {
        if (tapLT.getElapsed() >= 250 || tapLT.getElapsed() == 0) {
            tapLT.resetEffect();
            tapRT.resetEffect();
            tapLB.resetEffect();
            tapRB.resetEffect();
        }
    }

    @Override
    public void render(Canvas c) {
        drawBackground(c);
        drawSparks(c);
        if (isAnimationDone) {
            drawTitle(c);
            btnReplay.draw(c);
            btnExit.draw(c);
        }
    }

    private void drawSparks(Canvas c) {
        if (isAnimationDone) return;

        if (top != 0) {
            float midX = (left + right)/2f;
            tapLT.show(c, midX, top + effectOffset);
            tapLB.show(c, midX, bottom - effectOffset);
        }
        else {
            tapLT.show(c, left + effectOffset, top + effectOffset);
            tapLB.show(c, left + effectOffset, bottom - effectOffset);
            tapRT.show(c, right - effectOffset, top + effectOffset);
            tapRB.show(c, right - effectOffset, bottom - effectOffset);
        }
    }

    private void drawBackground(Canvas c) {
        if (frame != null) frame.draw(c);
        if (background != null) background.draw(c);
    }

    private void drawTitle(Canvas c) {
        if (!Objects.equals(BaseActivity.getLang(), currentLang)) {
            currentLang = BaseActivity.getLang();
            updateTitleMeasurements();
        }
        titlePaint.drawWithShadow(getTitle(), c);
    }

    @Override
    public void touchEvents(MotionEvent event) {
        if (isAnimationDone) {
            btnReplay.eventHandler(event);
            btnExit.eventHandler(event);
        }
    }

    private void resetAnimation() {
        left = SCREEN_WIDTH / 2 - SCALE_MULTIPLIER;
        top = SCREEN_HEIGHT / 2;
        right = SCREEN_WIDTH / 2 + SCALE_MULTIPLIER;
        bottom = SCREEN_HEIGHT / 2;
        isAnimationDone = false;
        isMusicPlaying = false;

        if (background != null) {
            background.setTint(Color.RED);
            background.setTintMode(PorterDuff.Mode.MULTIPLY);
            background.setBounds(left, top, right, bottom);
        }

        if (frame != null) {
            frame.setTint(Color.RED);
            frame.setTintMode(PorterDuff.Mode.MULTIPLY);
            frame.setBounds(left, top, right, bottom);
        }
    }

    @Override
    public void onClick(BaseButton button) {
        if (button == btnReplay) {
            MusicManager.getInstance(context).play(R.raw.music_calm_village);
            game.restartGame();
        } else if (button == btnExit) {
            MusicManager.getInstance(context).play(R.raw.music_launcher);
            returnLauncher();
        }
        resetAnimation();

    }

    private String getTitle() {
        return context.getString(R.string.you_died);
    }
}