package com.example.tutorialgame.managers;

import static com.example.tutorialgame.engine.core.GameConstants.View.SCREEN_HEIGHT;
import static com.example.tutorialgame.engine.core.GameConstants.View.SCREEN_WIDTH;
import static com.example.tutorialgame.engine.core.GameConstants.View.VIEW_MARGIN;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;
import com.example.tutorialgame.MyApp;
import com.example.tutorialgame.engine.audio.MusicManager;
import com.example.tutorialgame.engine.core.GameConstants;
import com.example.tutorialgame.engine.renderer.TileRenderer;
import com.example.tutorialgame.engine.ui.effects.weathereffects.Cloud;
import com.example.tutorialgame.engine.ui.effects.weathereffects.WeatherEffect;
import com.example.tutorialgame.engine.ui.effects.weathereffects.leaves.Leaf;
import com.example.tutorialgame.engine.ui.effects.weathereffects.leaves.Leaves;
import com.example.tutorialgame.entities.characters.Character;
import com.example.tutorialgame.entities.characters.Player;
import com.example.tutorialgame.environments.Doorway;
import com.example.tutorialgame.environments.GameMap;
import com.example.tutorialgame.environments.Tiles;
import com.example.tutorialgame.environments.maploder.ObjectData;
import com.example.tutorialgame.managers.objectpool.ObjectPoolManager;
import com.example.tutorialgame.ui.base.BaseActivity;
import com.example.tutorialgame.utils.TmxLoaderUtils;
import com.example.tutorialgame.gamestates.playing.playingstates.OverWorld;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.example.tutorialgame.environments.maploder.MapLoadData;

/**
 * Orchestrates map loading, transitions, and environmental effects.
 * Manages the lifecycle of world entities like coins and XP effects with focus on performance.
 */
public class MapManager {
    public interface OnMapReadyListener {
        void onMapReady(GameMap map);
    }

    private final static String TAG = "MapManager";
    private static GameMap currentMap;
    private static final Map<String, GameMap> loadedMaps = new ConcurrentHashMap<>();

    private final OverWorld overWorld;
    private final List<WeatherEffect> weatherEffects = Collections.synchronizedList(new ArrayList<>());
    
    private final RectF viewRect = new RectF();
    private final TileRenderer tileRenderer;

    public MapManager(OverWorld overWorld) {
        this.overWorld = overWorld;
        this.tileRenderer = new TileRenderer();
        if (currentMap == null) initStartingWorld();
        initWeatherForCurrentMap();
    }

    // --- Environmental Effects Management (Unified) ---

    public void initWeatherForCurrentMap() {
        GameMap map = getCurrentMap();
        weatherEffects.clear();

        if (map == null || !map.hasEffects()) return;
        
        if (map.hasLeavesEffect()) {
            for (int i = 0; i < 15; i++) {
                PointF pos = getRandomViewPosition();
                weatherEffects.add(new Leaf(MyApp.RND.nextBoolean() ? Leaves.LEAF_GREEN : Leaves.LEAF_PINK, pos.x, pos.y));
            }
        }

        if (map.hasCloudsEffect()) {
            for (int i = 0; i < 4; i++) {
                PointF pos = getRandomViewPosition();
                weatherEffects.add(new Cloud(pos.x, pos.y));
            }
        }
    }

    public void updateWeather(double delta) {
        if (weatherEffects.isEmpty()) return;
        synchronized (weatherEffects) {
            for (int i = 0; i < weatherEffects.size(); i++) {
                WeatherEffect effect = weatherEffects.get(i);
                effect.update(delta);
                if (effect.isDead()) {
                    PointF pos = getRandomViewPosition();
                    effect.respawn(pos.x, pos.y);
                }
            }
        }
    }

    /**
     * מצייר את אפקטי המזג אוויר לפי סדר הציור המבוקש.
     * @param c הקנבס לציור.
     * @param order הסדר (מעל או מתחת לישויות).
     */
    public void drawWeather(Canvas c, WeatherEffect.RenderOrder order) {
        if (weatherEffects.isEmpty()) return;
        synchronized (weatherEffects) {
            for (int i = 0; i < weatherEffects.size(); i++) {
                WeatherEffect effect = weatherEffects.get(i);
                if (effect.getRenderOrder() == order) {
                    effect.draw(c);
                }
            }
        }
    }

    private PointF getRandomViewPosition() {
        return new PointF(
            viewRect.left + MyApp.RND.nextFloat() * viewRect.width(),
            viewRect.top + MyApp.RND.nextFloat() * viewRect.height()
        );
    }

    // --- Entity Updates ---

    /**
     * Updates internal map entities like Coins, XP, and Lighting.
     */
    public void updateWorldEntities(Player player, double delta) {
        if (currentMap != null) {
            currentMap.update(delta, player);
        }
    }

    public void drawXpEffects(Canvas c) {
        GameMap map = getCurrentMap();
        if (map == null) return;
        synchronized (map.getXpList()) {
            for (int i = 0; i < map.getXpList().size(); i++) {
                map.getXpList().get(i).draw(c);
            }
        }
    }

    // --- View and Rendering ---

    public void updateViewRect() {
        float zoom = CameraManager.getTempZoom();
        float invZoom = 1.0f / zoom;
        float visibleWidth = SCREEN_WIDTH * invZoom;
        float visibleHeight = SCREEN_HEIGHT * invZoom;
        float margin = VIEW_MARGIN * invZoom;

        float left = -CameraManager.getOffsetX() - margin;
        float top = -CameraManager.getOffsetY() - margin;
        viewRect.set(left, top, left + visibleWidth + 2 * margin, top + visibleHeight + 2 * margin);
    }

