package com.example.tutorialgame.entities.foregrounds.statics;

import static com.example.tutorialgame.engine.core.GameConstants.Sprite.SCALE_MULTIPLIER;
import android.graphics.Bitmap;
import android.graphics.PointF;
import com.example.tutorialgame.R;
import com.example.tutorialgame.engine.interfaces.StaticObjectData;
import com.example.tutorialgame.managers.BitmapManager;

public enum Buildings implements StaticObjectData {
    HOUSE_ONE(0, 0, 64, 48, 23, 42, 12, 36),
    HOUSE_TWO(64, 4, 62, 44, 23, 37, 11, 31),
    HOUSE_THREE(128, 0, 64, 48, 23, 42, 12, 36),
    DOJO(192, 2, 64, 46, 23, 37, 12, 36),
    RESTAURANT(256, 0, 48, 48, 23, 42, 12, 36),
    BAKERY(368, 0, 48, 48, 23, 42, 21, 39),
    MAIL_STATION(0, 48, 64, 63, 23, 55, 30, 55),
    WEAPON_STORE(304, 0, 64, 48, 39, 42, 18, 37),
    CHIEF_HOUSE_TOP(111, 92, 64, 80, 40,40, 25, 60),
    CHIEF_HOUSE_BOTTOM(111, 172, 64, 32, 23, 28, -20, 22);


    final Bitmap houseImg;
    final PointF doorwayPoint;
    final int hitboxRoof, hitboxFloor, hitboxHeight, hitboxWidth;


    Buildings(int x, int y, int width, int height, int doorwayX, int doorwayY, int hitboxRoof, int hitboxFloor) {
        this.hitboxRoof = hitboxRoof * SCALE_MULTIPLIER;
        this.hitboxFloor = hitboxFloor * SCALE_MULTIPLIER;
        this.hitboxHeight = this.hitboxFloor - this.hitboxRoof;
        this.hitboxWidth = width * SCALE_MULTIPLIER;

        houseImg = BitmapManager.getBitmapRegion(R.drawable.atl_buildings, x, y, width, height, 1.0, false);
        doorwayPoint = new PointF(doorwayX * SCALE_MULTIPLIER, doorwayY * SCALE_MULTIPLIER);
    }

    public PointF getDoorwayPoint() {
        return doorwayPoint;
    }

    @Override
    public Bitmap getBitmap() {
        return houseImg;
    }

    @Override
    public int getHitboxWidth() {
        return hitboxWidth;
    }

    @Override
    public int getHitboxHeight() {
        return hitboxHeight;
    }

    @Override
    public int getHitboxRoof() {
        return hitboxRoof;
    }
}
