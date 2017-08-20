package com.warlockgaming.weatherassist.weatherassist;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;

/**
 * Created by nickr on 7/19/2017.
 */

public class WundergroundInterface {
    private Activity activity;
    private JSONObject jsonObject;
    private double rainThreshold = 40.0;
    private double hours = 12.0;

    public WundergroundInterface(Activity activity) {
        this.activity = activity;
    }

    public boolean getInfoLatLng(Double[] location) {
        double rainAmount = 0.0;
        boolean overThreshold;
        Log.d("WeatherAssist", "Calling Task");
        GetInfoFromLatLng getInfo = new GetInfoFromLatLng();

        Double[] input = new Double[] {location[0], location[1], hours};

        try {
            //getInfo.execute(input);
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

        Log.d("WeatherAssist", "Complete");
        return overThreshold;
    }

    public double readMessage(double hours)
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

    private class GetInfoFromZip extends AsyncTask<String, Void, Double> {
        @Override
        protected Double doInBackground(String... zip) {
            query(zip[0]);
            return readMessage(Double.parseDouble(zip[1]));
        }

        public void query(String zip)
        {
            StringBuilder builder = new StringBuilder();
            StringBuilder jsonBuilder = new StringBuilder();
            String json;
            builder.append("http://api.wunderground.com/api/87599b3b7dbd4302/forecast");
            builder.append("/q/");
            builder.append(zip);
            builder.append(".json");

            String url = builder.toString();

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
    }

    private class GetInfoFromLatLng extends AsyncTask<Double, Void, Double> {
        @Override
        protected Double doInBackground(Double... params) {

            query(params[0], params[1]);

            return readMessage(params[2]);
        }

        public void query(double lat, double lon) {
            StringBuilder builder = new StringBuilder();
            StringBuilder jsonBuilder = new StringBuilder();
            String json;
            builder.append("http://api.wunderground.com/api/87599b3b7dbd4302/hourly");
            builder.append("/q/");
            builder.append(Double.toString(lat));
            builder.append(",");
            builder.append(Double.toString(lon));
            builder.append(".json");

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
