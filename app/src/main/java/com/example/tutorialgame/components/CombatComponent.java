package com.example.tutorialgame.components;

import static com.example.tutorialgame.engine.core.GameConstants.Face_Dir.DOWN;
import static com.example.tutorialgame.engine.core.GameConstants.Face_Dir.LEFT;
import static com.example.tutorialgame.engine.core.GameConstants.Face_Dir.RIGHT;
import static com.example.tutorialgame.engine.core.GameConstants.Face_Dir.UP;
import static com.example.tutorialgame.engine.core.GameConstants.Sprite.SCALE_MULTIPLIER;
import static com.example.tutorialgame.engine.core.GameConstants.Sprite.X_DRAW_OFFSET;
import static com.example.tutorialgame.engine.core.GameConstants.Sprite.Y_DRAW_OFFSET;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import com.example.tutorialgame.R;
import com.example.tutorialgame.MyApp;
import com.example.tutorialgame.engine.audio.SoundManager;
import com.example.tutorialgame.engine.core.GameConstants;
import com.example.tutorialgame.engine.ui.effects.WorldAnimationEffect;
import com.example.tutorialgame.entities.Weapons;
import com.example.tutorialgame.entities.characters.Character;
import com.example.tutorialgame.managers.CameraManager;
import com.example.tutorialgame.ui.base.BaseActivity;

/**
 * Orchestrates all combat-related logic for a character.
 * Manages weapon positioning, attack hitboxes, visual effects, and physics-based interactions.
 */
public class CombatComponent {
    private final Character owner;
    private Weapons weapon;
    private final RectF attackBox = new RectF();
    private final PointF weaponPos = new PointF();
    private float weaponRotation;

    // Visual Effect management using WorldAnimationEffect
    private final WorldAnimationEffect visualEffect = new WorldAnimationEffect();
    private final Matrix drawMatrix = new Matrix();

    public CombatComponent(Character owner) {
        this.owner = owner;
        this.weapon = owner.getWeapon();
    }

    public void update() {
        if (weapon == null) return;

        updateWeaponTransform();
        updateAttackBox();
        updateVisualEffect();
    }

    private void updateWeaponTransform() {
        RectF ownerHitbox = owner.getHitBox();
        float elevation = owner.getElevation();

        switch (owner.getFaceDir()) {
            case UP:
                weaponPos.set(ownerHitbox.left - 0.5f * SCALE_MULTIPLIER, ownerHitbox.top - weapon.getHeight() - Y_DRAW_OFFSET - elevation);
                weaponRotation = 180;
                break;
            case DOWN:
                weaponPos.set(ownerHitbox.left + 0.5f * SCALE_MULTIPLIER, ownerHitbox.bottom - elevation);
                weaponRotation = 0;
                break;
            case LEFT:
                weaponPos.set(ownerHitbox.left - weapon.getHeight() - X_DRAW_OFFSET, ownerHitbox.bottom - weapon.getWidth() - 0.75f * SCALE_MULTIPLIER - elevation);
                weaponRotation = 90;
                break;
            case RIGHT:
                weaponPos.set(ownerHitbox.right + X_DRAW_OFFSET, ownerHitbox.bottom - weapon.getWidth() - 0.75f * SCALE_MULTIPLIER - elevation);
                weaponRotation = 270;
                break;
        }
    }

    private void updateAttackBox() {
        float w = (owner.getFaceDir() == LEFT || owner.getFaceDir() == RIGHT) ? weapon.getHeight() : weapon.getWidth();
        float h = (owner.getFaceDir() == UP || owner.getFaceDir() == DOWN) ? weapon.getHeight() : weapon.getWidth();
        attackBox.set(weaponPos.x, weaponPos.y, weaponPos.x + w, weaponPos.y + h);
    }

    private void updateVisualEffect() {
        if (!visualEffect.isActive() || owner.isDead()) return;

        // אם זה אפקט סווינג (לא ImpactOnly), הוא עוקב אחרי הנשק
        if (weapon != null && weapon.getEffectType() != null && !weapon.getEffectType().isImpactOnly()) {
            visualEffect.setPos(attackBox.centerX(), attackBox.centerY());
            visualEffect.setRotation(calculateEffectRotation());
        }

        visualEffect.update();
    }

