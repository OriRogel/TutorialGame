package com.example.tutorialgame.entities.foregrounds.statics;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;

import com.example.tutorialgame.engine.interfaces.StaticEntity;
import com.example.tutorialgame.engine.interfaces.StaticObjectData;
import com.example.tutorialgame.entities.Entity;

public class StaticObject extends Entity implements StaticEntity {

    private final StaticObjectData objectData;

    public StaticObject(PointF pos, StaticObjectData objectData, boolean isCollider) {
        super(new PointF(pos.x, pos.y + objectData.getHitboxRoof()),
                objectData.getHitboxWidth(),
                objectData.getHitboxHeight(),
                isCollider);
        this.objectData = objectData;
    }

    public StaticObjectData getObjectData() {
        return objectData;
    }

    @Override
    public Bitmap getBitmap() {
        return objectData.getBitmap();
    }

    @Override
    public float getDrawY() {
        return getHitBox().top - objectData.getHitboxRoof();
    }

    @Override
    public void draw(Canvas c) {
        c.drawBitmap(getBitmap(), hitBox.left, getDrawY(), null);
    }
}
