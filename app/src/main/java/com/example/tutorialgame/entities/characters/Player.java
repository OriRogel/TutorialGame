package com.example.tutorialgame.entities.characters;

import static com.example.tutorialgame.engine.core.GameConstants.Faction.PLAYER;
import static com.example.tutorialgame.engine.core.GameConstants.Sprite.TILE_SIZE;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.RectF;
import com.example.tutorialgame.R;
import com.example.tutorialgame.cloud.UserRepository;
import com.example.tutorialgame.cloud.document.StatsDoc;
import com.example.tutorialgame.cloud.document.WorldStateDoc;
import com.example.tutorialgame.components.drop.DropEntry;
import com.example.tutorialgame.engine.audio.MusicManager;
import com.example.tutorialgame.engine.audio.SoundManager;
import com.example.tutorialgame.engine.core.GameConstants;
import com.example.tutorialgame.engine.ui.effects.WorldAnimationEffect;
import com.example.tutorialgame.engine.ui.effects.impcateffects.ImpactEffectType;
import com.example.tutorialgame.entities.Weapons;
import com.example.tutorialgame.entities.foregrounds.breakable.BreakableEntity;
import com.example.tutorialgame.environments.GameMap;
import com.example.tutorialgame.environments.SurfaceType;
import com.example.tutorialgame.managers.CameraManager;
import com.example.tutorialgame.ui.base.BaseActivity;
import com.example.tutorialgame.utils.Other;
import java.util.Collections;
import java.util.List;

/**
 * Represents the main playable character in the game world.
 * Manages input, movement, and unique visual effects like landing dust.
 */
public class Player extends Character {
    private Runnable onDeathCompleteCallback;
    private long attackStartTime;
    private int attackSpeed;
    private PointF lastTouchDiff;

    private Character currentSpeaker;
    private int lastAniIndex;
    private List<String> interiorDialogue = null;
    private boolean pendingDialogue;

    private final WorldAnimationEffect landingDustEffect = new WorldAnimationEffect();
    private final StatsDoc statsDoc;
    private final WorldStateDoc stateDoc;
    private final UserRepository userRepository;

    public Player(UserRepository userRepository) {
        super(new PointF(userRepository.getWorldStateDoc().getLastPosition().x*TILE_SIZE, userRepository.getWorldStateDoc().getLastPosition().y*TILE_SIZE), GameCharacters.PLAYER, PLAYER);
        this.userRepository = userRepository;
        this.statsDoc = userRepository.getPlayerStats();
        this.stateDoc = userRepository.getWorldStateDoc();

        this.attackSpeed = statsDoc.getAttackSpeed();

        // 1. סנכרון הנתונים האמיתיים (מכיוון שהבנאי של Character השתמש בערכי ברירת מחדל)
        refreshStats();
        setWeapon(stateDoc.getCurrentWeapon());

        // 2. הגדרת המאזין לנחיתה
        this.jumpComponent.setOnLandListener(() -> {
            SoundManager.getInstance(BaseActivity.getContext()).playRndPitchSfx(R.raw.sfx_landing);
            landingDustEffect.init(ImpactEffectType.DUST_CLOUD_LANDING, hitBox.centerX(), hitBox.bottom);
            CameraManager.startShake(0.09f, 0.07f);
        });
    }

    public void reset() {
        movementComponent.cancelKnockback();
        teleportTo(stateDoc.getLastPosition().x*TILE_SIZE, stateDoc.getLastPosition().y*TILE_SIZE);
        health.reset();
        stamina.reset();

        // איפוס המצבים החדש שלנו
        changeState(EntityState.IDLE);
        this.diedBy = -1;
        this.collider = true;
    }

    public void refreshStats() {
        int newMax = getHealth();
        int oldMax = health.getMaxHealth();

        if (newMax != oldMax) {
            health.setMaxHealth(newMax);
            if (newMax > oldMax) {
                health.heal(newMax - oldMax);
            }
        }
        if (stamina.getMaxStamina() != statsDoc.getMaxStamina()) {
            stamina.setMaxStamina(statsDoc.getMaxStamina());
        }
        this.attackSpeed = statsDoc.getAttackSpeed();
    }

