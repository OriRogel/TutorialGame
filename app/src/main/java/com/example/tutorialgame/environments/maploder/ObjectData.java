package com.example.tutorialgame.environments.maploder;

import android.graphics.PointF;
import android.graphics.RectF;
import com.example.tutorialgame.engine.ui.effects.lighting.LightSource;

public class ObjectData {
    public final String name;
    public final String type;
    public final PointF position; // נשמר לתאימות עם דמויות ואובייקטים
    public final RectF bounds;    // נוסף עבור טריגרים ומשימות

    // מאפיינים ספציפיים לדלתות
    public final String connectsTo;
    public final String targetDoor;
    public final String requiredCheckPoint;

    // מאפיינים ספציפיים לאורות
    public float radius;
    public int color;
    public LightSource.LightType lightType;

    public ObjectData(String name, String type, PointF position, RectF bounds, String connectsTo, String targetDoor, String requiredCheckPoint) {
        this.name = name;
        this.type = type;
        this.position = position;
        this.bounds = bounds;
        this.connectsTo = (connectsTo != null) ? connectsTo : "";
        this.targetDoor = (targetDoor != null) ? targetDoor : "";
        this.requiredCheckPoint = (requiredCheckPoint != null) ? requiredCheckPoint : "";
    }

    public void setLightProperties(float radius, int color, LightSource.LightType lightType) {
        this.radius = radius;
        this.color = color;
        this.lightType = lightType;
    }
}
