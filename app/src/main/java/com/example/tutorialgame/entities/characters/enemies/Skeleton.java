package com.example.tutorialgame.entities.characters.enemies;

import android.graphics.PointF;

import com.example.tutorialgame.MyApp;
import com.example.tutorialgame.components.drop.DropEntry;
import com.example.tutorialgame.entities.Weapons;
import com.example.tutorialgame.entities.characters.GameCharacters;
import com.example.tutorialgame.entities.foregrounds.statics.Items;
import com.example.tutorialgame.environments.GameMap;
import com.example.tutorialgame.engine.core.GameConstants;
import com.example.tutorialgame.R;

public class Skeleton extends Enemy {
    private long lastDirChange;

    public Skeleton(PointF pos) {
        super(pos, GameCharacters.SKELETON, 500, 250, 2.8f);

        this.lastDirChange = System.currentTimeMillis();
    }

    public void update(double delta, GameMap gameMap) {
        super.update(delta, gameMap);
    }

    @Override
    protected void onIdeal(double delta, GameMap gameMap) {
        super.onIdeal(delta, gameMap);
        animation.update();

        if (System.currentTimeMillis() - lastDirChange >= MyApp.getRandom().nextInt(3000) + 3000) {
            faceDir = MyApp.getRandom().nextInt(4);
            lastDirChange = System.currentTimeMillis();
            setJumping(true);
        }

        float deltaChange = (float) (delta * baseSpeed);

        switch (faceDir) {
            case GameConstants.Face_Dir.DOWN:
                if (movementComponent.canMoveTo(0, deltaChange, gameMap)) {
                    hitBox.top += deltaChange;
                    hitBox.bottom += deltaChange;
                } else
                    faceDir = GameConstants.Face_Dir.UP;
                break;

            case GameConstants.Face_Dir.UP:
                if (movementComponent.canMoveTo(0, -deltaChange, gameMap)) {
                    hitBox.top -= deltaChange;
                    hitBox.bottom -= deltaChange;
                } else
                    faceDir = GameConstants.Face_Dir.DOWN;
                break;

            case GameConstants.Face_Dir.RIGHT:
                if (movementComponent.canMoveTo(deltaChange, 0, gameMap)) {
                    hitBox.left += deltaChange;
                    hitBox.right += deltaChange;
                } else
                    faceDir = GameConstants.Face_Dir.LEFT;
                break;

            case GameConstants.Face_Dir.LEFT:
                if (movementComponent.canMoveTo(-deltaChange, 0, gameMap)) {
                    hitBox.left -= deltaChange;
                    hitBox.right -= deltaChange;
                } else
                    faceDir = GameConstants.Face_Dir.RIGHT;
                break;
        }
    }

    //abstracted implements from character
    @Override
    public Weapons getWeapon() {
        return Weapons.BONE;
    }
    @Override
    public int getAttackDamage() {
        return 12;
    }
    @Override
    protected int getHealth() {
        return 50;
    }
    @Override
    protected int getStamina() {
        return 300;
    }
    @Override
    protected int getStaminaCoolDown() {
        return 3000;
    }
    @Override
    public int getImpactSfx() {
        return R.raw.sfx_impact_enemy2;
    }
    @Override
    public DropEntry getDropEntry() {
        return new DropEntry(0, 1, 10, 15, 0.8, Items.MEDIPACK);
    }
    @Override
    public float getTimeToApexSec() {
        return 0.4f;
    }
    @Override
    public float getDesiredTiles() {
        return 1.5f;
    }
}