    @Override
    public void update(double delta, GameMap gameMap) {
        super.update(delta, gameMap);

        // עדכון אפקט האבק
        landingDustEffect.update();

        if (isDistancedFromNPC()) currentSpeaker = null;

        if (this.moving) {
            animation.update();
            updatePlayerMove(delta, gameMap);
            handleFootstepSound(gameMap);
        }

        if (isAttacking()) {
            if (!attackChecked) {
                checkPlayerAttack(gameMap.getCharacterArrayList(), gameMap.getBreakableEntities());
                attackChecked = true;
            }
            if (System.currentTimeMillis() - this.attackStartTime > attackSpeed) {
                setAttacking(false);
            }
        }

        if (isDead()) health.update(delta);
    }

    @Override
    public void draw(Canvas c) {
        // ציור האבק מתחת לשחקן (לפני ה-super.draw)
        landingDustEffect.draw(c);
        super.draw(c);
    }

    private void handleFootstepSound(GameMap gameMap) {
        int currentAni = animation.getAniIndex();
        if (currentAni != lastAniIndex) {
            lastAniIndex = currentAni;
            if (currentAni == 1 || currentAni == 3) {
                playFootstepSound(gameMap);
            }
        }
    }

    private void playFootstepSound(GameMap gameMap) {
        if (isJumping()) return; // עודכן להשתמש במתודה!
        int tileX = (int) (hitBox.centerX() / TILE_SIZE);
        int tileY = (int) (hitBox.bottom / TILE_SIZE);
        SurfaceType surfaceType = gameMap.getSurfaceTypeAt(tileX, tileY);
        if (surfaceType != SurfaceType.NONE) {
            SoundManager.getInstance(BaseActivity.getContext()).playRndPitchSfx(surfaceType.getSfxId());
        }
    }

    public void setMovementInput(boolean movePlayer, PointF lastTouchDiff) {
        this.moving = movePlayer;
        this.lastTouchDiff = lastTouchDiff;
    }

    @Override
    public void setAttacking(boolean attacking) {
        super.setAttacking(attacking);
        if (attacking && stamina.hasEnough(5) && !stamina.isStaminaLocked()) {
            this.attackStartTime = System.currentTimeMillis();
        }
    }

    @Override
    public void setJumping(boolean jumping) {
        if (!isAirborne()) {
            super.setJumping(jumping);
        }
    }

    private void checkPlayerAttack(List<Character> characters, List<BreakableEntity> breakables) {
        if (characters == null) return;

        RectF attackBox = getAttackBox();
        for (Character target : characters) {
            if (!target.isActive() || target.isDead() || target == this || target.getFaction() == PLAYER) continue;

            if (RectF.intersects(attackBox, target.getProjectedHitBox())) {
                damageCharacter(target);
                SoundManager.getInstance(BaseActivity.getContext()).playSpatialSfx(target.getImpactSfx(), target.getHitBox().centerX());
            }
        }

        if (breakables == null) return;
        for (BreakableEntity target : breakables) {
            RectF targetHitBox = target.getHitBox();
            if (!RectF.intersects(attackBox, targetHitBox)) continue;

            if (target.handleHit()) {
                getMovementComponent().applyRecoil(50, targetHitBox.centerX(), targetHitBox.centerY());
                CameraManager.startShake(1.67f, 0.1f);
                pendingDialogue = true;
                setInteriorDialogue(Collections.singletonList(BaseActivity.getContext().getString(R.string.not_strong)));
                return;
            }
        }
    }

    public void setInteriorDialogue(List<String> dialogue) {
        interiorDialogue = dialogue;
        pendingDialogue = true;
    }

