package com.donfbecker.telemetryreceiver;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class BookmarkViewHolder extends RecyclerView.ViewHolder {
    private final TextView bookmarkName;
    private final TextView bookmarkFrequency;
    private final OnItemClickListener listener;

    private BookmarkViewHolder(View itemView, OnItemClickListener listener) {
        super(itemView);
        bookmarkName = itemView.findViewById(R.id.name);
        bookmarkFrequency = itemView.findViewById(R.id.frequency);
        this.listener = listener;
    }

    public void bind(Bookmark bookmark) {
        bookmarkName.setText(bookmark.name);
        bookmarkFrequency.setText(Integer.toString(bookmark.frequency));

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                listener.onItemClick(bookmark);
            }
        });
    }

    static BookmarkViewHolder create(ViewGroup parent, OnItemClickListener listener) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bookmark_item, parent, false);
        return new BookmarkViewHolder(view, listener);
    }
}
