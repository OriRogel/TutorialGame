package com.example.tutorialgame.engine.ui;

import android.graphics.Bitmap;
import com.example.tutorialgame.R;
import com.example.tutorialgame.managers.BitmapManager;

public enum HealthIcons {
    HEART_FULL(0),
    HEART_3Q(1),
    HEART_HALF(2),
    HEART_1Q(3),
    HEART_EMPTY(4);

    private final Bitmap icon;

    HealthIcons(int xPos) {
        int widthHeight = 16;
        icon = BitmapManager.getBitmapRegion(R.drawable.atl_health_icons, xPos * widthHeight, 0, widthHeight, widthHeight, 1, false);
    }

    public Bitmap getIcon() {
        return icon;
    }

}
