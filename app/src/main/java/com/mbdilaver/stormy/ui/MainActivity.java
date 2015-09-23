package com.mbdilaver.stormy.ui;

import android.app.DialogFragment;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mbdilaver.stormy.R;
import com.mbdilaver.stormy.weather.Current;
import com.mbdilaver.stormy.weather.Day;
import com.mbdilaver.stormy.weather.Forecast;
import com.mbdilaver.stormy.weather.Hour;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();
    private Forecast mForecast;

    @Bind(R.id.timeLabel) TextView mTimeLabel;
    @Bind(R.id.temperatureLabel) TextView mTemperatureLabel;
    @Bind(R.id.humidityValue) TextView mHumidityValue;
    @Bind(R.id.precipValue) TextView mPrecipValue;
    @Bind(R.id.summaryLabel) TextView mSummaryLabel;
    @Bind(R.id.iconImageView) ImageView mIconImageView;
    @Bind(R.id.refreshImageView) ImageView mRefreshImageView;
    @Bind(R.id.progressBar) ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mProgressBar.setVisibility(View.INVISIBLE);

        final double latitude = 41.0991;
        final double longitude = 28.8112;

        mRefreshImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getForecast(latitude, longitude);
            }
        });
        getForecast(latitude, longitude);

    }

    private void getForecast(double latitude, double longitude) {
        String apikey = "37e0d30dcedc5496917733bed357303e";
        String forecastUrl = "https://api.forecast.io/forecast/" + apikey + '/' + latitude + ',' + longitude;

        if( isNetworkAvailable() ) {
            toggleRefresh();

            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(forecastUrl)
                    .build();
            Call call = client.newCall(request);

            call.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toggleRefresh();
                        }
                    });
                    alertUserAboutError();
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toggleRefresh();
                        }
                    });
                    try {
                        if (response.isSuccessful()) {
                            String jsonData = response.body().string();

                            mForecast = parseForecastDetails(jsonData);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateDisplay();
                                }
                            });

                        } else {
                            alertUserAboutError();
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Exception caught: ", e);
                    }

                }

            });
        }
        else {
            Toast.makeText(this, R.string.noInternetConnectionAlert, Toast.LENGTH_LONG).show();
        }
    }

    private Forecast parseForecastDetails(String jsonData) throws JSONException {
        Forecast forecast = new Forecast();

        forecast.setCurrent(getCurrentDetails(jsonData));
        forecast.setHourlyForecast(getHourlyForecast(jsonData));
        forecast.setDailyForecast(getDailyForecast(jsonData));


        return forecast;
    }

    private Day[] getDailyForecast(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");

        JSONObject daily = forecast.getJSONObject("daily");
        JSONArray data = daily.getJSONArray("data");

        Day[] days = new Day[data.length()];

        for (int i = 0; i < data.length(); i++) {
            JSONObject jsonDay = data.getJSONObject(i);
            Day day = new Day();

            day.setIcon(
                    jsonDay.getString("icon"));
            day.setTime(
                    jsonDay.getLong("time"));
            day.setSummary(
                    jsonDay.getString("summary"));
            day.setTemperatureMax(
                    jsonDay.getDouble("temperatureMax"));
            day.setTimezone(
                    timezone);

            days[i] = day;
        }
        return days;
    }

    private Hour[] getHourlyForecast(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        JSONObject hourly = forecast.getJSONObject("hourly");
        JSONArray data = hourly.getJSONArray("data");

        Hour[] hours = new Hour[data.length()];

        for (int i = 0; i < data.length(); i++) {
            JSONObject jsonHour = data.getJSONObject(i);
            Hour hour = new Hour();

            hour.setIcon(
                    jsonHour.getString("icon"));
            hour.setSummary(
                    jsonHour.getString("summary"));
            hour.setTemperature(
                    jsonHour.getDouble("temperature"));
            hour.setTime(
                    jsonHour.getLong("time"));
            hour.setTimezone(
                    timezone);

            hours[i] = hour;
        }
        return hours;
    }

    private void toggleRefresh() {
        if (mProgressBar.getVisibility() == View.INVISIBLE) {
            mProgressBar.setVisibility(View.VISIBLE);
            mRefreshImageView.setVisibility(View.INVISIBLE);
        }
        else {
            mProgressBar.setVisibility(View.INVISIBLE);
            mRefreshImageView.setVisibility(View.VISIBLE);
        }

    }

    private void updateDisplay() {
        Current current = mForecast.getCurrent();

        mTemperatureLabel.setText(
                current.getTemperature() + ""
        );
        mTimeLabel.setText(
                "At " + current.getFormattedTime() + " it will be"
        );
        mHumidityValue.setText(
                current.getHumidity() + ""
        );
        mPrecipValue.setText(
                current.getPrecipChance() + "%"
        );
        mSummaryLabel.setText(
                current.getSummary()
        );

        Drawable drawable = getResources().getDrawable(current.getIconId());
        mIconImageView.setImageDrawable(drawable);

    }

    private Current getCurrentDetails(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject( jsonData );

        JSONObject currently = forecast.getJSONObject("currently");

        Current current = new Current();
        current.setTemperature(
                currently.getDouble("temperature")
        );
        current.setHumidity(
                currently.getDouble("humidity")
        );
        current.setIcon(
                currently.getString("icon")
        );
        current.setPrecipChance(
                currently.getDouble("precipProbability")
        );
        current.setTime(
                currently.getLong("time")
        );
        current.setSummary(
                currently.getString("summary")
        );
        current.setTimeZone(
                forecast.getString("timezone")
        );

        Log.v(TAG, " Time: " + current.getFormattedTime());

        return current;
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
