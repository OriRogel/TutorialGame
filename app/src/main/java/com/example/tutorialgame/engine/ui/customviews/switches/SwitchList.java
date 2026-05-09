package com.example.tutorialgame.engine.ui.customviews.switches;

import android.graphics.Bitmap;
import androidx.annotation.StringRes;
import com.example.tutorialgame.R;
import com.example.tutorialgame.managers.BitmapManager;
import com.example.tutorialgame.ui.base.BaseActivity;

public enum SwitchList {
    HAPTICS(R.string.haptics, "haptics", true),
    HITBOX(R.string.hitbox, "hitbox", false),
    UI(R.string.ui, "ui", true),
    FPS(R.string.fps, "fps", false),
    CAM_SHAKE(R.string.cam_shake, "cam_shake", true);

    private static final Bitmap checked, unChecked;
    private final String key;
    private final boolean defValue;
    private final @StringRes int name;

    static {
        checked = BitmapManager.getBitmap(R.drawable.sw_checked);
        unChecked = BitmapManager.getBitmap(R.drawable.sw_unchecked);
    }
    SwitchList(@StringRes int name, String key, boolean defValue) {
        this.key = key;
        this.name = name;
        this.defValue = defValue;
    }

    public String getKey() {
        return key;
    }
    public String getName() {
        return BaseActivity.getContext().getString(name);
    }
    public boolean getDefValue() {
        return defValue;
    }
    public Bitmap getChecked() {
        return checked;
    }
    public Bitmap getUnChecked() {
        return unChecked;
    }
    public float getWidth() {
        return checked.getWidth();
    }
    public float getHeight() {
        return checked.getHeight();
    }
}
