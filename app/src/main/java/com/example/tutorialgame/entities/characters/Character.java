package com.example.tutorialgame.entities.characters;

import static com.example.tutorialgame.engine.core.GameConstants.Animation.DEATH_FRAME_INDEX;
import static com.example.tutorialgame.engine.core.GameConstants.Face_Dir.DOWN;
import static com.example.tutorialgame.engine.core.GameConstants.Face_Dir.LEFT;
import static com.example.tutorialgame.engine.core.GameConstants.Face_Dir.RIGHT;
import static com.example.tutorialgame.engine.core.GameConstants.Face_Dir.UP;
import static com.example.tutorialgame.engine.core.GameConstants.Sprite.HITBOX_OFFSET;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.RectF;

import androidx.annotation.NonNull;

import com.example.tutorialgame.R;
import com.example.tutorialgame.components.AnimationComponent;
import com.example.tutorialgame.components.CombatComponent;
import com.example.tutorialgame.components.DialogueComponent;
import com.example.tutorialgame.components.EmoteComponent;
import com.example.tutorialgame.components.HealthComponent;
import com.example.tutorialgame.components.JumpComponent;
import com.example.tutorialgame.components.MovementComponent;
import com.example.tutorialgame.components.StaminaComponent;
import com.example.tutorialgame.components.drop.DropComponent;
import com.example.tutorialgame.components.drop.DropEntry;
import com.example.tutorialgame.engine.audio.SoundManager;
import com.example.tutorialgame.engine.renderer.CharacterRenderer;
import com.example.tutorialgame.entities.Entity;
import com.example.tutorialgame.entities.Weapons;
import com.example.tutorialgame.engine.core.GameConstants;
import com.example.tutorialgame.environments.GameMap;
import com.example.tutorialgame.managers.MapManager;
import com.example.tutorialgame.ui.base.BaseActivity;
import java.util.HashMap;
import java.util.Map;

/**
 * Scene class for all living characters in the game.
 * Uses a component-based architecture to handle physics, combat, and rendering.
 */
public abstract class Character extends Entity {
    protected float homeX, homeY, ragePercentage;
    protected int faceDir = DOWN, faction;
    protected final GameCharacters gameCharType;

    // הורדנו את dying, jumping, attacking - אנחנו משתמשים כעת ב-state!
    protected boolean moving, attackChecked;

    protected long now;
    protected final float viewDistance;
    private final Map<Character, Long> badBoyList;

    // --- State Management ---
    public enum EntityState {
        IDLE, WALKING, ATTACKING, JUMPING, ROLLING, DYING
    }
    protected EntityState state = EntityState.IDLE;

    // --- Components ---
    protected final HealthComponent health;
    protected final StaminaComponent stamina;
    protected final AnimationComponent animation;
    protected final JumpComponent jumpComponent;
    protected final MovementComponent movementComponent;
    protected final DialogueComponent dialogueComponent;
    protected final CombatComponent combatComponent;
    protected EmoteComponent emoteComponent;
    protected final DropComponent drop;

    private final CharacterRenderer characterRenderer;

    // --- Hitboxes ---
    private final RectF projectedHitBox = new RectF();

    // --- Death Handling ---
    private static final double DEATH_STEP_DELAY_SEC = 0.2; // 200ms
    private int deathStep = 0;
    private double deathTimer = 0;
    protected int diedBy = -1;
    private final boolean hasRage;

    public Character(PointF pos, GameCharacters gameCharType, int faction) {
        super(pos, gameCharType.getWidth() * HITBOX_OFFSET, gameCharType.getHeight() * HITBOX_OFFSET, true);
        this.gameCharType = gameCharType;
        this.faction = faction;
        this.moving = false;
        this.viewDistance = gameCharType.getViewDistance();
        this.hasRage = gameCharType.hasRage();

        this.health = new HealthComponent(getHealth());
        this.stamina = new StaminaComponent(getStamina(), getStaminaCoolDown());
        this.animation = new AnimationComponent(GameConstants.Animation.SPEED, GameConstants.Animation.AMOUNT);
        this.jumpComponent = new JumpComponent(getDesiredTiles(), getTimeToApexSec());
        this.movementComponent = new MovementComponent(this);
        this.combatComponent = new CombatComponent(this);

        this.emoteComponent = new EmoteComponent(this);
        this.dialogueComponent = new DialogueComponent();
        this.characterRenderer = new CharacterRenderer(this);
        this.drop = new DropComponent(this);

        badBoyList = new HashMap<>();
        homeX = pos.x;
        homeY = pos.y;
    }

