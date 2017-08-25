package com.olegsagenadatrytwo.geocoding.view.mapsactivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.olegsagenadatrytwo.geocoding.R;


public class MapsActivity extends FragmentActivity implements MapsActivityContract.View {

    private static final String TAG = "MapsActivity";
    public static final String KEY = "AIzaSyBw_dqcm1S5EL43DT-ytOuqlyRWtIdBzrk";
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 1;
    private EditText etAddress;


    private boolean gps_enabled = false;
    private boolean network_enabled = false;
    private LocationManager locationManager;

    private MapsActivityPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        etAddress = findViewById(R.id.etAddress);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkGPSthenPermision();
    }

    private void checkGPSthenPermision() {
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        gps_enabled = false;
        network_enabled = false;

        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        if(gps_enabled && network_enabled) {
            askPermissionAgain();
        }
        else{
            askUserToEnableGPS();
        }
    }

    private void askUserToEnableGPS() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Please Enable GPS in settings!!!")
                .setMessage("Click Yes to go to settings" + "\n" + "No to quit.")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);

                    }

                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        System.exit(0);
                    }
                })
                .show();
    }

    private void askPermissionAgain() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                Toast.makeText(this, "need permission", Toast.LENGTH_SHORT).show();
                new AlertDialog.Builder(this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Permission Needed!!!")
                        .setMessage("To use this application you must allow location. Click Yes to allow No to quit.")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                askPermission();
                            }

                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                System.exit(0);
                            }
                        })
                        .show();

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            presenter = new MapsActivityPresenter();
            presenter.attachView(this);
            presenter.setContext(this);
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            presenter.showMap(mapFragment);
        }
    }

    private void askPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                MY_PERMISSIONS_REQUEST_LOCATION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    presenter = new MapsActivityPresenter();
                    presenter.attachView(this);
                    presenter.setContext(this);
                    // Obtain the SupportMapFragment and get notified when the map is ready to be used.
                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.map);
                    presenter.showMap(mapFragment);

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.\
                    System.exit(0);
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    //on Click to submit the address and place the marker on the map
    public void submitAddress(View view) {
        Log.d(TAG, "submitAddress: ");
        //get the typed address from the edit text
        String address = etAddress.getText().toString();
        presenter.submitAddress(address);
    }

    @Override
    public void showError(String a) {
    }

    @Override
    public void mapShowed(boolean isSaved) {
        Log.d(TAG, "mapShowed: ");
    }

    @Override
    public void addressReceivedReadyToUpdate(final LatLng searchedLocation, final GoogleMap map) {
        Log.d(TAG, "addressReceivedReadyToUpdate: ");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                map.clear();
                map.addMarker(new MarkerOptions().position(searchedLocation).title("city"));
                map.moveCamera(CameraUpdateFactory.newLatLng(searchedLocation));
            }
        });
    }
}
