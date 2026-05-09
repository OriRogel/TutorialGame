package com.example.tutorialgame.components;

/**
 * Manages health, damage, and healing logic for game entities.
 * Includes visual feedback logic (flash effect).
 */
public class HealthComponent {
    private int maxHealth;
    private int currentHealth;
    private int prevHealth;
    private int lastFlashHeartIndex = -1;

    // Visual feedback (flash effect)
    private float flashAlpha = 0f;
    private boolean isCriticalFlash = false;
    private long flashStartTime = 0;

    // משך האפקט למכה קריטית (ארוך יותר) ומכה רגילה (קצר מאוד או ללא)
    private static final float CRIT_FLASH_DURATION_SEC = 0.3f;
    private static final float NORMAL_FLASH_DURATION_SEC = 0.15f;
    private float currentFlashDuration = NORMAL_FLASH_DURATION_SEC;

    public HealthComponent(int startHealth) {
        this.maxHealth = startHealth;
        this.currentHealth = startHealth;
    }

    /**
     * פונקציית עזר למכה רגילה.
     */
    public void takeDamage(int damage) {
        takeDamage(damage, false);
    }

    /**
     * מעדכן את הבריאות ומפעיל אפקט הבהוב במידת הצורך.
     * @param damage כמות הנזק.
     * @param isCritical האם המכה הייתה קריטית (מפעיל אפקט מיוחד).
     */
    public void takeDamage(int damage, boolean isCritical) {
        if (isDead() || damage <= 0) return;

        this.prevHealth = this.currentHealth;
        this.currentHealth = Math.max(0, this.currentHealth - damage);

        lastFlashHeartIndex = (prevHealth - 1) / 100;
        
        // הגדרת האפקט הויזואלי
        this.flashAlpha = 1f;
        this.isCriticalFlash = isCritical;
        this.flashStartTime = System.currentTimeMillis();
        this.currentFlashDuration = isCritical ? CRIT_FLASH_DURATION_SEC : NORMAL_FLASH_DURATION_SEC;
    }

    public void heal(int amount) {
        if (isDead() || amount <= 0) return;
        this.currentHealth = Math.min(maxHealth, this.currentHealth + amount);
    }

    public void update(double delta) {
        if (flashAlpha > 0f) {
            flashAlpha -= (float) (delta / currentFlashDuration);
            if (flashAlpha < 0f) {
                flashAlpha = 0f;
                isCriticalFlash = false;
            }
        }
    }

    public boolean isDead() {
        return currentHealth <= 0;
    }

    public void reset() {
        this.currentHealth = maxHealth;
        this.flashAlpha = 0f;
        this.isCriticalFlash = false;
        this.lastFlashHeartIndex = -1;
    }

    public int getMaxHealth() { return maxHealth; }
    public int getCurrentHealth() { return currentHealth; }
    public int getPrevHealth() { return prevHealth; }
    public int getLastFlashHeartIndex() { return lastFlashHeartIndex; }
    public float getFlashAlpha() { return flashAlpha; }
    public boolean isCriticalFlash() { return isCriticalFlash; }
    public long getFlashStartTime() { return flashStartTime; }

    public void setMaxHealth(int maxHealth) { 
        this.maxHealth = maxHealth;
        if (this.currentHealth > this.maxHealth) {
            this.currentHealth = this.maxHealth;
        }
    }

    public void setCurrentHealth(int newHealth) { 
        this.currentHealth = Math.min(maxHealth, Math.max(0, newHealth));
    }
}