    // --- מנהל המצבים החדש שלנו ---
    public void changeState(EntityState newState) {
        // חוסם שינוי מצב לדמות מתה, *אלא אם כן* מנסים להחזיר אותה לחיים (מצב IDLE)
        if (this.state == EntityState.DYING && newState != EntityState.IDLE) return;

        this.state = newState;

        // אם הדמות חזרה ל-IDLE, אנחנו מאפסים את המשתנים הפנימיים של המוות
        // זה מתקן גם את השחקן וגם את המפלצות שחוזרות מה-Object Pool
        if (newState == EntityState.IDLE) {
            this.deathStep = 0;
            this.deathTimer = 0;
        }
    }

    public void init(PointF pos) {
        this.active = true;
        this.collider = true;
        this.deathStep = 0;
        this.deathTimer = 0;
        this.diedBy = -1;
        this.moving = false;
        this.faceDir = DOWN;

        changeState(EntityState.IDLE); // מאתחל את המצב

        this.homeX = pos.x;
        this.homeY = pos.y;
        this.spawnPos.set(pos);
        this.hitBox.offsetTo(pos.x, pos.y);

        health.reset();
        stamina.reset();
        movementComponent.cancelKnockback(); // Add this
        animation.resetAnimation();
        badBoyList.clear();
    }

    protected synchronized Map<Character, Long> getBadBoyList() {
        return badBoyList;
    }

    public void update(double delta, GameMap gameMap) {
        now = System.currentTimeMillis();

        if (state == EntityState.DYING) {
            updateDeathSequence(delta);
            return;
        }

        health.update(delta);
        stamina.update(delta);
        jumpComponent.update(delta);
        emoteComponent.update(delta);
        movementComponent.update(delta, gameMap);
        combatComponent.update();

        updateProjectedHitBox();

        if (isJumping() && System.currentTimeMillis() - jumpComponent.getJumpStartTime() > 300) {
            setJumping(false);
        }

        // אם לא עושים פעולה מיוחדת, מתעדכנים בין הליכה לעמידה
        if (state == EntityState.IDLE || state == EntityState.WALKING) {
            changeState(moving ? EntityState.WALKING : EntityState.IDLE);
        }
    }

    private void updateDeathSequence(double delta) {
        deathTimer += delta;

        if (deathTimer >= DEATH_STEP_DELAY_SEC) {
            deathTimer = 0;

            switch (deathStep) {
                case 0: faceDir = DOWN; break;
                case 1: faceDir = LEFT; break;
                case 2: faceDir = UP; break;
                case 3: faceDir = RIGHT; break;
                case 4:
                    faceDir = DOWN;
                    animation.setAniIndex(DEATH_FRAME_INDEX);
                    break;
                case 5:
                    onDeathAnimationFinished();
                    return;
            }
            deathStep++;
        }
    }

    public void turnTowardsTarget(Character target) {
        if (target == null) return;
        float xDelta = hitBox.centerX() - target.getHitBox().centerX();
        float yDelta = hitBox.centerY() - target.getHitBox().centerY();
        if (Math.abs(xDelta) > Math.abs(yDelta))
            faceDir = (xDelta > 0) ? LEFT : RIGHT;
        else faceDir = (yDelta > 0) ? UP : DOWN;
    }

    public Bitmap getCurrentSprite() {
        return gameCharType.getSprite(getAniIndex(), faceDir);
    }

    @Override
    public void draw(Canvas c) {
        characterRenderer.drawFullCharacter(c);
    }

    // ========== Combat & Damage Logic ==========

    public void damageCharacter(Character target) {
        if (canDamage(target)) {
            combatComponent.attack(target);
        }
    }

    public void takeDamage(int amount, Character attacker) {
        takeDamage(amount, attacker, false);
    }

    public void takeDamage(int amount, Character attacker, boolean isCritical) {
        if (isDead() || amount <= 0) return;
        health.takeDamage(amount, isCritical);
        checkCharacterDead(attacker);
    }

    public boolean canDamage(Character target) {
        return this.faction != target.getFaction();
    }

    public boolean isDead() {
        return health.isDead() || state == EntityState.DYING;
    }

    public void setCharacterDead() {
        changeState(EntityState.DYING);
        this.collider = false;
        animation.resetAnimation();
        deathStep = 0;
        deathTimer = 0;
    }

