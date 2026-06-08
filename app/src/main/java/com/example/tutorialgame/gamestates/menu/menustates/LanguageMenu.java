package com.example.tutorialgame.gamestates.menu.menustates;

import static com.example.tutorialgame.engine.core.GameConstants.Sprite.SCALE_MULTIPLIER;
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
import com.example.tutorialgame.ui.base.BaseActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * A modular menu for selecting game language.
 * Automatically cycles through supported locales and updates the game instantly.
 */
public class LanguageMenu extends Menu implements GameButton.OnClickListener {

    private static class LanguageOption {
        final String code;
        final int stringRes;

        LanguageOption(String code, int stringRes) {
            this.code = code;
            this.stringRes = stringRes;
        }
    }

    private final List<LanguageOption> options = new ArrayList<>();
    private final List<RectButton> buttons = new ArrayList<>();
    private final RectButton btnDone;

    public LanguageMenu(Game game, MenuManager menuManager) {
        super(game, menuManager);

        // Define supported languages
        options.add(new LanguageOption("en", R.string.english));
        options.add(new LanguageOption("iw", R.string.hebrew));
        options.add(new LanguageOption("el", R.string.greek));

        int btnWidth = (int) (SCREEN_WIDTH * 0.6);
        int btnHeight = (int) (SCREEN_HEIGHT * 0.129);
        int startX = (int) (SCREEN_WIDTH * 0.2);
        float startY = SCREEN_HEIGHT * 0.298f;
        float spacing = btnHeight * 1.15f;

        // Create a button for each language option (Static creation, we only refresh their text)
        for (int i = 0; i < options.size(); i++) {
            RectButton btn = new RectButton(startX, startY + (i * spacing), btnWidth, btnHeight, RectImages.LANGUAGE, false);
            btn.setOverrideText(context.getString(options.get(i).stringRes));
            btn.setOnClickListener(this);
            buttons.add(btn);
        }

        btnDone = new RectButton(startX, (int) (SCREEN_HEIGHT * 0.78), btnWidth, btnHeight+4*SCALE_MULTIPLIER, RectImages.DONE, false);
        btnDone.setOnClickListener(this);
    }

    @Override
    public void update(double delta) {}

    @Override
    public void render(Canvas c) {
        drawBackground(c, R.string.language_cap);
        
        for (int i = 0; i < buttons.size(); i++) {
            buttons.get(i).draw(c);
        }
        
        btnDone.draw(c);
    }

    @Override
    public void touchEvents(MotionEvent event) {
        for (RectButton btn : buttons) {
            btn.eventHandler(event);
        }
        btnDone.eventHandler(event);
    }

    @Override
    public void onClick(GameButton button) {
        if (button == btnDone) {
            menuManager.setCurrentMenuState(MenuManager.MenuState.Options);
            return;
        }

        int index = buttons.indexOf((RectButton) button);
        if (index != -1) {
            String selectedLang = options.get(index).code;
            if (context instanceof BaseActivity) {
                ((BaseActivity) context).updateLanguageOnTheFly(selectedLang);
                game.refreshUI();
            }
        }
    }

    @Override
    public void refreshStrings() {
        super.refreshStrings();
        for (int i = 0; i < buttons.size(); i++) {
            buttons.get(i).setOverrideText(context.getString(options.get(i).stringRes));
        }
    }
}