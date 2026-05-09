package com.example.tutorialgame.cloud.document;

import androidx.annotation.NonNull;

import com.example.tutorialgame.MyApp;
import com.example.tutorialgame.cloud.BaseDocument;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages the user's cosmetic assets and currency (coins) in Firebase Firestore.
 * Implements atomic updates and defensive parsing for maximum data integrity.
 */
public class CosmeticDoc extends BaseDocument {
    // Database Field Constants
    private static final String KEY_ROOT = "cosmetic";
    private static final String KEY_COINS = KEY_ROOT + ".coinsLeft";
    private static final String KEY_CURRENT_FRAME = KEY_ROOT + ".currentFrame";
    private static final String KEY_AVAILABLE_FRAMES = KEY_ROOT + ".availableFrames";

    // Fallback Defaults
    private static final int DEFAULT_COINS = 0;
    private static final String DEFAULT_FRAME = "FRAME_00";

    private int coinsLeft;
    private String currentFrame;
    private List<String> availableFrames = new ArrayList<>();

    public CosmeticDoc(DocumentReference docRef, Runnable onFinishedLoading) {
        super(docRef, onFinishedLoading);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void parseData(@NonNull Map<String, Object> data) {
        // Defensive parsing for each field to handle missing keys or type mismatches
        
        // 1. Safe parsing for Coins (Number could be Long or Double from Firestore)
        Object coinsVal = data.get("coinsLeft");
        this.coinsLeft = (coinsVal instanceof Number) ? ((Number) coinsVal).intValue() : DEFAULT_COINS;

        // 2. Safe parsing for Current Frame
        Object frameVal = data.get("currentFrame");
        this.currentFrame = (frameVal instanceof String) ? (String) frameVal : DEFAULT_FRAME;
        
        // 3. Safe parsing for Available Frames list
        Object framesObj = data.get("availableFrames");
        if (framesObj instanceof List) {
            this.availableFrames = (List<String>) framesObj;
        } else {
            this.availableFrames = new ArrayList<>();
            this.availableFrames.add(DEFAULT_FRAME);
        }
    }

    @NonNull
    @Override
    protected String getDocName() {
        return KEY_ROOT;
    }

    /**
     * Initializes a new cosmetic document with default values.
     */
    public void createDoc() {
        Map<String, Object> cosmetic = new HashMap<>();
        cosmetic.put("coinsLeft", DEFAULT_COINS);
        cosmetic.put("currentFrame", DEFAULT_FRAME);
        
        List<String> initialFrames = new ArrayList<>();
        initialFrames.add(DEFAULT_FRAME);
        cosmetic.put("availableFrames", initialFrames);

        Map<String, Object> doc = new HashMap<>();
        doc.put(getDocName(), cosmetic);

        docRef.set(doc, SetOptions.merge());
    }

    /**
     * Attempts to purchase an item using an atomic database increment.
     * @param price The cost of the item.
     * @return true if the user has enough coins and the transaction started, false otherwise.
     */
    public boolean purchase(int price) {
        if (coinsLeft < price) return false;

        coinsLeft -= price;
        // Atomic decrement: ensures server-side consistency and prevents race conditions
        docRef.update(KEY_COINS, FieldValue.increment(-price));
        return true;
    }

    /**
     * Adds a single coin using an atomic database increment.
     */
    public void addCoin() {
        coinsLeft++;
        docRef.update(KEY_COINS, FieldValue.increment(1));
    }

    /**
     * Adds a bulk amount of coins.
     */
    public void increaseCoins(int amount) {
        if (amount <= 0) return;
        coinsLeft += amount;
        docRef.update(KEY_COINS, FieldValue.increment(amount));
    }

    /**
     * Adds a frame to the available collection if not already present.
     */
    public void addAvailableFrame(String frameName) {
        if (availableFrames != null && !availableFrames.contains(frameName)) {
            availableFrames.add(frameName);
            // arrayUnion ensures no duplicates are added server-side
            docRef.update(KEY_AVAILABLE_FRAMES, FieldValue.arrayUnion(frameName));
            MyApp.getCloudManager().getSlotsMetadata().updateFramesCount(MyApp.getCloudManager().getActiveSlotId(), 1);
        }
    }

    public void setCurrentFrame(String frameName) {
        this.currentFrame = frameName;
        docRef.update(KEY_CURRENT_FRAME, frameName);
    }

    public List<String> getAvailableFrames() { return availableFrames; }
    public boolean isFrameAvailable(String frameName) { return availableFrames.contains(frameName); }
    public int getCoinsLeft() { return coinsLeft; }
    public String getCurrentFrame() { return currentFrame; }
}
