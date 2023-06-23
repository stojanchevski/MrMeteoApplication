package mk.finki.ukim.mrmeteoapplication;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.os.Bundle;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.util.Random;

import cz.msebera.android.httpclient.Header;

public class MrMeteoController extends AppCompatActivity {

    final int REQUEST_CODE = 123;

    final String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather";
    // App ID to use OpenWeather data
    final String APP_ID = "f8eeb01887601572975c78c729158155";
    // Time between location updates (5000 milliseconds or 5 seconds)
    final long MIN_TIME = 5000;
    // Distance between location updates (1000m or 1km)
    final float MIN_DISTANCE = 1000;
    int[] musicResources = {R.raw.summer, R.raw.winter, R.raw.autumn};

    String LOCATION_PROVIDER = LocationManager.GPS_PROVIDER;


    // Member Variables:
    TextView mCityLabel, conditionLabel;
    ImageView mWeatherImage;
    TextView mTemperatureLabel;

    Button readMoreButton;

    LocationManager locationManager;
    LocationListener locationListener;
    MediaPlayer mediaPlayer;
    private boolean isMusicPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mr_meteo_controller);
        readMoreButton = (Button) findViewById(R.id.readMoreButton);

        // Linking the elements in the layout to Java code
        mCityLabel = (TextView) findViewById(R.id.locationTV);

        mWeatherImage = (ImageView) findViewById(R.id.weatherSymbolIV);
        conditionLabel = (TextView) findViewById(R.id.condition);
        mTemperatureLabel = (TextView) findViewById(R.id.tempTV);
        ImageButton changeCityButton = (ImageButton) findViewById(R.id.changeCityButton);

        Button playButton;


        playButton = findViewById(R.id.playButton);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playRandomMusic();
            }
        });

        Button quoteButton = findViewById(R.id.quoteButton);
        quoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] quotes = {
                        "Use your day",
                        "Get your maximum out !",
                        "Grab every day like it is last !",
                        "You are the best!",
                        "Get your perfect form!"
                };

                // Generate a random index
                int randomIndex = new Random().nextInt(quotes.length);

                // Get the random quote
                String randomQuote = quotes[randomIndex];

                // Display the quote using a Toast message for 5 seconds
                Toast.makeText(MrMeteoController.this, randomQuote, Toast.LENGTH_LONG).show();
            }
        });

        readMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String city = mCityLabel.getText().toString();
                openWikipediaPage(city);
            }
            private void openWikipediaPage(String city) {
                String wikipediaUrl = "https://en.wikipedia.org/wiki/" + city;
                Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(wikipediaUrl));
                startActivity(webIntent);
            }
        });

        changeCityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent goToCity = new Intent(MrMeteoController.this, CityActivity.class);
                startActivity(goToCity);
            }
        });


    }
    private void playRandomMusic() {
        if (isMusicPlaying) {
            // If music is already playing, pause it
            mediaPlayer.pause();
            isMusicPlaying = false;
        } else {
            // If music is not playing, start it
            int randomIndex = new Random().nextInt(musicResources.length);
            int musicResource = musicResources[randomIndex];

            if (mediaPlayer != null) {
                mediaPlayer.release();
            }

            mediaPlayer = MediaPlayer.create(this, musicResource);
            mediaPlayer.start();

            isMusicPlaying = true;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d("c", "onResume: Getting Location of user");

        Intent myIntent = getIntent();
        String city = myIntent.getStringExtra("city");

        if (city != null) {
            getCityWeather(city);
        } else {
            getWeatherForCurrentLocation();
        }
    }

    private void getCityWeather(String city) {
        RequestParams requestParams = new RequestParams();
        requestParams.put("q", city);
        requestParams.put("appid", APP_ID);
        networking(requestParams);
    }

    private void getWeatherForCurrentLocation() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d("MrMeteo", "onLocationChanged");
                String longitude = String.valueOf(location.getLatitude());
                String latiude = String.valueOf(location.getLongitude());

                RequestParams requestParams = new RequestParams();
                requestParams.put("lat", latiude);
                requestParams.put("lon", longitude);
                requestParams.put("appid", APP_ID);

                Log.d("Cima", "latitude: " + latiude);
                Log.d("MrMeteo", "longitude: " + longitude);

                networking(requestParams);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                Log.d("MrMeteo", "onProviderDisabled");
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return;
        }
        locationManager.requestLocationUpdates(LOCATION_PROVIDER, MIN_TIME, MIN_DISTANCE, locationListener);
    }

    private void networking(RequestParams requestParams) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(WEATHER_URL, requestParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("Clima", "onSuccess: " + response.toString());
                MrMeteoData weatherDataModel = MrMeteoData.fromJson(response);

                mCityLabel.setText(weatherDataModel.getmCity());
                conditionLabel.setText(weatherDataModel.condition);
                mTemperatureLabel.setText(weatherDataModel.getmTemperature());
                int resID = getResources().getIdentifier(weatherDataModel.getmIconName(), "drawable", getPackageName());
                mWeatherImage.setImageResource(resID);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject response) {
                Log.e("Clima", "onFailure: " + e.toString());
                Toast.makeText(MrMeteoController.this, "Request Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("clima", "onRequestPermissionsResult: permission granted");
                getWeatherForCurrentLocation();
            }
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (locationManager != null)
            locationManager.removeUpdates(locationListener);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finishAffinity();
    }
}