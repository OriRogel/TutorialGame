package com.example.tutorialgame.engine.interfaces;

import android.graphics.Bitmap;

public interface StaticObjectData {
    Bitmap getBitmap(); // שם אחיד!
    int getHitboxWidth();
    int getHitboxHeight();
    int getHitboxRoof();
}