    public void checkCharacterDead(Character attacker) {
        if (health.isDead() && diedBy == -1) {
            setCharacterDead();
            diedBy = attacker != null ? attacker.getFaction() : -1;
            onDeath(attacker);
        }
    }

    // ========== Abstract Hooks ==========
    protected abstract void onDeathAnimationFinished();
    protected abstract void onDeath(Character killer);
    public abstract Weapons getWeapon();
    public abstract int getAttackDamage();
    protected abstract int getHealth();
    protected abstract int getStamina();
    protected abstract int getStaminaCoolDown();
    public abstract int getImpactSfx();
    public abstract DropEntry getDropEntry();
    public abstract float getTimeToApexSec();
    public abstract float getDesiredTiles();

    // ========== Getters & Setters ==========
    public void setJumping(boolean shouldJump) {
        if (shouldJump && stamina.hasEnough(10) && !stamina.isStaminaLocked() && state != EntityState.JUMPING) {
            stamina.useStamina(10);
            SoundManager.getInstance(BaseActivity.getContext()).playSpatialSfx(R.raw.sfx_jump, hitBox.centerX());
            jumpComponent.startJump();
            changeState(EntityState.JUMPING);
            animation.setAniIndex(5);
        } else if (!shouldJump && state == EntityState.JUMPING) {
            changeState(EntityState.IDLE);
            animation.setAniIndex(0);
        }
    }

    public void setAttacking(boolean shouldAttack) {
        if (shouldAttack && stamina.hasEnough(5) && !stamina.isStaminaLocked() && state != EntityState.ATTACKING) {
            stamina.useStamina(5);
            combatComponent.onSwing();
            attackChecked = false;
            changeState(EntityState.ATTACKING);
        } else if (!shouldAttack && state == EntityState.ATTACKING) {
            changeState(EntityState.IDLE);
        }
    }

    public void moveToMap(String targetMapName, float x, float y) {
        MapManager.getCurrentMap().removeCharacter(this.getGameCharType().getName());
        this.teleportTo(x, y);
        MapManager.getMapByName(targetMapName).addCharacter(this);
    }

    public void teleportTo(float x, float y) {
        this.homeX = x;
        this.homeY = y;
        this.hitBox.offsetTo(x, y);
        this.spawnPos.set(x, y);
    }

    public void setHome(float x, float y) {
        this.homeX = x;
        this.homeY = y;
    }

    private void updateProjectedHitBox() {
        float e = getElevation();
        projectedHitBox.set(hitBox.left, hitBox.top - e, hitBox.right, hitBox.bottom - e);
    }

    public boolean isJumping() { return state == EntityState.JUMPING; }
    public boolean isAttacking() { return state == EntityState.ATTACKING; }
    public boolean isDamaged() { return health.getFlashAlpha() > 0f; }
    public boolean hasRage() { return hasRage; }
    public float getRagePercentage() { return ragePercentage; }
    public HealthComponent getHealthComponent() { return health; }
    public CombatComponent getCombatComponent() { return combatComponent; }
    public MovementComponent getMovementComponent() { return movementComponent; }
    public int getCurrentHealth() { return health.getCurrentHealth(); }
    public int getMaxHealth() { return health.getMaxHealth(); }
    public int getFaction() { return faction; }
    public int getFaceDir() { return faceDir; }
    public void setFaceDir(int faceDir) { this.faceDir = faceDir; }
    public GameCharacters getGameCharType() { return gameCharType; }
    public RectF getProjectedHitBox() { return projectedHitBox; }
    public boolean isAirborne() { return jumpComponent.isAirborne(); }
    public float getElevation() { return jumpComponent.getElevation(); }
    public RectF getAttackBox() { return combatComponent.getAttackBox(); }
    public float getJumpFraction() { return jumpComponent.getJumpFraction();}
    public float getViewDistance() { return  viewDistance;}
    public EmoteComponent getEmoteComponent() { return emoteComponent; }
    public StaminaComponent getStaminaComponent() { return stamina; }
    public void setWeapon(@NonNull Weapons weapon) { combatComponent.setWeapon(weapon); }
    public DialogueComponent getDialogueComponent() { return dialogueComponent; }
    public int getDiedBy() { return diedBy; }

    public int getAniIndex() {
        switch (state) {
            case ATTACKING: return 4;
            case JUMPING: return 5;
            case ROLLING: return 7;
            case DYING:
            case IDLE:
            case WALKING:
            default:
                return animation.getAniIndex();
        }
    }

    public void resetAnimation() { animation.resetAnimation(); }
    public void onDialogue() {}
}