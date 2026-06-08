package com.example.tutorialgame.gamestates.menu;

import android.graphics.Canvas;
import android.view.MotionEvent;

import com.example.tutorialgame.engine.core.Game;
import com.example.tutorialgame.gamestates.GameState;
import com.example.tutorialgame.gamestates.menu.menustates.LanguageMenu;
import com.example.tutorialgame.gamestates.menu.menustates.MainMenu;
import com.example.tutorialgame.gamestates.menu.menustates.OptionsMenu;
import com.example.tutorialgame.gamestates.menu.menustates.MusicSoundsMenu;
import com.example.tutorialgame.gamestates.menu.menustates.VideoSettingsMenu;

/**
 * Orchestrates the menu system by managing sub-menu states.
 * Handles transitions between different menu screens (MainMenu, OptionsMenu, etc.)
 */
public class MenuManager extends GameState {
    private MainMenu mainMenu;
    private OptionsMenu optionsMenu;
    private MusicSoundsMenu musicSoundsMenu;
    private VideoSettingsMenu videoSettingsMenu;
    private LanguageMenu languageMenu;
    
    public enum MenuState { Main, Options, MusicSounds, VideoSettings, Language }
    private MenuState currentMenuState = MenuState.Main;

    public MenuManager(Game game) {
        super(game);
        initMenuStates();
    }

    public void initMenuStates() {
        mainMenu = new MainMenu(game, this);
        optionsMenu = new OptionsMenu(game, this);
        musicSoundsMenu = new MusicSoundsMenu(game, this);
        videoSettingsMenu = new VideoSettingsMenu(game, this);
        languageMenu = new LanguageMenu(game, this);
    }

    public void refreshMenus() {
        mainMenu.refreshStrings();
        optionsMenu.refreshStrings();
        musicSoundsMenu.refreshStrings();
        videoSettingsMenu.refreshStrings();
        languageMenu.refreshStrings();
    }

    public void setCurrentMenuState(MenuState newState) {
        Menu old = getMenuInstance(currentMenuState);
        if (old != null) old.onExit();
        
        this.currentMenuState = newState;
        
        Menu now = getMenuInstance(currentMenuState);
        if (now != null) now.onEnter();
    }

    private Menu getMenuInstance(MenuState state) {
        switch (state) {
            case Main: return mainMenu;
            case Options: return optionsMenu;
            case MusicSounds: return musicSoundsMenu;
            case VideoSettings: return videoSettingsMenu;
            case Language: return languageMenu;
            default: return null;
        }
    }

    @Override
    public void update(double delta) {
        Menu current = getMenuInstance(currentMenuState);
        if (current != null) current.update(delta);
    }

    @Override
    public void render(Canvas c) {
        Menu current = getMenuInstance(currentMenuState);
        if (current != null) current.render(c);
    }

    @Override
    public void touchEvents(MotionEvent event) {
        Menu current = getMenuInstance(currentMenuState);
        if (current != null) current.touchEvents(event);
    }

    @Override
    public void onEnter() {
        // Reset to mainMenu menu whenever the game state switches back to MENU
        currentMenuState = MenuState.Main;
        mainMenu.onEnter();
    }
}
