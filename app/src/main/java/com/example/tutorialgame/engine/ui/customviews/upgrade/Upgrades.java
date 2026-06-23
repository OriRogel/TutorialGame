package com.example.tutorialgame.engine.ui.customviews.upgrade;

import android.graphics.Bitmap;
import androidx.annotation.StringRes;
import com.example.tutorialgame.R;
import com.example.tutorialgame.cloud.UserRepository;
import com.example.tutorialgame.cloud.document.StatsDoc;
import com.example.tutorialgame.managers.BitmapManager;
import com.example.tutorialgame.ui.base.BaseActivity;

public enum Upgrades {
    HEALTH(R.drawable.upgrade_health, StatsDoc.F_MAX_HEALTH, 25, StatsDoc.INIT_HEALTH, R.string.health, R.string.health_desc),
    STRENGTH(R.drawable.upgrade_strength, StatsDoc.F_STRENGTH, 5, StatsDoc.INIT_STRENGTH, R.string.strength, R.string.strength_desc),
    STAMINA(R.drawable.upgrade_stamina, StatsDoc.F_MAX_STAMINA, 10, StatsDoc.INIT_STAMINA, R.string.stamina, R.string.stamina_desc),
    CRIT_CHANCE(R.drawable.upgrade_crit, StatsDoc.F_CRIT_HIT_CHANCE, 1, StatsDoc.INIT_CRIT_HIT_CHANCE, R.string.crit_hit_chance, R.string.crit_hit_desc),
    ATTACK_SPEED(R.drawable.upgrade_attack_speed, StatsDoc.F_ATTACK_SPEED, 5, StatsDoc.INIT_ATTACK_SPEED, R.string.attack_speed, R.string.attack_speed_desc);

    private final Bitmap upgradeImg;
    private final String statField;
    private final int upgradeValue, stringID, descriptionID;
    private final double defaultValue;


    Upgrades(int resID, String statField, int upgradeValue, double defaultValue, @StringRes int stringId, @StringRes int descriptionId) {
        upgradeImg = BitmapManager.getBitmap(resID, 31.6/24, false);
        this.statField = statField;
        this.upgradeValue = upgradeValue;
        this.defaultValue = defaultValue;
        this.stringID = stringId;
        this.descriptionID = descriptionId;
    }

    public Bitmap getUpgradeImg() {
        return upgradeImg;
    }

    public String getStatField() {
        return statField;
    }

    public int getUpgradeValue() {
        return upgradeValue;
    }

    public int getCurrentStatValue(UserRepository userRepository) {
        switch (statField) {
            case StatsDoc.F_MAX_HEALTH:
                return userRepository.getPlayerStats().getMaxHealth();
            case StatsDoc.F_ATTACK_SPEED:
                return userRepository.getPlayerStats().getAttackSpeed();
            case StatsDoc.F_STRENGTH:
                return userRepository.getPlayerStats().getStrength();
            case StatsDoc.F_MAX_STAMINA:
                return userRepository.getPlayerStats().getMaxStamina();
            case StatsDoc.F_CRIT_HIT_CHANCE:
                return userRepository.getPlayerStats().getCritHitChance();
            default:
                return 0;
        }
    }

    public double getDefaultValue() {
        return defaultValue;
    }

    public String getText() {
        return BaseActivity.getContext().getString(stringID);
    }

    public String getDescription() {
        return BaseActivity.getContext().getString(descriptionID);
    }
}