package com.example.tutorialgame.cloud;

import com.example.tutorialgame.cloud.document.CosmeticDoc;
import com.example.tutorialgame.cloud.document.ProgressDoc;
import com.example.tutorialgame.cloud.document.StatsDoc;
import com.example.tutorialgame.cloud.document.WorldStateDoc;
import com.google.firebase.firestore.DocumentReference;

/**
 * מנהל את הנתונים בתוך סלוט שמירה ספציפי.
 * כל המודולים (Stats, Progress וכו') נשמרים תחת ה-slotDoc שמתקבל בבנאי.
 */
public class UserDataManager {
    private StatsDoc statsDoc;
    private ProgressDoc progressDoc;
    private CosmeticDoc cosmeticDoc;
    private WorldStateDoc worldStateDoc;
    private int successfulLoads;
    private boolean hasLoadingFailed;

    private final DocumentReference slotDoc;

    public interface OnDataLoadedListener {
        void onDataLoadSuccess();
        void onDataLoadFailed();
    }

    public UserDataManager(DocumentReference slotDoc) {
        this.slotDoc = slotDoc;
    }

    /**
     * מאתחל נתוני סלוט חדש (New Game)
     */
    public void initializeNewSlotData() {
        progressDoc = new ProgressDoc(slotDoc, null);
        statsDoc = new StatsDoc(slotDoc, null);
        cosmeticDoc = new CosmeticDoc(slotDoc, null);
        worldStateDoc = new WorldStateDoc(slotDoc, null);

        progressDoc.createProfile();
        statsDoc.createDefaults();
        cosmeticDoc.createDoc();
        worldStateDoc.createDefaults();
    }

    public void loadAndCache(OnDataLoadedListener listener) {
        this.successfulLoads = 0;
        this.hasLoadingFailed = false;

        OnDataLoadedListener safeListener = new OnDataLoadedListener() {
            @Override
            public void onDataLoadSuccess() {}
            @Override
            public void onDataLoadFailed() {
                if (!hasLoadingFailed) {
                    hasLoadingFailed = true;
                    listener.onDataLoadFailed();
                }
            }
        };

        // טעינת 4 מודולים (ProfileDoc עבר ל-CloudManager)
        progressDoc = new ProgressDoc(slotDoc, () -> checkAllDataLoaded(listener));
        statsDoc = new StatsDoc(slotDoc, () -> checkAllDataLoaded(listener));
        cosmeticDoc = new CosmeticDoc(slotDoc, () -> checkAllDataLoaded(listener));
        worldStateDoc = new WorldStateDoc(slotDoc, () -> checkAllDataLoaded(listener));

        slotDoc.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                progressDoc.loadAndCache(safeListener);
                statsDoc.loadAndCache(safeListener);
                cosmeticDoc.loadAndCache(safeListener);
                worldStateDoc.loadAndCache(safeListener);
            } else safeListener.onDataLoadFailed();
        });
    }

    private synchronized void checkAllDataLoaded(OnDataLoadedListener listener) {
        if (hasLoadingFailed) return;
        successfulLoads++;
        // 4 תהליכים: progress, stats, cosmetic, worldState
        if (successfulLoads == 4) {
            listener.onDataLoadSuccess();
        }
    }

    public void deleteData() {
        // מחיקת המסמך הראשי של הסלוט בענן
        slotDoc.delete();
        // איפוס האובייקטים המקומיים בזיכרון (Cache)
        statsDoc = null;
        progressDoc = null;
        cosmeticDoc = null;
        worldStateDoc = null;
    }

    public StatsDoc getPlayerStats() { return statsDoc; }
    public ProgressDoc getProgress() { return progressDoc; }
    public CosmeticDoc getCosmeticDoc() { return cosmeticDoc; }
    public WorldStateDoc getWorldStateDoc() { return worldStateDoc; }
}
