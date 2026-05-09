package com.example.tutorialgame.entities.foregrounds.statics;

import android.graphics.Bitmap;
import com.example.tutorialgame.R;
import com.example.tutorialgame.engine.core.GameConstants;
import com.example.tutorialgame.engine.interfaces.StaticObjectData;
import com.example.tutorialgame.managers.BitmapManager;

public enum Natures implements StaticObjectData {
    DEAD_TREE(64, 0, 32, 31, 23, 27),
    DARK_TREE(32, 0, 32, 31, 25, 28);

    final Bitmap natureImg;
    final int width, height;
    final int hitboxRoof, hitboxFloor, hitboxHeight;
    Natures(int x, int y, int width, int height, int hitboxRoof, int hitboxFloor) {
        this.width = width;
        this.height = height;
        this.hitboxRoof = hitboxRoof * GameConstants.Sprite.SCALE_MULTIPLIER;
        this.hitboxFloor = hitboxFloor * GameConstants.Sprite.SCALE_MULTIPLIER;
        this.hitboxHeight = (hitboxFloor - hitboxRoof) * GameConstants.Sprite.SCALE_MULTIPLIER;

        natureImg = BitmapManager.getBitmapRegion(R.drawable.atl_nature, x, y, width, height, 1.0, false);
    }

    @Override
    public int getHitboxHeight() {
        return hitboxHeight;
    }

    @Override
    public int getHitboxWidth() {
        return width * GameConstants.Sprite.SCALE_MULTIPLIER;
    }

    @Override
    public Bitmap getBitmap() {
        return natureImg;
    }

    @Override
    public int getHitboxRoof() {
        return hitboxRoof;
    }

}
