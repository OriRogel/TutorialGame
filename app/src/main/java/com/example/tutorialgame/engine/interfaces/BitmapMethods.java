package com.example.tutorialgame.engine.interfaces;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import com.example.tutorialgame.engine.core.GameConstants;

public interface BitmapMethods {
    BitmapFactory.Options options = new BitmapFactory.Options();

    default Bitmap getScaledBitmap(Bitmap bitmap) {
        return Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() * GameConstants.Sprite.SCALE_MULTIPLIER, bitmap.getHeight() * GameConstants.Sprite.SCALE_MULTIPLIER, false);
    }

    default Bitmap getMultiplyBitmapSmoth(Bitmap bitmap, double multiply) {
        Matrix matrix = new Matrix();
        matrix.setScale((float) multiply * GameConstants.Sprite.SCALE_MULTIPLIER, (float) multiply * GameConstants.Sprite.SCALE_MULTIPLIER);
        // יווצר לנו Bitmap חדש בגודל העשרוני שהומר לעיגול פנימי
        return Bitmap.createBitmap(
                bitmap,
                0, 0,
                bitmap.getWidth(),
                bitmap.getHeight(),
                matrix,
                true  // filter = true => יפעיל סינון bilinear
        );
    }

    default Bitmap getMultiplyBitmapClean(Bitmap bitmap, double multiply) {
        Matrix matrix = new Matrix();
        matrix.setScale((float) multiply * GameConstants.Sprite.SCALE_MULTIPLIER, (float) multiply * GameConstants.Sprite.SCALE_MULTIPLIER);
        // יווצר לנו Bitmap חדש בגודל העשרוני שהומר לעיגול פנימי
        return Bitmap.createBitmap(
                bitmap,
                0, 0,
                bitmap.getWidth(),
                bitmap.getHeight(),
                matrix,
                false  // filter = true => יפעיל סינון bilinear
        );
    }
}