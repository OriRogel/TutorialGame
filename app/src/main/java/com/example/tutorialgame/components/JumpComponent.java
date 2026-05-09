package com.example.tutorialgame.components;

import static com.example.tutorialgame.engine.core.GameConstants.Sprite.TILE_SIZE;

/**
 * Manages vertical movement (elevation) logic for game entities.
 * Simulates gravity and initial velocity to create jumping or hovering effects.
 */
public class JumpComponent {
    // ממשק המאזין - ה"חוזה"
    public interface OnLandListener {
        void onLand();
    }

    private float elevation;
    private float vz;  // Vertical velocity in pixels/second
    private float gravity;
    private float jumpVelocity;
    private float maxElevation;
    private long jumpStartTime;
    
    private OnLandListener landListener; // הרפרנס למאזין

    public JumpComponent(float desiredTiles, float timeToApexSec) {
        initFromTiles(desiredTiles, timeToApexSec);
    }

    private void initFromTiles(float desiredTiles, float timeToApexSec) {
        float h = desiredTiles * TILE_SIZE;
        float g = 2f * h / (timeToApexSec * timeToApexSec);
        float v = 2f * h / timeToApexSec;

        this.gravity = -g;
        this.jumpVelocity = v;
        this.maxElevation = h;
    }

    /**
     * Updates the elevation based on current velocity and gravity.
     */
    public void update(double deltaSeconds) {
        if (!isAirborne()) return;

        float prevElevation = elevation; // שומרים את הגובה הקודם
        vz += gravity * (float) deltaSeconds;
        elevation += vz * (float) deltaSeconds;
        
        // זיהוי רגע הנחיתה המדויק: הייתי מעל 0 ועכשיו אני ב-0 או מתחת
        if (elevation <= 0f) {
            elevation = 0f;
            vz = 0f;

            // אם היה גובה בפריים הקודם והיום נחתתי - תפעיל את המאזין
            if (prevElevation > 0f && landListener != null) {
                landListener.onLand();
            }
        }
    }

    public void setOnLandListener(OnLandListener listener) {
        this.landListener = listener;
    }

    /**
     * Initiates a jump if the entity is currently on the ground.
     */
    public void startJump() {
        if (elevation <= 0f) {
            this.vz = jumpVelocity;
            this.elevation = 0f;
            jumpStartTime = System.currentTimeMillis();
        }
    }

    public float getElevation()  {
        return elevation;
    }

    public float getJumpFraction() {
        if (maxElevation <= 0f) return 0f;
        float perc = elevation / maxElevation;
        return Math.max(0f, Math.min(1f, perc));
    }

    public boolean isAirborne() {
        return elevation > 0f || vz != 0f;
    }

    public long getJumpStartTime() {
        return jumpStartTime;
    }

    public void setGravity(float gravity) {
        this.gravity = gravity;
    }

    public float getGravity() {
        return gravity;
    }

    public void setJumpVelocity(float jumpVelocity) {
        this.jumpVelocity = jumpVelocity;
    }

    public float getJumpVelocity() {
        return jumpVelocity;
    }
}
