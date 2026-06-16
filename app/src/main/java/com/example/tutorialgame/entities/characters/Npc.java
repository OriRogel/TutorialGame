package com.example.tutorialgame.entities.characters;

import static com.example.tutorialgame.engine.core.GameConstants.Face_Dir.DOWN;
import static com.example.tutorialgame.engine.core.GameConstants.Face_Dir.LEFT;
import static com.example.tutorialgame.engine.core.GameConstants.Face_Dir.RIGHT;
import static com.example.tutorialgame.engine.core.GameConstants.Face_Dir.UP;
import static com.example.tutorialgame.engine.core.GameConstants.Sprite.TILE_SIZE;

import android.graphics.PointF;
import android.graphics.RectF;

import com.example.tutorialgame.MyApp;
import com.example.tutorialgame.engine.audio.SoundManager;
import com.example.tutorialgame.engine.core.GameConstants;
import com.example.tutorialgame.engine.ui.Emotes;
import com.example.tutorialgame.environments.GameMap;
import com.example.tutorialgame.managers.CameraManager;
import com.example.tutorialgame.managers.objectpool.ObjectPoolManager;
import com.example.tutorialgame.ui.base.BaseActivity;
import com.example.tutorialgame.utils.CollisionUtils;
import com.example.tutorialgame.utils.Other;

import java.util.List;
import java.util.Map;

/**
 * מחלקת ביניים המייצגת כל דמות שאינה השחקן (Non-Player Character).
 */
public abstract class Npc extends Character {

    // שינוי השם ל-AiBehavior כדי להפריד מ-EntityState של הגוף הפיזי
    protected enum AiBehavior { IDEAL, AGGRESSIVE, RETREAT }
    protected AiBehavior currentBehavior = AiBehavior.IDEAL;

    // --- תכונות ספציפיות ל-AI של NPC ---
    protected boolean preparingAttack;
    protected long timerBeforeAttack, timerAttackDuration;
    protected final long timeToAttack, attackDuration;
    protected long lastTimeSeenPlayer;
    protected Character currentTarget;
    protected final float baseSpeed;

    protected long lastTargetCheckTime;
    private static final long TARGET_CHECK_INTERVAL = 200;

    private final float camShakeMagnitude;

    public Npc(PointF pos, GameCharacters gameCharType, long timeToAttack, long attackDuration, int faction, float speed) {
        super(pos, gameCharType, faction);
        this.timeToAttack = timeToAttack;
        this.attackDuration = attackDuration;
        this.baseSpeed = speed * TILE_SIZE;
        this.moving = false; // מתחילים בעמידה
        camShakeMagnitude = getAttackDamage() / 54f;
    }

    @Override
    public void update(double delta, GameMap gameMap) {
        // חשוב: קוראים קודם ל-super שמטפל בפיזיקה, חיים ומעברי אנימציה (EntityState)
        super.update(delta, gameMap);

        if (isDead()) return;

        // בדיקות מצב פיזי
        boolean isBerserk = (float)health.getCurrentHealth() / health.getMaxHealth() <= 0.1f;
        boolean needsRest = (float)stamina.getCurrentStamina() / stamina.getMaxStamina() <= 0.3f;

        // ניהול מעברי מצבים של המוח (AI Transitions)
        if (currentBehavior == AiBehavior.AGGRESSIVE && needsRest && !isBerserk) {
            setAttacking(false);
            preparingAttack = false;
            currentBehavior = AiBehavior.RETREAT;
            emoteComponent.showEmote(Emotes.SCARED_AS_HELL, 2000);
        } else if (currentBehavior == AiBehavior.RETREAT && (stamina.getCurrentStamina() >= stamina.getMaxStamina() * 0.6f || isBerserk)) {
            currentBehavior = AiBehavior.AGGRESSIVE;
            emoteComponent.showEmote(Emotes.COCKY, 2000);
        }

        // ביצוע לוגיקה לפי המצב הנוכחי של ה-AI
        switch (currentBehavior) {
            case AGGRESSIVE:
                onAggressive(delta, gameMap);
                break;
            case IDEAL:
                onIdeal(delta, gameMap);
                break;
            case RETREAT:
                onRetreat(delta, gameMap);
                break;
        }
    }

