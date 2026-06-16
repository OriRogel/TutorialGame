package com.example.tutorialgame.entities.foregrounds.animated;

import static com.example.tutorialgame.engine.core.GameConstants.Sprite.SCALE_MULTIPLIER;
import static com.example.tutorialgame.engine.core.GameConstants.View.SCREEN_HEIGHT;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;

import com.example.tutorialgame.MyApp;
import com.example.tutorialgame.R;
import com.example.tutorialgame.components.JumpComponent;
import com.example.tutorialgame.engine.audio.SoundManager;
import com.example.tutorialgame.engine.ui.displays.CoinDisplay;
import com.example.tutorialgame.entities.characters.Player;
import com.example.tutorialgame.environments.GameMap;
import com.example.tutorialgame.managers.BitmapManager;
import com.example.tutorialgame.managers.CameraManager;
import com.example.tutorialgame.managers.MapManager;
import com.example.tutorialgame.managers.objectpool.ObjectPoolManager;
import com.example.tutorialgame.ui.base.BaseActivity;

/**
 * Represents a collectible coin entity in the game world.
 * Optimized with Object Pooling for better performance.
 */
public class Coin extends AnimatedObject {
    private static final Bitmap staticShadowSprite;

    static {
        staticShadowSprite = BitmapManager.getBitmap(R.drawable.shadow, 0.8, false);
    }

    private final Paint shadowPaint = new Paint();
    private JumpComponent jumpComponent;
    private boolean isHovering, isFirstJump;
    private float hoverOffset;
    private boolean flying;
    private PointF screenPosition;

    public Coin(float x, float y) {
        super(new PointF(x, y), AnimatedType.COIN);
        init(x, y);
    }

    /**
     * Resets the coin state for reuse from the Object Pool.
     */
    public void init(float x, float y) {
        this.hitBox.offsetTo(x, y);
        this.spawnPos.set(x, y);
        this.active = true;
        this.isFirstJump = true;
        this.isHovering = false;
        this.screenPosition = null;
        
        float desiredJumpHeight = Math.max(0.6f, MyApp.getRandom().nextFloat());
        float timeToApex = desiredJumpHeight / 3;
        this.hoverOffset = MyApp.getRandom().nextBoolean() ? SCALE_MULTIPLIER / 2f : -SCALE_MULTIPLIER / 2f;
        this.jumpComponent = new JumpComponent(desiredJumpHeight, timeToApex);
        
        if (animation != null) animation.setSpeed(4 + MyApp.getRandom().nextInt(2));
    }

    @Override
    public void update(double delta, Player player) {
        super.update(delta, player);

        if (!flying) {
            jumpComponent.update(delta);
            if (isFirstJump) {
                isFirstJump = false;
                jumpComponent.startJump();
            }
            updateHoverOffset();
            startHovering();
            pickCoin(player);
        } else {
            goToDestination(delta);
        }
    }

    private void pickCoin(Player player) {
        if (flying || !RectF.intersects(player.getProjectedHitBox(), hitBox)) return;

        flying = true;
        float zoom = CameraManager.getTempZoom();
        this.screenPosition = new PointF(
                (hitBox.centerX() + CameraManager.getOffsetX()) * zoom,
                (hitBox.centerY() + CameraManager.getOffsetY()) * zoom
        );

        SoundManager.getInstance(BaseActivity.getContext()).playRndPitchSfx(R.raw.sfx_coin_collected);
        MyApp.getCosmetic().addCoin();
        animation.setSpeed((int) (animation.getSpeed() * MyApp.getRandom().nextFloat()) + 1);
    }

    private void goToDestination(double delta) {
        if (screenPosition == null) return;

        float speed = (float) (SCREEN_HEIGHT / 1.5f * delta);
        float dx = CoinDisplay.xDestination - screenPosition.x;
        float dy = CoinDisplay.yDestination - screenPosition.y;
        float distance = (float) Math.hypot(dx, dy);

        if (distance < speed) {
            checkArrivedAtDestination();
            return;
        }

        screenPosition.x += (dx / distance) * speed;
        screenPosition.y += (dy / distance) * speed;
    }

    private void checkArrivedAtDestination() {
        GameMap current = MapManager.getCurrentMap();
        if (current != null) {
            active = false;
            ObjectPoolManager.releaseCoin(this); // Return to pool
        }
    }

    @Override
    public void draw(Canvas c) {
        if (!flying) {
            shadowPaint.setAlpha(Math.round(180 * (1 - jumpComponent.getJumpFraction())));
            c.drawBitmap(staticShadowSprite, hitBox.left - 0.5f * SCALE_MULTIPLIER, hitBox.bottom + 0.5f * SCALE_MULTIPLIER, shadowPaint);
            c.drawBitmap(sprites[animation.getAniIndex()], hitBox.left, hitBox.top - jumpComponent.getElevation(), null);
        } else if (screenPosition != null) {
            float zoom = CameraManager.getTempZoom();
            float drawX = (screenPosition.x / zoom) - CameraManager.getOffsetX();
            float drawY = (screenPosition.y / zoom) - CameraManager.getOffsetY();
            c.drawBitmap(sprites[animation.getAniIndex()], drawX - (hitBox.width() / (2f * zoom)), drawY - (hitBox.height() / (2f * zoom)), null);
        }
    }

    private void updateHoverOffset() {
        if (!isHovering && jumpComponent.isAirborne())
            this.hitBox.offset(hoverOffset * jumpComponent.getJumpFraction(), 0);
    }

    private void startHovering() {
        if (jumpComponent.isAirborne()) return;
        if (!isHovering) {
            isHovering = true;
            jumpComponent.setJumpVelocity(jumpComponent.getJumpVelocity() / SCALE_MULTIPLIER);
            jumpComponent.setGravity(jumpComponent.getGravity() / SCALE_MULTIPLIER / 2);
        }
        jumpComponent.startJump();
    }
}
