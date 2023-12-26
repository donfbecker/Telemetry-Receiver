package com.donfbecker.telemetryreceiver;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Database(entities = {Bookmark.class}, version = 1, exportSchema = false)
abstract class BookmarkRoomDatabase extends RoomDatabase {
    abstract BookmarkDao getBookmarkDao();

    private static volatile BookmarkRoomDatabase INSTANCE;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(4);

    public static BookmarkRoomDatabase getDatabase(final Context context) {
        if(INSTANCE == null) {
            synchronized (BookmarkRoomDatabase.class) {
                if(INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), BookmarkRoomDatabase.class, "bookmarks").build();
                }
            }
        }

        return INSTANCE;
    }
}
