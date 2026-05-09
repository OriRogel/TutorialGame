package com.example.tutorialgame.components;

/**
 * Manages the state of an animation sequence for a game object.
 * Tracks the current frame and updates it over time based on speed.
 */
public class AnimationComponent {
    private int aniTick, aniIndex;
    private int frameCount;
    private int speed;

    public AnimationComponent(int speed, int frameCount) {
        this.speed = speed;
        this.frameCount = frameCount;
    }

    /**
     * Updates the animation state.
     * @return true if a full animation cycle has completed in this tick.
     */
    public boolean update() {
        aniTick++;
        if (aniTick >= speed) {
            aniTick = 0;
            aniIndex++;
            if (aniIndex >= frameCount) {
                aniIndex = 0;
                return true; // Cycle completed!
            }
        }
        return false;
    }

    public int getAniIndex() {
        return aniIndex;
    }
    
    public void setAniIndex(int aniIndex) {
        this.aniIndex = aniIndex;
    }

    public void resetAnimation() {
        aniTick = 0;
        aniIndex = 0;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getSpeed() {
        return speed;
    }

    public void setFrameCount(int frameCount) {
        this.frameCount = frameCount;
    }
}
