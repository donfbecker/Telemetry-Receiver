package com.donfbecker.telemetryreceiver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

public class BookmarksActivity extends AppCompatActivity {
    private BookmarkViewModel bookmarkViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmarks);

        RecyclerView bookmarksRecycleView = findViewById(R.id.list_bookmarks);
        final BookmarkListAdapter adapter = new BookmarkListAdapter(new BookmarkListAdapter.BookmarkDiff(), new OnItemClickListener() {
            @Override
            public void onItemClick(Bookmark bookmark) {
                Intent reply = new Intent();
                reply.putExtra("FREQUENCY", bookmark.frequency);
                setResult(RESULT_OK, reply);
                finish();
            }
        });
        bookmarksRecycleView.setAdapter(adapter);
        bookmarksRecycleView.setLayoutManager(new LinearLayoutManager(this));
        bookmarksRecycleView.setNestedScrollingEnabled(false);

        bookmarkViewModel = new ViewModelProvider(this).get(BookmarkViewModel.class);
        bookmarkViewModel.getAllBookmarks().observe(this, bookmarks -> {
            adapter.submitList(bookmarks);
        });

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // this method is called when we swipe our item to right direction.
                // on below line we are getting the item at a particular position.
                int position = viewHolder.getAdapterPosition();
                Bookmark bookmark = adapter.getItemByPosition(position);
                bookmarkViewModel.delete(bookmark);
                adapter.notifyItemRemoved(position);
                adapter.notifyItemRangeChanged(position, adapter.getItemCount());

                // below line is to display our snackbar with action.
                Snackbar.make(bookmarksRecycleView, "Deleted " + bookmark.getName(), Snackbar.LENGTH_LONG).setAction("Undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // adding on click listener to our action of snack bar.
                        // below line is to add our item to array list with a position.
                        bookmarkViewModel.insert(bookmark);

                        // below line is to notify item is
                        // added to our adapter class.
                        adapter.notifyItemInserted(position);
                    }
                }).show();
            }
            // at last we are adding this
            // to our recycler view.
        }).attachToRecyclerView(bookmarksRecycleView);
    }
}