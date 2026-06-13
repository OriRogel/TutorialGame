package com.example.tutorialgame.environments;

import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;

import com.example.tutorialgame.MyApp;
import com.example.tutorialgame.engine.interfaces.StaticObjectData;
import com.example.tutorialgame.engine.ui.effects.XpEffect;
import com.example.tutorialgame.engine.ui.effects.lighting.LightSource;
import com.example.tutorialgame.entities.characters.GameCharacters;
import com.example.tutorialgame.entities.foregrounds.animated.AnimatedObject;
import com.example.tutorialgame.entities.foregrounds.animated.Coin;
import com.example.tutorialgame.entities.characters.Character;
import com.example.tutorialgame.entities.characters.Player;
import com.example.tutorialgame.entities.Entity;
import com.example.tutorialgame.entities.foregrounds.animated.AnimatedType;
import com.example.tutorialgame.entities.foregrounds.breakable.BreakableEntity;
import com.example.tutorialgame.entities.foregrounds.breakable.BreakableType;
import com.example.tutorialgame.entities.foregrounds.collectible.CollectibleItem;
import com.example.tutorialgame.entities.foregrounds.statics.StaticObject;
import com.example.tutorialgame.entities.foregrounds.statics.Buildings;
import com.example.tutorialgame.entities.foregrounds.statics.Fences;
import com.example.tutorialgame.entities.foregrounds.statics.Items;
import com.example.tutorialgame.entities.foregrounds.statics.Natures;
import com.example.tutorialgame.engine.core.GameConstants;
import com.example.tutorialgame.entities.foregrounds.statics.GameObjects;
import com.example.tutorialgame.environments.maploder.MapLoadData;
import com.example.tutorialgame.environments.maploder.ObjectData;
import com.example.tutorialgame.environments.maploder.TilesetData;
import com.example.tutorialgame.managers.MapManager;
import com.example.tutorialgame.managers.objectpool.ObjectPoolManager;
import com.example.tutorialgame.ui.base.BaseActivity;
import com.example.tutorialgame.utils.CollisionUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GameMap {
    private final int[][] groundIds, wallLayer, collisionLayer, surfaceLayer;
    private boolean[][] walkableMap;
    private SurfaceType[][] surfaceTypeMap;
    private final Tiles tilesType;
    private final MapLoadData mapLoadData;

    private final ArrayList<StaticObject> staticObjectList;
    private final ArrayList<Doorway> doorwayArrayList;
    private final ArrayList<Character> characterArrayList;
    private final List<XpEffect> xpEffectList = Collections.synchronizedList(new ArrayList<>());
    private final List<AnimatedObject> animatedEntities = Collections.synchronizedList(new ArrayList<>());
    private final List<CollectibleItem> collectibleItems = Collections.synchronizedList(new ArrayList<>());
    private final List<BreakableEntity> breakableEntities = Collections.synchronizedList(new ArrayList<>());
    
    private final List<LightSource> lightSources = new ArrayList<>();
    private final int ambientDarkness;

    // --- Dynamic Spawn System ---
    private String spawnType;
    private int minMonsters;
    private int musicRes;
    private final List<Point> walkableTiles = new ArrayList<>();
    private final List<ObjectPoolManager.RespawnTask> respawnQueue = new ArrayList<>();

    private Player player;
    private final List<Entity> drawableList = Collections.synchronizedList(new ArrayList<>());
    private final List<Entity> pendingAddition = Collections.synchronizedList(new ArrayList<>());
   
    private final String fileName;
    private final List<TilesetData> tilesets;
    private final boolean hasEffects;

    public GameMap(MapLoadData data, Tiles tilesType, boolean hasEffects) {
        this.fileName = data.fileName;
        this.mapLoadData = data;
        this.musicRes = getRawResourceIdByName(data.musicFile);
        this.groundIds = data.groundLayer;
        this.wallLayer = data.wallsLayer;
        this.collisionLayer = data.collisionLayer;
        this.surfaceLayer = data.surfaceLayer;
        this.tilesType = tilesType;
        this.tilesets = data.tilesets;
        this.ambientDarkness = data.ambientDarkness;
        this.hasEffects = hasEffects;
        
        this.spawnType = data.spawnType;
        this.minMonsters = data.minMonsters;

        this.staticObjectList = new ArrayList<>();
        this.characterArrayList = new ArrayList<>();
        this.doorwayArrayList = new ArrayList<>();
        
        loadAllEntities(data);
        initWalkableMap();
        initSurfaceMap();
        findWalkableTiles();
    }

    private void findWalkableTiles() {
        walkableTiles.clear();
        for (int y = 0; y < getArrayHeight(); y++) {
            for (int x = 0; x < getArrayWidth(); x++) {
                if (walkableMap[y][x]) walkableTiles.add(new Point(x, y));
            }
        }
    }

    private void loadAllEntities(MapLoadData data) {
        loadStaticObjects(data.buildingLayer, Buildings.class, true);
        loadStaticObjects(data.fenceLayer, Fences.class, true);
        loadStaticObjects(data.natureLayer, Natures.class, true);
        loadStaticObjects(data.itemLayer, Items.class, false);
        loadStaticObjects(data.objectLayer, GameObjects.class, true);

        if (data.characterLayer != null) {
            for (ObjectData characterData : data.characterLayer) {
                Character character = GameCharacters.createCharacter(characterData.type, characterData.position);
                if (character != null) this.characterArrayList.add(character);
            }
        }

        if (data.lightLayer != null) {
            for (ObjectData lightData : data.lightLayer) {
                lightSources.add(new LightSource(lightData.position, lightData.radius, lightData.color, lightData.lightType));
            }
        }

        if (data.animatedLayer != null) {
            for (ObjectData obj : data.animatedLayer) {
                AnimatedType type = AnimatedType.fromString(obj.type);
                if (type != null) {
                    AnimatedObject entity;
                    entity = new AnimatedObject(obj.position, type);
                    animatedEntities.add(entity);
                }
            }
        }

        if (data.breakableLayer != null) {
            for (ObjectData obj : data.breakableLayer) {
                try {
                    BreakableType type = BreakableType.valueOf(obj.type);
                    breakableEntities.add(new BreakableEntity(obj.position, type));
                } catch (IllegalArgumentException ignored) {}
            }
        }

        updateDrawableList();
    }

    private void updateDrawableList() {
        synchronized (drawableList) {
            drawableList.clear();
            drawableList.addAll(staticObjectList);
            drawableList.addAll(characterArrayList);
            drawableList.addAll(animatedEntities);
            drawableList.addAll(collectibleItems);
            drawableList.addAll(breakableEntities);
            if (player != null) drawableList.add(player);
        }
    }

    public void update(double delta, Player player) {
        processPendingEntities();
        handleDynamicSpawning(delta);

        for (LightSource ls : lightSources) ls.update(delta);

        synchronized (animatedEntities) {
            for (int i = animatedEntities.size() - 1; i >= 0; i--) {
                AnimatedObject ae = animatedEntities.get(i);
                if (!ae.isActive()) {
                    animatedEntities.remove(i);
                    synchronized (drawableList) { drawableList.remove(ae); }
                } else ae.update(delta, player);
            }
        }

        synchronized (collectibleItems) {
            for (int i = collectibleItems.size() - 1; i >= 0; i--) {
                CollectibleItem ci = collectibleItems.get(i);
                if (!ci.isActive()) {
                    collectibleItems.remove(i);
                    synchronized (drawableList) { drawableList.remove(ci); }
                } else ci.update(delta, player);
            }
        }

        synchronized (breakableEntities) {
            for (int i = breakableEntities.size() - 1; i >= 0; i--) {
                BreakableEntity be = breakableEntities.get(i);
                be.update(delta);
                if (!be.isActive()) {
                    breakableEntities.remove(i);
                    synchronized (drawableList) { drawableList.remove(be); }
                }
            }
        }

        synchronized (xpEffectList) {
            for (int i = xpEffectList.size() - 1; i >= 0; i--) {
                XpEffect effect = xpEffectList.get(i);
                if (!effect.isActive()) xpEffectList.remove(i);
                else effect.update(delta);
            }
        }

        if (player.isDead()) return;
        synchronized (characterArrayList) {
            for (int i = characterArrayList.size() - 1; i >= 0; i--) {
                Character c = characterArrayList.get(i);
                if (!c.isActive()) {
                    characterArrayList.remove(i);
                    synchronized (drawableList) { drawableList.remove(c); }
                } else c.update(delta, this);
            }
        }
    }


    private void handleDynamicSpawning(double delta) {
        if (spawnType == null || spawnType.isEmpty() || minMonsters <= 0) return;

        // 1. Update existing respawn tasks
        for (int i = respawnQueue.size() - 1; i >= 0; i--) {
            ObjectPoolManager.RespawnTask task = respawnQueue.get(i);
            task.timeLeft -= delta;
            if (task.timeLeft <= 0) {
                if (spawnNewMonster(15)) {
                    ObjectPoolManager.releaseRespawnTask(task);
                    respawnQueue.remove(i);
                }
            }
        }

        // 2. Check population and add more tasks if needed
        int currentCount = 0;
        synchronized (characterArrayList) {
            for (Character c : characterArrayList) {
                if (c.isActive() && !c.isDead() && c.getGameCharType().name().equals(spawnType)) {
                    currentCount++;
                }
            }
        }

        if (currentCount + respawnQueue.size() < minMonsters && !walkableTiles.isEmpty()) {
            double delay = 2.0 + MyApp.getRandom().nextDouble() * 4.0; // Wait 2-6 seconds
            respawnQueue.add(ObjectPoolManager.acquireRespawnTask(delay));
        }
    }

    /**
     * יוצר מפלצת חדשה בערך במרחק שצוין מהשחקן.
     * @param targetDistanceInTiles המרחק הרצוי ב-Tiles.
     * @return true אם הצליח ליצור מפלצת.
     */
    private boolean spawnNewMonster(int targetDistanceInTiles) {
        if (player == null || walkableTiles.isEmpty()) return false;

        float targetDistPx = targetDistanceInTiles * GameConstants.Sprite.TILE_SIZE;
        float tolerance = 5 * GameConstants.Sprite.TILE_SIZE; // טווח גמישות של 5 טיילים

        for (int i = 0; i < 20; i++) { // יותר ניסיונות כי אנחנו יותר ספציפיים
            Point tile = walkableTiles.get(MyApp.getRandom().nextInt(walkableTiles.size()));
            float worldX = tile.x * GameConstants.Sprite.TILE_SIZE;
            float worldY = tile.y * GameConstants.Sprite.TILE_SIZE;

            float currentDist = CollisionUtils.getDistance(worldX, worldY, player.getHitBox().centerX(), player.getHitBox().centerY());

            // האם המרחק הוא בערך מה שביקשנו? (בין המרחק לבין המרחק + גמישות)
            // בתוך spawnNewMonster
            if (currentDist >= targetDistPx && currentDist <= targetDistPx + tolerance) {
                Character monster = ObjectPoolManager.acquireCharacter(spawnType, new PointF(worldX, worldY));
                addCharacter(monster);
                return true;
            }
        }

        // fallback: אם לא מצאנו בטווח המדויק, ננסה לפחות לוודא שהיא לא *על* השחקן
        return spawnAtMinimumDistance(targetDistanceInTiles);
    }

    private boolean spawnAtMinimumDistance(int minDistanceInTiles) {
        float minDistPx = minDistanceInTiles * GameConstants.Sprite.TILE_SIZE;
        for (int i = 0; i < 15; i++) {
            Point tile = walkableTiles.get(MyApp.getRandom().nextInt(walkableTiles.size()));
            float worldX = tile.x * GameConstants.Sprite.TILE_SIZE;
            float worldY = tile.y * GameConstants.Sprite.TILE_SIZE;

            if (CollisionUtils.getDistance(worldX, worldY, player.getHitBox().centerX(), player.getHitBox().centerY()) > minDistPx) {
                addCharacter(ObjectPoolManager.acquireCharacter(spawnType, new PointF(worldX, worldY)));
                return true;
            }
        }
        return false;
    }

    // --- Getters & Accessors ---
    public MapLoadData getMapLoadData() { return mapLoadData; }
    public List<LightSource> getLightSources() { return lightSources; }
    public int getAmbientDarkness() { return ambientDarkness; }
    public synchronized List<Entity> getDrawableList() { return drawableList; }
    public synchronized ArrayList<Character> getCharacterArrayList() { return characterArrayList; }
    public synchronized List<BreakableEntity> getBreakableEntities() { return breakableEntities; }
    public ArrayList<Doorway> getDoorwayArrayList() { return doorwayArrayList; }
    public List<XpEffect> getXpList() { return xpEffectList; }
    public boolean hasLeavesEffect() { return hasEffects; }
    public boolean hasCloudsEffect() { return hasEffects; }
    public Player getPlayer() { return player; }
    public String getFileName() { return fileName; }
    public Tiles getGround() { return tilesType; }
    public int getMusicRes() { return musicRes; }
    public void setMusicRes(int musicRes) { this.musicRes = musicRes; }

    public void setPlayer(Player player) {
        synchronized (drawableList) {
            if (this.player != null) drawableList.remove(this.player);
            this.player = player;
            if (player != null && !drawableList.contains(player)) drawableList.add(player);
        }
    }

    public void removePlayer() {
        synchronized (drawableList) {
            drawableList.remove(player);
            player = null;
        }
    }

    public void addCharacter(Character character) {
        synchronized (characterArrayList) {
            if (!characterArrayList.contains(character)) {
                characterArrayList.add(character);
                synchronized (drawableList) {
                    drawableList.add(character);
                }
            }
        }
    }

    public void removeCharacter(String name) {
        synchronized (characterArrayList) {
            for (int i = characterArrayList.size() - 1; i >= 0; i--) {
                if (characterArrayList.get(i).getGameCharType().getName().equals(name)) {
                    Character removed = characterArrayList.remove(i);
                    synchronized (drawableList) {
                        drawableList.remove(removed);
                    }
                }
            }
        }
    }

    public void processPendingEntities() {
        if (pendingAddition.isEmpty()) return;
        synchronized (pendingAddition) {
            synchronized (drawableList) {
                for (int i = 0; i < pendingAddition.size(); i++) {
                    Entity e = pendingAddition.get(i);
                    if (e instanceof Coin) {
                        animatedEntities.add((Coin) e);
                    } else if (e instanceof CollectibleItem) {
                        collectibleItems.add((CollectibleItem) e);
                    }
                    if (!drawableList.contains(e)) drawableList.add(e);
                }
            }
            pendingAddition.clear();
        }
    }

    public void addCoin(Coin coin) { pendingAddition.add(coin); }
    public void addCollectibleItem(CollectibleItem item) { pendingAddition.add(item); }

    public void addXp(XpEffect xpEffect) { synchronized (xpEffectList) { xpEffectList.add(xpEffect); } }
    public void addDoorway(Doorway doorway) { this.doorwayArrayList.add(doorway); }

    // --- Tile Data Access ---
    public int getGroundIds(int col, int row) { return groundIds[row][col]; }
    public int getWallLayerID(int col, int row) { return wallLayer != null ? wallLayer[row][col] : -1; }
    public int getCollisionLayerID(int col, int row) { return collisionLayer != null ? collisionLayer[row][col] : -1; }
    public int getArrayWidth() { return groundIds[0].length; }
    public int getArrayHeight() { return groundIds.length; }
    public int getMapWidth() { return getArrayWidth() * GameConstants.Sprite.TILE_SIZE; }
    public int getMapHeight() { return getArrayHeight() * GameConstants.Sprite.TILE_SIZE; }

    public boolean isTileWalkable(int x, int y) {
        if (x < 0 || y < 0 || y >= getArrayHeight() || x >= getArrayWidth()) return false;
        return walkableMap[y][x];
    }

    private <T extends Enum<T> & StaticObjectData> void loadStaticObjects(List<ObjectData> list, Class<T> enumClass, boolean coll) {
        if (list != null) {
            for (ObjectData data : list) {
                try { this.staticObjectList.add(new StaticObject(data.position, Enum.valueOf(enumClass, data.type), coll)); } 
                catch (IllegalArgumentException ignored) {}
            }
        }
    }

    private void initWalkableMap() {
        int h = getArrayHeight(), w = getArrayWidth();
        walkableMap = new boolean[h][w];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int wall = (wallLayer != null) ? wallLayer[y][x] : -1;
                int coll = (collisionLayer != null) ? collisionLayer[y][x] : -1;
                walkableMap[y][x] = (wall <= 0 && coll <= 0);
            }
        }
    }

    private void initSurfaceMap() {
        int h = getArrayHeight(), w = getArrayWidth();
        surfaceTypeMap = new SurfaceType[h][w];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (surfaceLayer == null) { surfaceTypeMap[y][x] = SurfaceType.NONE; continue; }

                int localId = surfaceLayer[y][x];
                if (localId < 0) surfaceTypeMap[y][x] = SurfaceType.NONE; 
                else {
                    String typeStr = getTileProperty(localId, "type");
                    try { surfaceTypeMap[y][x] = (typeStr != null) ? SurfaceType.valueOf(typeStr) : SurfaceType.NONE; }
                    catch (Exception e) { surfaceTypeMap[y][x] = SurfaceType.NONE; }
                }
            }
        }
    }

    public SurfaceType getSurfaceTypeAt(int x, int y) {
        if (x < 0 || y < 0 || y >= getArrayHeight() || x >= getArrayWidth()) return SurfaceType.NONE;
        return surfaceTypeMap[y][x];
    }


    public String getTileProperty(int localId, String propertyName) {
        if (localId < 0 || tilesets == null) return null;

        for (int i = tilesets.size() - 1; i >= 0; i--) {
            Map<String, String> props = tilesets.get(i).tileProperties.get(localId);
            if (props != null && props.containsKey(propertyName)) {
                return props.get(propertyName);
            }
        }
        return null;
    }

    public Doorway getDoorwayByName(String name) {
        for (Doorway d : doorwayArrayList) if (d.getName().equals(name)) return d;
        return null;
    }

    public void reload() {
        synchronized (MapManager.class) {
            for (ObjectPoolManager.RespawnTask task : respawnQueue) {
                ObjectPoolManager.releaseRespawnTask(task);
            }
            respawnQueue.clear();

            characterArrayList.clear();
            staticObjectList.clear();
            xpEffectList.clear();
            doorwayArrayList.clear();
            lightSources.clear();
            animatedEntities.clear();
            collectibleItems.clear();
            breakableEntities.clear();

            loadAllEntities(mapLoadData);
            initWalkableMap();
            initSurfaceMap();
            findWalkableTiles();
        }
    }
    public int getRawResourceIdByName(String resourceName) {
        Context context = BaseActivity.getContext();
        return context.getResources().getIdentifier(
                resourceName,
                "raw",
                context.getPackageName()
        );
    }

    public void setSpawnType(String spawnType) {
        this.spawnType = spawnType;
    }
    public void setMinMonsters(int minMonsters) {
        this.minMonsters = minMonsters;
    }
    public void spawnMonsterOnPlayer(int num) {
        for (int i = 0; i < num; i++) {
            // כאן נשתמש במרחק קטן (נניח 5-8 טיילים) כדי שהם יופיעו סביבו במרדף
            spawnNewMonster(3);
        }
    }

    public boolean hasEffects() {
        return hasEffects;
    }
}
