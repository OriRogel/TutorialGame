package com.example.tutorialgame.utils;

import android.graphics.PointF;

import com.example.tutorialgame.engine.core.GameConstants;
import com.example.tutorialgame.entities.characters.Character;

public class Other {
    public static boolean IsCharacterSeesTarget(Character attacker, Character target) {
        if(target == null || attacker == null) return false;

        return CollisionUtils.isWithinRange(attacker.getHitBox().centerX(), attacker.getHitBox().centerY(),
                target.getHitBox().centerX(), target.getHitBox().centerY(), attacker.getViewDistance());
    }

    public static int TurnTowardsTarget(float x, float y, Character character) {
        float xDelta = character.getProjectedHitBox().centerX() - x;
        float yDelta = character.getProjectedHitBox().centerY() - y;

        if (Math.abs(xDelta) > Math.abs(yDelta))
            return (xDelta > 0) ? GameConstants.Face_Dir.LEFT : GameConstants.Face_Dir.RIGHT;
        else
            return (yDelta > 0) ? GameConstants.Face_Dir.UP : GameConstants.Face_Dir.DOWN;
    }


    //מוצא חיתוך בין מעגל בעל רדיוס ומרכז ידוע לבין נקודה חיצונית
    public static void IntersectCircle(float cx, float cy,
                                       float r,
                                       float px, float py,
                                       PointF point) {
        float dx = px - cx;
        float dy = py - cy;
        // מרחק מהמרכז אל הנקודה
        float dist = (float) Math.hypot(dx, dy);
        if (dist == 0f) {
            // קו לא מוגדר; מחזירים המרכז (או מטפלים בשגיאה)
            point.set(cx, cy);
        }
        // פרמטר t
        float t = r / dist;
        // נקודת החיתוך
        float ix = cx + dx * t;
        float iy = cy + dy * t;
        point.set(ix, iy);
    }
}