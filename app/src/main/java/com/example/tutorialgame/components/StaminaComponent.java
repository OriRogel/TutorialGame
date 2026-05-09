package com.example.tutorialgame.components;

/**
 * Manages character stamina, including consumption, automatic recovery, and exhaustion states.
 * Uses a delta-time based simulation for smooth and consistent behavior.
 */
public class StaminaComponent {
    private int maxStamina;
    private float currentStamina; 
    private int prevStamina;

    private float cooldownTimer;
    private final float cooldownDuration; // In seconds
    private boolean staminaLock;

    private float flashAlpha = 0f;
    private static final float FLASH_DECREASE_DURATION = 0.3f;
    private static final float REGEN_RATE = 15f; // Stamina units per second

    public StaminaComponent(int startStamina, int coolDownTimeMs) {
        this.maxStamina = startStamina;
        this.currentStamina = startStamina;
        this.cooldownDuration = coolDownTimeMs / 1000f; // Convert MS to Seconds
        this.cooldownTimer = cooldownDuration;
    }

    /**
     * Consumes a specified amount of stamina if not locked.
     * @param amount The amount to consume.
     */
    public void useStamina(int amount) {
        if (amount <= 0 || staminaLock) return;

        this.prevStamina = (int) this.currentStamina;
        this.currentStamina = Math.max(0, this.currentStamina - amount);

        this.flashAlpha = 1f;
        this.cooldownTimer = 0f; // Reset cooldown timer on use
    }

    /**
     * Restores a specified amount of stamina.
     * @param amount The amount to restore.
     */
    public void restoreStamina(int amount) {
        if (amount <= 0) return;
        this.currentStamina = Math.min(maxStamina, currentStamina + amount);
        
        // If we were exhausted, check if we can unlock
        if (staminaLock && currentStamina >= maxStamina * 0.5f) {
            staminaLock = false;
        }
    }

    /**
     * Updates the component state, handling regeneration, cooldowns, and visual effects.
     */
    public void update(double delta) {
        // 1. Manage Stamina Lock (Exhaustion Logic)
        if (currentStamina <= 0) {
            staminaLock = true;
        } else if (staminaLock && currentStamina >= maxStamina * 0.5f) {
            // Unlock only when recovered to 50%
            staminaLock = false;
        }

        // 2. Handle Visual Flash Decay
        if (flashAlpha > 0f) {
            flashAlpha -= (float) (delta / FLASH_DECREASE_DURATION);
            if (flashAlpha < 0f) flashAlpha = 0f;
        }
        
        // 3. Handle Cooldown and Regeneration
        if (currentStamina < maxStamina) {
            cooldownTimer += (float) delta;
            if (cooldownTimer >= cooldownDuration) {
                this.currentStamina = Math.min(maxStamina, currentStamina + (float)(REGEN_RATE * delta));
            }
        }
    }

    public void reset() {
        this.currentStamina = maxStamina;
        this.staminaLock = false;
        this.cooldownTimer = cooldownDuration;
        this.flashAlpha = 0f;
    }

    public boolean hasEnough(int amount) {
        return currentStamina >= amount && !staminaLock;
    }

    public int getMaxStamina() { return maxStamina; }
    public int getCurrentStamina() { return (int) currentStamina; }
    public int getPrevStamina() { return prevStamina; }
    public float getFlashAlpha() { return flashAlpha; }
    public boolean isStaminaLocked() { return staminaLock; }

    public void setMaxStamina(int maxStamina) { 
        this.maxStamina = maxStamina; 
        if (currentStamina > maxStamina) currentStamina = maxStamina;
    }
}
