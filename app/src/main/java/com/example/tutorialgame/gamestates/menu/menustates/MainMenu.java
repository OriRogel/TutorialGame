package com.example.tutorialgame.gamestates.menu.menustates;

import static com.example.tutorialgame.engine.core.GameConstants.Sprite.TILE_SIZE;
import static com.example.tutorialgame.engine.core.GameConstants.View.SCREEN_HEIGHT;
import static com.example.tutorialgame.engine.core.GameConstants.View.SCREEN_WIDTH;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.MotionEvent;

import com.example.tutorialgame.R;
import com.example.tutorialgame.engine.core.Game;
import com.example.tutorialgame.engine.ui.customviews.buttons.GameButton;
import com.example.tutorialgame.engine.ui.customviews.buttons.rects.RectButton;
import com.example.tutorialgame.engine.ui.customviews.buttons.rects.RectImages;
import com.example.tutorialgame.gamestates.menu.Menu;
import com.example.tutorialgame.gamestates.menu.MenuManager;

/**
 * The main entry point of the menu system.
 * Handles the initial sliding animation and primary navigation options.
 */
public class MainMenu extends Menu implements GameButton.OnClickListener {
    
    private boolean isEntering, isExiting;
    private final RectButton btnBack, btnOptions, btnExit;
    private float offsetY;
    private static final float SLIDE_SPEED = TILE_SIZE * 22;

    public MainMenu(Game game, MenuManager menuManager) {
        super(game, menuManager);

        // UI Setup - Using ratios for screen adaptability
        int btnWidth = (int) (SCREEN_WIDTH * 0.6);
        int btnHeight = (int) (SCREEN_HEIGHT * 0.15);
        int startX = (int) (SCREEN_WIDTH * 0.2);

        btnBack = new RectButton(startX, (int) (SCREEN_HEIGHT * 0.3), btnWidth, btnHeight, RectImages.BACK, false);
        btnOptions = new RectButton(startX, (int) (SCREEN_HEIGHT * 0.5), btnWidth, btnHeight, RectImages.OPTIONS, false);
        btnExit = new RectButton(startX, (int) (SCREEN_HEIGHT * 0.7), btnWidth, btnHeight, RectImages.EXIT_MENU, false);

        btnBack.setOnClickListener(this);
        btnOptions.setOnClickListener(this);
        btnExit.setOnClickListener(this);
        
        offsetY = -SCREEN_HEIGHT;
        isEntering = true;
    }

    @Override
    public void onEnter() {
        super.onEnter();
        // Only reset animation if the panel is currently hidden (e.g., coming from the game)
        if (offsetY <= -SCREEN_HEIGHT) {
            resetAnimation();
        }
    }

    private void resetAnimation() {
        offsetY = -SCREEN_HEIGHT;
        isEntering = true;
        isExiting = false;

        // Apply visual theme
        if (frame != null) {
            frame.setTint(Color.WHITE);
            frame.setTintMode(PorterDuff.Mode.MULTIPLY);
        }
        if (background != null) {
            background.setTint(Color.WHITE);
            background.setTintMode(PorterDuff.Mode.MULTIPLY);
        }
    }

    @Override
    public void update(double delta) {
        if (isEntering) {
            offsetY += (float) (SLIDE_SPEED * delta);
            if (offsetY >= 0) {
                offsetY = 0;
                isEntering = false;
            }
        } else if (isExiting) {
            offsetY -= (float) (SLIDE_SPEED * delta * 1.5f);
            if (offsetY <= -SCREEN_HEIGHT) {
                offsetY = -SCREEN_HEIGHT;
                isExiting = false;
                Game.setNextGameState(Game.GameState.PLAYING);
            }
        }
    }

    @Override
    public void render(Canvas c) {
        c.save();
        c.translate(0, offsetY);

        drawBackground(c, R.string.game_menu);
        btnBack.draw(c);
        btnOptions.draw(c);
        btnExit.draw(c);

        c.restore();
    }

    @Override
    public void touchEvents(MotionEvent event) {
        // Interaction allowed only when not animating
        if (!isEntering && !isExiting) {
            btnBack.eventHandler(event);
            btnOptions.eventHandler(event);
            btnExit.eventHandler(event);
        }
    }

    @Override
    public void onClick(GameButton button) {
        if (button == btnBack) {
            isExiting = true;
        } else if (button == btnOptions) {
            menuManager.setCurrentMenuState(MenuManager.MenuState.Options);
        } else if (button == btnExit) {
            returnLauncher();
        }
    }
}
