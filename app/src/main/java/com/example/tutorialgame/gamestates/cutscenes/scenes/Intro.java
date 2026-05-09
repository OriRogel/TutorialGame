package com.example.tutorialgame.gamestates.cutscenes.scenes;

import com.example.tutorialgame.engine.core.Game;
import com.example.tutorialgame.gamestates.cutscenes.BaseScene;
import com.example.tutorialgame.gamestates.cutscenes.SceneManager;
import com.example.tutorialgame.gamestates.cutscenes.Scenes;

// 1. לגרום ל-Intro לרשת מ-BaseScene
public class Intro extends BaseScene {

    // 2. להגדיר קונסטרוקטור שקורא לקונסטרוקטור של מחלקת האב
    public Intro(Game game, SceneManager sceneManager) {
        // העבר את המידע הספציפי לסצנת ה-INTRO
        super(game, sceneManager, Scenes.INTRO);
    }

    @Override
    public void onEnter() {
        super.onEnter();
        checkCheckPoint();
    }
}
