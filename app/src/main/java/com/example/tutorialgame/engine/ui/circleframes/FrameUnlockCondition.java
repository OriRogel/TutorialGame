package com.example.tutorialgame.engine.ui.circleframes;

import com.example.tutorialgame.MyApp;
import java.util.List;

public class FrameUnlockCondition {
    public enum Condition {
        PURCHASE, DAYS, ENEMIES_DEFEATED, LEVEL, BOSSES_DEFEATED
    }
    private final List<Integer> priceArr;
    private final Condition selectedCondition;

    public FrameUnlockCondition(Condition selectedCondition, List<Integer> arr) {
        this.selectedCondition = selectedCondition;
        priceArr = arr;
    }

    public Condition getSelectedCondition() {
        return selectedCondition;
    }

    public List<Integer> getPriceArr() {
        return priceArr;
    }

    public int getCurrentAmount() {
        switch (selectedCondition) {
            case DAYS: return MyApp.getProgress().getDaysLoggedIn();
            case LEVEL: return MyApp.getProgress().getLevel();
            case ENEMIES_DEFEATED: return MyApp.getProgress().getEnemiesDefeated();
            case PURCHASE: return MyApp.getCosmetic().getCoinsLeft();
            default: return -99999999;
        }
    }
}
