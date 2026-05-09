package com.example.tutorialgame.engine.ui;

import android.graphics.Bitmap;
import com.example.tutorialgame.R;
import com.example.tutorialgame.managers.BitmapManager;

public enum Emotes {
    HEART(0, 0),
    BROKEN_HEART(0, 1),
    SAD(0, 2),
    SURPRISED(0, 3),
    DIALOGUE(0, 4),
    ANGRY(1, 0),
    SCARED_AS_HELL(1,1),
    COCKY(1,2);

    private final Bitmap emote;

    Emotes(int x, int y) {
        int width = 14;
        int height = 13;
        emote = BitmapManager.getBitmapRegion(R.drawable.atl_emotes, x*width, y*height, width, height, 1, false);
    }

    public Bitmap getEmote() {
        return emote;
    }
}
