package com.example.tutorialgame.engine.ui.displays;

import static com.example.tutorialgame.engine.core.GameConstants.Sprite.SCALE_MULTIPLIER;
import static com.example.tutorialgame.engine.core.GameConstants.Sprite.TILE_SIZE;
import static com.example.tutorialgame.engine.core.GameConstants.View.SCREEN_WIDTH;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.NinePatchDrawable;

import androidx.core.content.ContextCompat;

import com.example.tutorialgame.MyApp;
import com.example.tutorialgame.R;
import com.example.tutorialgame.engine.interfaces.BitmapMethods;
import com.example.tutorialgame.engine.renderer.TextRenderer;
import com.example.tutorialgame.managers.BitmapManager;
import com.example.tutorialgame.ui.base.BaseActivity;

import java.util.Objects;

/**
 * A HUD component that displays the current coin count with a slide-down animation.
 * Provides destination coordinates for collected coin entities.
 */
public class CoinDisplay implements BitmapMethods {
    private final Bitmap coinImg;
    private final NinePatchDrawable background;
    private final TextRenderer textPaint;
    private int coinsCount;
    private float offset;
    private long lastUpdate;
    
    // Global destinations for coin animations to target
    public static float xDestination, yDestination;

    public CoinDisplay() {
        // Use BitmapManager for consistent resource handling
        this.coinImg = BitmapManager.getBitmapRegion(R.drawable.spr_coin, 0, 0, 10, 10, 1.0, false);

        textPaint = new TextRenderer(Objects.requireNonNull(coinImg).getHeight(), R.color.black);
        textPaint.setY(textPaint.getTextSize() + 3 * SCALE_MULTIPLIER);

        background = (NinePatchDrawable) ContextCompat.getDrawable(BaseActivity.getContext(), R.drawable.choicebox);
        
        // Initial state: hidden above the screen
        coinsCount = MyApp.getCosmetic().getCoinsLeft();
        setPositions();
        offset = -Objects.requireNonNull(background).getBounds().height();
    }

    private void setPositions() {
        float width = coinImg.getWidth() + textPaint.measureText(String.valueOf(coinsCount)) + 0.7f * TILE_SIZE;

        background.setBounds((int) ((SCREEN_WIDTH - width) / 2),
                0,
                (int) ((SCREEN_WIDTH + width) / 2f),
                (int) (coinImg.getHeight() + 0.6 * TILE_SIZE));

        textPaint.setX(background.getBounds().left + coinImg.getWidth() + 7 * SCALE_MULTIPLIER);
        lastUpdate = System.currentTimeMillis();

        // Target for coin "fly" animation
        xDestination = background.getBounds().left + 5 * SCALE_MULTIPLIER;
    }

    public void update(double delta) {
        // Check for data changes to trigger the animation
        int currentCoins = MyApp.getCosmetic().getCoinsLeft();
        if (coinsCount != currentCoins) {
            coinsCount = currentCoins;
            setPositions();
        }

        slideAnimation(delta);
        
        // Update the dynamic Y destination based on the current animation offset
        yDestination = (4.5f * SCALE_MULTIPLIER) + offset;
    }

    private void slideAnimation(double delta) {
        float speed = (float) (4.167*TILE_SIZE * delta); // Pixels per second adjusted by delta

        // Stay visible for 3 seconds after the last coin update
        if (System.currentTimeMillis() - lastUpdate <= 3000) {
            if (offset < 0) offset += speed;
            else offset = 0;
        } else {
            // Slide back up
            if (offset > -background.getBounds().height()) offset -= 1.2f * speed;
            else offset = -background.getBounds().height();
        }
    }

    public void draw(Canvas c) {
        if (offset <= -background.getBounds().height()) return;

        c.save();
        c.translate(0, offset);

        background.draw(c);
        c.drawBitmap(coinImg, xDestination, 4.5f * SCALE_MULTIPLIER, null);
        textPaint.drawText(String.valueOf(coinsCount), c);

        c.restore();
    }
}