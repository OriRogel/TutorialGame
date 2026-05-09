package com.example.tutorialgame.engine.ui.effects.weathereffects.leaves;

import android.graphics.Bitmap;

import com.example.tutorialgame.R;
import com.example.tutorialgame.managers.BitmapManager;

public enum Leaves {
    LEAF_GREEN(R.drawable.particle_leaf_green),
    LEAF_PINK(R.drawable.particle_leaf_pink);

    private final Bitmap[] sprites;
    Leaves(int resID) {
        sprites = BitmapManager.getSpritesheet(resID, 12, 6, 6, 1, false);
    }

    public Bitmap getSprite(int xPos) {
        return sprites[xPos];
    }
}
