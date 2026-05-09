package com.example.tutorialgame.gamestates.cutscenes;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.example.tutorialgame.engine.interfaces.BitmapMethods;
import com.example.tutorialgame.ui.base.BaseActivity;
import com.example.tutorialgame.R;

public enum Scenes implements BitmapMethods {
    INTRO(R.drawable.cutscene_1, 5, "seen_cutscene_coldOpening", R.raw.cutscene_intro, false),
    GETTING_SWORD(R.drawable.cutscene_2, 3, "seen_cutscene_gettingSword", R.raw.cutscene_getting_sword, true),
    SKELETON_ARISE(R.drawable.cutscene_3, 6, "seen_cutscene_skeletonArise", R.raw.cutscene_skeleton_arise, false);

    private final Bitmap[] frameArr;
    private final String checkPoint;
    private final int musicRes;
    private final boolean dialogueAfter;

    Scenes(int resId, int frames, String checkPoint, int musicRes, boolean dialogueAfter) {
        // ביטול Scaling אוטומטי של אנדרואיד כדי לשמור על הרזולוציה המקורית של ה-Atlas
        options.inScaled = false;
        Bitmap atlas = BitmapFactory.decodeResource(BaseActivity.getContext().getResources(), resId, options);

        frameArr = new Bitmap[frames];
        for (int i = 0; i < frames; i++) {
            frameArr[i] = Bitmap.createBitmap(atlas, 0, i * 180, 320, 180);
        }

        // שחרור ה-Atlas מהזיכרון לאחר החיתוך
        atlas.recycle();

        this.checkPoint = checkPoint;
        this.musicRes = musicRes;
        this.dialogueAfter = dialogueAfter;
    }

    public Bitmap[] getFrameArr() {
        return frameArr;
    }

    public String getCheckPoint() { return checkPoint; }
    public int getMusicRes() { return musicRes; }
    public boolean getDialogueAfter() {
        return dialogueAfter;
    }
}