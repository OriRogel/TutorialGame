package com.example.tutorialgame.entities.characters.nonenemies.neutral;

import android.graphics.PointF;
import com.example.tutorialgame.engine.core.GameConstants;
import com.example.tutorialgame.engine.ui.Emotes;
import com.example.tutorialgame.entities.characters.Character;
import com.example.tutorialgame.entities.characters.GameCharacters;
import com.example.tutorialgame.entities.characters.nonenemies.NonEnemy;
import com.example.tutorialgame.environments.GameMap;
import com.example.tutorialgame.utils.Other;

public abstract class Neutral extends NonEnemy {
    private final static long TIME_TO_FORGET = 10_000;

    public Neutral(PointF pos, GameCharacters gameCharType, long timeToAttack, long attackDuration, float speed) {
        // קורא לבנאי של NonEnemy ומגדיר את הפלג כ-NEUTRAL
        super(pos, gameCharType, timeToAttack, attackDuration, GameConstants.Faction.NEUTRAL, speed);
    }

    @Override
    public void update(double delta, GameMap gameMap) {
        // מנקים תוקפים ישנים בכל פריים, ואז נותנים ל-AI הכללי לעבוד
        forgetOldTargets(TIME_TO_FORGET);
        super.update(delta, gameMap);
    }

    @Override
    protected void findTargetClap(GameMap gameMap) {
        // דריסה חכמה: הניטרלי מחפש מטרות רק מתוך ה-BadBoyList במקום מהמפה הכללית!
        Character potentialTarget = findClosestTarget();

        // מוודא שהמטרה קיימת וגם שהוא רואה אותה
        if (Other.IsCharacterSeesTarget(this, potentialTarget)) {
            this.currentTarget = potentialTarget;
        } else {
            this.currentTarget = null;
        }
        // ברגע שהגדרנו currentTarget, ה-Npc יעביר אוטומטית ל-AiBehavior.AGGRESSIVE
    }

    @Override
    protected void onAggressive(double delta, GameMap gameMap) {
        super.onAggressive(delta, gameMap);
        // מעדכן את הטיימר כל עוד הוא רודף ותוקף (לשימוש במחלקות היורשות)
        lastTimeAggressive = now;
    }

    // --- התיקון הקריטי: מתעצבן כשחוטף נזק, לא כשמת! ---
    @Override
    public void takeDamage(int amount, Character attacker, boolean isCritical) {
        super.takeDamage(amount, attacker, isCritical);

        // אם הדמות כבר מתה או שאין תוקף מוגדר, יוצאים
        if (attacker == null || isDead() || amount <= 0) return;

        putCharInBadBoyList(attacker);

        // אימוט "לב שבור" כי השחקן בגד בו (רק אם כרגע אין אימוט פעיל של כעס)
        if (attacker.getFaction() == GameConstants.Faction.PLAYER && !emoteComponent.isActive()) {
            emoteComponent.showEmote(Emotes.BROKEN_HEART, 2000);
        }
    }

    protected void putCharInBadBoyList(Character character) {
        // פעולת put מכניסה את התוקף (או מעדכנת את הזמן אם הוא כבר שם)
        // היא מחזירה את הערך הישן. אם חזר null, סימן שזו הפעם הראשונה שהוא תוקף!
        Long previousAttackTime = this.getBadBoyList().put(character, now);

        if (previousAttackTime == null) {
            emoteComponent.showEmote(Emotes.ANGRY, 2000);
        }
    }

    @Override
    public boolean canDamage(Character target) {
        // נוודא שהניטרלי יכול לפגוע רק במי שנמצא ברשימה השחורה שלו
        return this.getBadBoyList().containsKey(target);
    }
}