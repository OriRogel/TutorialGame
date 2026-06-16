package com.example.tutorialgame.gamestates.menu.menustates;

import static com.example.tutorialgame.engine.core.GameConstants.View.SCREEN_HEIGHT;
import static com.example.tutorialgame.engine.core.GameConstants.View.SCREEN_WIDTH;

import android.graphics.Canvas;
import android.view.MotionEvent;

import com.example.tutorialgame.R;
import com.example.tutorialgame.engine.core.Game;
import com.example.tutorialgame.engine.ui.customviews.buttons.GameButton;
import com.example.tutorialgame.engine.ui.customviews.buttons.rects.RectButton;
import com.example.tutorialgame.engine.ui.customviews.buttons.rects.RectImages;
import com.example.tutorialgame.gamestates.menu.Menu;
import com.example.tutorialgame.gamestates.menu.MenuManager;

import java.util.ArrayList;
import java.util.List;

/**
 * A sub-menu that acts as a central hub for various game settings.
 * Manages navigation between audio, video, and general configuration screens.
 */
public class OptionsMenu extends Menu implements GameButton.OnClickListener {
    private final List<RectButton> buttons = new ArrayList<>();
    private final RectButton btnDone, btnVideoSettings, btnMusicSounds, btnLanguage, btnCredits;

    public OptionsMenu(Game game, MenuManager menuManager) {
        super(game, menuManager);

        // Grid Layout calculation
        int btnWidth = (int) (SCREEN_WIDTH * 0.34);
        int btnHeight = (int) (SCREEN_HEIGHT * 0.15);
        int leftColX = (int) (SCREEN_WIDTH * 0.16);
        int rightColX = (int) (SCREEN_WIDTH * 0.55);

        // Initialize buttons with consistent grid alignment
        btnVideoSettings = new RectButton(leftColX, (int) (SCREEN_HEIGHT * 0.3), btnWidth, btnHeight, RectImages.VIDEO_SETTINGS, false);
        btnMusicSounds = new RectButton(rightColX, (int) (SCREEN_HEIGHT * 0.3), btnWidth, btnHeight, RectImages.MUSIC_SOUNDS, false);
        btnLanguage = new RectButton(leftColX, (int) (SCREEN_HEIGHT * 0.5), btnWidth, btnHeight, RectImages.LANGUAGE, false);
        btnCredits = new RectButton(rightColX, (int) (SCREEN_HEIGHT * 0.5), btnWidth, btnHeight, RectImages.CREDITS, false);
        btnDone = new RectButton(leftColX, (int) (SCREEN_HEIGHT * 0.7), (int) (SCREEN_WIDTH * 0.74), btnHeight, RectImages.DONE, false);

        // Register buttons for central management
        registerButton(btnVideoSettings);
        registerButton(btnMusicSounds);
        registerButton(btnLanguage);
        registerButton(btnCredits);
        registerButton(btnDone);
    }

    private void registerButton(RectButton btn) {
        btn.setOnClickListener(this);
        buttons.add(btn);
    }

    @Override
    public void update(double delta) {}

    @Override
    public void render(Canvas c) {
        drawBackground(c, R.string.options);
        for (RectButton btn : buttons) {
            btn.draw(c);
        }
    }

    @Override
    public void touchEvents(MotionEvent event) {
        for (RectButton btn : buttons) {
            btn.eventHandler(event);
        }
    }

    @Override
    public void onClick(GameButton button) {
        if (button == btnDone) {
            menuManager.setCurrentMenuState(MenuManager.MenuState.Main);
        } else if (button == btnMusicSounds) {
            menuManager.setCurrentMenuState(MenuManager.MenuState.MusicSounds);
        } else if (button == btnVideoSettings) {
            menuManager.setCurrentMenuState(MenuManager.MenuState.VideoSettings);
        } else if (button == btnLanguage) {
            menuManager.setCurrentMenuState(MenuManager.MenuState.Language);
        } else if (button == btnCredits) {
            // TODO: Logic for showing credits will go here
        }
    }

    @Override
    public void refreshStrings() {
        super.refreshStrings();
        // RectButton uses RectImages which handles getText() dynamically using BaseActivity.getContext()
        // No extra work needed for RectButtons unless we need to re-center text, 
        // but draw() calls rectImages.setTextPos(pushed) which re-measures every frame.
    }
}
