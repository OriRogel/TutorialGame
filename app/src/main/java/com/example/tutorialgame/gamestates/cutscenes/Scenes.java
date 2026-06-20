package com.example.tutorialgame.gamestates.cutscenes;

import android.graphics.Bitmap;

import com.example.tutorialgame.R;
import com.example.tutorialgame.managers.BitmapManager;

public enum Scenes {
    INTRO(R.drawable.cutscene_1, 5, "seen_cutscene_coldOpening", R.raw.cutscene_intro, false, null),
    GETTING_SWORD(R.drawable.cutscene_2, 3, "seen_cutscene_gettingSword", R.raw.cutscene_getting_sword, true, null),
    SKELETON_ARISE(R.drawable.cutscene_3, 6, "seen_cutscene_skeletonArise", R.raw.cutscene_skeleton_arise, false, "RUNWAY");

    private final Bitmap[] frameArr;
    private final String checkPoint;
    private final int musicRes;
    private final boolean dialogueAfter;
    private final String onExitEvent;

    Scenes(int resId, int frames, String checkPoint, int musicRes, boolean dialogueAfter, String onExitEvent) {
        // שימוש במנהל הביטמפים - חיתוך אנכי (horizontal = false)
        this.frameArr = BitmapManager.getSpritesheet(resId, 320, 180, frames, 1.0, false, false);

        this.checkPoint = checkPoint;
        this.musicRes = musicRes;
        this.dialogueAfter = dialogueAfter;
        this.onExitEvent = onExitEvent;
    }

    public Bitmap[] getFrameArr() {
        return frameArr;
    }

    public String getCheckPoint() { return checkPoint; }
    public int getMusicRes() { return musicRes; }
    public boolean getDialogueAfter() {
        return dialogueAfter;
    }
    public String getOnExitEvent() { return onExitEvent; }
}