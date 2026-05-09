package com.example.tutorialgame.entities.characters.nonenemies;

import static com.example.tutorialgame.engine.core.GameConstants.Face_Dir.DOWN;
import static com.example.tutorialgame.engine.core.GameConstants.Sprite.SCALE_MULTIPLIER;
import android.graphics.PointF;

import com.example.tutorialgame.engine.core.GameConstants;
import com.example.tutorialgame.entities.characters.GameCharacters;
import com.example.tutorialgame.entities.characters.Npc;
import com.example.tutorialgame.environments.GameMap;
import com.example.tutorialgame.utils.CollisionUtils;

/**
 * An abstract base class for all non-enemy characters in the game.
 */
public abstract class NonEnemy extends Npc {
    protected float legalOffset;
    private static final long TIME_TO_LOOK_DOWN = 3000;
    protected long lastTimeAggressive;

    public NonEnemy(PointF pos, GameCharacters gameCharType, long timeToAttack, long attackDuration, int faction, float speed) {
        super(pos, gameCharType, timeToAttack, attackDuration, faction, speed);
        this.homeX = pos.x;
        this.homeY = pos.y;
        this.legalOffset = 8 * SCALE_MULTIPLIER;
    }

    @Override
    public void update(double delta, GameMap gameMap) {
        super.update(delta, gameMap);

        if (now - lastTimeSeenPlayer >= TIME_TO_LOOK_DOWN) {
            faceDir = DOWN;
        }
    }

    @Override
    protected void onIdeal(double delta, GameMap gameMap) {
        if (now - lastTimeAggressive <= 2000) {
            setAttacking(false);
            resetAnimation();
            return;
        }

        goHome(delta, gameMap);
        reverseDamage(delta);
        setPlayerSpeaker(gameMap);
    }

    protected void goHome(double delta, GameMap gameMap) {
        if (isHome()) {
            hitBox.offsetTo(homeX, homeY);
            resetAnimation();
            return;
        }

        float speed = (float) (baseSpeed * delta);
        float diffX = homeX - hitBox.left;
        float diffY = homeY - hitBox.top;

        if (Math.abs(diffX) > legalOffset) {
            faceDir = (diffX > 0) ? GameConstants.Face_Dir.RIGHT : GameConstants.Face_Dir.LEFT;
        } else if (Math.abs(diffY) > legalOffset) {
            faceDir = (diffY > 0) ? GameConstants.Face_Dir.DOWN : GameConstants.Face_Dir.UP;
        }

        animation.update();
        movementComponent.moveInDir(speed, gameMap);
    }

    protected boolean isHome() {
        return CollisionUtils.isWithinRange(hitBox.left, hitBox.top, homeX, homeY, legalOffset);
    }

    protected void reverseDamage(double delta) {
        if (!isHome() || health.getCurrentHealth() == health.getMaxHealth()) return;

        // ריפוי הדרגתי: מחשבים כמה לרפא ומעבירים רק את הכמות ל-heal()
        int amountToHeal = (int) (getAttackDamage() * delta * 2);
        if (amountToHeal > 0) {
            health.heal(amountToHeal);
        } else if (delta > 0 && Math.random() < 0.1) { 
            // הבטחת ריפוי מינימלי גם ב-delta קטן מאוד (באופן הסתברותי)
            health.heal(1);
        }
    }
}
