package com.example.tutorialgame.environments.maploder;

import java.util.List;

public class MapLoadData {
    public final String fileName;
    public final int[][] groundLayer;
    public final int[][] wallsLayer;
    public final int[][] collisionLayer;
    public final int[][] surfaceLayer;
    public final List<ObjectData> buildingLayer;
    public final List<ObjectData> fenceLayer;
    public final List<ObjectData> characterLayer;
    public final List<ObjectData> natureLayer;
    public final List<ObjectData> itemLayer;
    public final List<ObjectData> objectLayer;
    public final List<ObjectData> doorLayer;
    public final List<ObjectData> lightLayer; 
    public final List<ObjectData> animatedLayer;
    public final List<ObjectData> breakableLayer;
    public final List<ObjectData> triggerLayer;
    public final List<TilesetData> tilesets;
    
    public int ambientDarkness; 
    public String musicFile; // The raw resource name (e.g., "music_village")

    // --- מאפייני Spawn דינמי ---
    public String spawnType;
    public int minMonsters;

    public MapLoadData(String fileName, int[][] ground, int[][] walls, int[][] collision, int[][] surface,
                       List<TilesetData> tilesets, List<ObjectData> buildings, List<ObjectData> fences,
                       List<ObjectData> characterLayer, List<ObjectData> natureLayer,
                       List<ObjectData> items, List<ObjectData> objects, List<ObjectData> doorLayer,
                       List<ObjectData> lightLayer, List<ObjectData> animatedLayer, 
                       List<ObjectData> breakableLayer, List<ObjectData> triggerLayer) {
        this.fileName = fileName;
        this.groundLayer = ground;
        this.wallsLayer = walls;
        this.collisionLayer = collision;
        this.surfaceLayer = surface;
        this.tilesets = tilesets;
        this.buildingLayer = buildings;
        this.fenceLayer = fences;
        this.characterLayer = characterLayer;
        this.natureLayer = natureLayer;
        this.itemLayer = items;
        this.objectLayer = objects;
        this.doorLayer = doorLayer;
        this.lightLayer = lightLayer;
        this.animatedLayer = animatedLayer;
        this.breakableLayer = breakableLayer;
        this.triggerLayer = triggerLayer;
    }
}
