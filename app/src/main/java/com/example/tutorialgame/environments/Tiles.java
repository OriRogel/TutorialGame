package com.example.tutorialgame.environments;

import android.graphics.Bitmap;
import com.example.tutorialgame.R;
import com.example.tutorialgame.engine.core.GameConstants;
import com.example.tutorialgame.managers.BitmapManager;

public enum Tiles {
    OUTSIDE(R.drawable.spr_tilesetfloor, 22, 26),
    INSIDE(R.drawable.spr_floor_inside, 22, 17),
    WALLS(R.drawable.spr_walls, 16, 20);

    private final Bitmap[] sprites;

    Tiles(int resID, int tilesInWidth, int tilesInHeight) {
        this.sprites = BitmapManager.getSpritesheetFlattened(
                resID,
                GameConstants.Sprite.DEFAULT_SIZE,
                GameConstants.Sprite.DEFAULT_SIZE,
                tilesInHeight,
                tilesInWidth,
                1.0,
                false
        );
    }

    public Bitmap getSprite(int id) {
        return sprites[id];
    }
}
