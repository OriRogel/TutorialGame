package com.example.tutorialgame.environments;

import android.graphics.PointF;
import android.graphics.RectF;
import com.example.tutorialgame.MyApp;

public class Doorway {


    private boolean active = true;
    private final GameMap gameMapLocatedIn;
    private Doorway doorwayConnectedTo;
    private final PointF doorwayPoint;
    private final String name, requiredCheckPoint;

    public Doorway(String name, String flag, PointF doorwayPoint, GameMap gameMapLocatedIn) {
        this.name = name;
        this.doorwayPoint = doorwayPoint;
        this.gameMapLocatedIn = gameMapLocatedIn;
        gameMapLocatedIn.addDoorway(this);
        this.requiredCheckPoint = flag;
    }


    public boolean canBeUsed() {
        if (!active) return false;
        // אם אין דרישה (המאפיין היה ריק בטיילד), הדלת תמיד שמישה
        if (requiredCheckPoint == null || requiredCheckPoint.isEmpty()) {
            return true;
        }
        // אם יש דרישה, בדוק את ה-checkpoint
        return MyApp.getWorldStateDoc().getCheckPoint(requiredCheckPoint);
    }

    public String getName() {
        return name;
    }

    public void connectDoorway(Doorway destinationDoorway) {
        this.doorwayConnectedTo = destinationDoorway;
    }

    public Doorway getDoorwayConnectedTo() {
        if (doorwayConnectedTo != null)
            return doorwayConnectedTo;
        return null;
    }

    public boolean isPlayerInsideDoorway(RectF playerHitbox) {
        return playerHitbox.contains(doorwayPoint.x, doorwayPoint.y);
    }

    public boolean isDoorwayActive() {
        return active;
    }
    public void setDoorwayActive(boolean active) {
        this.active = active;
    }
    public PointF getPosOfDoorway() {
        return doorwayPoint;
    }
    public GameMap getGameMapLocatedIn() {
        return gameMapLocatedIn;
    }
}
