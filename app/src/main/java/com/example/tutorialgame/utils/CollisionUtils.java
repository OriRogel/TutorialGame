package com.example.tutorialgame.utils;

import android.graphics.Point;
import android.graphics.RectF;

import com.example.tutorialgame.engine.core.GameConstants;
import com.example.tutorialgame.environments.GameMap;

/**
 * Utility class providing static methods for collision detection, 
 * coordinate transformation, and distance calculations.
 */
public final class CollisionUtils {

    private CollisionUtils() {} // Prevent instantiation

    /**
     * Checks if all provided tile coordinates are walkable on the given map.
     */
    public static boolean areTilesWalkable(Point[] tileCords, GameMap gameMap) {
        if (gameMap == null) return false;
        for (Point p : tileCords) {
            if (!gameMap.isTileWalkable(p.x, p.y))
                return false;
        }
        return true;
    }

    /**
     * Updates an existing array of Points with the tile indices corresponding 
     * to the four corners of a hitbox after a potential movement.
     */
    public static void setTileCords(RectF hitBox, float deltaX, float deltaY, Point[] resultPoints) {
        if (resultPoints == null || resultPoints.length < 4) return;

        int tileSize = GameConstants.Sprite.TILE_SIZE;
        int left = (int) ((hitBox.left + deltaX) / tileSize);
        int right = (int) ((hitBox.right + deltaX) / tileSize);
        int top = (int) ((hitBox.top + deltaY) / tileSize);
        int bottom = (int) ((hitBox.bottom + deltaY) / tileSize);

        resultPoints[0].set(left, top);
        resultPoints[1].set(right, top);
        resultPoints[2].set(left, bottom);
        resultPoints[3].set(right, bottom);
    }

    /**
     * Retrieves the Wall and Collision layer IDs for a set of tile coordinates.
     */
    public static void setTileIds(int[] wallIds, int[] collisionIds, Point[] tileCords, GameMap gameMap) {
        if (gameMap == null) return;
        for (int i = 0; i < wallIds.length; i++) {
            wallIds[i] = gameMap.getWallLayerID(tileCords[i].x, tileCords[i].y);
            collisionIds[i] = gameMap.getCollisionLayerID(tileCords[i].x, tileCords[i].y);
        }
    }

    /**
     * Calculates the Euclidean distance between two points.
     */
    public static float getDistance(float x1, float y1, float x2, float y2) {
        return (float) Math.hypot(x1 - x2, y1 - y2);
    }

    /**
     * Checks if a point is within a certain range of another point.
     * More efficient than getDistance for simple range checks.
     */
    public static boolean isWithinRange(float x1, float y1, float x2, float y2, float range) {
        float dx = x1 - x2;
        float dy = y1 - y2;
        return (dx * dx + dy * dy) <= (range * range);
    }

    /**
     * Verifies if the tile indices are within the map boundaries.
     */
    public static boolean isWithinMapBounds(int col, int row, GameMap map) {
        return col >= 0 && row >= 0 && col < map.getArrayWidth() && row < map.getArrayHeight();
    }
}
