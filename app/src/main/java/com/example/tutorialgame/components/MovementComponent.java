package com.example.tutorialgame.components;

import static com.example.tutorialgame.engine.core.GameConstants.Face_Dir.DOWN;
import static com.example.tutorialgame.engine.core.GameConstants.Face_Dir.LEFT;
import static com.example.tutorialgame.engine.core.GameConstants.Face_Dir.RIGHT;
import static com.example.tutorialgame.engine.core.GameConstants.Face_Dir.UP;
import static com.example.tutorialgame.engine.core.GameConstants.Sprite.TILE_SIZE;
import android.graphics.Point;
import android.graphics.RectF;
import com.example.tutorialgame.entities.Entity;
import com.example.tutorialgame.entities.characters.Character;
import com.example.tutorialgame.environments.GameMap;
import com.example.tutorialgame.utils.CollisionUtils;
import java.util.List;

/**
 * Handles the movement logic and collision detection for a {@link Character} entity.
 * This component is responsible for determining whether a proposed movement is valid
 * by checking against map boundaries, other collidable entities, and non-walkable tiles.
 * It encapsulates the rules of movement, including elevation-based interactions,
 * to decide if a character can occupy a new position on the game map.
 * Handles the movement logic, collision detection, and knockback for a {@link Character}.
 */

public class MovementComponent {
    private final RectF tempHitbox = new RectF();
    private final Character self;
    private final RectF selfHitbox;
    private final Point[] tileCords;

    private float kbX, kbY;
    private static final float KB_FRICTION = 0.85f;
    private static final float KB_THRESHOLD = 0.006f * TILE_SIZE;

    public MovementComponent(Character character) {
        this.self = character;
        this.selfHitbox = character.getHitBox();
        this.tileCords = new Point[]{new Point(), new Point(), new Point(), new Point()};
    }

    /**
     * Moves the character in a specific direction based on its current faceDir.
     * <p>
     * Useful for NPCs and AI.
     */
    public void moveInDir(float speed, GameMap gameMap) {
        float dx = 0, dy = 0;

        switch (self.getFaceDir()) {
            case UP: dy = -speed; break;
            case DOWN: dy = speed; break;
            case LEFT: dx = -speed; break;
            case RIGHT: dx = speed; break;
        }
        applyMovement(dx, dy, gameMap);
    }

    /**
     * Applies raw movement with advanced wall-sliding logic.
     */
    public void applyMovement(float dx, float dy, GameMap gameMap) {
        if (isKnockbackActive() || gameMap == null) return;

        // Try moving the full distance
        if (canMoveTo(dx, dy, gameMap)) {
            selfHitbox.offset(dx, dy);
        } else {
            // Slide logic: If diagonal/full move is blocked, try horizontal or vertical independently
            if (dx != 0 && canMoveTo(dx, 0, gameMap)) {
                selfHitbox.offset(dx, 0);
            }
            if (dy != 0 && canMoveTo(0, dy, gameMap)) {
                selfHitbox.offset(0, dy);
            }
        }
    }

    public boolean canMoveTo(float deltaX, float deltaY, GameMap gameMap) {
        tempHitbox.set(selfHitbox);
        tempHitbox.offset(deltaX, deltaY);

        if (tempHitbox.left < 0 || tempHitbox.top < 0 ||
                tempHitbox.right >= gameMap.getMapWidth() ||
                tempHitbox.bottom >= gameMap.getMapHeight()) {
            return false;
        }

        List<Entity> drawables = gameMap.getDrawableList();
        if (drawables != null) {
            for (int i = 0; i < drawables.size(); i++) {
                Entity e = drawables.get(i);
                if (e == null || !e.isCollider() || self == e) continue;
                if (RectF.intersects(e.getHitBox(), tempHitbox)) {
                    if (isBlockedByElevation(self, e)) return false;
                }
            }
        }

        CollisionUtils.setTileCords(selfHitbox, deltaX, deltaY, tileCords);
        return CollisionUtils.areTilesWalkable(tileCords, gameMap);
    }


    private boolean isBlockedByElevation(Character self, Entity other) {
        if (!(other instanceof Character)) return true;

        Character otherChar = (Character) other;
        float threshold = otherChar.getHitBox().height() * 0.5f;

        return self.getElevation() <= otherChar.getElevation() + threshold;
    }


    public float applyKnockback(float weaponForce, float attackerMass, int totalDamage, float sourceX, float sourceY) {
        if (weaponForce <= 0) return 0;

        float targetMass = self.getGameCharType().getMass();
        float finalForce = weaponForce * (1f + (totalDamage / 100f)) * (attackerMass / targetMass);
        float dx = selfHitbox.centerX() - sourceX;
        float dy = selfHitbox.centerY() - sourceY;
        float dist = (float) Math.hypot(dx, dy);

        if (dist > 0) {
            float startingVelocity = finalForce * (1f - KB_FRICTION);
            this.kbX += (dx / dist) * startingVelocity;
            this.kbY += (dy / dist) * startingVelocity;
        }

        return finalForce;
    }

    public void applyRecoil(float force, float targetX, float targetY) {
        if (force <= 0) return;

        float dx = selfHitbox.centerX() - targetX;
        float dy = selfHitbox.centerY() - targetY;
        float dist = (float) Math.hypot(dx, dy);

        if (dist == 0) return;

        float startingVelocity = force * (1f - KB_FRICTION);

        this.kbX += (dx / dist) * startingVelocity;
        this.kbY += (dy / dist) * startingVelocity;
    }


    public void update(double delta, GameMap gameMap) {
        if (isKnockbackActive()) {
            float dx = (float) (kbX * delta * 60);
            float dy = (float) (kbY * delta * 60);

            if (canMoveTo(dx, dy, gameMap)) selfHitbox.offset(dx, dy);
            else cancelKnockback();

            float frictionFactor = (float) Math.pow(KB_FRICTION, delta * 60);

            kbX *= frictionFactor;
            kbY *= frictionFactor;
        }
    }

    public boolean isKnockbackActive() {
        return Math.abs(kbX) > KB_THRESHOLD || Math.abs(kbY) > KB_THRESHOLD;
    }

    public void cancelKnockback() {
        kbX = 0;
        kbY = 0;
    }
}