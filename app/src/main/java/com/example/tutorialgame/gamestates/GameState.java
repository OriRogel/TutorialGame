package com.example.tutorialgame.gamestates;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import com.example.tutorialgame.engine.interfaces.GameStateInterface;
import com.example.tutorialgame.environments.GameMap;
import com.example.tutorialgame.managers.MapManager;
import com.example.tutorialgame.ui.activities.LauncherActivity;
import com.example.tutorialgame.ui.base.BaseActivity;
import com.example.tutorialgame.engine.interfaces.StateSwitcher;
import com.example.tutorialgame.engine.core.Game;

public abstract class GameState implements GameStateInterface {
    protected Game game;
    protected StateSwitcher switcher;
    protected Context context;
    private final Intent intent;

    public GameState(Game game) {
        this.game = game;
        this.switcher = game;
        this.context = BaseActivity.getContext();
        this.intent = new Intent(context, LauncherActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    public Game getGame() {
        return game;
    }

    private void finishGameActivity() {
        if (game.getContext() instanceof Activity) {
            ((Activity) game.getContext()).finish();
        }
    }

    /**
     * Safely shuts down the game session and returns to the launcher activity.
     */
    protected void returnLauncher() {
        // Clean up map resources
        GameMap current = MapManager.getCurrentMap();
        if (current != null) {
            current.removePlayer();
            game.restartGame();
        }

        // Finalize state and switch activity
        game.stopGameLoop();
        context.startActivity(intent);
        finishGameActivity();
    }

    public void onEnter() {}
    public void onExit() {}
}
