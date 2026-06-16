package com.example.tutorialgame.engine.renderer;

import static com.example.tutorialgame.engine.core.GameConstants.Sprite.SCALE_MULTIPLIER;
import static com.example.tutorialgame.engine.core.GameConstants.Sprite.X_DRAW_OFFSET;
import static com.example.tutorialgame.engine.core.GameConstants.Sprite.Y_DRAW_OFFSET;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;

import com.example.tutorialgame.R;
import com.example.tutorialgame.components.HealthComponent;
import com.example.tutorialgame.engine.core.GameConstants;
import com.example.tutorialgame.engine.ui.NpcHealthBar;
import com.example.tutorialgame.entities.characters.Character;
import com.example.tutorialgame.managers.BitmapManager;
import com.example.tutorialgame.ui.base.BaseActivity;

/**
 * Responsible for rendering all visual components of a Character.
 */
public class CharacterRenderer {
    // --- Pre-allocated Filters (Zero GC overhead during gameplay) ---
    private static final PorterDuffColorFilter FILTER_RED = new PorterDuffColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);
    private static final PorterDuffColorFilter FILTER_WHITE = new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);

    private static final Bitmap shadowBitmap;
    private static boolean SHOW_HITBOX;

    private final Paint shadowPaint, hitboxPaint, ragePaint, flashPaint;
    private final NpcHealthBar healthBar;
    private final Character character;

    static {
        SharedPreferences sp = BaseActivity.getContext().getSharedPreferences("settings", Context.MODE_PRIVATE);
        SHOW_HITBOX = sp.getBoolean("hitbox", false);
        shadowBitmap = BitmapManager.getBitmap(R.drawable.shadow);
    }

    public CharacterRenderer(Character character) {
        this.character = character;
        this.healthBar = new NpcHealthBar(character);

        shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadowPaint.setFilterBitmap(true);
        shadowPaint.setDither(true);

        hitboxPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hitboxPaint.setStrokeWidth(SCALE_MULTIPLIER / 2f);
        hitboxPaint.setStyle(Paint.Style.STROKE);
        hitboxPaint.setColor(character.getFaction() == GameConstants.Faction.PLAYER ? Color.RED : Color.BLUE);

        ragePaint = new Paint();
        flashPaint = new Paint();
    }

    public void drawFullCharacter(Canvas c) {
        if (!character.isActive()) return;

        drawShadow(c);
        drawEmote(c);
        drawCharacter(c);

        character.getCombatComponent().draw(c);
        if (SHOW_HITBOX && character.isAttacking()) {
            c.drawRect(character.getAttackBox(), hitboxPaint);
        }

        drawHealthBar(c);
    }

    private void drawCharacter(Canvas c) {
        Bitmap sprite = character.getCurrentSprite();
        if (sprite == null) return;

        c.save();
        c.translate(0, -character.getElevation());

        float drawX = character.getHitBox().left - X_DRAW_OFFSET;
        float drawY = character.getHitBox().top - Y_DRAW_OFFSET;

        // 1. Scene Layer
        c.drawBitmap(sprite, drawX, drawY, null);

        // 2. Effects Layers
        drawCriticalFlashOverlay(c, sprite, drawX, drawY);
        drawRageOverlay(c, drawX, drawY);

        // 3. Debug Layer
        if (SHOW_HITBOX) c.drawRect(character.getHitBox(), hitboxPaint);

        c.restore();
    }

    private void drawCriticalFlashOverlay(Canvas c, Bitmap sprite, float drawX, float drawY) {
        // מגדירים את הטיפוס במפורש ושומרים את הרפרנס פעם אחת
        HealthComponent hc = character.getHealthComponent();
        float alpha = hc.getFlashAlpha();

        // סידור מהיר: קודם מספרים בוליאנים פשוטים, בסוף קריאות פונקציה מורכבות יותר
        if (alpha <= 0 || character.isDead() || character.getFaction() == GameConstants.Faction.PLAYER || !hc.isCriticalFlash()) {
            return;
        }

        // חישוב הזמן שעבר
        long elapsed = System.currentTimeMillis() - hc.getFlashStartTime();

        // מעבר סופר-מהיר (30 מילישניות בלבד לפלאש האדום)
        PorterDuffColorFilter activeFilter = (elapsed < 30) ? FILTER_RED : FILTER_WHITE;

        flashPaint.setColorFilter(activeFilter);
        flashPaint.setAlpha((int) (255 * alpha));

        c.drawBitmap(sprite, drawX, drawY, flashPaint);
    }

    private void drawRageOverlay(Canvas c, float drawX, float drawY) {
        if (!character.hasRage() || character.getRagePercentage() <= 0) return;

        ragePaint.setAlpha((int) (255 * character.getRagePercentage()));
        Bitmap rageSprite = character.getGameCharType().getRageSprites(character.getAniIndex(), character.getFaceDir());

        c.drawBitmap(rageSprite, drawX, drawY, ragePaint);
    }

    private void drawShadow(Canvas c) {
        shadowPaint.setAlpha((int) (200 * (1 - character.getJumpFraction())));
        float shadowY = character.getHitBox().bottom - 5 * SCALE_MULTIPLIER + (character.getElevation() / SCALE_MULTIPLIER);
        c.drawBitmap(shadowBitmap, character.getHitBox().left, shadowY, shadowPaint);
    }

    private void drawEmote(Canvas c) {
        if (character.getEmoteComponent() != null) {
            character.getEmoteComponent().drawEmote(c);
        }
    }

    private void drawHealthBar(Canvas c) {
        if (character.getCurrentHealth() < character.getMaxHealth() && character.getFaction() != GameConstants.Faction.PLAYER) {
            healthBar.drawBar(c);
        }
    }

    public static void setShowHitbox(boolean flag) {
        SHOW_HITBOX = flag;
    }
}