    protected void onRetreat(double delta, GameMap gameMap) {
        if (currentTarget == null) {
            currentBehavior = AiBehavior.IDEAL;
            return;
        }

        this.moving = true; // המוח אומר לגוף לזוז

        // התרחקות מהמטרה
        float speed = (float) (baseSpeed * delta * 0.8f);
        float dx = hitBox.centerX() - currentTarget.getHitBox().centerX();
        float dy = hitBox.centerY() - currentTarget.getHitBox().centerY();

        // קביעת כיוון הפנים (הפוך מהמטרה)
        if (Math.abs(dx) > Math.abs(dy)) faceDir = (dx > 0) ? RIGHT : LEFT;
        else faceDir = (dy > 0) ? DOWN : UP;

        animation.update();
        movementComponent.moveInDir(speed, gameMap);

        // בדיקה אם המטרה עדיין קיימת/נראית גם בזמן נסיגה
        if (!currentTarget.isActive() || currentTarget.isDead() || !Other.IsCharacterSeesTarget(this, currentTarget)) {
            currentTarget = null;
            currentBehavior = AiBehavior.IDEAL;
        }
    }

    private void checkCurrentTarget(double delta, GameMap gameMap) {
        if (currentTarget == null || !currentTarget.isActive() || currentTarget.isDead() || !Other.IsCharacterSeesTarget(this, currentTarget)) {
            currentTarget = null;
            currentBehavior = AiBehavior.IDEAL;
            return;
        }

        this.moving = true; // פוקדים על הגוף לזוז לכיוון המטרה
        animation.update();
        float speed = (float) (baseSpeed * delta);
        movementComponent.moveInDir(speed, gameMap);
        lastTimeSeenPlayer = now;
    }

    protected void onAggressive(double delta, GameMap gameMap) {
        checkCurrentTarget(delta, gameMap);
        checkTimeToAttackTimer();
        updateAttackTimer();
        processAttack();
    }

    protected void onIdeal(double delta, GameMap gameMap) {
        this.moving = false; // כשאין מה לעשות, עוצרים את הגוף הפיזי (יחזור ל-IDLE)
        findTargetClap(gameMap);
        setAttacking(false);
        preparingAttack = false;
        if (currentTarget != null) currentBehavior = AiBehavior.AGGRESSIVE;
    }

    protected void findTargetClap(GameMap gameMap) {
        if (now - lastTargetCheckTime > TARGET_CHECK_INTERVAL) {
            currentTarget = findTarget(gameMap);
            lastTargetCheckTime = now;
        }
    }

    protected void setPlayerSpeaker(GameMap gameMap) {
        if (isCloseToPlayer(gameMap)) {
            turnTowardsTarget(gameMap.getPlayer());
            lastTimeSeenPlayer = now;
            gameMap.getPlayer().setCurrentSpeaker(this);
            if (!emoteComponent.isActive())
                emoteComponent.showEmote(Emotes.DIALOGUE, 2000);
        }
    }

    protected boolean isCloseToPlayer(GameMap gameMap) {
        return Other.IsCharacterSeesTarget(gameMap.getPlayer(), this);
    }

    private Character findTarget(GameMap gameMap) {
        Player player = gameMap.getPlayer();
        if (player != null && !player.isDead() && canDamage(player) && Other.IsCharacterSeesTarget(this, player)) return player;
        else {
            List<Character> chars = gameMap.getCharacterArrayList();
            if (chars == null || chars.isEmpty()) return null;

            Character bestTarget = null;
            float minDistanceSq = Float.MAX_VALUE;

            for (Character target : chars) {
                if (target == this || target == player) continue;
                if (!target.isActive() || target.isDead() || !canDamage(target)) continue;
                
                if (Other.IsCharacterSeesTarget(this, target)) {
                    float distSq = CollisionUtils.getDistance(hitBox.centerX(), hitBox.centerY(), 
                                                           target.getHitBox().centerX(), target.getHitBox().centerY());
                    
                    // Priority: stick to current target if it's still visible and within a reasonable extra distance
                    if (target == currentTarget) distSq *= 0.8f; 

                    if (distSq < minDistanceSq) {
                        minDistanceSq = distSq;
                        bestTarget = target;
                    }
                }
            }
            return bestTarget;
        }
    }

