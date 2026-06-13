package com.example.tutorialgame.entities.characters.enemies;

import static com.example.tutorialgame.engine.core.GameConstants.Sprite.TILE_SIZE;
import android.graphics.PointF;

import com.example.tutorialgame.MyApp;
import com.example.tutorialgame.R;
import com.example.tutorialgame.components.drop.DropEntry;
import com.example.tutorialgame.engine.audio.SoundManager;
import com.example.tutorialgame.engine.core.GameConstants;
import com.example.tutorialgame.entities.Weapons;
import com.example.tutorialgame.entities.characters.GameCharacters;
import com.example.tutorialgame.entities.characters.Player;
import com.example.tutorialgame.entities.foregrounds.statics.Items;
import com.example.tutorialgame.environments.GameMap;
import com.example.tutorialgame.ui.base.BaseActivity;
import com.example.tutorialgame.utils.CollisionUtils;

/**
 * מפלצת ייחודית שמתעצבנת כשהשחקן מסתכל עליה (בהשראת אנדרמן).
 * משתמשת במנגנון ויסות צלילים גלובלי ובחרוט ראייה עבור השחקן.
 */
public class EyeMonster extends Enemy {
    private long lastSoundTime;
    private long lastDirChange;
    private boolean maxRageSoundPlayed = false;
    private boolean lookingAround; // האם כרגע המפלצת עוצרת ומסתכלת מסביב
    private int lookSteps; // כמה פעמים היא החליפה כיוון בשלב הבהייה

    // הגדרות חרוט ראייה של השחקן
    private static final float FOV_ANGLE = 35f; // חצי זווית (סה"כ 70 מעלות)
    private static final float PLAYER_VISION_RANGE = TILE_SIZE * 8f;
    private static final float PLAYER_VISION_RANGE_SQ = PLAYER_VISION_RANGE * PLAYER_VISION_RANGE; // לטובת חישוב מהיר

    // הסרנו את השדה moving המקומי שהסתיר את זה של מחלקת הבסיס

    public EyeMonster(PointF pos) {
        super(pos, GameCharacters.EYE_MONSTER, 60, 150, 1.2f);
    }

    @Override
    public void update(double delta, GameMap gameMap) {
        super.update(delta, gameMap);
        Player p = gameMap.getPlayer();
        if (p == null || isDead()) return;

        // בדיקה האם השחקן רואה את המפלצת בתוך חרוט הראייה שלו
        boolean isLookingAtMe = isPlayerLookingAtMe(p);

        // 1. לוגיקת זעם: עולה ככל שהשחקן מסתכל על העין
        if (isLookingAtMe) {
            ragePercentage = Math.min(1.0f, ragePercentage + (float) delta * 0.4f);
            handleScarySounds();
        } else {
            ragePercentage = Math.max(0f, ragePercentage - (float) delta * 0.15f);
            if (ragePercentage < 0.9f) maxRageSoundPlayed = false;
        }

        // 2. בדיקת קרבה קיצונית - תוקפת בכל מקרה אם השחקן קרוב מדי
        float distSq = CollisionUtils.getDistance(hitBox.centerX(), hitBox.centerY(), p.getHitBox().centerX(), p.getHitBox().centerY());

        // התאמה למערכת המצבים החדשה
        if (distSq < TILE_SIZE) {
            ragePercentage = 1;
            currentBehavior = AiBehavior.AGGRESSIVE;
            currentTarget = p;
        }
    }

    private void handleScarySounds() {
        if (ragePercentage > 0.1f && ragePercentage < 0.9f && now - lastSoundTime > 3000) {
            SoundManager.getInstance(BaseActivity.getContext()).playSpatialSfxThrottled(R.raw.sfx_scarry2, hitBox.centerX(), "scary_ambient", 2, 1000);
            lastSoundTime = now;
        }
        else if (ragePercentage >= 1.0f && !maxRageSoundPlayed) {
            SoundManager.getInstance(BaseActivity.getContext()).playSpatialSfxThrottled(R.raw.sfx_scarry1, hitBox.centerX(), "scary_ambient", 2, 1000);
            maxRageSoundPlayed = true;
        }
    }

    /**
     * מחשב האם השחקן מסתכל לכיוון המפלצת בטווח ראייה ובזווית חרוט.
     */
    private boolean isPlayerLookingAtMe(Player p) {
        float dx = hitBox.centerX() - p.getHitBox().centerX();
        float dy = hitBox.centerY() - p.getHitBox().centerY();

        // 1. בדיקת טווח מרחק בסיסי (אופטימיזציה: מרחק בריבוע חוסך Math.sqrt)
        float distSq = dx * dx + dy * dy;
        if (distSq > PLAYER_VISION_RANGE_SQ) return false;

        // 2. חישוב זווית מהשחקן למפלצת (במעלות)
        double angleToMonster = Math.toDegrees(Math.atan2(dy, dx));

        // 3. הגדרת זווית המבט של השחקן לפי כיוון התנועה
        double facingAngle = 0;
        switch (p.getFaceDir()) {
            case GameConstants.Face_Dir.RIGHT: facingAngle = 0; break;
            case GameConstants.Face_Dir.DOWN:  facingAngle = 90; break;
            case GameConstants.Face_Dir.LEFT:  facingAngle = 180; break;
            case GameConstants.Face_Dir.UP:    facingAngle = -90; break; // או 270
        }

        // 4. חישוב הפרש זוויות ושימוש בערך מוחלט על המעגל
        double angleDiff = Math.abs(angleToMonster - facingAngle);
        if (angleDiff > 180) angleDiff = 360 - angleDiff;

        // האם בתוך החרוט?
        return angleDiff <= FOV_ANGLE;
    }

