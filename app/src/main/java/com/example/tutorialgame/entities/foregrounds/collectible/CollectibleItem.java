package com.example.tutorialgame.entities.foregrounds.collectible;

import static com.example.tutorialgame.engine.core.GameConstants.Sprite.SCALE_MULTIPLIER;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;

import com.example.tutorialgame.MyApp;
import com.example.tutorialgame.components.JumpComponent;
import com.example.tutorialgame.engine.audio.SoundManager;
import com.example.tutorialgame.entities.Entity;
import com.example.tutorialgame.entities.characters.Player;
import com.example.tutorialgame.entities.foregrounds.statics.Items;
import com.example.tutorialgame.managers.BitmapManager;
import com.example.tutorialgame.R;
import com.example.tutorialgame.managers.objectpool.ObjectPoolManager;
import com.example.tutorialgame.ui.base.BaseActivity;

public class CollectibleItem extends Entity {
    // --- Constants for Tuning ---
    private static final float ABSORB_ANIM_DURATION = 0.25f;
    private static final float POP_SCALE_MAX = 1.3f;
    private static final float POP_DURATION_FRACTION = 0.3f;
    private static final float HORIZONTAL_DRAG = 0.95f;
    private static final float SHADOW_ALPHA_MAX = 130f;

    // אופטימיזציה: שימוש בביטמפ סטטי לצל
    private static final Bitmap staticShadowSprite;
    static {
        staticShadowSprite = BitmapManager.getBitmap(R.drawable.shadow, 0.8, false);
    }

    // Paints: Non-static to avoid state bleeding between instances
    private final Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint shadowPaint = new Paint();
    private final Paint absorbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF destRect = new RectF();

    private JumpComponent jumpComponent;
    private Items itemType;
    private boolean isHovering, isFirstJump;
    private float horizontalVelocity;
    private boolean isCollected;
    private float absorbProgress;

    private static final float OUTLINE_OFFSET = 0.5f * SCALE_MULTIPLIER;

