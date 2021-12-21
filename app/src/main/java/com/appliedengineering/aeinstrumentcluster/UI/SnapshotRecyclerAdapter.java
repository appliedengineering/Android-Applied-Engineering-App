package com.appliedengineering.aeinstrumentcluster.UI;

import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.appliedengineering.aeinstrumentcluster.Backend.DataManager;
import com.appliedengineering.aeinstrumentcluster.R;
import com.appliedengineering.aeinstrumentcluster.SnapshotViewer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class SnapshotRecyclerAdapter extends RecyclerView.Adapter<SnapshotRecyclerAdapter.SnapshotViewHolder> {

    List<String> snapshots =  new ArrayList<>();

    public void setData(Set<String> stringSet){
        snapshots.clear();
        if(stringSet == null){
            return;
        }
        snapshots.addAll(stringSet);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SnapshotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SnapshotViewHolder(
                LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.snapshot_view, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SnapshotViewHolder holder, int position) {
        holder.setDetails(snapshots.get(position));
    }

    @Override
    public int getItemCount() {
        return snapshots.size();
    }

    public static class SnapshotViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private final TextView title;
        private final TextView charCount;
        private final TextView hash;
        private final TextView date;

        private String snapshot;
        public SnapshotViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.snapshot_title);
            charCount = itemView.findViewById(R.id.snapshot_char_count);
            hash = itemView.findViewById(R.id.snapshot_hash);
            date = itemView.findViewById(R.id.snapshot_date);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        public void setDetails(String snapshot) {
            this.snapshot = snapshot;

            String timeStampString = snapshot.split(DataManager.SERIALIZATION_DELIMITER)[0];
            long timeStamp = Long.parseLong(timeStampString);
            // get the date and time from the timestamp
            Date d = new Date();
            d.setTime(timeStamp);
            date.setText(d.toString());


            title.setText(snapshot.split(DataManager.SERIALIZATION_DELIMITER)[1].substring(0, 50));
            charCount.setText(snapshot.length() + " chars");
            hash.setText(getHash(snapshot));
        }

        @Override
        public void onClick(View view) {
            if(snapshot != null) {
                if(!HomeActivity.isSnapshotLoadable){
                    Toast.makeText(view.getContext(), "Disable debug data and network before loading snapshot!", Toast.LENGTH_LONG).show();
                    return;
                }
                DataManager.dataManager.loadDataFromString(snapshot);
                HomeActivity.snapshotLoadedIndicator.setText("yes");
            }
        }

        @Override
        public boolean onLongClick(View view) {
            Vibrator vibrator = (Vibrator) view.getContext().getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(100);
            Intent snapshotViewerIntent = new Intent(view.getContext(), SnapshotViewer.class);
            snapshotViewerIntent.putExtra("SNAPSHOT_INDEX", getPrettyPrint(snapshot.split(DataManager.SERIALIZATION_DELIMITER)[1]));
            view.getContext().startActivity(snapshotViewerIntent);
            return true;
        }

        public String getHash(String string) {
            MessageDigest md = null;
            try {
                md = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e) {
                return "failed to generate hash";
            }
            md.update(string.getBytes());
            byte[] hash = md.digest();
            StringBuilder hexString = new StringBuilder();
            for (int i = 0; i < hash.length; i++) {
                if ((0xff & hash[i]) < 0x10) {
                    hexString.append("0"
                            + Integer.toHexString((0xFF & hash[i])));
                } else {
                    hexString.append(Integer.toHexString(0xFF & hash[i]));
                }
            }
            return hexString.toString();
        }

        public String getPrettyPrint(String string) {
            JsonParser parser = new JsonParser();
            JsonObject json = parser.parse(string).getAsJsonObject();

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String prettyJson = gson.toJson(json);

            return prettyJson;
        }
    }
}