    private float calculateEffectRotation() {
        if (weapon == null || weapon.getEffectType() == null || !weapon.getEffectType().isRotationable()) return 0;
        switch (owner.getFaceDir()) {
            case UP:    return 270;
            case DOWN:  return 90;
            case LEFT:  return 180;
            default:    return 0;
        }
    }

    /**
     * Triggered when the character starts a swing.
     */
    public void onSwing() {
        if (weapon == null || owner.getStaminaComponent().isStaminaLocked()) return;

        if (weapon.getEffectType() != null && !weapon.getEffectType().isImpactOnly()) {
            visualEffect.init(weapon.getEffectType(), attackBox.centerX(), attackBox.centerY(), calculateEffectRotation());
        }
        SoundManager.getInstance(BaseActivity.getContext()).playSpatialSfx(weapon.getSwingSfx(), owner.getHitBox().centerX());
    }

    /**
     * Triggered when a hit is confirmed on a target.
     */
    public void onImpact() {
        if (weapon != null && weapon.getEffectType() != null && weapon.getEffectType().isImpactOnly()) {
            visualEffect.init(weapon.getEffectType(), attackBox.centerX(), attackBox.centerY(), 0);
        }
    }

    /**
     * Executes an attack against a target, calculating damage and applying physics.
     */
    public void attack(Character target) {
        if (weapon == null || target == null || target.isDead()) return;

        onImpact();


        int totalDamage = owner.getAttackDamage() + weapon.getBaseDamage();
        boolean isCritical = false;

        if (owner.getFaction() == GameConstants.Faction.PLAYER) {
            int chance = MyApp.getPlayerStats().getCritHitChance();
            int roll = MyApp.RND.nextInt(100);
            if (roll < chance) {
                totalDamage *= 2;
                isCritical = true;
                SoundManager.getInstance(BaseActivity.getContext()).playSpatialSfx(R.raw.sfx_explosion3, target.getHitBox().centerX());
            }
        }

        target.takeDamage(totalDamage, owner, isCritical);

        float forceApplied = target.getMovementComponent().applyKnockback(
                weapon.getKnockBackForce(),
                owner.getGameCharType().getMass(),
                totalDamage,
                owner.getHitBox().centerX(),
                owner.getHitBox().centerY()
        );

        float targetResistance = target.getGameCharType().getMass() * target.getAttackDamage() / 2;

        if (forceApplied > targetResistance) return;
        float recoilForce = (targetResistance - forceApplied) * 0.5f;
        recoilForce = Math.min(recoilForce, weapon.getKnockBackForce() * 0.6f);

        owner.getMovementComponent().applyRecoil(
                recoilForce,
                target.getHitBox().centerX(),
                target.getHitBox().centerY()
        );

        if (owner.getFaction() == GameConstants.Faction.PLAYER)
            CameraManager.startShake(recoilForce / 12, 0.1f);
    }

    public void draw(Canvas c) {
        if (weapon == null || owner.isDead()) return;
        
        // ציור הנשק רק בזמן התקפה פעילה
        if (owner.isAttacking()) {
            update();
            if (weapon.getWeaponInHandImg() != null) {
                drawMatrix.reset();
                float pivotX = attackBox.left;
                float pivotY = attackBox.top;

                drawMatrix.postTranslate(wepAdjustLeft() + wepAdjustBottom(), wepAdjustTop());
                drawMatrix.postRotate(weaponRotation, 0, 0);
                drawMatrix.postTranslate(pivotX, pivotY);
                c.drawBitmap(weapon.getWeaponInHandImg(), drawMatrix, null);
            }
        }

        // ציור האפקט (יכול להמשיך גם אחרי שההתקפה הסתיימה)
        visualEffect.draw(c);
    }

    private float wepAdjustTop() { return (owner.getFaceDir() == LEFT || owner.getFaceDir() == UP) ? -weapon.getHeight() : 0; }
    private float wepAdjustLeft() { return (owner.getFaceDir() == UP || owner.getFaceDir() == RIGHT) ? -weapon.getWidth() : 0; }
    private float wepAdjustBottom() { return (owner.getFaceDir() == DOWN) ? weapon.getBottomOffset() : 0; }

    public RectF getAttackBox() { return attackBox; }
    public Weapons getWeapon() { return weapon; }
    public void setWeapon(Weapons weapon) { this.weapon = weapon; }
}
