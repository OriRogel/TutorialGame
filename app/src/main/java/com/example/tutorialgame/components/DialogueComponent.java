package com.example.tutorialgame.components;

import android.view.MotionEvent;

import java.util.Collections;
import java.util.List;

public class DialogueComponent {
    private List<String> dialogueLines;
    private int currentLineIndex;

    /**
     * בנאי שמתחיל עם רשימה ריקה.
     */
    public DialogueComponent() {
        this.dialogueLines = Collections.emptyList();
        this.currentLineIndex = -1;
    }

    /**
     * מתודה חדשה לעדכון רשימת השורות ואיפוס השיחה.
     * @param newLines רשימת השורות החדשה לשיחה.
     */
    public void updateLines(List<String> newLines) {
        // אם הרשימה החדשה היא null, החלף אותה ברשימה ריקה בטוחה
        this.dialogueLines = (newLines != null) ? newLines : Collections.emptyList();
        this.currentLineIndex = -1; // אפס את המיקום בשיחה
    }

    /**
     * מחזיר את שורת הדיאלוג הבאה ומקדם את האינדקס.
     */
    public String getNextLine() {
        if (isEmpty()) {
            return null;
        }

        currentLineIndex++;
        if (currentLineIndex < dialogueLines.size()) {
            return dialogueLines.get(currentLineIndex);
        } else {
            return null; // הדיאלוג הסתיים
        }
    }

    /**
     * בודק אם לרכיב זה יש שורות דיאלוג.
     */
    public boolean isEmpty() {
        return dialogueLines.isEmpty();
    }
}