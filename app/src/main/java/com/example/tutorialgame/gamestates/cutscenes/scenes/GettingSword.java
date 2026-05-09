package com.example.tutorialgame.gamestates.cutscenes.scenes;

import com.example.tutorialgame.engine.core.Game;
import com.example.tutorialgame.gamestates.cutscenes.BaseScene;
import com.example.tutorialgame.gamestates.cutscenes.SceneManager;
import com.example.tutorialgame.gamestates.cutscenes.Scenes;

public class GettingSword extends BaseScene {

    /**
     * קונסטרוקטור עבור סצנת קבלת החרב.
     * כל הלוגיקה המורכבת של הצגה, דילוג וניהול זמן
     * מגיעה בירושה מ-BaseScene.
     * @param game מופע של ה-Game.
     * @param sceneManager מנהל הסצנות שקרא ליצירת הסצנה הזו.
     */
    public GettingSword(Game game, SceneManager sceneManager) {
        // קורא לקונסטרוקטור של מחלקת האב ומעביר לו את המידע
        // הספציפי לסצנה הזו, שנלקח מה-enum Scenes.
        super(game, sceneManager, Scenes.GETTING_SWORD);
    }
}

