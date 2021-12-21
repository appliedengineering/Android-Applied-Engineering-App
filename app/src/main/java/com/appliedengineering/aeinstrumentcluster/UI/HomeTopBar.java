package com.appliedengineering.aeinstrumentcluster.UI;
//import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

import com.appliedengineering.aeinstrumentcluster.R;
import com.appliedengineering.aeinstrumentcluster.SettingsActivity;

public class HomeTopBar extends Fragment implements View.OnClickListener {
    public HomeTopBar() {
        super(R.layout.home_top_bar_layout);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // inflate the layout for this fragment
        View view = super.onCreateView(inflater, container, savedInstanceState);
        ImageView settingsButton = view.findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View view) {
        startActivity(new Intent(getContext(), SettingsActivity.class));
    }


}
