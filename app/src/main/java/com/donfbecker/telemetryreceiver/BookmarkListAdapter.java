package com.donfbecker.telemetryreceiver;

import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

public class BookmarkListAdapter extends ListAdapter<Bookmark, BookmarkViewHolder> {
    private final OnItemClickListener listener;

    public BookmarkListAdapter(@NonNull DiffUtil.ItemCallback<Bookmark> diffCallback, OnItemClickListener listener) {
        super(diffCallback);
        this.listener = listener;
    }

    public Bookmark getItemByPosition(int position) {
        return getItem(position);
    }

    @Override
    public BookmarkViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return BookmarkViewHolder.create(parent, listener);
    }

    @Override
    public void onBindViewHolder(BookmarkViewHolder holder, int position) {
        Bookmark current = getItem(position);
        holder.bind(current);
    }

    static class BookmarkDiff extends DiffUtil.ItemCallback<Bookmark> {
        @Override
        public boolean areItemsTheSame(@NonNull Bookmark oldItem, @NonNull Bookmark newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Bookmark oldItem, @NonNull Bookmark newItem) {
            return oldItem.getName().equals(newItem.getName()) && (oldItem.getFrequency() == newItem.getFrequency());
        }
    }
}
