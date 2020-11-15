package com.company.climapm;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;


public class WeatherController extends AppCompatActivity {

    // Constants:
    final int Request_Code = 123;
    final String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather";
    // App ID to use OpenWeather data
    final String APP_ID = "ebecb1fdff69c54de83d642e667e0244";
    // Time between location updates (5000 milliseconds or 5 seconds)
    final long MIN_TIME = 5000;
    // Distance between location updates (1000m or 1km)
    final float MIN_DISTANCE = 1000;

    // TODO: Set LOCATION_PROVIDER here:

    String Location_provider = LocationManager.GPS_PROVIDER;


    // Member Variables:
    TextView mCityLabel;
    ImageView mWeatherImage;
    TextView mTemperatureLabel;

    // TODO: Declare a LocationManager and a LocationListener here:

    LocationManager mLocationManager;

    LocationListener mLocationListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_controller_layout);

        // Linking the elements in the layout to Java code
        mCityLabel = (TextView) findViewById(R.id.locationTV);
        mWeatherImage = (ImageView) findViewById(R.id.weatherSymbolIV);
        mTemperatureLabel = (TextView) findViewById(R.id.tempTV);
        ImageButton changeCityButton = (ImageButton) findViewById(R.id.changeCityButton);


        // TODO: Add an OnClickListener to the changeCityButton here:

        changeCityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(WeatherController.this,ChangeCity.class);
                startActivity(intent);


                
            }
        });

    }


    // TODO: Add onResume() here:

    @Override

    protected void onResume() {

        super.onResume();

        Log.d("clima", "onResume() called");

        Intent intent = getIntent();
        String city = intent.getStringExtra("City");

        if (city != null){

            getWeatherForNewCity(city);

        }else {

            Log.d("clima", "Getting weather from current Location");

            getWeatherForCurrentLocation();
        }

    }


    // TODO: Add getWeatherForNewCity(String city) here:

    private void getWeatherForNewCity(String city){


        RequestParams params = new RequestParams();

        params.put("q",city);
        params.put("AppID",APP_ID);

        SomeNetworking(params);


    }


    // TODO: Add getWeatherForCurrentLocation() here:
    protected void getWeatherForCurrentLocation() {

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d("clima", "onLocationChanged() callback received");

                String Longitude = String.valueOf(location.getLongitude());
                String Latitude = String.valueOf(location.getLatitude());

                Log.d("clima","Longitude= " + Longitude);
                Log.d("clima","Latitude=" + Latitude);

                RequestParams params = new RequestParams();

                params.put("lat",Latitude);
                params.put("lon",Longitude);
                params.put("AppID",APP_ID);

                SomeNetworking(params);

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

                Log.d("clima", "onProviderDisabled() callback received");

            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},Request_Code);
            return;
        }
        mLocationManager.requestLocationUpdates(Location_provider, MIN_TIME, MIN_DISTANCE, mLocationListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == Request_Code){

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Log.d("clima","onRequestPermissionResult() Permission Granted");

                getWeatherForCurrentLocation();
            }
            else {
                Log.d("clima","Permission denied");
            }
        }
    }


    // TODO: Add SomeNetworking(RequestParams params) here:

     private void SomeNetworking(RequestParams params){


         AsyncHttpClient client = new AsyncHttpClient();

         client.get(WEATHER_URL,params,new JsonHttpResponseHandler(){

             @Override

             public void onSuccess(int statusCode, Header[] headers, JSONObject response){


                 Log.d("clima","Success, JSON: " + response.toString());

                 WeatherDataModel weatherData = WeatherDataModel.fromJSON(response);

                 updateUI(weatherData);
             }

             @Override

             public void onFailure(int statusCode,Header[] headers,Throwable e, JSONObject response){

                 Log.d("clima","fail" + e.toString());
                 Log.d("clima","statusCode" + statusCode);

                 Toast.makeText(WeatherController.this,"Request failed",Toast.LENGTH_LONG).show();

             }
         });

    }


    // TODO: Add updateUI() here:


    private void updateUI(WeatherDataModel weather){

        mTemperatureLabel.setText(weather.getmTemperature());
        mCityLabel.setText(weather.getmCity());


        int resourceID = getResources().getIdentifier(weather.getmIconName(),"drawable",getPackageName());
        mWeatherImage.setImageResource(resourceID);


    }




    // TODO: Add onPause() here:


    @Override
    protected void onPause() {
        super.onPause();

        if (mLocationManager != null) mLocationManager.removeUpdates(mLocationListener);
    }
}
