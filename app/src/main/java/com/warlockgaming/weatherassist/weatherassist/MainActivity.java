package com.warlockgaming.weatherassist.weatherassist;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback{

    private AdView mAdView;
    private TextView locationTextView;
    private TextView rainTextView;
    private ImageView imageView;
    private SupportMapFragment mapView;
    private Button settingsButton;

    private WundergroundInterface wInterface;

    private boolean permission_granted_location_course;
    private final int MY_PERMISSION_ACCESS_COURSE_LOCATION = 1;

    private double lat;
    private double lon;
    private float map_zoom = 14.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        double rainThreshold = 50.0;
        double hours = 8.0;

        Log.d("WeatherAssist", "Started");

        // Add ads
        MobileAds.initialize(this, getString(R.string.admob_app_id));

        mAdView = (AdView)findViewById(R.id.main_ad);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        // preferences
        SharedPreferences prefs = getSharedPreferences("settings", Context.MODE_PRIVATE);

        rainThreshold = (double)prefs.getFloat("rainThreshold", 50.0f);
        hours = (double)prefs.getFloat("hours", 8.0f);

        // set image view
        imageView = (ImageView)findViewById(R.id.weather_image);

        // add other text view
        rainTextView = (TextView)findViewById(R.id.rain_text);

        // add button
        settingsButton = (Button)findViewById(R.id.settings_button);

        // set location stuff
        locationTextView = (TextView)findViewById(R.id.location_title);
        double[] location = new double[2];
        location = getGPS();
        lat = location[0];
        lon = location[1];

        String locationText = getString(R.string.location_title);
        locationText = locationText + ": " + Double.toString(lat) + ", " +
                Double.toString(lon);

        locationTextView.setText(locationText);

        // Render map
        mapView = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.location_map);
        Log.d("WeatherAssist", "Loading Map");
        mapView.getMapAsync(this);

        // Wunderground interface
        wInterface = new WundergroundInterface(this, rainThreshold, hours);
        boolean overThreshold = wInterface.getInfoLatLng(new Double[] {lat, lon} );

        if(!overThreshold) {
            rainTextView.setText("It will not rain");
            imageView .setImageResource(R.drawable.sun_small);
        }

        // add button task
        settingsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                settingsButtonClicked();
            }
        });
    }

    public void settingsButtonClicked() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
        finish();
    }

    // onMapReady
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("WeatherAssist", "Map Ready");
        // add marker and move map
        LatLng currentLocation = new LatLng(lat, lon);
        googleMap.addMarker(new MarkerOptions().position(currentLocation));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, map_zoom));
    }

    // get GPS location
    private double[] getGPS() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        double[] gps = new double[2];
        List<String> providers = lm.getProviders(true);

        Location l = null;
        gps[0] = 0.0;
        gps[1] = 0.0;

        // don't check everytime
        if (!permission_granted_location_course) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[] { Manifest.permission.ACCESS_COARSE_LOCATION },
                        MY_PERMISSION_ACCESS_COURSE_LOCATION );
            } else {
                permission_granted_location_course = true;
            }
        }

        if (permission_granted_location_course) {
            Log.d("WeatherAssist", "Access granted");
            for(int i = 0; i < providers.size(); i++) {
                l = lm.getLastKnownLocation(providers.get(i));
                if (l != null) break;
            }

            if(l != null) {
                gps[0] = l.getLatitude();
                gps[1] = l.getLongitude();
            }

            Log.d("WeatherAssist", "Lat: " + Double.toString(gps[0]));
            Log.d("WeatherAssist", "Lon: " + Double.toString(gps[1]));
        }

        return gps;
    }

    // handle permission requests
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                          int[] grantResults) {
        switch(requestCode) {
            case MY_PERMISSION_ACCESS_COURSE_LOCATION: {
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    permission_granted_location_course = true;
                }
                else {
                    permission_granted_location_course = false;
                }
            }
        }

        recreate();

        return;
    }
}
