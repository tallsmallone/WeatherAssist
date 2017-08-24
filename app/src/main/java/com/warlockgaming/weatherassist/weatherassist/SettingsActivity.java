package com.warlockgaming.weatherassist.weatherassist;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

public class SettingsActivity extends AppCompatActivity {
    private SharedPreferences prefs;

    private Button returnButton;
    private EditText thresholdEdit;
    private EditText hourEdit;
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Add ads
        mAdView = (AdView)findViewById(R.id.main_ad);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        // preferences to save and read initials
        prefs = getSharedPreferences("settings", Context.MODE_PRIVATE);

        // add edits
        thresholdEdit = (EditText)findViewById(R.id.settings_edit_threshold);
        hourEdit = (EditText)findViewById(R.id.settings_edit_hours);

        double rainThreshold = (double)prefs.getFloat("rainThreshold", 50.0f);
        double hours = (double)prefs.getFloat("hours", 8.0f);
        thresholdEdit.setText(Double.toString(rainThreshold));
        hourEdit.setText(Double.toString(hours));

        // add button task
        returnButton = (Button)findViewById(R.id.settings_return);
        returnButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                returnButtonClicked();
            }
        });
    }

    public void returnButtonClicked() {
        saveValues();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void saveValues() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat("rainThreshold", Float.parseFloat(thresholdEdit.getText().toString()));
        editor.putFloat("hours", Float.parseFloat(hourEdit.getText().toString()));
        editor.commit();
    }
}
