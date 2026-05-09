package com.example.tutorialgame.ui.dialogs;

import android.text.InputType;

import com.example.tutorialgame.R;
import com.example.tutorialgame.cloud.document.ProfileDoc;

public enum DialogKeys {
    PROFILE_EMAIL(ProfileDoc.Field.EMAIL, R.string.email_title, R.string.email_message, InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS, R.string.email),
    PROFILE_NICKNAME(ProfileDoc.Field.NICKNAME, R.string.nickname_title, R.string.nickname_message, InputType.TYPE_CLASS_TEXT, R.string.nickname),
    CHANGE_SLOT(null, R.string.change_slot_title, R.string.change_slot_message, -1, -1),
    CREATE_SLOT(null, R.string.create_slot_title, R.string.create_slot_message, -1, -1),
    SETTINGS_LOGOUT(null, R.string.wanna_logout, -1, -1, -1),
    DELETE_SLOT_POSITIVE(null, R.string.delete_slot_title, R.string.delete_slot_message_positive,-1 ,-1),
    DELETE_SLOT_NEGATIVE(null, R.string.delete_slot_title, R.string.delete_slot_message_negative, -1, -2);

    private final int title, message, hint;
    private final ProfileDoc.Field key;
    private final int inputType;
    DialogKeys(ProfileDoc.Field key, int title, int message, int type, int hint) {
        this.key = key;
        this.title = title;
        this.message = message;
        this.inputType = type;
        this.hint = hint;
    }

    public ProfileDoc.Field getKey() {
        return key;
    }

    public int getTitle() {
        return title;
    }

    public int getMessage() {
        return message;
    }

    public int getInputType() {
        return inputType;
    }

    public int getHint() {
        return hint;
    }
}
