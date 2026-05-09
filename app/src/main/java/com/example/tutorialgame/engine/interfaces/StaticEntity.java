package com.example.tutorialgame.engine.interfaces;

import android.graphics.Bitmap;
import android.graphics.RectF;

public interface StaticEntity {
    Bitmap getBitmap();
    float getDrawY();
    RectF getHitBox();
}
