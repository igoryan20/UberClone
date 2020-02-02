package ru.apps.igoryan20.uberclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class DriverRequestActivity extends AppCompatActivity implements View.OnClickListener,
        AdapterView.OnItemClickListener {


    private Button btnGetRequests;

    private LocationManager mLocationManager;
    private LocationListener mLocationListener;

    private ListView mListView;
    private ArrayList<String> mNearByDriveRequests;
    private ArrayAdapter mAdapter;
    private ArrayList<Double> mPassengersLatitudes;
    private ArrayList<Double> mPassengersLongitudes;

    //Here callbacks started

    //Callback for creating Activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_request);

        btnGetRequests = findViewById(R.id.btnGetRequests);
        btnGetRequests.setOnClickListener(this);

        mListView = findViewById(R.id.requestListView);
        mNearByDriveRequests = new ArrayList<>();
        mAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, mNearByDriveRequests);

        mPassengersLatitudes = new ArrayList<>();
        mPassengersLongitudes = new ArrayList<>();

        mListView.setAdapter(mAdapter);

        mNearByDriveRequests.clear();

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if(Build.VERSION.SDK_INT < 23 || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){

            mLocationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,0,mLocationListener);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };

        }

        mListView.setOnItemClickListener(this);


    }

    //Callback for creating something in on options menu on the top
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.driver_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    //Callback for handle selected item in options menu
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == R.id.driverLogOutItem){

            ParseUser.logOutInBackground(new LogOutCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        finish();
                    }
                }
            });

        }

        return super.onOptionsItemSelected(item);
    }

    //Callback for handle the clicked item
    @Override
    public void onClick(View v) {

        if(Build.VERSION.SDK_INT < 23){
            Location currentDriverLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            updateRequestsListView(currentDriverLocation);

        } else if (Build.VERSION.SDK_INT >= 23) {
            if(ContextCompat.checkSelfPermission(DriverRequestActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(DriverRequestActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        1000);
            } else {
                Location currentDriverLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                updateRequestsListView(currentDriverLocation);
            }
        }
    }

    //Callback for request permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1000 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            if(ContextCompat.checkSelfPermission(DriverRequestActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED)
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);

                Location currentDriverLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                updateRequestsListView(currentDriverLocation);
        }
    }

    //Callback for handling List in Activity
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT).show();

    }
    //Callbacks ended

    //The methods of this Activity
    private void updateRequestsListView(Location driverLocation) {

        if(driverLocation != null) {

            if(mNearByDriveRequests.size() > 0) {
                mNearByDriveRequests.clear();
            }
            if(mPassengersLatitudes.size() > 0){
                mPassengersLatitudes.clear();
            }
            if(mPassengersLongitudes.size() > 0) {
                mPassengersLongitudes.clear();
            }

            final ParseGeoPoint driverCurrentLocation = new ParseGeoPoint(driverLocation.getLatitude(), driverLocation.getLongitude());

            ParseQuery<ParseObject> requestCarQuery = ParseQuery.getQuery("RequestCar");
            requestCarQuery.whereNear("passengerLocation", driverCurrentLocation);
            requestCarQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if(e == null) {
                        if (objects.size() > 0) {
                            for (ParseObject nearRequest : objects) {

                                Double milesDistanceToPassenger = driverCurrentLocation.distanceInMilesTo(nearRequest.getParseGeoPoint("passengerLocation"));

                                float roundedDistanceValue = Math.round(milesDistanceToPassenger * 10) / 10;

                                mNearByDriveRequests.add("There are " + roundedDistanceValue + " miles to " + nearRequest.get("username"));

                                mPassengersLatitudes.add(((ParseGeoPoint) nearRequest.get("passengerLocation")).getLatitude());
                                mPassengersLongitudes.add(((ParseGeoPoint) nearRequest.get("passengerLocation")).getLongitude());


                            }
                        } else {
                            Toast.makeText(DriverRequestActivity.this, "Not yet requests", Toast.LENGTH_LONG).show();

                        }
                        mAdapter.notifyDataSetChanged();
                    }
                }
            });

        }


    }


}
