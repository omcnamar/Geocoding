package com.olegsagenadatrytwo.geocoding.view.mapsactivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.olegsagenadatrytwo.geocoding.model.AddressInfo;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by omcna on 8/25/2017.
 */

public class MapsActivityPresenter implements MapsActivityContract.Presenter, OnMapReadyCallback{

    public static final String TAG = "MapsActivityPresenter";

    private MapsActivityContract.View view;
    private Context context;

    public static final String KEY = "AIzaSyBw_dqcm1S5EL43DT-ytOuqlyRWtIdBzrk";
    private GoogleMap mMap;
    private FusedLocationProviderClient fuseLocationProviderCliend;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private double lat;
    private double lon;
    private AddressInfo addressInfo;
    private LatLng searchedLocation;
    private SupportMapFragment mapFragment;

    @Override
    public void attachView(MapsActivityContract.View view) {
        this.view = view;
    }

    @Override
    public void removeView() {
        this.view = null;
    }

    @Override
    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public void showMap(SupportMapFragment mapFragmentIn) {

        Log.d(TAG, "showMap: ");
        fuseLocationProviderCliend = LocationServices.getFusedLocationProviderClient(context);
        fuseLocationProviderCliend.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        LatLng searchedLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.clear();
                        mMap.addMarker(new MarkerOptions().position(searchedLocation).title("here"));
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(searchedLocation));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = mapFragmentIn;
        mapFragment.getMapAsync(this);

    }

    @Override
    public void submitAddress(String address) {
        Log.d(TAG, "submitAddress: ");
        //make request to get the l
        final OkHttpClient okHttpClient;
        final Request request;
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host("maps.googleapis.com")
                .addPathSegment("maps")
                .addPathSegment("api")
                .addPathSegment("geocode")
                .addPathSegment("json")
                .addQueryParameter("address", address)
                .addQueryParameter("key",KEY)
                .build();

        okHttpClient = new OkHttpClient();
        request = new Request.Builder()
                .url(url)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure: ");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG, "onResponse: ");
                Gson gson = new Gson();
                addressInfo = gson.fromJson(response.body().string(), AddressInfo.class);
                //use the LatLng and change the marker to appropriate place based on address
                searchedLocation = new LatLng(addressInfo.getResults().get(0).getGeometry().getLocation().getLat(),
                        addressInfo.getResults().get(0).getGeometry().getLocation().getLng());
                view.addressReceivedReadyToUpdate(searchedLocation, mMap);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: ");
        mMap = googleMap;

        locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                lat = location.getLatitude();
                lon = location.getLongitude();
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
            }

            @Override
            public void onProviderEnabled(String s) {
            }

            @Override
            public void onProviderDisabled(String s) {
            }
        };

        if(Build.VERSION.SDK_INT < 23){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }else{

            //if we don't have permission
            if(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                //ask for permission
                ActivityCompat.requestPermissions((MapsActivity)context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
            }else {
                //we have permission
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location lastKnown = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                LatLng userLocation;
                if(lastKnown != null) {
                    userLocation = new LatLng(lastKnown.getLatitude(), lastKnown.getLongitude());
                }else{
                    userLocation = new LatLng(-33, 80);
                }
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(userLocation).title("Your Location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(userLocation));
            }
        }

        view.mapShowed(true);
    }
}
