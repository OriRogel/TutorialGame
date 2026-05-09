package com.example.tutorialgame.entities.foregrounds.breakable;

import android.graphics.Bitmap;
import androidx.annotation.RawRes;
import com.example.tutorialgame.R;
import com.example.tutorialgame.managers.BitmapManager;

public enum BreakParticlesType {
    WOOD(R.drawable.broken_particle_wood, R.raw.sfx_explosion1, 2, 6, 16, 16),
    GRASS(R.drawable.broken_particle_grass, R.raw.sfx_explosion5, 1, 6, 12, 13),
    ROCK(R.drawable.broken_particle_rock, R.raw.sfx_explosion1, 3, 5, 13, 16);

    private final Bitmap[] spr;
    private final int sfxId, levelRequired;

    BreakParticlesType(int resId, @RawRes int sfxId, int levelRequired, int count, int width, int height) {
        this.spr = BitmapManager.getSpritesheet(resId, width, height, count, 1.0, false);
        this.sfxId = sfxId;
        this.levelRequired = levelRequired;
    }

    public Bitmap getRandomParticle() {
        return spr[(int) (Math.random() * spr.length)];
    }
    public int getSfxId() {
        return sfxId;
    }
    public int getLevelRequired() {
        return levelRequired;
    }
}