    public void drawTiles(Canvas c) {
        GameMap map = getCurrentMap();
        if (map != null) tileRenderer.drawTiles(c, map);
    }

    // --- Map Transitions ---

    public Doorway isPlayerOnDoorway(RectF playerHitbox) {
        GameMap map = getCurrentMap();
        if (map == null) return null;
        for (Doorway doorway : map.getDoorwayArrayList()) {
            if (doorway.isPlayerInsideDoorway(playerHitbox)) return doorway;
        }
        return null;
    }

    public void changeMap(Doorway doorwayTarget) {
        if (doorwayTarget == null) return;
        GameMap newMap = doorwayTarget.getGameMapLocatedIn();
        if (newMap == null) return;

        currentMap = newMap;
        CameraManager.setMapSize(newMap.getMapWidth(), newMap.getMapHeight());

        // קודם כל מרעננים את מצב העולם כדי לוודא שמוזיקת המפה מעודכנת לפי העלילה
        WorldEventManager.refreshWorldState();

        // עכשיו מנגנים - זה ינגן את השיר המעודכן ביותר שנקבע למפה
        MusicManager.getInstance(BaseActivity.getContext()).play(newMap.getMusicRes());
        
        overWorld.getPlayer().teleportTo(
            doorwayTarget.getPosOfDoorway().x - GameConstants.Sprite.HITBOX_SIZE / 2f,
            doorwayTarget.getPosOfDoorway().y - GameConstants.Sprite.HITBOX_SIZE / 2f
        );

        if (!newMap.getDrawableList().contains(overWorld.getPlayer())) {
            newMap.setPlayer(overWorld.getPlayer());
        }
        overWorld.setDoorwayJustPassed(true);
        connectDoorwaysForMap(newMap);
        QuestManager.onEnterZone(newMap.getFileName());
        initWeatherForCurrentMap();
    }

    // --- Static Global Management ---

    public static GameMap getCurrentMap() {
        return currentMap;
    }

    public static void clearCache() {
        loadedMaps.clear();
        currentMap = null;
        ObjectPoolManager.clearAllPools();
        Log.d(TAG, "Map cache and object pools cleared");
    }

    public static void reloadAllLoadedMaps() {
        for (GameMap map : loadedMaps.values()) {
            if (map != null) map.reload();
        }
    }

    public void resetWorldForRestart(OnMapReadyListener listener) {
        reloadAllLoadedMaps();
        ObjectPoolManager.clearAllPools();
        String startingMapName = MyApp.getWorldStateDoc().getLastMap();
        currentMap = getMapByName(startingMapName);
        connectDoorwaysForMap(currentMap);

        if (currentMap != null) {
            CameraManager.setMapSize(currentMap.getMapWidth(), currentMap.getMapHeight());
        }
        
        initWeatherForCurrentMap(); // וידוא שהמזג אוויר מתאפס לפי המפה החדשה-ישנה
        
        if (listener != null) listener.onMapReady(currentMap);
    }

    public static GameMap getMapByName(String mapFileName) {
        GameMap cached = loadedMaps.get(mapFileName);
        if (cached != null) return cached;

        Log.d(TAG, "Loading map: " + mapFileName);
        MapLoadData data = TmxLoaderUtils.loadMapFromTMX(mapFileName);
        Tiles tilesType = (mapFileName.contains("village")) ? Tiles.OUTSIDE : Tiles.INSIDE;
        
        // תיקון: בדיקה חכמה יותר ל-hasEffects
        boolean hasEffects = mapFileName.toLowerCase().contains("village") || mapFileName.toLowerCase().contains("forest");

        GameMap newMap = new GameMap(data, tilesType, hasEffects);
        loadedMaps.put(mapFileName, newMap);
        return newMap;
    }

    public static Character getCharacterById(String id, String targetMap) {
        GameMap map = getMapByName(targetMap);
        for (Character character : map.getCharacterArrayList()) {
            if (character.getGameCharType().name().equals(id)) return character;
        }
        return null;
    }

    private static void connectDoorwaysForMap(GameMap sourceMap) {
        if (sourceMap == null || sourceMap.getMapLoadData().doorLayer == null) return;

        for (ObjectData doorData : sourceMap.getMapLoadData().doorLayer) {
            Doorway sourceDoor = sourceMap.getDoorwayByName(doorData.name);
            if (sourceDoor == null) {
                sourceDoor = new Doorway(doorData.name, doorData.requiredCheckPoint, doorData.position, sourceMap);
            }
            if (sourceDoor.getDoorwayConnectedTo() != null) continue;

            GameMap targetMap = getMapByName(doorData.connectsTo);

            Doorway targetDoor = targetMap.getDoorwayByName(doorData.targetDoor);
            if (targetDoor == null) {
                for (ObjectData d : targetMap.getMapLoadData().doorLayer) {
                    if (d.name.equals(doorData.targetDoor)) {
                        targetDoor = new Doorway(d.name, d.requiredCheckPoint, d.position, targetMap);
                        break;
                    }
                }
            }

            if (targetDoor != null) {
                sourceDoor.connectDoorway(targetDoor);
                targetDoor.connectDoorway(sourceDoor);
            }
        }
    }

    public static void initStartingWorld() {
        String startingMapName = MyApp.getWorldStateDoc().getLastMap();
        GameMap map = getMapByName(startingMapName);
        connectDoorwaysForMap(map);
        currentMap = map;
        CameraManager.setMapSize(currentMap.getMapWidth(), currentMap.getMapHeight());
    }

    public RectF getViewRect() { return viewRect; }
}