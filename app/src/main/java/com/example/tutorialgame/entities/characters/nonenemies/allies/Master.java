package com.example.tutorialgame.entities.characters.nonenemies.allies;

import android.graphics.PointF;

import com.example.tutorialgame.R;
import com.example.tutorialgame.components.drop.DropEntry;
import com.example.tutorialgame.entities.Weapons;
import com.example.tutorialgame.entities.characters.GameCharacters;

public class Master extends Ally {
    public Master(PointF pos) {
        super(pos, GameCharacters.MASTER, 400, 300, 2.1f);
    }

    //abstracted implements from character
    @Override
    public Weapons getWeapon() {
        return Weapons.HAMMER;
    }
    @Override
    public int getAttackDamage() {
        return 50;
    }
    @Override
    protected int getHealth() {
        return 150;
    }
    @Override
    protected int getStamina() {
        return 200;
    }
    @Override
    protected int getStaminaCoolDown() {
        return 3000;
    }
    @Override
    public int getImpactSfx() {
        return R.raw.sfx_impact_player;
    }
    @Override
    public DropEntry getDropEntry() {
        return new DropEntry(0, 0, 0, 0, 0);
    }
    @Override
    public float getTimeToApexSec() {
        return 0.3f;
    }
    @Override
    public float getDesiredTiles() {
        return 1;
    }
}
