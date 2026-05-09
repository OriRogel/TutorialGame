package com.example.tutorialgame.engine.ui.customviews.buttons.circles;

import android.graphics.Bitmap;
import com.example.tutorialgame.R;
import com.example.tutorialgame.engine.ui.customviews.buttons.BaseButton;
import com.example.tutorialgame.managers.BitmapManager;

public enum CircleImages {
    JUMP(R.drawable.btn_jump_normal, R.drawable.btn_jump_pressed, R.drawable.btn_jumb_disabled, BaseButton.PressType.ON_DOWN),
    ATTACK(R.drawable.btn_attack_normal, R.drawable.btn_attack_pressed, R.drawable.btn_attack_disabled, BaseButton.PressType.ON_DOWN),
    UPGRADE(R.drawable.btn_upgrade_normal, R.drawable.btn_upgrade_pressed, R.drawable.btn_upgrade_disabled, BaseButton.PressType.ON_UP),
    DOWNGRADE(R.drawable.btn_downgrade_normal, R.drawable.btn_downgrade_pressed, R.drawable.btn_downgrade_disabled, BaseButton.PressType.ON_UP),
    APPLY(R.drawable.btn_apply_normal, R.drawable.btn_apply_pressed, R.drawable.btn_apply_disabled, BaseButton.PressType.ON_UP),
    RADIO(R.drawable.radio_normal, R.drawable.radio_pressed, R.drawable.radio_disabled, BaseButton.PressType.ON_UP),
    PAUSE(R.drawable.btn_pause_normal, R.drawable.btn_pause_pressed, -1, BaseButton.PressType.ON_UP),
    NEXT(R.drawable.btn_next_normal, R.drawable.btn_next_pressed, -1 , BaseButton.PressType.ON_UP),
    SPEAK(R.drawable.btn_speak_normal, R.drawable.btn_speak_pressed, -1, BaseButton.PressType.ON_UP);
    private final Bitmap normal, pressed, disabled;
    private final BaseButton.PressType pressType;

    CircleImages(int normal, int pressed, int disabled, BaseButton.PressType pressType) {
        this.normal = BitmapManager.getBitmap(normal);
        this.pressed = BitmapManager.getBitmap(pressed);

        if (disabled != -1)
            this.disabled = BitmapManager.getBitmap(disabled);
        else this.disabled = null;

        this.pressType = pressType;
    }

    public Bitmap getNormal() {
        return normal;
    }

    public Bitmap getPressed() {
        return pressed;
    }

    public Bitmap getDisabled() {
        return disabled;
    }

    public int getWidth() {
        return normal.getWidth();
    }

    public int getHeight() {
        return normal.getHeight();
    }

    public BaseButton.PressType getPressType() {
        return pressType;
    }
}
