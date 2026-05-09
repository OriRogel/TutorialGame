package com.example.tutorialgame.environments.maploder;

import java.util.HashMap;
import java.util.Map;

public class TilesetData {
    public final int firstGid;
    public final String name;
    // מפה שמקשרת ID מקומי (בתוך ה-tileset) למאפיינים שלו
    public final Map<Integer, Map<String, String>> tileProperties;

    // עדכן את הבנאי
    public TilesetData(int firstGid, String name) {
        this.firstGid = firstGid;
        this.name = name;
        this.tileProperties = new HashMap<>();
    }
}