package com.example.tutorialgame.di;

import android.content.Context;

import com.example.tutorialgame.engine.audio.MusicManager;
import com.example.tutorialgame.engine.audio.SoundManager;

import java.util.concurrent.ThreadLocalRandom;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class AppModule {

    @Provides
    @Singleton
    public static SoundManager provideSoundManager(@ApplicationContext Context context) {
        return SoundManager.getInstance(context);
    }

    @Provides
    @Singleton
    public static MusicManager provideMusicManager(@ApplicationContext Context context) {
        return MusicManager.getInstance(context);
    }

    @Provides
    public static ThreadLocalRandom provideThreadLocalRandom() {
        return ThreadLocalRandom.current();
    }
}
