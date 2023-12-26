package com.donfbecker.telemetryreceiver;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "bookmark")
public class Bookmark {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "frequency")
    public int frequency;

    public Bookmark(String name, int frequency) {
        this.name = name;
        this.frequency = frequency;
    }

    public String getName() {
        return this.name;
    }

    public int getFrequency() {
        return this.frequency;
    }
}
