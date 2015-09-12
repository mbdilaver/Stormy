package com.mbdilaver.stormy;

import android.app.DialogFragment;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();
    private CurrentWeather mCurrentWeather;

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
                            mCurrentWeather = getCurrentDetails(jsonData);
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
        mTemperatureLabel.setText(
                mCurrentWeather.getTemperature() + ""
        );
        mTimeLabel.setText(
                "At " + mCurrentWeather.getFormattedTime() + " it will be"
        );
        mHumidityValue.setText(
                mCurrentWeather.getHumidity() + ""
        );
        mPrecipValue.setText(
                mCurrentWeather.getPrecipChance() + "%"
        );
        mSummaryLabel.setText(
                mCurrentWeather.getSummary()
        );

        Drawable drawable = getResources().getDrawable(mCurrentWeather.getIconId());
        mIconImageView.setImageDrawable(drawable);

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
        currentWeather.setSummary(
                currently.getString("summary")
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
