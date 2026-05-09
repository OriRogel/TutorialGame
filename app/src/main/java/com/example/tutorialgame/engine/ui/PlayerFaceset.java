package com.example.tutorialgame.engine.ui;

import android.graphics.Bitmap;
import com.example.tutorialgame.R;
import com.example.tutorialgame.managers.BitmapManager;

public enum PlayerFaceset {
    IDLE(0),
    HURT(1),
    EXHAUSTED(2);
    private final Bitmap face;

    PlayerFaceset(int xPos) {
        int height = 29;
        int width = 31;
        face = BitmapManager.getBitmapRegion(R.drawable.atl_player_faceset, xPos * width, 0, width, height, 1, false);
    }

    public Bitmap getFace() {
        return face;
    }
}
