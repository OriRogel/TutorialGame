package com.example.tutorialgame.engine.ui.customviews.upgrade;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.StringRes;

import com.example.tutorialgame.MyApp;
import com.example.tutorialgame.R;
import com.example.tutorialgame.engine.interfaces.BitmapMethods;
import com.example.tutorialgame.cloud.document.StatsDoc;
import com.example.tutorialgame.ui.base.BaseActivity;

public enum Upgrades implements BitmapMethods {
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
        options.inScaled = false;
        Bitmap rawImg = getScaledBitmap(BitmapFactory.decodeResource(BaseActivity.getContext().getResources(), resID, options));
        upgradeImg = getMultiplyBitmapClean(rawImg, 31.6f / rawImg.getWidth());
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

    public int getCurrentStatValue() {
        switch (statField) {
            case StatsDoc.F_MAX_HEALTH:
                return MyApp.getPlayerStats().getMaxHealth();
            case StatsDoc.F_ATTACK_SPEED:
                return MyApp.getPlayerStats().getAttackSpeed();
            case StatsDoc.F_STRENGTH:
                return MyApp.getPlayerStats().getStrength();
            case StatsDoc.F_MAX_STAMINA:
                return MyApp.getPlayerStats().getMaxStamina();
            case StatsDoc.F_CRIT_HIT_CHANCE:
                return MyApp.getPlayerStats().getCritHitChance();
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