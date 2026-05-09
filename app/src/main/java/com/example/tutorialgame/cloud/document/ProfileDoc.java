package com.example.tutorialgame.cloud.document;

import androidx.annotation.NonNull;

import com.example.tutorialgame.cloud.BaseDocument;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Manages the user's personal identity data (nickname, email, login history) in Firebase Firestore.
 */
public class ProfileDoc extends BaseDocument {
    private static final String KEY_ROOT = "profile";
    private static final String KEY_NICKNAME = KEY_ROOT + ".nickname";
    private static final String KEY_EMAIL = KEY_ROOT + ".email";
    private static final String KEY_LAST_LOGIN = KEY_ROOT + ".lastLoginDate";
    private static final String KEY_LAST_SLOT = KEY_ROOT + ".lastSelectedSlot";

    public enum Field {EMAIL, NICKNAME}
    
    private String nickname = "";
    private String email = "";
    private String lastLoginDate = "";
    private int lastSelectedSlot = 1;
    
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public ProfileDoc(DocumentReference userRef, Runnable onFinishedLoading) {
        super(userRef, onFinishedLoading);
    }

    @Override
    protected void parseData(@NonNull Map<String, Object> data) {
        Object val;
        
        if ((val = data.get("nickname")) instanceof String) 
            this.nickname = (String) val;
            
        if ((val = data.get("email")) instanceof String) 
            this.email = (String) val;
            
        if ((val = data.get("lastLoginDate")) instanceof String) 
            this.lastLoginDate = (String) val;

        if ((val = data.get("lastSelectedSlot")) instanceof Number)
            this.lastSelectedSlot = ((Number) val).intValue();
    }

    @NonNull
    @Override
    protected String getDocName() {
        return KEY_ROOT;
    }

    public void createProfile(String nickname, String email) {
        String now = dateFormat.format(Calendar.getInstance().getTime());
        
        Map<String, Object> profile = new HashMap<>();
        profile.put("nickname", nickname);
        profile.put("email", email);
        profile.put("created", now);
        profile.put("lastLoginDate", now);
        profile.put("lastSelectedSlot", 1);

        Map<String, Object> doc = new HashMap<>();
        doc.put(getDocName(), profile);

        docRef.set(doc, SetOptions.merge());
    }

    /**
     * Updates the last login date for the entire profile (user-level).
     */
    public void updateLogin() {
        String todayDate = dateFormat.format(Calendar.getInstance().getTime());
        if (!Objects.equals(lastLoginDate, todayDate)) {
            lastLoginDate = todayDate;
            docRef.update(KEY_LAST_LOGIN, lastLoginDate);
        }
    }

    public Task<Void> updateLastSelectedSlot(int slotId) {
        this.lastSelectedSlot = slotId;
        return docRef.update(KEY_LAST_SLOT, slotId);
    }

    public int getLastSelectedSlot() { return lastSelectedSlot; }
    public String getNickname() { return nickname; }
    public String getEmail() { return email; }

    public String getField(Field key) {
        switch (key) {
            case EMAIL: return email;
            case NICKNAME: return nickname;
            default: return "";
        }
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
        docRef.update(KEY_NICKNAME, nickname);
    }
}
