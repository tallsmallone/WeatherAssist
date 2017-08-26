package com.warlockgaming.weatherassist.weatherassist;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by nickr on 7/19/2017.
 */

public class WundergroundInterface{
    private Activity activity;
    private JSONObject jsonObject;
    private double rainThreshold = 50.0;
    private double hours = 8.0;

    public WundergroundInterface(Activity activity, double rainThreshold, double hours) {
        this.activity = activity;
        this.hours = hours;
        this.rainThreshold = rainThreshold;
    }

    public boolean getInfoLatLng(Double[] location) {
        double rainAmount = 0.0;
        boolean overThreshold;
        Log.d("WeatherAssist", "Calling Task");
        GetInfo getInfo = new GetInfo();

        String[] input = new String[] {Double.toString(location[0]) + "," +
                Double.toString(location[1]), Double.toString(hours)};

        try {
            rainAmount = getInfo.execute(input).get();
        } catch (Exception e) {
            Log.d("WeatherAssist", "Error getting information, exception: " + e.toString());
        }

        if(rainAmount >= rainThreshold) {
            overThreshold = true;
            Log.d("WeatherAssist", "Above Threshold");
        }
        else if(rainAmount == -1) {
            Log.d("WeatherAssist", "Server sent -1");
            Toast.makeText(activity, "Error reading from server...", Toast.LENGTH_SHORT);
            Toast.makeText(activity, "Please try again later", Toast.LENGTH_LONG);
            overThreshold = false;
        }
        else {
            overThreshold = false;
            Log.d("WeatherAssist", "Under Threshold");
        }

        Log.d("WeatherAssist", "Threshold = " + rainThreshold);
        Log.d("WeatherAssist", "Hours = " + hours);
        Log.d("WeatherAssist", "Rain Amount = " + rainAmount);
        Log.d("WeatherAssist", "Complete");
        return overThreshold;
    }

    public boolean getInfoZip(String zip) {
        double rainAmount = 0.0;
        boolean overThreshold;
        Log.d("WeatherAssist", "Calling Task");
        GetInfo getInfo = new GetInfo();

        String[] input = new String[] {zip, Double.toString(hours)};

        try {
            rainAmount = getInfo.execute(input).get();
        } catch (Exception e) {
            Log.d("WeatherAssist", "Error getting information, exception: " + e.toString());
        }

        if(rainAmount >= rainThreshold) {
            overThreshold = true;
            Log.d("WeatherAssist", "Above Threshold");
        }
        else {
            overThreshold = false;
            Log.d("WeatherAssist", "Under Threshold");
        }

        Log.d("WeatherAssist", "Threshold = " + rainThreshold);
        Log.d("WeatherAssist", "Hours = " + hours);
        Log.d("WeatherAssist", "Rain Amount = " + rainAmount);
        Log.d("WeatherAssist", "Complete");
        return overThreshold;
    }

    private double readMessage(double hours)
    {
        String response = "";
        Double precipitation = 0.0;
        try {
            JSONArray subArrayA = jsonObject.getJSONArray("hourly_forecast");

            // check hours
            if (hours > (double)subArrayA.length()) {
                hours = (double)subArrayA.length();
            } else if(hours < 1.0) {
                hours = 1.0;
            }

            for(int i = 0; i < (int) hours; i++) {
                JSONObject subObjectB = subArrayA.getJSONObject(i);
                Double currentPop = subObjectB.getDouble("pop");

                if(precipitation < currentPop) {
                    precipitation = currentPop;
                }
            }
        }
        catch (Exception e) {
            Log.d("WeatherAssist", "Lat Long Read Exception: " + e.toString());
            precipitation = -1.0;
            e.printStackTrace();
        }
        return precipitation;
    }

    private void printJSONObjects(JSONObject object) {
        Iterator<String> iter = object.keys();
        while(iter.hasNext()) {
            String key = iter.next();
            Log.d("WeatherAssist", "Key: " + key);
        }
    }

    private class GetInfo extends AsyncTask<String[], Void, Double> {
        @Override
        protected Double doInBackground(String[]... params) {

            query(params[0][0]);

            return readMessage(Double.parseDouble(params[0][1]));
        }

        public void query(String input) {
            StringBuilder builder = new StringBuilder();
            StringBuilder jsonBuilder = new StringBuilder();
            String json;
            builder.append("https://weather-assist-177801.appspot.com/query/");
            builder.append(input);

            String url = builder.toString();
            Log.d("WeatherAssist", "URL: " + url);

            try {
                URL request_url = new URL(url);
                HttpURLConnection urlConnection = (HttpURLConnection) request_url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader in_reader = new InputStreamReader(in);

                if(in_reader != null) {
                    BufferedReader br = new BufferedReader(in_reader);
                    String line;

                    while((line = br.readLine()) != null) {
                        jsonBuilder.append(line + "\n");
                    }
                    in.close();
                    json = jsonBuilder.toString();

                    if (json.equals("No location specified") || json.equals("Too many calls") ||
                            json.equals("Error getting page")) {
                        jsonObject = null;
                    }

                    try {
                        jsonObject = new JSONObject(json);
                    } catch (JSONException e) {
                        Log.d("WeatherAssist", "Error parsing data: " + e.toString());
                    }
                }
                else {
                    jsonObject = null;
                    Log.d("WeatherAssist", "JsonReader = null");
                }


            } catch (Exception e) {
                Log.d("WeatherAssist", "Lat Long query Exception: " + e.toString());
                jsonObject = null;
            }
        }

        @Override
        protected void onPostExecute(Double value) {
        }
    }
}
