package com.example.tutorialgame.utils;

import static com.example.tutorialgame.engine.core.GameConstants.Sprite.SCALE_MULTIPLIER;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;
import com.example.tutorialgame.engine.ui.effects.lighting.LightSource;
import com.example.tutorialgame.environments.maploder.MapLoadData;
import com.example.tutorialgame.environments.maploder.ObjectData;
import com.example.tutorialgame.environments.maploder.TilesetData;
import com.example.tutorialgame.ui.base.BaseActivity;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Utility class for loading and parsing TMX (Tiled Map Editor) files.
 * Handles layers, objects, tilesets, and custom properties.
 */
public class TmxLoaderUtils {
    private static final String TAG = "TmxLoaderUtils";

    private TmxLoaderUtils() { }

    /**
     * Loads a TMX map from the assets folder and parses its content into MapLoadData.
     * @param fileName The name of the TMX file (expected in assets/maps/).
     * @return A MapLoadData object containing all parsed layers and objects.
     */
    public static MapLoadData loadMapFromTMX(String fileName) {
        List<TilesetData> localTilesets = new ArrayList<>();
        int[][] groundLayer = null, wallLayer = null, collisionLayer = null, surfaceLayer = null;

        List<ObjectData> buildingLayer = new ArrayList<>(), fenceLayer = new ArrayList<>(),
                characterLayer = new ArrayList<>(), doorLayer = new ArrayList<>(),
                natureLayer = new ArrayList<>(), itemLayer = new ArrayList<>(),
                objectLayer = new ArrayList<>(), lightLayer = new ArrayList<>(),
                animatedLayer = new ArrayList<>(), breakableLayer = new ArrayList<>(),
                triggerLayer = new ArrayList<>();

        int ambientDarkness = 0, minMonsters = 0;
        String spawnType = "";
        String musicFile = "music_calm_village"; // Fallback value if property is missing

        try (InputStream is = BaseActivity.getContext().getAssets().open("maps/" + fileName)) {
            DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = docBuilder.parse(is);
            doc.getDocumentElement().normalize();

            Element mapRoot = doc.getDocumentElement();
            
            // Parse global map properties
            NodeList mapPropsNodes = mapRoot.getElementsByTagName("properties");
            if (mapPropsNodes.getLength() > 0) {
                NodeList props = ((Element)mapPropsNodes.item(0)).getElementsByTagName("property");
                for (int i = 0; i < props.getLength(); i++) {
                    Element p = (Element) props.item(i);
                    String pName = p.getAttribute("name");
                    String pVal = p.getAttribute("value");
                    if ("ambientDarkness".equals(pName)) ambientDarkness = Integer.parseInt(pVal);
                    else if ("minMonsters".equals(pName)) minMonsters = Integer.parseInt(pVal);
                    else if ("spawnType".equals(pName)) spawnType = pVal;
                    else if ("music".equals(pName) && !pVal.isEmpty()) musicFile = pVal;
                }
            }

            // Parse tilesets
            NodeList tilesetNodes = doc.getElementsByTagName("tileset");
            for (int i = 0; i < tilesetNodes.getLength(); i++) {
                TilesetData ts = parseTileset((Element) tilesetNodes.item(i));
                if (ts != null) localTilesets.add(ts);
            }

            // Parse tile layers
            groundLayer = parseTileLayer(doc, "Ground", localTilesets);
            wallLayer = parseTileLayer(doc, "Walls", localTilesets);
            collisionLayer = parseTileLayer(doc, "Collision", localTilesets);
            surfaceLayer = parseTileLayer(doc, "Surface", localTilesets);

            // Parse object layers
            buildingLayer = parseObjectLayer(doc, "Buildings");
            fenceLayer = parseObjectLayer(doc, "Fences");
            characterLayer = parseObjectLayer(doc, "Characters");
            natureLayer = parseObjectLayer(doc, "Natures");
            itemLayer = parseObjectLayer(doc, "Items");
            objectLayer = parseObjectLayer(doc, "Objects");
            doorLayer = parseObjectLayer(doc, "Doors");
            lightLayer = parseObjectLayer(doc, "Lights");
            animatedLayer = parseObjectLayer(doc, "AnimatedObjects");
            breakableLayer = parseObjectLayer(doc, "Breakables");
            triggerLayer = parseObjectLayer(doc, "Triggers");

        } catch (Exception e) {
            Log.e(TAG, "Error loading TMX map file: " + fileName, e);
        }

        MapLoadData loadData = new MapLoadData(
                fileName, groundLayer, wallLayer, collisionLayer, surfaceLayer, localTilesets,
                buildingLayer, fenceLayer, characterLayer, natureLayer,
                itemLayer, objectLayer, doorLayer, lightLayer, animatedLayer, breakableLayer, triggerLayer
        );
        loadData.ambientDarkness = ambientDarkness;
        loadData.minMonsters = minMonsters;
        loadData.spawnType = spawnType;
        loadData.musicFile = musicFile;
        return loadData;
    }

