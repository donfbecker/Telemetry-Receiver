package com.donfbecker.telemetryreceiver;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import java.util.List;

public class BookmarkRepository {
    private BookmarkDao bookmarkDao;
    private LiveData<List<Bookmark>> allBookmarks;

    public BookmarkRepository(Application application) {
        BookmarkRoomDatabase db = BookmarkRoomDatabase.getDatabase(application);
        bookmarkDao = db.getBookmarkDao();
        allBookmarks = bookmarkDao.getAllBookmarks();
    }

    public LiveData<List<Bookmark>> getAllBookmarks() {
        return allBookmarks;
    }

    public void insert(Bookmark bookmark) {
        BookmarkRoomDatabase.databaseWriteExecutor.execute(() -> {
            bookmarkDao.insert(bookmark);
        });
    }

    public void delete(Bookmark bookmark) {
        BookmarkRoomDatabase.databaseWriteExecutor.execute(() -> {
            bookmarkDao.deleteById(bookmark.id);
        });
    }
}
