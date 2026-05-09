package com.example.tutorialgame.entities.characters.enemies;

import android.graphics.PointF;

import com.example.tutorialgame.engine.core.GameConstants;
import com.example.tutorialgame.entities.characters.GameCharacters;
import com.example.tutorialgame.entities.characters.Npc;

/**
 * מחלקת בסיס לכל הדמויות העוינות.
 * יורשת מ-Npc ופשוט קובעת את הפלג (faction) להיות ENEMY.
 */
public abstract class Enemy extends Npc {

    public Enemy(PointF pos, GameCharacters gameCharType, long timeToAttack, long attackDuration, float speed) {
        // קורא לבנאי של Npc ומעביר את הפלג ENEMY באופן אוטומטי
        super(pos, gameCharType, timeToAttack, attackDuration, GameConstants.Faction.ENEMY, speed);
    }
}