    private static TilesetData parseTileset(Element tilesetElement) {
        try {
            String source = tilesetElement.getAttribute("source");
            int firstGid = Integer.parseInt(tilesetElement.getAttribute("firstgid"));
            
            // Extract tileset filename and open from assets/tilesets/
            String tsFileName = source.substring(source.lastIndexOf('/') + 1);
            try (InputStream is = BaseActivity.getContext().getAssets().open("tilesets/" + tsFileName)) {
                Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
                doc.getDocumentElement().normalize();
                Element root = doc.getDocumentElement();
                
                TilesetData tilesetData = new TilesetData(firstGid, root.getAttribute("name"));
                
                // Parse individual tile properties (e.g., specific metadata for a tile ID)
                NodeList tileNodes = root.getElementsByTagName("tile");
                for (int i = 0; i < tileNodes.getLength(); i++) {
                    Element tileEl = (Element) tileNodes.item(i);
                    int localId = Integer.parseInt(tileEl.getAttribute("id"));
                    Map<String, String> properties = new HashMap<>();
                    NodeList propertyNodes = tileEl.getElementsByTagName("property");
                    for (int j = 0; j < propertyNodes.getLength(); j++) {
                        Element propEl = (Element) propertyNodes.item(j);
                        properties.put(propEl.getAttribute("name"), propEl.getAttribute("value"));
                    }
                    tilesetData.tileProperties.put(localId, properties);
                }
                return tilesetData;
            }
        } catch (Exception e) {
            Log.w(TAG, "Error parsing tileset", e);
            return null;
        }
    }

    private static int[][] parseTileLayer(Document doc, String layerName, List<TilesetData> tilesets) {
        Element layerEl = findLayerElement(doc, layerName);
        if (layerEl == null) return null;

        int width = Integer.parseInt(layerEl.getAttribute("width"));
        int height = Integer.parseInt(layerEl.getAttribute("height"));
        int[][] mapData = new int[height][width];

        NodeList dataNodes = layerEl.getElementsByTagName("data");
        if (dataNodes.getLength() == 0) return null;
        
        String csvData = dataNodes.item(0).getTextContent();
        try (Scanner scanner = new Scanner(csvData).useDelimiter("[\\s,]+")) {
            for (int row = 0; row < height; row++) {
                for (int col = 0; col < width; col++) {
                    if (!scanner.hasNextInt()) break;
                    int gid = scanner.nextInt();
                    if (gid <= 0) mapData[row][col] = -1;
                    else {
                        int firstGid = findFirstGidForGid(gid, tilesets);
                        mapData[row][col] = gid - firstGid;
                    }
                }
            }
        }
        return mapData;
    }

    private static Element findLayerElement(Document doc, String layerName) {
        NodeList layerNodes = doc.getElementsByTagName("layer");
        for (int i = 0; i < layerNodes.getLength(); i++) {
            Element el = (Element) layerNodes.item(i);
            if (layerName.equals(el.getAttribute("name"))) return el;
        }
        return null;
    }

    private static int findFirstGidForGid(int gid, List<TilesetData> tilesets) {
        for (int i = tilesets.size() - 1; i >= 0; i--) {
            if (gid >= tilesets.get(i).firstGid) return tilesets.get(i).firstGid;
        }
        return 0;
    }

    private static List<ObjectData> parseObjectLayer(Document doc, String layerName) {
        List<ObjectData> objectList = new ArrayList<>();
        NodeList objectGroups = doc.getElementsByTagName("objectgroup");
        for (int i = 0; i < objectGroups.getLength(); i++) {
            Element objectGroup = (Element) objectGroups.item(i);
            if (layerName.equals(objectGroup.getAttribute("name"))) {
                NodeList objects = objectGroup.getElementsByTagName("object");
                for (int j = 0; j < objects.getLength(); j++) {
                    try {
                        Element objectEl = (Element) objects.item(j);
                        String name = objectEl.getAttribute("name");
                        float x = Float.parseFloat(objectEl.getAttribute("x")) * SCALE_MULTIPLIER;
                        float y = Float.parseFloat(objectEl.getAttribute("y")) * SCALE_MULTIPLIER;
                        float w = (objectEl.hasAttribute("width") ? Float.parseFloat(objectEl.getAttribute("width")) : 0) * SCALE_MULTIPLIER;
                        float h = (objectEl.hasAttribute("height") ? Float.parseFloat(objectEl.getAttribute("height")) : 0) * SCALE_MULTIPLIER;
                        
                        String type = objectEl.hasAttribute("type") ? objectEl.getAttribute("type") :
                                (objectEl.hasAttribute("class") ? objectEl.getAttribute("class") : "");

                        String connectsTo = "", targetDoor = "", requiredCp = "";
                        int lightColor = Color.WHITE;
                        LightSource.LightType lightType = LightSource.LightType.STATIC;

                        // Parse custom properties for the object
                        NodeList properties = objectEl.getElementsByTagName("property");
                        for (int k = 0; k < properties.getLength(); k++) {
                            Element prop = (Element) properties.item(k);
                            String pName = prop.getAttribute("name");
                            String pVal = prop.getAttribute("value");
                            switch (pName) {
                                case "type": type = pVal; break;
                                case "connectsTo": connectsTo = pVal; break;
                                case "targetDoor": targetDoor = pVal; break;
                                case "requiredCheckPoint": requiredCp = pVal; break;
                                case "color": try { lightColor = Color.parseColor(pVal); } catch (Exception ignored) {} break;
                                case "lightType": try { lightType = LightSource.LightType.valueOf(pVal.toUpperCase()); } catch (Exception ignored) {} break;
                            }
                        }

                        // Use center position for lights, top-left for other objects
                        PointF pos = (layerName.equals("Lights")) ?
                                new PointF(x + w / 2f, y + h / 2f) :
                                new PointF(x, y);

                        RectF bounds = new RectF(x, y, x + w, y + h);
                        ObjectData data = new ObjectData(name, type, pos, bounds, connectsTo, targetDoor, requiredCp);
                        
                        if (layerName.equals("Lights")) {
                            data.setLightProperties(w / 2f, lightColor, lightType);
                        }
                        objectList.add(data);
                    } catch (Exception e) {
                        Log.w(TAG, "Failed to parse object in layer " + layerName, e);
                    }
                }
                break;
            }
        }
        return objectList;
    }
}