    public CollectibleItem(float x, float y, Items type) {
        super(new PointF(x, y), type.getHitboxWidth(), type.getHitboxHeight(), true);
        glowPaint.setColorFilter(new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP));
        init(x, y, type);
        collider = false;
    }

    public void init(float x, float y, Items type) {
        this.itemType = type;
        this.hitBox.offsetTo(x, y);
        this.spawnPos.set(x, y);
        this.active = true;
        this.isFirstJump = true;
        this.isHovering = false;
        this.isCollected = false;
        this.absorbProgress = 0f;

        // Reset Alphas
        this.absorbPaint.setAlpha(255);
        this.glowPaint.setAlpha(255);

        // Randomized ejection
        float desiredJumpHeight = 0.5f + MyApp.getRandom().nextFloat() * 0.4f;
        float timeToApex = desiredJumpHeight / 2.5f;
        this.jumpComponent = new JumpComponent(desiredJumpHeight, timeToApex);
        this.horizontalVelocity = (MyApp.getRandom().nextFloat() - 0.5f) * 4f * SCALE_MULTIPLIER;
    }

    public void update(double delta, Player player) {
        if (!active) return;

        if (isCollected) {
            updateAbsorbAnimation(delta);
            return;
        }

        jumpComponent.update(delta);

        if (isFirstJump) {
            isFirstJump = false;
            jumpComponent.startJump();
        }

        if (jumpComponent.isAirborne() && !isHovering) {
            this.hitBox.offset((float) (horizontalVelocity * delta), 0);
            // Optimization: Avoid Math.pow for simple drag
            horizontalVelocity *= (float) Math.pow(HORIZONTAL_DRAG, delta * 60);
        }

        // Handle Hover Logic
        if (!jumpComponent.isAirborne()) {
            if (!isHovering) {
                isHovering = true;
                // Soften the jump for hovering effect
                jumpComponent.setJumpVelocity(jumpComponent.getJumpVelocity() / 15f);
                jumpComponent.setGravity(jumpComponent.getGravity() / 35f);
            }
            jumpComponent.startJump();
        }

        checkPickup(player);
    }

    private void updateAbsorbAnimation(double delta) {
        absorbProgress += (float) (delta / ABSORB_ANIM_DURATION);
        if (absorbProgress >= 1f) {
            active = false;
            ObjectPoolManager.releaseCollectibleItem(this);
        }
    }

    private void checkPickup(Player player) {
        // Optimization: Standardize collision check
        if (RectF.intersects(player.getProjectedHitBox(), hitBox)) {
            triggerCollection(player);
        }
    }

    private void triggerCollection(Player player) {
        itemType.applyEffect(player);
        isCollected = true;
    }

    @Override
    public void draw(Canvas c) {
        if (!active) return;

        if (isCollected) {
            drawAbsorbAnimation(c);
            return;
        }

        float elevation = jumpComponent.getElevation();
        float drawX = hitBox.left;
        float drawY = hitBox.top - elevation;

        // Draw Shadow
        int shadowAlpha = Math.round(SHADOW_ALPHA_MAX * (1 - jumpComponent.getJumpFraction()));
        shadowPaint.setAlpha(shadowAlpha);
        c.drawBitmap(staticShadowSprite, drawX - 0.2f * SCALE_MULTIPLIER, hitBox.bottom - 0.2f * SCALE_MULTIPLIER, shadowPaint);

        // Draw Outline & Sprite
        drawOutline(c, drawX, drawY, itemType.getBitmap());
        c.drawBitmap(itemType.getBitmap(), drawX, drawY, null);
    }

    private void drawOutline(Canvas c, float x, float y, Bitmap bmp) {
        // Optimization: Using a loop or an array of offsets could make this cleaner,
        // but hardcoded is actually slightly faster on Android Canvas.
        c.drawBitmap(bmp, x - OUTLINE_OFFSET, y, glowPaint);
        c.drawBitmap(bmp, x + OUTLINE_OFFSET, y, glowPaint);
        c.drawBitmap(bmp, x, y - OUTLINE_OFFSET, glowPaint);
        c.drawBitmap(bmp, x, y + OUTLINE_OFFSET, glowPaint);

        c.drawBitmap(bmp, x - OUTLINE_OFFSET, y - OUTLINE_OFFSET, glowPaint);
        c.drawBitmap(bmp, x + OUTLINE_OFFSET, y - OUTLINE_OFFSET, glowPaint);
        c.drawBitmap(bmp, x - OUTLINE_OFFSET, y + OUTLINE_OFFSET, glowPaint);
        c.drawBitmap(bmp, x + OUTLINE_OFFSET, y + OUTLINE_OFFSET, glowPaint);
    }

    private void drawAbsorbAnimation(Canvas c) {
        float t = absorbProgress;
        float scale;
        int alpha;

        if (t < POP_DURATION_FRACTION) {
            float grow = t / POP_DURATION_FRACTION;
            scale = 1.0f + ((POP_SCALE_MAX - 1.0f) * grow);
            alpha = 255;
        } else {
            float shrink = (t - POP_DURATION_FRACTION) / (1f - POP_DURATION_FRACTION);
            scale = POP_SCALE_MAX * (1f - shrink);
            alpha = (int) ((1f - shrink) * 255);
        }

        float width = itemType.getHitboxWidth() * scale;
        float height = itemType.getHitboxHeight() * scale;
        float centerX = hitBox.centerX();
        float centerY = hitBox.centerY() - jumpComponent.getElevation();

        destRect.set(centerX - width / 2f, centerY - height / 2f, centerX + width / 2f, centerY + height / 2f);

        absorbPaint.setAlpha(alpha);
        Bitmap bmp = itemType.getBitmap();

        // Draw shrinking outline
        drawOutlineWithRect(c, bmp, alpha);

        // Draw shrinking sprite
        c.drawBitmap(bmp, null, destRect, absorbPaint);
    }

    private void drawOutlineWithRect(Canvas c, Bitmap bmp, int alpha) {
        glowPaint.setAlpha(alpha);
        float originalLeft = destRect.left;
        float originalTop = destRect.top;

        // Offset-based drawing to reuse destRect without allocation
        destRect.offset(-OUTLINE_OFFSET, 0); c.drawBitmap(bmp, null, destRect, glowPaint);
        destRect.offset(OUTLINE_OFFSET * 2, 0); c.drawBitmap(bmp, null, destRect, glowPaint);
        destRect.offset(-OUTLINE_OFFSET, -OUTLINE_OFFSET); c.drawBitmap(bmp, null, destRect, glowPaint);
        destRect.offset(0, OUTLINE_OFFSET * 2); c.drawBitmap(bmp, null, destRect, glowPaint);

        // Reset rect position
        destRect.offsetTo(originalLeft, originalTop);
    }
}