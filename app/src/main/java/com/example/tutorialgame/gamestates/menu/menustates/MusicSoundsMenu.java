package com.example.tutorialgame.gamestates.menu.menustates;

import static com.example.tutorialgame.engine.core.GameConstants.Sprite.TILE_SIZE;
import static com.example.tutorialgame.engine.core.GameConstants.View.SCREEN_HEIGHT;
import static com.example.tutorialgame.engine.core.GameConstants.View.SCREEN_WIDTH;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.view.MotionEvent;

import com.example.tutorialgame.R;
import com.example.tutorialgame.engine.audio.MusicManager;
import com.example.tutorialgame.engine.audio.SoundManager;
import com.example.tutorialgame.engine.core.Game;
import com.example.tutorialgame.engine.ui.customviews.CustomSeekBar;
import com.example.tutorialgame.engine.ui.customviews.buttons.rects.RectImages;
import com.example.tutorialgame.engine.ui.customviews.switches.CustomSwitch;
import com.example.tutorialgame.engine.ui.customviews.buttons.rects.RectButton;
import com.example.tutorialgame.engine.ui.customviews.switches.SwitchList;
import com.example.tutorialgame.gamestates.menu.Menu;
import com.example.tutorialgame.gamestates.menu.MenuManager;
import com.example.tutorialgame.ui.base.BaseActivity;

/**
 * Sub-menu for managing audio settings (Music, SFX) and haptic feedback.
 * Synchronizes UI components with persistent storage and audio managers.
 */
public class MusicSoundsMenu extends Menu implements CustomSeekBar.OnProgressChangedListener {
    private final CustomSeekBar sbMusic, sbSound;
    private final RectButton btnDone;
    private final CustomSwitch swHaptics;

    public MusicSoundsMenu(Game game, MenuManager menuManager) {
        super(game, menuManager);

        // UI Initialization using screen ratios
        sbMusic = new CustomSeekBar((int) (SCREEN_WIDTH * 0.2), (int) (SCREEN_HEIGHT * 0.3), (int) (SCREEN_WIDTH * 0.6), (int) (SCREEN_HEIGHT * 0.1), R.string.music_cap);
        sbSound = new CustomSeekBar((int) (SCREEN_WIDTH * 0.2), (int) (SCREEN_HEIGHT * 0.45), (int) (SCREEN_WIDTH * 0.6), (int) (SCREEN_HEIGHT * 0.1), R.string.sounds_cap);
        
        float switchX = (SCREEN_WIDTH - SwitchList.HAPTICS.getWidth()) / 2f - 1.5f*TILE_SIZE;

        swHaptics = new CustomSwitch(switchX, (int) (SCREEN_HEIGHT * 0.6), SwitchList.HAPTICS);
        btnDone = new RectButton((int) (SCREEN_WIDTH * 0.2), (int) (SCREEN_HEIGHT * 0.75), (int) (SCREEN_WIDTH * 0.6), (int) (SCREEN_HEIGHT * 0.15), RectImages.DONE, false);

        // Set listeners
        btnDone.setOnClickListener(button -> menuManager.setCurrentMenuState(MenuManager.MenuState.Options));
        swHaptics.setOnCheckedChangeListener((customSwitch, isChecked) -> BaseActivity.setDoesVibe(isChecked));
        sbMusic.setOnProgressChangedListener(this);
        sbSound.setOnProgressChangedListener(this);
    }

    @Override
    public void onEnter() {
        super.onEnter();
        refreshSettings();
    }

    /**
     * Loads the latest settings from storage and updates the UI components.
     */
    private void refreshSettings() {
        SharedPreferences sp = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        sbMusic.setProgressNormalized(sp.getFloat("music_volume", 0.3f));
        sbSound.setProgressNormalized(sp.getFloat("sound_volume", 0.5f));
        // swHaptics loads its own state internally via SwitchList/SharedPreferences
    }

    @Override
    public void update(double delta) {} // Static menu, no logic updates needed

    @Override
    public void render(Canvas c) {
        drawBackground(c, R.string.music_sounds);
        sbMusic.drawSeekBar(c);
        sbSound.drawSeekBar(c);
        swHaptics.draw(c);
        btnDone.draw(c);
    }

    @Override
    public void touchEvents(MotionEvent event) {
        sbMusic.eventHandler(event);
        sbSound.eventHandler(event);
        swHaptics.eventHandler(event);
        btnDone.eventHandler(event);
    }

    @Override
    public void onProgressChanged(CustomSeekBar seekBar, float progress) {
        if (seekBar == sbMusic)
            MusicManager.getInstance(context).setVolume(progress);
        else if (seekBar == sbSound)
            SoundManager.getInstance(context).setVolume(progress);
    }

    @Override
    public void refreshStrings() {
        super.refreshStrings();
        sbMusic.refreshStrings();
        sbSound.refreshStrings();
        swHaptics.refreshStrings();
    }
}
