package com.example.tutorialgame.gamestates.cutscenes.scenes;

import com.example.tutorialgame.engine.audio.MusicManager;
import com.example.tutorialgame.engine.core.Game;
import com.example.tutorialgame.gamestates.cutscenes.BaseScene;
import com.example.tutorialgame.gamestates.cutscenes.SceneManager;
import com.example.tutorialgame.gamestates.cutscenes.Scenes;
import com.example.tutorialgame.R;
import com.example.tutorialgame.managers.WorldEventManager;

public class SkeletonArise extends BaseScene {

    public SkeletonArise(Game game, SceneManager sceneManager) {
        super(game, sceneManager, Scenes.SKELETON_ARISE);
    }

    @Override
    public void onExit() {
        super.onExit();
        WorldEventManager.triggerEvent("RUNWAY");
    }
}
