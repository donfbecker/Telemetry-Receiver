package com.donfbecker.telemetryreceiver;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class NewBookmarkActivity extends AppCompatActivity {
    private EditText nameText;
    private EditText frequencyText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_bookmark);

        nameText = findViewById(R.id.edit_name);
        frequencyText = findViewById(R.id.edit_frequency);
        int f = getIntent().getIntExtra("FREQUENCY", 150000000);
        frequencyText.setText(Integer.toString(f));

        final Button button = findViewById(R.id.button_save);
        button.setOnClickListener(view -> {
            if(!TextUtils.isEmpty(nameText.getText()) && !TextUtils.isEmpty(frequencyText.getText())) {
                String name = nameText.getText().toString();
                int frequency = Integer.parseInt(frequencyText.getText().toString());

                BookmarkRepository repository = new BookmarkRepository(getApplication());
                repository.insert(new Bookmark(name, frequency));

                setResult(RESULT_OK);
                finish();
            }
        });
    }
}