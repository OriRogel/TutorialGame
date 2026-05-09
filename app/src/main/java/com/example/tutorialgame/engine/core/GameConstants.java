package com.example.tutorialgame.engine.core;


import static com.example.tutorialgame.engine.core.GameConstants.Sprite.SCALE_MULTIPLIER;
import static com.example.tutorialgame.engine.core.GameConstants.Sprite.TILE_SIZE;
import static com.example.tutorialgame.engine.core.GameConstants.View.SCREEN_WIDTH;

public final class GameConstants {
    public static final class Face_Dir {
        public static final int DOWN = 0;
        public static final int UP = 1;
        public static final int LEFT = 2;
        public static final int RIGHT = 3;
    }

    public static final class Sprite {
        public static final int DEFAULT_SIZE = 16;

        public static int SCALE_MULTIPLIER = SCREEN_WIDTH / 320;

        public static final int TILE_SIZE = DEFAULT_SIZE * SCALE_MULTIPLIER;
        public static final int HITBOX_SIZE = 12 * SCALE_MULTIPLIER;
        public static final float HITBOX_OFFSET = 0.75f * SCALE_MULTIPLIER;
        public static final int X_DRAW_OFFSET = 2 * SCALE_MULTIPLIER;
        public static final int Y_DRAW_OFFSET = 4 * SCALE_MULTIPLIER;

    }

    public static final class Animation {
        public static final int SPEED = 10;
        public static final int AMOUNT = 4;
        public static final int DEATH_FRAME_INDEX = 6;
    }



    public static final class View {
        // 1. הסר את ה-final ואת האתחול המיידי.
        public static int SCREEN_WIDTH;
        public static int SCREEN_HEIGHT;
        public static float VIEW_MARGIN;

        /**
         * מתודת אתחול שתקרא מ-SplashActivity אחרי שהמידות ידועות.
         */
        public static void initScreenDimensions(int width, int height) {
            if (SCREEN_WIDTH == 0 && width > 0) {
                SCREEN_WIDTH = width;
                VIEW_MARGIN = 3*TILE_SIZE;
            }
            if (SCREEN_HEIGHT == 0 && height > 0) {
                SCREEN_HEIGHT = height;
            }
            System.out.println("width:" + SCREEN_WIDTH + " height:" + SCREEN_HEIGHT);
            System.out.println("scale: " + SCALE_MULTIPLIER);
        }
    }

    public static final class Faction {
        public static final int PLAYER = 0;
        public static final int ALLY = 1;
        public static final int ENEMY = 2;
        public static final int NEUTRAL = 3;


    }
}
