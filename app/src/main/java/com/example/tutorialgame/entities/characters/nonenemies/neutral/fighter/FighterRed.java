package com.example.tutorialgame.entities.characters.nonenemies.neutral.fighter;

import android.graphics.PointF;

import com.example.tutorialgame.R;
import com.example.tutorialgame.components.drop.DropEntry;
import com.example.tutorialgame.entities.Weapons;
import com.example.tutorialgame.entities.characters.GameCharacters;

public class FighterRed extends Fighter {

    public FighterRed(PointF pos) {
        // מהירות 1.2f - לוחם איטי יחסית
        super(pos, GameCharacters.FIGHTER_RED, 3.126f);
    }

    //abstracted implements from character
    @Override
    public Weapons getWeapon() {
        return Weapons.HAMMER; // נשק חלש
    }
    @Override
    public int getAttackDamage() {
        return 25;
    }
    @Override
    protected int getHealth() {
        return 100;
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
        return new DropEntry(2, 3, 25, 30, 0.7);
    }
    @Override
    public float getTimeToApexSec() {
        return 0.4f;
    }
    @Override
    public float getDesiredTiles() {
        return 2.5f;
    }
}