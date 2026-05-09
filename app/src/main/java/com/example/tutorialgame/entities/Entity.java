package com.example.tutorialgame.entities;

import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.RectF;

public abstract class Entity implements Comparable<Entity> {
    protected RectF hitBox;
    protected boolean active, collider;
    protected final PointF spawnPos;

    public Entity(PointF pos, float width, float height, boolean collider) {
        this.hitBox = new RectF(pos.x, pos.y, pos.x + width, pos.y + height);
        this.active = true;
        this.collider = collider;
        this.spawnPos = pos;
    }

    //---------Getters-----------------//
    public boolean isActive() {
        return active;
    }

    public boolean isCollider() {
        return collider;
    }

    public RectF getHitBox() {
        return hitBox;
    }
    public abstract void draw(Canvas c);

    @Override
    public int compareTo(Entity other) {
        return Float.compare(hitBox.bottom, other.hitBox.bottom);
    }
}
