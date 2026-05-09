package com.example.tutorialgame.entities.foregrounds.statics;

import android.graphics.Bitmap;
import com.example.tutorialgame.R;
import com.example.tutorialgame.engine.audio.SoundManager;
import com.example.tutorialgame.engine.interfaces.StaticObjectData;
import com.example.tutorialgame.entities.characters.Player;
import com.example.tutorialgame.managers.BitmapManager;
import com.example.tutorialgame.ui.base.BaseActivity;

/**
 * Defines all collectible items in the game and their effects using the Strategy Pattern.
 */
public enum Items implements StaticObjectData {

    EMPTY_POT(R.drawable.ic_empty_pot, 0, (player, value) -> {}, -1),
    MEDIPACK(R.drawable.ic_medipack, 20, (player, value) -> player.getHealthComponent().heal(value), R.raw.sfx_heal),
    FISH(R.drawable.ic_fish, 30, (player, value) -> player.getStaminaComponent().restoreStamina(value), R.raw.sfx_restore_stamina);

    // הממשק הפונקציונלי שלנו - הלוגיקה שמוזרקת לכל פריט
    public interface ItemEffect {
        void apply(Player player, int value);
    }

    private final Bitmap image;
    private final int value, pickUpRes;
    private final ItemEffect effect; // הפעולה הספציפית של הפריט

    Items(int resId, int value, ItemEffect effect, int pickRes) {
        this.image = BitmapManager.getBitmap(resId);
        this.value = value;
        this.effect = effect;
        this.pickUpRes = pickRes;
    }

    /**
     * מפעיל את האפקט של הפריט ישירות על השחקן
     */
    public void applyEffect(Player player) {
        if (effect != null && player != null) {
            effect.apply(player, value);
            if (pickUpRes != -1)
                SoundManager.getInstance(BaseActivity.getContext()).playSfx(pickUpRes);
        }
    }

    @Override
    public Bitmap getBitmap() {
        return image;
    }

    public int getValue() { return value; }

    @Override public int getHitboxWidth() { return image.getWidth(); }
    @Override public int getHitboxHeight() { return image.getHeight(); }
    @Override public int getHitboxRoof() { return 0; }
}