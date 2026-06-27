package com.example.tutorialgame.cloud.document;

import static com.example.tutorialgame.engine.core.GameConstants.Sprite.TILE_SIZE;

import android.graphics.PointF;

import androidx.annotation.NonNull;

import com.example.tutorialgame.MyApp;
import com.example.tutorialgame.cloud.BaseDocument;
import com.example.tutorialgame.entities.Weapons;
import com.example.tutorialgame.entities.characters.Player;
import com.example.tutorialgame.managers.MapManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class WorldStateDoc extends BaseDocument {
    private String lastMap, currentWeapon;
    private PointF lastPosition = new PointF(11*TILE_SIZE, 25*TILE_SIZE);
    private Map<String, Boolean> storyFlags = new HashMap<>();

    public WorldStateDoc(DocumentReference userRef, Runnable onFinishedLoading) {
        super(userRef, onFinishedLoading);
    }

    @NonNull
    @Override
    protected String getDocName() {
        return "worldState";
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void parseData(@NonNull Map<String, Object> data) {
        lastMap = (String) data.getOrDefault("lastMap", "village.tmx");
        currentWeapon = (String) data.get("currentWeapon");
        storyFlags = (Map<String, Boolean>) data.get("storyFlags");
        Map<String, Number> posMap = (Map<String, Number>) data.get("lastPosition");
        if (posMap != null) {
            Number xNum = posMap.get("x");
            Number yNum = posMap.get("y");
            if (xNum != null && yNum != null) {
                lastPosition = new PointF(xNum.floatValue(), yNum.floatValue());
            }
        }
    }

    public void createDefaults() {
        Map<String, Object> state = new HashMap<>();
        state.put("lastMap", "chief_home.tmx");
        state.put("lastHealth", StatsDoc.INIT_HEALTH);
        state.put("currentWeapon", "");
        defaultStoryFlags();
        state.put("storyFlags", storyFlags);
        Map<String, Float> posMap = new HashMap<>();
        posMap.put("x", 2f);
        posMap.put("y", 11f);
        state.put("lastPosition", posMap);
        Map<String, Object> doc = new HashMap<>();
        doc.put(getDocName(), state);
        docRef.set(doc, SetOptions.merge());
    }

    private void defaultStoryFlags() {
        storyFlags.put("seen_cutscene_coldOpening", false);
        storyFlags.put("event_player_talkedToFather", false);
        storyFlags.put("event_player_talkedToFriend", false);
        storyFlags.put("event_blacksmith_finishedFirstTalk", false);
        storyFlags.put("seen_cutscene_gettingSword", false);
        storyFlags.put("event_player_receivedWeapon", false);
        storyFlags.put("event_player_talkedToFriend2", false);
        storyFlags.put("event_player_talkedToWhiteKnight", false);
        storyFlags.put("quest_find_clues", false);
        storyFlags.put("event_enter_maze", false);
        storyFlags.put("event_enter_maze_center", false);
        storyFlags.put("seen_cutscene_skeletonArise", false);
        storyFlags.put("event_escaped_from_maze", false);
    }

    /**
     * מחשב איזה "מפתח תמונה" להציג בתפריט הסלוטים על פי ההתקדמות בעלילה.
     */
    private String getVisualCheckpointKey() {
        if (getCheckPoint("state_player_isExiled")) return "EXILED";
        if (getCheckPoint("quest_escapeVillage_active")) return "ESCAPING";
        if (getCheckPoint("event_player_receivedWeapon")) return "ARMED";
        if (getCheckPoint("event_player_talkedToFather")) return "VILLAGE";
        return "START";
    }

    private void saveWorldState() {
        lastMap = MapManager.getCurrentMap().getFileName();
        Player player = MapManager.getCurrentMap().getPlayer();
        lastPosition = new PointF(player.getHitBox().left/TILE_SIZE, player.getHitBox().top/TILE_SIZE);

        docRef.update("worldState.lastMap", lastMap);
        Map<String, Float> pos = new HashMap<>();
        pos.put("x", lastPosition.x);
        pos.put("y", lastPosition.y);
        docRef.update("worldState.lastPosition", pos);

        // עדכון המטא-דאטה של הסלוט הפעיל
        if (MyApp.getCloudManager() != null) {
            int activeId = MyApp.getCloudManager().getActiveSlotId();
            if (activeId != -1) {
                MyApp.getCloudManager().getSlotsMetadata().updateSlotMetadata(
                        activeId,
//                        getVisualCheckpointKey(), // המפתח לתמונה
                        Objects.requireNonNull(MyApp.getProgress()).getLevel(),
                        MyApp.getCosmetic().getAvailableFrames().size()
                );
            }
        }
    }

    public void setCheckPoint(String flagName) {
        storyFlags.put(flagName, true);
        docRef.update("worldState.storyFlags." + flagName, true);
        saveWorldState();
    }

    public String getLastMap() { return lastMap; }
    public PointF getLastPosition() { return lastPosition; }
    public Weapons getCurrentWeapon() {
        if (currentWeapon != null && !currentWeapon.isEmpty()) return Weapons.valueOf(currentWeapon);
        else return null;
    }
    public boolean getCheckPoint(String flagName) {
        return Boolean.TRUE.equals(storyFlags.getOrDefault(flagName, false));
    }
    public void setCurrentWeapon(String weaponName) {
        currentWeapon = weaponName;
        docRef.update("worldState.currentWeapon", currentWeapon);
    }
}
