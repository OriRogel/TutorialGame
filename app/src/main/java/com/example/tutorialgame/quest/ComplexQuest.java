package com.example.tutorialgame.quest;

import androidx.annotation.StringRes;

import java.util.List;

public class ComplexQuest {
    private final List<Quest> subQuests;
    private final int titleRes; // כותרת המשימה הגדולה (למשל: "מצא את הנפח")

    public ComplexQuest(@StringRes int titleRes, List<Quest> subQuests) {
        this.titleRes = titleRes;
        this.subQuests = subQuests;
    }

    /**
     * מחזיר את תת-המשימה הנוכחית שעדיין לא הושלמה.
     */
    public Quest getCurrentSubQuest() {
        for (Quest q : subQuests) {
            if (!q.isCompleted()) {
                return q;
            }
        }
        return null; // כל תת-המשימות הושלמו
    }

    public boolean isAllCompleted() {
        return getCurrentSubQuest() == null;
    }

    public int getTitleRes() {
        return titleRes;
    }
}