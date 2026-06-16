package com.example.tutorialgame.managers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.example.tutorialgame.engine.interfaces.BitmapMethods;
import com.example.tutorialgame.ui.base.BaseActivity;

import java.util.HashMap;
import java.util.Map;

/**
 * מנהל מרכזי לטעינה, שינוי גודל (Scaling) ומטמון (Caching) של Bitmaps.
 * תומך בטעינת אובייקטים בודדים, מערכי אנימציה וחיתוך אזורים מתוך אטלס (Atlas).
 */
public class BitmapManager implements BitmapMethods {
    private static final String TAG = "BitmapManager";

    private static final BitmapMethods helper = new BitmapMethods() {};
    
    private static final Map<String, Bitmap> bitmapCache = new HashMap<>();
    private static final Map<String, Bitmap[]> sheetCache = new HashMap<>();
    private static final Map<String, Bitmap[][]> sheet2DCache = new HashMap<>();
    
    // מטמון זמני לקבצי המקור (Atlases) כדי למנוע decode חוזר במהלך הטעינה
    private static final Map<Integer, Bitmap> rawAtlasCache = new HashMap<>();

    static {
        options.inScaled = false;
    }

    private static String getCacheKey(int resId, double multiply, boolean smooth) {
        return resId + "_" + multiply + "_" + (smooth ? "s" : "c");
    }

    private static Bitmap getRawAtlas(int resId) {
        if (rawAtlasCache.containsKey(resId)) return rawAtlasCache.get(resId);
        Bitmap raw = BitmapFactory.decodeResource(BaseActivity.getContext().getResources(), resId, options);
        if (raw != null) rawAtlasCache.put(resId, raw);
        return raw;
    }

    public static Bitmap getBitmap(int resId) {
        return getBitmap(resId, 1.0, false);
    }


    /**
     * מחזיר Bitmap מותאם.
     */
    public static Bitmap getBitmap(int resId, double multiply, boolean smooth) {
        String key = getCacheKey(resId, multiply, smooth);
        if (bitmapCache.containsKey(key)) return bitmapCache.get(key);

        Bitmap raw = getRawAtlas(resId);
        if (raw == null) return null;

        Bitmap processed = smooth ? helper.getMultiplyBitmapSmoth(raw, multiply) : helper.getMultiplyBitmapClean(raw, multiply);
        bitmapCache.put(key, processed);
        return processed;
    }

    /**
     * חותך אזור ספציפי מתוך אטלס, מבצע Scaling ושומר במטמון.
     */
    public static Bitmap getBitmapRegion(int resId, int x, int y, int w, int h, double multiply, boolean smooth) {
        String key = getCacheKey(resId, multiply, smooth) + "_r_" + x + "_" + y + "_" + w + "_" + h;
        if (bitmapCache.containsKey(key)) return bitmapCache.get(key);

        Bitmap atlas = getRawAtlas(resId);
        if (atlas == null) return null;

        Bitmap rawRegion = Bitmap.createBitmap(atlas, x, y, w, h);
        Bitmap processed = smooth ? helper.getMultiplyBitmapSmoth(rawRegion, multiply) : helper.getMultiplyBitmapClean(rawRegion, multiply);
        
        bitmapCache.put(key, processed);
        return processed;
    }

    /**
     * טוען גיליון אנימציה (שורה אחת - הוריזונטלי כברירת מחדל).
     */
    public static Bitmap[] getSpritesheet(int resId, int frameW, int frameH, int count, double multiply, boolean smooth) {
        return getSpritesheet(resId, frameW, frameH, count, multiply, smooth, true);
    }

    /**
     * טוען גיליון אנימציה עם בחירת כיוון (הוריזונטלי או ורטיקלי).
     */
    public static Bitmap[] getSpritesheet(int resId, int frameW, int frameH, int count, double multiply, boolean smooth, boolean horizontal) {
        String key = getCacheKey(resId, multiply, smooth) + "_s_" + count + "_" + (horizontal ? "h" : "v");
        if (sheetCache.containsKey(key)) return sheetCache.get(key);

        Bitmap sheet = getRawAtlas(resId);
        if (sheet == null) return null;

        Bitmap[] frames = new Bitmap[count];
        for (int i = 0; i < count; i++) {
            int x = horizontal ? i * frameW : 0;
            int y = horizontal ? 0 : i * frameH;
            Bitmap rawFrame = Bitmap.createBitmap(sheet, x, y, frameW, frameH);
            frames[i] = smooth ? helper.getMultiplyBitmapSmoth(rawFrame, multiply) : helper.getMultiplyBitmapClean(rawFrame, multiply);
        }

        sheetCache.put(key, frames);
        return frames;
    }

    /**
     * טוען גיליון אנימציה דו-מימדי (מטריצה).
     */
    public static Bitmap[][] getSpritesheet2D(int resId, int frameW, int frameH, int rows, int cols, double multiply, boolean smooth) {
        String key = getCacheKey(resId, multiply, smooth) + "_m_" + rows + "x" + cols;
        if (sheet2DCache.containsKey(key)) return sheet2DCache.get(key);

        Bitmap sheet = getRawAtlas(resId);
        if (sheet == null) return null;

        Bitmap[][] frames = new Bitmap[rows][cols];
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Bitmap rawFrame = Bitmap.createBitmap(sheet, col * frameW, row * frameH, frameW, frameH);
                frames[row][col] = smooth ? helper.getMultiplyBitmapSmoth(rawFrame, multiply) : helper.getMultiplyBitmapClean(rawFrame, multiply);
            }
        }

        sheet2DCache.put(key, frames);
        return frames;
    }

    public static void clearCache() {
        bitmapCache.clear();
        sheetCache.clear();
        sheet2DCache.clear();
        rawAtlasCache.clear();
    }
}
