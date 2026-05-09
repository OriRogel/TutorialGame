package com.example.tutorialgame.entities.foregrounds.statics;

import android.graphics.Bitmap;
import com.example.tutorialgame.R;
import com.example.tutorialgame.engine.core.GameConstants;
import com.example.tutorialgame.engine.interfaces.StaticObjectData;
import com.example.tutorialgame.managers.BitmapManager;

public enum GameObjects implements StaticObjectData {

    PILLAR_YELLOW(0, 6, 16, 42, 32, 38),
    STATUE_ANGRY_YELLOW(16, 1, 32, 47, 24, 35),
    MONK_STATUE_BALL_YELLOW(49, 2, 30, 30, 16, 26),
    MONK_STATUE_YELLOW(81, 2, 30, 30, 16, 26),
    SOLDIER_SPEAR_YELLOW(112, 1, 16, 31, 23, 28),
    PLANTER_STICKS_YELLOW(128, 11, 16, 20, 12, 17),
    CUBE_YELLOW(32, 48, 16, 16, 3, 13),
    FROG_YELLOW(48, 38, 32, 26, 16, 24),
    SOLDIER_SWORD_YELLOW(81, 32, 31, 32, 20, 29),
    PILLAR_SHORT_YELLOW(112, 32, 16, 32, 21, 30),
    PILLAR_SNOW_YELLOW(128, 32, 16, 32, 21, 30),
    PILLAR_GREEN(0, 70, 16, 42, 32, 38),
    STATUE_ANGRY_GREEN(16, 65, 32, 47, 24, 35),
    MONK_STATUE_BALL_GREEN(49, 66, 30, 30, 16, 26),
    MONK_STATUE_GREEN(81, 66, 30, 30, 16, 26),
    SOLDIER_SPEAR_GREEN(112, 65, 16, 31, 23, 28),
    PLANTER_STICKS_GREEN(128, 75, 16, 20, 12, 17),
    CUBE_GREEN(32, 112, 16, 16, 3, 13),
    FROG_GREEN(48, 102, 32, 26, 16, 24),
    SOLDIER_SWORD_GREEN(81, 96, 31, 32, 20, 29),
    PILLAR_SHORT_GREEN(112, 96, 16, 32, 21, 30),
    PILLAR_SNOW_GREEN(128, 96, 16, 32, 21, 30),
    POT_ONE_FULL(144, 0, 16, 19, 10, 17),
    POT_ONE_EMPTY(160, 0, 16, 19, 10, 17),
    POT_TWO_FULL(144, 19, 16, 21, 12, 19),
    POT_TWO_EMPTY(160, 20, 16, 20, 12, 19),
    BASKET_FULL_RED_FRUIT(144, 40, 16, 16, 5, 14),
    BASKET_FULL_CHICKEN(160, 40, 16, 16, 5, 14),
    BASKET_EMPTY(144, 56, 16, 16, 5, 14),
    BASKET_FULL_BREAD(160, 56, 16, 16, 5, 14),
    OVEN_SNOW_YELLOW(144, 72, 28, 39, 20, 35),
    OVEN_YELLOW(0, 129, 28, 28, 10, 24),
    OVEN_GREEN(28, 128, 30, 29, 10, 24),
    STOMP(58, 128, 16, 22, 10, 18),
    SMALL_POT_FULL(0, 112, 16, 13, 4, 10),
    SMALL_POT_EMPTY(16, 12, 16, 13, 4, 10),
    WEAPON_BENCH_1(74, 128, 32, 21, 4, 13),
    WEAPON_BENCH_2(105, 128, 32, 21, 4, 13),
    ANVIL(176, 26, 16, 14, 4, 11),
    BENCH_FRONT(144,111,48,16,2,11),
    BENCH_SIDE(0,1,1,2,0,1),
    WEAPONS_BASKET(176,0,16,26,10,20),
    WORK_DESK(0,1,1,2,0,1);

    private final Bitmap objectImg;
    private final int width;
    private final int hitboxRoof;
    private final int hitboxHeight;

    GameObjects(int x, int y, int width, int height, int hitboxRoof, int hitboxFloor) {
        this.width = width;
        this.hitboxRoof = hitboxRoof * GameConstants.Sprite.SCALE_MULTIPLIER;
        this.hitboxHeight = (hitboxFloor - hitboxRoof) * GameConstants.Sprite.SCALE_MULTIPLIER;

        objectImg = BitmapManager.getBitmapRegion(R.drawable.atl_world_objects, x, y, width, height, 1.0, false);
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
        return objectImg;
    }
    @Override
    public int getHitboxRoof() {
        return hitboxRoof;
    }
}
