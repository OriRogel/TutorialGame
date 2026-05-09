package com.example.tutorialgame.entities.foregrounds.animated;

import android.graphics.Bitmap;
import com.example.tutorialgame.R;
import com.example.tutorialgame.engine.core.GameConstants;
import com.example.tutorialgame.managers.BitmapManager;

public enum AnimatedType {
    // השמות כאן חייבים להתאים למה שתכתוב ב-Type ב-Tiled (לא משנה Capitalization אם נשתמש ב-toUpperCase)
    TORCH_FRONT(R.drawable.spr_torch_front, 4, 6, 8, 13, false),
    TORCH_SIDE_LEFT(R.drawable.spr_torch_side_left, 4, 6, 6, 13, false),
    TORCH_SIDE_RIGHT(R.drawable.spr_torch_side_right, 4, 6, 6, 13, false),
    COIN(R.drawable.spr_coin, 4, 5, 10, 10, false);

    private final Bitmap[] sprites;
    private final int speed;
    private final float width, height;
    private final boolean collision;

    AnimatedType(int resId, int frameCount, int speed, int width, int height, boolean collision) {
        this.sprites = BitmapManager.getSpritesheet(resId, width, height, frameCount, 1.0, false);
        this.speed = speed;
        this.width = width * GameConstants.Sprite.SCALE_MULTIPLIER;
        this.height = height * GameConstants.Sprite.SCALE_MULTIPLIER;
        this.collision = collision;
    }

    public static AnimatedType fromString(String type) {
        try {
            return valueOf(type.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }

    public Bitmap[] getSprites() { return sprites; }
    public int getSpeed() { return speed; }
    public float getWidth() { return width; }
    public float getHeight() { return height; }
    public boolean isCollision() { return collision; }
}