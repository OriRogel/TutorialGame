package com.example.tutorialgame.entities.characters.nonenemies.neutral.fighter;

import android.graphics.PointF;
import com.example.tutorialgame.engine.ui.Emotes;
import com.example.tutorialgame.entities.characters.Character;
import com.example.tutorialgame.entities.characters.GameCharacters;
import com.example.tutorialgame.entities.characters.nonenemies.neutral.Neutral;
import com.example.tutorialgame.environments.GameMap;

public abstract class Fighter extends Neutral {

    // משתנה שיקבע אם הקרב פעיל עבור הלוחם הספציפי הזה
    private boolean isDuelActive = false;

    public Fighter(PointF pos, GameCharacters gameCharType, float speed) {
        // ערכים ברירת מחדל ללוחמי דוג'ו (זמן טעינת התקפה ומשך התקפה)
        super(pos, gameCharType, 500, 300, speed);
    }

    @Override
    public void update(double delta, GameMap gameMap) {
        // אנחנו תמיד קוראים ל-super כדי שהאנימציות, החיים והפיזיקה יתעדכנו!
        // הלוחם יתנהג כנייטרלי (לא יתקוף אלא אם הרביצו לו או שהתחיל קרב)
        super.update(delta, gameMap);

        // אם יש קרב פעיל, אנחנו בודקים את תנאי הסיום שלו
        if (isDuelActive) {
            checkDuelStatus(gameMap.getPlayer());
        }
    }

    /**
     * פונקציה שתופעל מהדיאלוג כדי להתחיל את הקרב
     */
    public void startChallenge(Character player) {
        this.isDuelActive = true;
        this.currentTarget = player;

        // התאמה למערכת החדשה שלנו
        this.currentBehavior = AiBehavior.AGGRESSIVE;
        gameCharType.setViewDistance(5);

        // הוספה לרשימת ה"ילדים הרעים" כדי שה-Neutral logic יזהה אותו כאויב
        getBadBoyList().put(player, System.currentTimeMillis());

        emoteComponent.showEmote(Emotes.ANGRY, 2000);
    }

    private void checkDuelStatus(Character player) {
        // אם הלוחם הובס (מת)
        if (this.isDead()) {
            endDuel(true);
        }
        // אם השחקן הובס (נשאר לו פחות מ-5 חיים, אנחנו לא באמת הורגים אותו באימון)
        else if (player.getCurrentHealth() <= 5) {
            endDuel(false);
        }
    }

    public void endDuel(boolean playerWon) {
        this.isDuelActive = false;

        // התאמה למערכת החדשה
        this.currentBehavior = AiBehavior.IDEAL;
        this.currentTarget = null;
        this.moving = false; // מוודא שהוא עוצר בסוף הקרב

        gameCharType.setViewDistance(1.5f);
        this.active = true;

        getBadBoyList().clear(); // שוכח מהשחקן כדי להפסיק לתקוף

//        if (playerWon) {
//            emoteComponent.showEmote(Emotes.SURPRISE, 2000);
//        } else {
//            emoteComponent.showEmote(Emotes.LAUGH, 2000);
//        }
    }
}