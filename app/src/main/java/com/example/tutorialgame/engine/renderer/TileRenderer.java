package com.example.tutorialgame.engine.renderer;

import static com.example.tutorialgame.engine.core.GameConstants.View.SCREEN_HEIGHT;
import static com.example.tutorialgame.engine.core.GameConstants.View.SCREEN_WIDTH;

import android.graphics.Canvas;

import com.example.tutorialgame.engine.core.GameConstants;
import com.example.tutorialgame.environments.GameMap;
import com.example.tutorialgame.environments.Tiles;
import com.example.tutorialgame.managers.CameraManager;

public class TileRenderer {

    public void drawTiles(Canvas c, GameMap map) {
        // שליפת הנתונים מהמצלמה
        float lookAtX = CameraManager.getLookAtX();
        float lookAtY = CameraManager.getLookAtY();
        float zoom = CameraManager.getTempZoom();
        int tileSize = GameConstants.Sprite.TILE_SIZE;

        // חישוב גודל המסך ה"ווירטואלי" (כמה אריחים נכנסים בזום הנוכחי)
        float viewW = SCREEN_WIDTH / zoom;
        float viewH = SCREEN_HEIGHT / zoom;

        // חישוב האריחים שבאמת רואים על המסך (Culling)
        int startCol = Math.max(0, (int) ((lookAtX - viewW / 2) / tileSize));
        int endCol = Math.min(map.getArrayWidth(), (int) ((lookAtX + viewW / 2) / tileSize) + 1);
        int startRow = Math.max(0, (int) ((lookAtY - viewH / 2) / tileSize));
        int endRow = Math.min(map.getArrayHeight(), (int) ((lookAtY + viewH / 2) / tileSize) + 1);

        // ציור השכבות
        drawLayer(c, map, startRow, endRow, startCol, endCol, tileSize, true);
        drawLayer(c, map, startRow, endRow, startCol, endCol, tileSize, false);
    }

    private void drawLayer(Canvas c, GameMap map, int startRow, int endRow, int startCol, int endCol, int tileSize, boolean isGround) {
        for (int row = startRow; row < endRow; row++) {
            for (int col = startCol; col < endCol; col++) {
                int spriteId = isGround ? map.getGroundIds(col, row) : map.getWallLayerID(col, row);

                if (spriteId == -1) continue;

                c.drawBitmap(
                        isGround ? map.getGround().getSprite(spriteId) : Tiles.WALLS.getSprite(spriteId),
                        col * tileSize,
                        row * tileSize,
                        null
                );
            }
        }
    }
}