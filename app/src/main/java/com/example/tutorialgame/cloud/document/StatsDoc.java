package com.example.tutorialgame.cloud.document;

import androidx.annotation.NonNull;

import com.example.tutorialgame.MyApp;
import com.example.tutorialgame.cloud.BaseDocument;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the player's permanent upgraded statistics (maximum limits).
 */
public class StatsDoc extends BaseDocument {
    // Database Constants
    public static final String DOC_NAME = "stats";
    public static final String KEY_ROOT = DOC_NAME+".";

    // Field Names (for internal map access)
    public static final String F_MAX_HEALTH = "maxHealth";
    public static final String F_STRENGTH = "strength";
    public static final String F_ATTACK_SPEED = "attackSpeed";
    public static final String F_MAX_STAMINA = "maxStamina";
    public static final String F_CRIT_HIT_CHANCE = "critHitChance";


    // Default starting capacities
    public static final int INIT_HEALTH = 100;
    public static final int INIT_STRENGTH = 0;
    public static final int INIT_ATTACK_SPEED = 300;
    public static final int INIT_STAMINA = 50;
    public static final int INIT_CRIT_HIT_CHANCE = 0;

    private int maxHealth = INIT_HEALTH;
    private int strength = INIT_STRENGTH;
    private int attackSpeed = INIT_ATTACK_SPEED;
    private int maxStamina = INIT_STAMINA;
    private int critHitChance = INIT_CRIT_HIT_CHANCE;

    public StatsDoc(DocumentReference userRef, Runnable onFinishedLoading) {
        super(userRef, onFinishedLoading);
    }

    @Override
    protected void parseData(@NonNull Map<String, Object> data) {
        Object val;
        if ((val = data.get(F_MAX_HEALTH)) instanceof Number) this.maxHealth = ((Number) val).intValue();
        if ((val = data.get(F_STRENGTH)) instanceof Number) this.strength = ((Number) val).intValue();
        if ((val = data.get(F_ATTACK_SPEED)) instanceof Number) this.attackSpeed = ((Number) val).intValue();
        if ((val = data.get(F_MAX_STAMINA)) instanceof Number) this.maxStamina = ((Number) val).intValue();
        if ((val = data.get(F_CRIT_HIT_CHANCE)) instanceof Number) this.critHitChance = ((Number) val).intValue();
    }

    @NonNull
    @Override
    protected String getDocName() {
        return DOC_NAME;
    }

    /**
     * Initializes a new stats document with starting values.
     */
    public void createDefaults() {
        Map<String, Object> stats = new HashMap<>();
        stats.put(F_MAX_HEALTH, INIT_HEALTH);
        stats.put(F_STRENGTH, INIT_STRENGTH);
        stats.put(F_ATTACK_SPEED, INIT_ATTACK_SPEED);
        stats.put(F_MAX_STAMINA, INIT_STAMINA);
        stats.put(F_CRIT_HIT_CHANCE, INIT_CRIT_HIT_CHANCE);

        Map<String, Object> doc = new HashMap<>();
        doc.put(getDocName(), stats);
        docRef.set(doc, SetOptions.merge());
    }

    /**
     * Increments a specific stat and updates the cloud database atomically.
     * @param field The stat field to upgrade.
     * @param increment The total amount to add to the stat.
     * @param ignoredPrice Now ignored as points are managed in UpgradeState/ProgressDoc.
     * @param count The number of levels upgraded.
     */
    public void updateStat(String field, int increment, int ignoredPrice, int count) {
        switch (field) {
            case F_MAX_HEALTH:
                maxHealth += increment;
                docRef.update(KEY_ROOT + F_MAX_HEALTH, FieldValue.increment(increment));
                break;
            case F_STRENGTH:
                strength += increment;
                docRef.update(KEY_ROOT + F_STRENGTH, FieldValue.increment(increment));
                break;
            case F_ATTACK_SPEED:
                attackSpeed = Math.max(100, attackSpeed - increment);
                docRef.update(KEY_ROOT + F_ATTACK_SPEED, attackSpeed);
                break;
            case F_MAX_STAMINA:
                maxStamina += increment;
                docRef.update(KEY_ROOT + F_MAX_STAMINA, FieldValue.increment(increment));
                break;
            case F_CRIT_HIT_CHANCE:
                if (critHitChance >= 100) return;
                critHitChance += increment;
                docRef.update(KEY_ROOT + F_CRIT_HIT_CHANCE, FieldValue.increment(increment));
                break;
            default:
                throw new IllegalArgumentException("Unknown stat field: " + field);
        }
        MyApp.getProgress().addUpgradesDone(count);
    }

    public int getMaxHealth() { return maxHealth; }
    public int getStrength() { return strength; }
    public int getAttackSpeed() { return attackSpeed; }
    public int getMaxStamina() { return maxStamina; }
    public int getCritHitChance() { return critHitChance; }
}
