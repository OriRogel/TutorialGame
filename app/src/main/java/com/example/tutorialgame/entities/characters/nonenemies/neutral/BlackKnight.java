package com.example.tutorialgame.entities.characters.nonenemies.neutral;

import static com.example.tutorialgame.engine.core.GameConstants.Face_Dir.DOWN;

import android.graphics.PointF;

import com.example.tutorialgame.R;
import com.example.tutorialgame.components.drop.DropEntry;
import com.example.tutorialgame.entities.Weapons;
import com.example.tutorialgame.entities.characters.GameCharacters;
import com.example.tutorialgame.environments.GameMap;
import com.example.tutorialgame.gamestates.playing.playingstates.DialogState;
import com.example.tutorialgame.managers.MapManager;

public class BlackKnight extends Neutral {
    private boolean angry;
    public BlackKnight(PointF pos) {
        super(pos, GameCharacters.BLACK_KNIGHT, 400, 800, 2);
        faceDir = DOWN;
        this.moving = false;
    }

    @Override
    public void update(double delta, GameMap gameMap) {
        super.update(delta, gameMap);
        if (moving) {
            animation.update();
        }
    }

    @Override
    public void onDialogue() {
        if (!angry) return;

        putCharInBadBoyList(MapManager.getCurrentMap().getPlayer());
        DialogState.endDialogue(gameCharType.getName());
    }
    public void setAngry(boolean angry) {
        this.angry = angry;
    }

    @Override
    protected void updateAttackTimer() {
        super.updateAttackTimer();
        moving = false;
    }

    //abstracted implements from character
    @Override
    public Weapons getWeapon() {
        return Weapons.SPEAR;
    }
    @Override
    public int getAttackDamage() {
        return 100;
    }
    @Override
    protected int getHealth() {
        return 1000;
    }
    @Override
    protected int getStamina() {
        return 700;
    }
    @Override
    protected int getStaminaCoolDown() {
        return 2300;
    }
    @Override
    public int getImpactSfx() {
        return R.raw.sfx_impact3;
    }
    @Override
    public DropEntry getDropEntry() {
        return new DropEntry(2, 4, 30, 35, 0.7);
    }
    @Override
    public float getTimeToApexSec() {
        return 0.4f;
    }
    @Override
    public float getDesiredTiles() {
        return 2;
    }

}
