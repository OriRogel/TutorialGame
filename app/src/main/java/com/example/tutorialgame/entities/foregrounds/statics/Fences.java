package com.example.tutorialgame.entities.foregrounds.statics;

import android.graphics.Bitmap;
import com.example.tutorialgame.R;
import com.example.tutorialgame.engine.core.GameConstants;
import com.example.tutorialgame.engine.interfaces.StaticObjectData;
import com.example.tutorialgame.managers.BitmapManager;

public enum Fences implements StaticObjectData {
    PILLAR_SMALL(144, 148, 12, 31, 10, 28),
    PILLAR_BIG(110, 1, 48, 79, 44, 68),
    PILLAR_BIG_CORNER(54, 105, 48, 47, 15, 38),
    FENCE_FRONT(159, 147, 48, 31, 10, 20),
    MAINGATE_CLOSE(159, 102, 48, 43, 12, 30),
    WALL_FRONT(159, 6, 48, 43, 12, 35),
    WALL_SIDE(29, 106, 16, 63, 15, 53),
    TOP_LEFT(36, 39, 9, 9, 9, 0),
    TOP_RIGHT(30, 55, 9, 9, 3, 6),
    TOP_MIDDLE(98, 40, 7, 8, 3, 6);

    final Bitmap fenceImg;
    final int width, height;
    final int hitboxRoof, hitboxFloor, hitboxHeight;

    Fences(int x, int y, int width, int height, int hitboxRoof, int hitboxFloor) {
        this.width = width;
        this.height = height;
        this.hitboxRoof = hitboxRoof * GameConstants.Sprite.SCALE_MULTIPLIER;
        this.hitboxFloor = hitboxFloor * GameConstants.Sprite.SCALE_MULTIPLIER;
        this.hitboxHeight = (hitboxFloor - hitboxRoof) * GameConstants.Sprite.SCALE_MULTIPLIER;

        fenceImg = BitmapManager.getBitmapRegion(R.drawable.atl_fences, x, y, width, height, 1.0, false);
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
        return fenceImg;
    }

    @Override
    public int getHitboxRoof() {
        return hitboxRoof;
    }
}
