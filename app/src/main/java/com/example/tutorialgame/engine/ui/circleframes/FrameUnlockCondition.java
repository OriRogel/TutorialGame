package com.example.tutorialgame.engine.ui.circleframes;

import com.example.tutorialgame.cloud.UserRepository;

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

    public int getCurrentAmount(UserRepository userRepository) {
        switch (selectedCondition) {
            case DAYS: return userRepository.getProgress().getDaysLoggedIn();
            case LEVEL: return userRepository.getProgress().getLevel();
            case ENEMIES_DEFEATED: return userRepository.getProgress().getEnemiesDefeated();
            case PURCHASE: return userRepository.getCosmetic().getCoinsLeft();
            default: return -99999999;
        }
    }
}