    protected void forgetOldTargets(long timeoutMillis) {
        getBadBoyList().entrySet().removeIf(entry -> (now - entry.getValue() > timeoutMillis));
    }

    protected Character findClosestTarget() {
        Map<Character, Long> currentTargets = getBadBoyList();
        if (currentTargets.isEmpty()) return null;

        Character closest = null;
        float minDistanceSq = Float.MAX_VALUE;

        for (Character target : currentTargets.keySet()) {
            if (target == null || !target.isActive() || target.isDead()) continue;

            float distanceSq = CollisionUtils.getDistance(getHitBox().centerX(), getHitBox().centerY(),
                    target.getHitBox().centerX(), target.getHitBox().centerY());
            if (distanceSq < minDistanceSq) {
                minDistanceSq = distanceSq;
                closest = target;
            }
        }
        return closest;
    }

    protected void processAttack() {
        if (currentTarget == null) return;

        // השתמשנו ב-Getter החדש שיצרנו!
        if (isAttacking()) {
            if (!attackChecked) {
                attackChecked = true;
                if (RectF.intersects(combatComponent.getAttackBox(), currentTarget.getProjectedHitBox())) {
                    if ((this.isAirborne() && !currentTarget.isAirborne()) || (!this.isAirborne() && currentTarget.isAirborne())) return;
                    SoundManager.getInstance(BaseActivity.getContext()).playSpatialSfx(currentTarget.getImpactSfx(), currentTarget.getProjectedHitBox().centerX());
                    damageCharacter(currentTarget);
                    currentTarget.checkCharacterDead(this);

                    if (currentTarget.getFaction() == GameConstants.Faction.PLAYER) CameraManager.startShake(camShakeMagnitude, 0.15f);
                }
            }
        } else if (!isPreparingAttack()) {
            prepareAttack(currentTarget);
        }
    }

    public void prepareAttack(Character target) {
        if (isAirborne() || preparingAttack) return;
        preparingAttack = true;
        timerBeforeAttack = now;

        this.moving = false; // הגוף הפיזי עוצר לקראת מכה (מחזיר לאנימציית עמידה)

        turnTowardsTarget(target);
        lastTimeSeenPlayer = now;
    }

    protected void updateAttackTimer() {
        // מתבססים על המצב הפיזי!
        if (isAttacking() && (now > timerAttackDuration + attackDuration)) {
            setAttacking(false); // הפונקציה ב-Character כבר תדאג להעביר אותנו ל-IDLE פיזית
            this.moving = true;  // המוח מורה לגוף שהוא רשאי לזוז שוב
        }
    }

    protected void checkTimeToAttackTimer() {
        if (preparingAttack && (now > timerBeforeAttack + timeToAttack)) {
            setAttacking(true); // זה יעדכן את המצב הפיזי ל-ATTACKING (אם יש מספיק סטמינה)
            preparingAttack = false;
            timerAttackDuration = now;
        }
    }

    public boolean isPreparingAttack() {
        return preparingAttack;
    }

    @Override
    protected void onDeath(Character killer) {
        drop.dropXp();

        if (killer != null && killer.getFaction() == GameConstants.Faction.PLAYER) {
            MyApp.getProgress().increaseEnemiesDefeated();
        }
    }

    @Override
    protected void onDeathAnimationFinished() {
        this.active = false;
        drop.dropLoot();
        ObjectPoolManager.releaseCharacter(this);
    }
}