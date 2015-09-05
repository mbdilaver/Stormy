package com.mbdilaver.stormy;

import android.app.DialogFragment;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();
    private CurrentWeather mCurrentWeather;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String apikey = "37e0d30dcedc5496917733bed357303e";
        double latitude = 41.0991;
        double longitude = 28.8112;
        String forecastUrl = "https://api.forecast.io/forecast/" + apikey + '/' + latitude + ',' + longitude;

        if( isNetworkAvailable() ) {
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(forecastUrl)
                    .build();
            Call call = client.newCall(request);

            call.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {

                }

                @Override
                public void onResponse(Response response) throws IOException {
                    String jsonData = response.body().string();
                    Log.v(TAG, jsonData);
                    try {
                        if (response.isSuccessful()) {
                            mCurrentWeather = getCurrentDetails(jsonData);
                        }
                        else {
                            alertUserAboutError();
                        }
                    }
                    catch(JSONException e){
                        Log.e(TAG, "Exception caught: ", e);
                    }

                }

            });
        }
        else {
            Toast.makeText( this, R.string.noInternetConnectionAlert, Toast.LENGTH_LONG ).show();
        }
    }

    private CurrentWeather getCurrentDetails(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject( jsonData );
        CurrentWeather currentWeather = new CurrentWeather();

        JSONObject currently = forecast.getJSONObject("currently");

        currentWeather.setTemperature(
                currently.getDouble("temperature")
        );
        currentWeather.setHumidity(
                currently.getDouble("humidity")
        );
        currentWeather.setIcon(
                currently.getString("icon")
        );
        currentWeather.setPrecipChance(
                currently.getDouble("precipProbability")
        );
        currentWeather.setTime(
                currently.getLong("time")
        );
        currentWeather.setTimeZone(
                forecast.getString("timezone")
        );
        Log.v(TAG, " Time: " + currentWeather.getFormattedTime());

        return currentWeather;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }
        return isAvailable;
    }

    private void alertUserAboutError() {
        DialogFragment alertFragment = new AlertDialogFragment();
        alertFragment.show( getFragmentManager(), "NoInternet" );
    }


}
