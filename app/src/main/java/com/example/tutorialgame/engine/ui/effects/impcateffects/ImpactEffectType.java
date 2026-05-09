package com.example.tutorialgame.engine.ui.effects.impcateffects;

import android.graphics.Bitmap;

import com.example.tutorialgame.R;
import com.example.tutorialgame.managers.BitmapManager;

public enum ImpactEffectType {
    // ResID, Frames, FrameW, FrameH, AnimSpeed, Rotationable, isImpactOnly
    WHITE_CIRCLE(R.drawable.particle_tap_whitecircle, 4, 32, 32, 2, false, true),
    ORANGE_CIRCLE(R.drawable.particle_tap_orangecircle, 4, 32, 32, 2, false, true),
    SPARK(R.drawable.particle_tap_spark, 6, 32, 32, 3, false, true),
    SPARK2(R.drawable.praticle_tap_spark2, 5, 32, 32, 2, false, true),
    SLASH(R.drawable.spr_fx_slash, 4, 32, 32, 3, true, false),
    DUST_CLOUD_LANDING(R.drawable.spr_smoke_circular, 8, 30, 14, 2, false, true);

    private final Bitmap[] impactSpr;
    private final int frameCount, animSpeed;
    private final boolean rotationable, impactOnly;

    ImpactEffectType(int resID, int frames, int w, int h, int speed, boolean rotationable, boolean impactOnly) {
        this.impactSpr = BitmapManager.getSpritesheet(resID, w, h, frames, 1, false);
        this.frameCount = frames;
        this.animSpeed = speed;
        this.rotationable = rotationable;
        this.impactOnly = impactOnly;
    }

    public Bitmap getSprite(int index) {
        if (index >= 0 && index < impactSpr.length) return impactSpr[index];
        return null;
    }

    public int getFrameCount() { return frameCount; }
    public int getAnimSpeed() { return animSpeed; }
    public boolean isRotationable() { return rotationable; }
    public boolean isImpactOnly() { return impactOnly; }
}
