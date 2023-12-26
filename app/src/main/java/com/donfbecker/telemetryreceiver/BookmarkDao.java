package com.donfbecker.telemetryreceiver;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface BookmarkDao {
    @Query("SELECT * FROM bookmark ORDER BY name ASC")
    LiveData<List<Bookmark>> getAllBookmarks();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Bookmark bookmark);

    @Query("DELETE FROM bookmark WHERE id = :id")
    void deleteById(int id);

    @Query("DELETE FROM bookmark")
    void deleteAll();
}
