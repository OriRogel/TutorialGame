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
    // מנגנון ויסות צלילים סטטי - מונע רעש מוגזם כשיש הרבה מפלצות
    private static int globalSoundCount = 0;
    private static long lastGlobalResetTime = 0;
    private static final int MAX_GLOBAL_SOUNDS_PER_SEC = 2;

    private long lastSoundTime;
    private long lastDirChange;
    private boolean maxRageSoundPlayed = false;

    // הגדרות חרוט ראייה של השחקן
    private static final float FOV_ANGLE = 35f; // חצי זווית (סה"כ 70 מעלות)
    private static final float PLAYER_VISION_RANGE = TILE_SIZE * 8f;
    private static final float PLAYER_VISION_RANGE_SQ = PLAYER_VISION_RANGE * PLAYER_VISION_RANGE; // לטובת חישוב מהיר

    private boolean moving;

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
        // איפוס המונה הגלובלי בכל שנייה
        if (now - lastGlobalResetTime > 1050) {
            globalSoundCount = 0;
            lastGlobalResetTime = now;
        }

        if (ragePercentage > 0.1f && ragePercentage < 0.9f && now - lastSoundTime > 3000) {
            // רק אם לא עברנו את המכסה הגלובלית לשנייה זו
            if (globalSoundCount < MAX_GLOBAL_SOUNDS_PER_SEC) {
                SoundManager.getInstance(BaseActivity.getContext()).playSpatialSfx(R.raw.sfx_scarry2, hitBox.centerX());
                lastSoundTime = now;
                globalSoundCount++;
            }
        }
        else if (ragePercentage >= 1.0f && !maxRageSoundPlayed) {
            SoundManager.getInstance(BaseActivity.getContext()).playSpatialSfx(R.raw.sfx_scarry1, hitBox.centerX());
            maxRageSoundPlayed = true;
            globalSoundCount++; // צליל זעם מקסימלי נספר גם הוא
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
            this.moving = false; // עדכון הגוף
            return;
        }

        checkTimeToAttackTimer();
        updateAttackTimer();

        float dist = CollisionUtils.getDistance(hitBox.centerX(), hitBox.centerY(), currentTarget.getHitBox().centerX(), currentTarget.getHitBox().centerY());
        boolean isEnragedEnough = ragePercentage > 0.5f || dist < TILE_SIZE * 0.8f;

        if (isEnragedEnough && !isPreparingAttack() && !isAttacking()) {
            this.moving = true; // המוח אומר לגוף לזוז
            animation.update();
            turnTowardsTarget(currentTarget);
            float currentSpeed = (float) (baseSpeed * delta * (1f + ragePercentage * 2.0f));
            movementComponent.moveInDir(currentSpeed, gameMap);
        } else if (!isEnragedEnough) {
            if (ragePercentage <= 0.1f) {
                currentBehavior = AiBehavior.IDEAL;
                currentTarget = null;
                this.moving = false;
                return;
            }
            this.moving = false; // עוצרים במקום ובוהים
            turnTowardsTarget(currentTarget);
        }

        if (dist < hitBox.width() * 1.2f || isPreparingAttack() || isAttacking()) {
            combatComponent.update();
            processAttack();
        }

        lastTimeSeenPlayer = now;
    }

    @Override
    protected void onIdeal(double delta, GameMap gameMap) {
        // ב-Npc קראנו ל-moving = false בהתחלה, אז נעדכן בהתאם ללוגיקה הפנימית
        super.onIdeal(delta, gameMap);

        if (now - lastDirChange >= MyApp.RND.nextInt(2500) + 2500) {
            faceDir = MyApp.RND.nextInt(4);
            lastDirChange = now;
            // קוד נקי יותר לקביעת תנועה רנדומלית
            moving = (MyApp.RND.nextFloat() <= 0.8f);
        }

        if (moving) {
            animation.update();
            float moveStep = (float) (baseSpeed * delta);
            movementComponent.moveInDir(moveStep, gameMap);
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