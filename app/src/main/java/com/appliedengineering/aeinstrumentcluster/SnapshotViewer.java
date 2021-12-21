package com.appliedengineering.aeinstrumentcluster;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class SnapshotViewer extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snapshot_viewer);

        String snapshot = getIntent().getStringExtra("SNAPSHOT_INDEX");

        TextView snapshotText = findViewById(R.id.snapshot_viewer_text);
        snapshotText.setText(snapshot);
    }
}