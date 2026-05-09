package com.example.tutorialgame.entities.characters.nonenemies.neutral;

import android.graphics.PointF;
import com.example.tutorialgame.R;
import com.example.tutorialgame.components.drop.DropEntry;
import com.example.tutorialgame.entities.Weapons;
import com.example.tutorialgame.entities.characters.GameCharacters;

public class Father extends Neutral {
    public Father(PointF pos) {
        super(pos, GameCharacters.FATHER, 350, 250, 2.2f);
    }

    //abstracted implements from character
    @Override
    public Weapons getWeapon() {
        return Weapons.CLUB;
    }
    @Override
    public int getAttackDamage() {
        return 75;
    }
    @Override
    protected int getHealth() {
        return 800;
    }
    @Override
    protected int getStamina() {
        return 600;
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
        return new DropEntry(3, 4, 30, 40, 0.2);
    }
    @Override
    public float getTimeToApexSec() {
        return 0.35f;
    }
    @Override
    public float getDesiredTiles() {
        return 1.5f;
    }
}
