package com.example.tutorialgame.quest;

public class QuestType {

    // סוג האירוע שמשלים את המשימה
    public enum Type {
        DIALOGUE_WITH,    // סיום שיחה עם דמות
        COLLECT_ITEM,     // איסוף פריט
        ENTER_ZONE,       // כניסה לאזור
        DEFEAT_ENEMY      // הבסת אויב
    }

    public final Type type;
    public final String targetId; // מזהה המטרה (שם הדמות, סוג הפריט, שם האזור וכו')

    public QuestType(Type type, String targetId) {
        this.type = type;
        this.targetId = targetId;
    }
}
