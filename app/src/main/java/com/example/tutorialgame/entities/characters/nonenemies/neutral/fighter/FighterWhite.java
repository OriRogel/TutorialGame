package com.example.tutorialgame.entities.characters.nonenemies.neutral.fighter;

import android.graphics.PointF;

import com.example.tutorialgame.components.drop.DropEntry;
import com.example.tutorialgame.entities.Weapons;
import com.example.tutorialgame.entities.characters.GameCharacters;

public class FighterWhite extends Fighter {

    public FighterWhite(PointF pos) {
        // מהירות 1.6f - לוחם מהיר ומאתגר
        super(pos, GameCharacters.FIGHTER_WHITE, 3.3f);
    }

    //abstracted implements from character
    @Override
    public Weapons getWeapon() {
        return Weapons.BIG_SWARD; // נשק חזק יותר
    }
    @Override
    public int getAttackDamage() {
        return 25;
    }
    @Override
    protected int getHealth() {
        return 250;
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
        return -1;
    }
    @Override
    public DropEntry getDropEntry() {
        return new DropEntry(4, 5, 30, 35, 0.7);
    }
    @Override
    public float getTimeToApexSec() {
        return 0.35f;
    }
    @Override
    public float getDesiredTiles() {
        return 3f;
    }
}