    @Override
    public void setCharacterDead() {
        super.setCharacterDead();
        SoundManager.getInstance(BaseActivity.getContext()).playSpatialSfx(R.raw.sfx_scarry3, hitBox.centerX());
    }

    @Override
    protected void onAggressive(double delta, GameMap gameMap) {
        if (currentTarget == null) {
            currentBehavior = AiBehavior.IDEAL;
            this.moving = false;
            return;
        }

        checkTimeToAttackTimer();
        updateAttackTimer();

        float dist = CollisionUtils.getDistance(hitBox.centerX(), hitBox.centerY(), currentTarget.getHitBox().centerX(), currentTarget.getHitBox().centerY());
        
        // EyeMonster logic: move if enraged enough OR if player is relatively close
        boolean shouldMove = ragePercentage > 0.4f || dist < TILE_SIZE * 3f;

        if (shouldMove && !isPreparingAttack() && !isAttacking()) {
            this.moving = true;
            animation.update();
            turnTowardsTarget(currentTarget);
            float currentSpeed = (float) (baseSpeed * delta * (1f + ragePercentage * 2.0f));
            movementComponent.moveInDir(currentSpeed, gameMap);
        } else if (!shouldMove && !isPreparingAttack() && !isAttacking()) {
            if (ragePercentage <= 0.05f && dist > TILE_SIZE * 5f) {
                currentBehavior = AiBehavior.IDEAL;
                currentTarget = null;
                this.moving = false;
                return;
            }
            this.moving = false; // Stop and stare
            turnTowardsTarget(currentTarget);
        }

        if (dist < hitBox.width() * 1.3f || isPreparingAttack() || isAttacking()) {
            combatComponent.update();
            processAttack();
        }

        lastTimeSeenPlayer = now;
    }

    @Override
    protected void onIdeal(double delta, GameMap gameMap) {
        findTargetClap(gameMap);
        setAttacking(false);
        preparingAttack = false;

        if (currentTarget != null) {
            float distSq = CollisionUtils.getDistance(hitBox.centerX(), hitBox.centerY(), currentTarget.getHitBox().centerX(), currentTarget.getHitBox().centerY());
            // Only go aggressive if enraged or player is very close
            if (ragePercentage > 0.15f || distSq < TILE_SIZE * 2f) {
                currentBehavior = AiBehavior.AGGRESSIVE;
                return;
            }
        }

        if (lookingAround) {
            this.moving = false;
            if (now - lastDirChange > 400 + MyApp.getRandom().nextInt(300)) {
                faceDir = MyApp.getRandom().nextInt(4);
                lastDirChange = now;
                lookSteps++;

                if (lookSteps > 2 + MyApp.getRandom().nextInt(3)) {
                    lookingAround = false;
                    this.moving = true; 
                    faceDir = MyApp.getRandom().nextInt(4);
                    lastDirChange = now; 
                }
            }
        } else {
            this.moving = true; 
            if (now - lastDirChange > 3000 + MyApp.getRandom().nextInt(2000)) {
                this.moving = false; 
                lookingAround = true;
                lookSteps = 0;
                lastDirChange = now;
            }
        }

        if (this.moving) {
            animation.update();
            float moveStep = (float) (baseSpeed * delta);
            
            // Check if blocked by wall or obstacle in current direction
            float dx = 0, dy = 0;
            switch (faceDir) {
                case GameConstants.Face_Dir.UP:    dy = -moveStep; break;
                case GameConstants.Face_Dir.DOWN:  dy = moveStep; break;
                case GameConstants.Face_Dir.LEFT:  dx = -moveStep; break;
                case GameConstants.Face_Dir.RIGHT: dx = moveStep; break;
            }

            if (movementComponent.canMoveTo(dx, dy, gameMap)) {
                movementComponent.moveInDir(moveStep, gameMap);
            } else {
                // Hitting a wall: turn immediately
                faceDir = MyApp.getRandom().nextInt(4);
                lastDirChange = now; 
            }
        } else {
            animation.resetAnimation();
        }
    }

    @Override
    public Weapons getWeapon() { return Weapons.NULL; }
    @Override
    public int getAttackDamage() { return 10 + (int) (ragePercentage * 15); }
    @Override
    protected int getHealth() { return 80; }
    @Override
    protected int getStamina() { return 200; }
    @Override
    protected int getStaminaCoolDown() { return 500; }
    @Override
    public int getImpactSfx() { return R.raw.sfx_impact_enemy1; }
    @Override
    public DropEntry getDropEntry() { return new DropEntry(1, 2, 20, 30, 0.5, Items.FISH); }
    @Override
    public float getTimeToApexSec() { return 0.3f; }
    @Override
    public float getDesiredTiles() { return 0.8f; }
    @Override
    public int getAniIndex() { return animation.getAniIndex(); }
}