package com.example.tutorialgame.environments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.example.tutorialgame.R;
import com.example.tutorialgame.engine.core.GameConstants;
import com.example.tutorialgame.engine.interfaces.BitmapMethods;
import com.example.tutorialgame.ui.base.BaseActivity;

public enum Tiles implements BitmapMethods {
    OUTSIDE(R.drawable.spr_tilesetfloor, 22, 26),
    INSIDE(R.drawable.spr_floor_inside, 22, 17),
    WALLS(R.drawable.spr_walls, 16, 20);
    private final Bitmap[] sprites;

    Tiles(int resID, int tilesInWidth, int tilesInHeight) {
        options.inScaled = false;
        sprites = new Bitmap[tilesInHeight * tilesInWidth];
        Bitmap spriteSheet = BitmapFactory.decodeResource(BaseActivity.getContext().getResources(), resID, options);
        for (int j = 0; j < tilesInHeight; j++)
            for (int i = 0; i < tilesInWidth; i++) {
                int index = j * tilesInWidth + i;
                sprites[index] = getScaledBitmap(Bitmap.createBitmap(spriteSheet, GameConstants.Sprite.DEFAULT_SIZE * i, GameConstants.Sprite.DEFAULT_SIZE * j, GameConstants.Sprite.DEFAULT_SIZE, GameConstants.Sprite.DEFAULT_SIZE));
            }
    }

    public Bitmap getSprite(int id) {
        return sprites[id];
    }
}
