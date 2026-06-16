package com.example.tutorialgame.engine.ui.effects;

import static com.example.tutorialgame.engine.core.GameConstants.Sprite.SCALE_MULTIPLIER;
import static com.example.tutorialgame.engine.core.GameConstants.Sprite.TILE_SIZE;
import static com.example.tutorialgame.engine.core.GameConstants.View.SCREEN_WIDTH;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import com.example.tutorialgame.MyApp;
import com.example.tutorialgame.R;
import com.example.tutorialgame.engine.audio.MusicManager;
import com.example.tutorialgame.engine.audio.SoundManager;
import com.example.tutorialgame.engine.interfaces.BitmapMethods;
import com.example.tutorialgame.engine.renderer.TextRenderer;
import com.example.tutorialgame.ui.base.BaseActivity;

public class LevelUpEffect implements BitmapMethods {
    private int currentLevel = MyApp.getProgress().getLevel();
    private final Bitmap circle;
    private final float x, y;
    private float offset;
    private long lastUpdate;
    private final TextRenderer levelPaint;
    private boolean played, start;

    public LevelUpEffect() {
        options.inScaled = false;
        circle = getMultiplyBitmapClean(BitmapFactory.decodeResource(BaseActivity.getContext().getResources(), R.drawable.xp_img, options), 1.6);
        x = (SCREEN_WIDTH - circle.getWidth())/2f;
        y = 4*TILE_SIZE;
        offset = -y/3;

        levelPaint = new TextRenderer(circle.getWidth()/2f, R.color.floral_white);
    }

    public void update(double delta) {
        if (currentLevel != MyApp.getProgress().getLevel()) setPositions();
        slideAnimation(delta);
    }

    private void setPositions() {
        currentLevel = MyApp.getProgress().getLevel();
        lastUpdate = System.currentTimeMillis();
        float setX = x + (circle.getWidth() - levelPaint.measureText(String.valueOf(currentLevel)))/2f - 1.5f*SCALE_MULTIPLIER;
        levelPaint.setPosition(setX, y+levelPaint.getTextSize()*1.34f);
        start = true;
        offset = -y/2f;
        levelPaint.setAlpha(0);
    }

    private void slideAnimation(double delta) {
        if (!start) return;
        float speed = (float) (3.123*TILE_SIZE*delta);

        if (System.currentTimeMillis() - lastUpdate <= 3000) {
            if(offset < 0) {
                offset += speed;
                levelPaint.setAlpha((int) (255+offset));
            }
            else {
                offset = 0;
                levelPaint.setAlpha(255);
                if (!played) {
                    SoundManager.getInstance(BaseActivity.getContext()).setVolume(SoundManager.getInstance(BaseActivity.getContext()).getVolume()*0.3f);
                    MusicManager.getInstance(BaseActivity.getContext()).play(R.raw.jingle_level_up);
                    MusicManager.getInstance(BaseActivity.getContext()).setLooping(false);
                    played = true;
                }
            }
        } else {
            if (offset > -2*y) offset -= 2*speed;
            else {
                offset = -2*y;
                played = false;
                start = false;
                SoundManager.getInstance(BaseActivity.getContext()).setVolume(SoundManager.getInstance(BaseActivity.getContext()).getVolume()/0.3f);
                MusicManager.getInstance(BaseActivity.getContext()).play(MusicManager.getInstance(BaseActivity.getContext()).getLastResId());
                MusicManager.getInstance(BaseActivity.getContext()).setLooping(true);
            }
        }
    }

    public void draw(Canvas c) {
        if (!start) return;
        c.save();
        c.translate(0, offset);

        c.drawBitmap(circle, x, y, levelPaint);
        levelPaint.drawText(String.valueOf(currentLevel), c);

        c.restore();
    }
}
