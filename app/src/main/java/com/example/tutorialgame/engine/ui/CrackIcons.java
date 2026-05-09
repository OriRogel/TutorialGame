package com.example.tutorialgame.engine.ui;

import android.graphics.Bitmap;
import com.example.tutorialgame.R;
import com.example.tutorialgame.managers.BitmapManager;

public enum CrackIcons {
    CRACKED_1(3),
    CRACKED_2(2),
    CRACKED_3(1),
    CRACKED_4(0);

    private final Bitmap icon;
    CrackIcons(int xPos) {
        int widthHeight = 16;
        icon = BitmapManager.getBitmapRegion(R.drawable.atl_heart_cracked, xPos * widthHeight, 0, widthHeight, widthHeight, 1, false);
    }

    public Bitmap getIcon() {
        return icon;
    }
}
