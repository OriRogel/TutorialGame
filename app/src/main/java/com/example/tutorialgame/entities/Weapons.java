package com.example.tutorialgame.entities;

import static com.example.tutorialgame.engine.core.GameConstants.Sprite.TILE_SIZE;

import android.graphics.Bitmap;

import com.example.tutorialgame.R;
import com.example.tutorialgame.engine.core.GameConstants;
import com.example.tutorialgame.engine.ui.effects.impcateffects.ImpactEffectType;
import com.example.tutorialgame.managers.BitmapManager;

/**
 * Defines the weapons available in the game, including their visual assets, 
 * base damage, knockback force, and attack animation effects.
 */
public enum Weapons {
    BIG_SWARD(R.drawable.wpn_big_sword, 0, 0.4f, 10, ImpactEffectType.SLASH, R.raw.sfx_slash),
    CLUB(R.drawable.wpn_club, 0, 0.8f, 10, ImpactEffectType.SLASH, R.raw.sfx_whoosh),
    BONE(R.drawable.wpn_bone, -GameConstants.Sprite.SCALE_MULTIPLIER, 0.6f, 8, null, R.raw.sfx_slash3),
    SPEAR(R.drawable.wpn_spear, 0, 0.2f, 12, ImpactEffectType.ORANGE_CIRCLE, R.raw.sfx_slash2),
    HAMMER(R.drawable.wpn_hammer, 0, 0.9f, 20, ImpactEffectType.SLASH, R.raw.sfx_hammer_swing),
    NULL(-1, 0, 1f, 0, ImpactEffectType.WHITE_CIRCLE, R.raw.sfx_explosion3);


    private final Bitmap weaponInHandImg;
    private final int bottomOffset;
    private final float knockBackForce;
    private final int baseDamage;
    private final ImpactEffectType effectType;
    private final int swingSfx, resId;
    private static final int NULL_SIZE = (int) (0.4 * TILE_SIZE);

    Weapons(int resID, int bottomOffset, float knockBackForce, int baseDamage, ImpactEffectType effectType, int swingSfx) {
        this.resId = resID;
        this.weaponInHandImg = BitmapManager.getBitmap(resID);
        this.bottomOffset = bottomOffset;
        this.knockBackForce = knockBackForce * TILE_SIZE;
        this.baseDamage = baseDamage;
        this.effectType = effectType;
        this.swingSfx = swingSfx;
    }

    public Bitmap getWeaponInHandImg() { return weaponInHandImg; }
    public int getWidth() { return resId != -1 ? weaponInHandImg.getWidth() : NULL_SIZE; }
    public int getHeight() { return resId != -1 ? weaponInHandImg.getHeight() : NULL_SIZE; }
    public int getBottomOffset() { return bottomOffset; }
    public float getKnockBackForce() { return knockBackForce; }
    public int getBaseDamage() { return baseDamage; }
    public int getSwingSfx() { return swingSfx; }
    public ImpactEffectType getEffectType() { return effectType; }
}
