package com.example.tutorialgame.entities.characters.nonenemies.allies;

import android.graphics.PointF;
import com.example.tutorialgame.engine.core.GameConstants;
import com.example.tutorialgame.entities.characters.Character;
import com.example.tutorialgame.entities.characters.GameCharacters;
import com.example.tutorialgame.entities.characters.nonenemies.NonEnemy;
import com.example.tutorialgame.environments.GameMap;

public abstract class Ally extends NonEnemy {

    public Ally(PointF pos, GameCharacters gameCharType, long timeToAttack, long attackDuration, float speed) {
        // קורא לבנאי של NonEnemy ומגדיר את הפלג כ-ALLY
        super(pos, gameCharType, timeToAttack, attackDuration, GameConstants.Faction.ALLY, speed);
    }

    @Override
    protected void onAggressive(double delta, GameMap gameMap) {
        // נותן למוח של ה-Npc לעשות את כל עבודת הרדיפה והתקיפה!
        super.onAggressive(delta, gameMap);

        // מעדכן את הטיימר שאני מניח ששייך ל-NonEnemy
        lastTimeAggressive = now;
    }

    @Override
    public boolean canDamage(Character target) {
        // דריסה ייחודית ל-Ally: יכול לפגוע רק באויבים, אף פעם לא בשחקן או בחברים אחרים.
        // ה-Npc ישתמש בזה אוטומטית כשהוא מחפש מטרות!
        return target.getFaction() == GameConstants.Faction.ENEMY;
    }
}