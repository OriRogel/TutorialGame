package com.example.tutorialgame.gamestates;

/**
 * Defines all possible states of the game engine.
 * Each state contains configuration data for rendering and update behavior.
 */
public enum State {
    MENU(true, 0),
    PLAYING(false, 1),
    DEATH_SCREEN(true, 0),
    UPGRADE_STATE(true, 0.8),
    CUTSCENE(false, 1);

    private final boolean transparent;
    private final double backgroundUpdateSpeed;

    State(boolean transparent, double backgroundUpdateSpeed) {
        this.transparent = transparent;
        this.backgroundUpdateSpeed = backgroundUpdateSpeed;
    }

    /**
     * @return true if the game world should be rendered behind this state.
     */
    public boolean isTransparent() {
        return transparent;
    }

    /**
     * @return the speed multiplier for the game world update when this state is active.
     */
    public double getBackgroundUpdateSpeed() {
        return backgroundUpdateSpeed;
    }
}