    @Override
    public void takeDamage(int amount, Character attacker) {
        super.takeDamage(amount, attacker);
        SoundManager.getInstance(BaseActivity.getContext()).playSpatialSfx(getImpactSfx(), hitBox.centerX());
    }

    @Override
    public boolean canDamage(Character target) {
        return target.getFaction() != GameConstants.Faction.ALLY;
    }

    @Override
    public Weapons getWeapon() {
        return (stateDoc != null) ? stateDoc.getCurrentWeapon() : Weapons.NULL;
    }

    @Override
    public int getAttackDamage() {
        return (statsDoc != null) ? statsDoc.getStrength() : 10;
    }

    @Override
    protected int getHealth() {
        return (statsDoc != null) ? statsDoc.getMaxHealth() : 100;
    }

    @Override
    protected int getStamina() {
        return (statsDoc != null) ? statsDoc.getMaxStamina() : 50;
    }

    @Override
    protected int getStaminaCoolDown() { return 5000; }

    @Override
    public int getImpactSfx() { return R.raw.sfx_impact_player; }

    @Override
    public DropEntry getDropEntry() { return null; }

    @Override
    public float getTimeToApexSec() { return 0.35f; }

    @Override
    public float getDesiredTiles() { return 1.5f; }

    @Override
    public int getCritChance() {
        return (statsDoc != null) ? statsDoc.getCritHitChance() : 0;
    }

    private boolean isDistancedFromNPC() {
        if (currentSpeaker == null) return true;
        return !Other.IsCharacterSeesTarget(this, currentSpeaker);
    }

    public void setCurrentSpeaker(Character npc) {
        if (currentSpeaker == null) currentSpeaker = npc;
    }

    public Character getCurrentSpeaker() { return currentSpeaker; }

    private void updatePlayerMove(double delta, GameMap map) {
        if (lastTouchDiff == null) return;
        float dx = lastTouchDiff.x;
        float dy = lastTouchDiff.y;
        updatePlayerFaceDir(dx, dy);

        float dist = (float) Math.hypot(dx, dy);
        if (dist == 0) return;

        float baseSpeed = (float) (delta * 3.125*TILE_SIZE) * dist;
        if (stamina.isStaminaLocked()) baseSpeed *= 0.6f;
        animation.setSpeed((int) (GameConstants.Animation.SPEED / Math.max(0.5f, dist)));

        float nx = dx / dist;
        float ny = dy / dist;
        float moveX = nx * baseSpeed;
        float moveY = ny * baseSpeed;

        movementComponent.applyMovement(moveX, moveY, map);
    }

    private void updatePlayerFaceDir(float dx, float dy) {
        if (Math.abs(dx) > Math.abs(dy)) {
            setFaceDir(dx > 0 ? GameConstants.Face_Dir.RIGHT : GameConstants.Face_Dir.LEFT);
        } else {
            setFaceDir(dy > 0 ? GameConstants.Face_Dir.DOWN : GameConstants.Face_Dir.UP);
        }
    }

    public boolean hasPendingDialogue() {
        return pendingDialogue && interiorDialogue != null && !movementComponent.isKnockbackActive();
    }

    public List<String> consumeInteriorDialogue() {
        pendingDialogue = false;
        List<String> temp = interiorDialogue;
        interiorDialogue = null;
        return temp;
    }

    public void setOnDeathCompleteListener(Runnable listener) {
        this.onDeathCompleteCallback = listener;
    }

    @Override
    protected void onDeathAnimationFinished() {
        if (onDeathCompleteCallback != null) {
            onDeathCompleteCallback.run();
        }
    }

    @Override
    public String getName() {
        return (userRepository.getProfile() != null) ? userRepository.getProfile().getNickname() : super.getName();
    }

    @Override
    protected void onDeath(Character killer) {
        MusicManager.getInstance(BaseActivity.getContext()).play(R.raw.jingle_gameover4);
        MusicManager.getInstance(BaseActivity.getContext()).setLooping(false);
        this.moving = false;
    }
}