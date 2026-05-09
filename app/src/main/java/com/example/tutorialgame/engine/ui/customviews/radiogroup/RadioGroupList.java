package com.example.tutorialgame.engine.ui.customviews.radiogroup;

import androidx.annotation.StringRes;
import com.example.tutorialgame.R;
import com.example.tutorialgame.ui.base.BaseActivity;

public enum RadioGroupList {
    TAP_EFFECT(R.string.tap_effect,
            new int[]{R.string.white_circle,
            R.string.orange_circle,
            R.string.spark,
            R.string.spark2},
            "tapEffect");

    private final @StringRes int titleID;
    private final @StringRes int[] namesID;
    private final String key;

    RadioGroupList(@StringRes int titleID, @StringRes int[] namesID, String key) {
        this.titleID = titleID;
        this.namesID = namesID;
        this.key = key;
    }

    public String getKey() {
        return key;
    }
    public String getName(int i) {
        return BaseActivity.getContext().getString(namesID[i]);
    }
    public String getTitle() {
        return BaseActivity.getContext().getString(titleID);
    }

    public int[] getNamesID() {
        return namesID;
    }
}
