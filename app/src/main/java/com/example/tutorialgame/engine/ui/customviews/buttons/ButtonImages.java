package com.example.tutorialgame.engine.ui.customviews.buttons;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.example.tutorialgame.R;
import com.example.tutorialgame.engine.interfaces.BitmapMethods;
import com.example.tutorialgame.ui.base.BaseActivity;

public enum ButtonImages implements BitmapMethods {
    PLAYING_MENU(R.drawable.playing_button_menu, 140, 140),
    MENU_REPLAY(R.drawable.mainmenu_button_replay, 300, 140);

    private final int width, height;
    private final Bitmap normal, pushed;

    ButtonImages(int resID, int width, int height) {
        options.inScaled = false;
        this.width = width;
        this.height = height;

        Bitmap buttonAtlas = BitmapFactory.decodeResource(BaseActivity.getContext().getResources(), resID, options);
        normal = Bitmap.createBitmap(buttonAtlas, 0, 0, width, height);
        pushed = Bitmap.createBitmap(buttonAtlas, width, 0, width, height);

    }


    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Bitmap getBtnImg(boolean isBtnPushed) {
        return isBtnPushed ? pushed : normal;
    }
}