package com.movieapp.data.local;

import android.content.Context;
import androidx.room.*;

/** Room 数据库单例 */
@Database(entities = {MovieEntity.class}, version = 2, exportSchema = false)
public abstract class MovieDatabase extends RoomDatabase {
    public abstract MovieDao movieDao();
    private static volatile MovieDatabase INSTANCE;

    public static MovieDatabase getInstance(Context ctx) {
        if (INSTANCE == null) {
            synchronized (MovieDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(ctx.getApplicationContext(),
                            MovieDatabase.class, "movie_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
