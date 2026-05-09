package com.example.tutorialgame.cloud.document;

import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.tutorialgame.MyApp;
import com.example.tutorialgame.R;
import com.example.tutorialgame.cloud.BaseDocument;
import com.example.tutorialgame.ui.base.BaseActivity;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.SetOptions;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Manages the player's progression metrics (XP, Level, Achievements) in Firebase Firestore.
 * Implements defensive parsing and atomic updates for reliable data tracking.
 */
public class ProgressDoc extends BaseDocument {
    // Database Field Constants
    private static final String KEY_ROOT = "progress";
    private static final String KEY_XP = KEY_ROOT + ".xp";
    private static final String KEY_LEVEL = KEY_ROOT + ".level";
    private static final String KEY_UPGRADE_POINTS = KEY_ROOT + ".upgradePoints";
    private static final String KEY_UPGRADES_DONE = KEY_ROOT + ".upgradesDone";
    private static final String KEY_DAYS_LOGGED = KEY_ROOT + ".daysLoggedIn";
    private static final String KEY_ENEMIES_KILLED = KEY_ROOT + ".enemiesDefeated";
    private static final String KEY_QUESTS_DONE = KEY_ROOT + ".questsCompleted";
    private static final String KEY_MET_CHARS = KEY_ROOT + ".metCharacters";
    private static final String KEY_LAST_LOGIN = KEY_ROOT + ".lastLoginDate";

    // Fields initialized with default values
    private int xp = 0;
    private int level = 1;
    private int upgradePoints = 0;
    private int upgradesDone = 0;
    private int daysLoggedIn = 0;
    private int enemiesDefeated = 0;
    private int questsCompleted = 0;
    private String lastLoginDate = "";
    private List<String> metCharacters = new ArrayList<>();
    
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public ProgressDoc(DocumentReference userRef, Runnable onFinishedLoading) {
        super(userRef, onFinishedLoading);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void parseData(@NonNull Map<String, Object> data) {
        Object val;

        if ((val = data.get("xp")) instanceof Number) this.xp = ((Number) val).intValue();
        if ((val = data.get("level")) instanceof Number) this.level = ((Number) val).intValue();
        if ((val = data.get("upgradePoints")) instanceof Number) this.upgradePoints = ((Number) val).intValue();
        if ((val = data.get("upgradesDone")) instanceof Number) this.upgradesDone = ((Number) val).intValue();
        if ((val = data.get("daysLoggedIn")) instanceof Number) this.daysLoggedIn = ((Number) val).intValue();
        if ((val = data.get("enemiesDefeated")) instanceof Number) this.enemiesDefeated = ((Number) val).intValue();
        if ((val = data.get("questsCompleted")) instanceof Number) this.questsCompleted = ((Number) val).intValue();
        if ((val = data.get("lastLoginDate")) instanceof String) this.lastLoginDate = (String) val;
        
        Object metObj = data.get("metCharacters");
        if (metObj instanceof List) {
            this.metCharacters = (List<String>) metObj;
        }
    }

    @NonNull
    @Override
    protected String getDocName() {
        return KEY_ROOT;
    }

    public void createProfile() {
        String now = dateFormat.format(Calendar.getInstance().getTime());
        Map<String, Object> progress = new HashMap<>();
        progress.put("xp", 0);
        progress.put("level", 1);
        progress.put("upgradePoints", 0);
        progress.put("upgradesDone", 0);
        progress.put("daysLoggedIn", 1); // יום ראשון
        progress.put("enemiesDefeated", 0);
        progress.put("questsCompleted", 0);
        progress.put("metCharacters", new ArrayList<String>());
        progress.put("lastLoginDate", now);

        Map<String, Object> doc = new HashMap<>();
        doc.put(getDocName(), progress);
        docRef.set(doc, SetOptions.merge());
    }

    /**
     * מעדכן את הכניסה היומית עבור הסלוט הספציפי.
     */
    public void updateLogin() {
        String todayDate = dateFormat.format(Calendar.getInstance().getTime());
        if (!Objects.equals(lastLoginDate, todayDate)) {
            lastLoginDate = todayDate;
            increaseDaysLoggedIn();
            docRef.update(KEY_LAST_LOGIN, lastLoginDate);
        }
    }

    public void updateXp(int value) {
        if (value == 0) return;
        this.xp += value;
        docRef.update(KEY_XP, FieldValue.increment(value));
        checkLevelUp();
    }

    private void checkLevelUp() {
        boolean leveled = false;
        while (xp >= neededXpForLevelUp()) {
            int needed = neededXpForLevelUp();
            this.xp -= needed;
            docRef.update(KEY_XP, FieldValue.increment(-needed));
            
            increaseLevel();
            increaseUpgradePoints(3); 
            leveled = true;
        }
        
        if (leveled) {
            MyApp.getCloudManager().getSlotsMetadata().updateLevel(MyApp.getCloudManager().getActiveSlotId(), level);
        }
    }

    public void registerEncounter(String charId) {
        if (metCharacters != null && !metCharacters.contains(charId)) {
            metCharacters.add(charId);
            docRef.update(KEY_MET_CHARS, FieldValue.arrayUnion(charId));
            updateXp(5);

            BaseActivity activity = (BaseActivity) BaseActivity.getContext();
            if (activity != null) {
                activity.showToast(MessageFormat.format("{0} {1} {2} 5 xp", 
                        activity.getString(R.string.new_char_met), charId, activity.getString(R.string.you_gained)), 
                        Toast.LENGTH_LONG);
            }
        }
    }

    private void increaseLevel() {
        this.level++;
        docRef.update(KEY_LEVEL, FieldValue.increment(1));
    }

    public int neededXpForLevelUp() {
        return (int) Math.pow(level + 1, 2) * 10;
    }

    public void increaseUpgradePoints(int amount) {
        this.upgradePoints += amount;
        docRef.update(KEY_UPGRADE_POINTS, FieldValue.increment(amount));
    }

    public void decreaseUpgradePoints(int price) {
        this.upgradePoints -= price;
        docRef.update(KEY_UPGRADE_POINTS, FieldValue.increment(-price));
    }

    public void increaseUpgradeDone() {
        addUpgradesDone(1);
    }

    public void addUpgradesDone(int amount) {
        this.upgradesDone += amount;
        docRef.update(KEY_UPGRADES_DONE, FieldValue.increment(amount));
    }

    public void increaseDaysLoggedIn() {
        this.daysLoggedIn++;
        docRef.update(KEY_DAYS_LOGGED, FieldValue.increment(1));
    }

    public void increaseEnemiesDefeated() {
        this.enemiesDefeated++;
        docRef.update(KEY_ENEMIES_KILLED, FieldValue.increment(1));
    }

    public void increaseQuestsCompleted() {
        this.questsCompleted++;
        docRef.update(KEY_QUESTS_DONE, FieldValue.increment(1));
    }

    // Getters
    public int getUpgradePoints() { return upgradePoints; }
    public int getXp() { return xp; }
    public int getLevel() { return level; }
    public int getUpgradesDone() { return upgradesDone; }
    public int getDaysLoggedIn() { return daysLoggedIn; }
    public int getEnemiesDefeated() { return enemiesDefeated; }
    public int getQuestsCompleted() { return questsCompleted; }
}
