package com.example.tutorialgame.environments;

import com.example.tutorialgame.R;

public enum SurfaceType {
    NONE(-1), // לא מוגדר / אוויר
    GRASS(R.raw.sfx_elemental_grass),
    DIRT(R.raw.sfx_elemental_dirt),
    STONE(R.raw.sfx_elemental_stone);

//    WATER(R.raw.sfx_water_splash); // (דוגמה עתידית)

    private final int sfxId;

    SurfaceType(int sfxId) {
        this.sfxId = sfxId;
    }

    public int getSfxId() {
        return sfxId;
    }
}
    