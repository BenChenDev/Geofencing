package com.example.benjamin.scavengerhunt;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMyLocationButtonClickListener,
        OnMyLocationClickListener, OnMapReadyCallback{

    private GoogleMap mMap ;
    private GoogleApiClient googleApiClient = null;
    private  int Location_PERMISSION_CODE = 1;
    private GeofencingClient geofencingClient;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for(Location location:locationResult.getLocations()){
                if(location != null){
                    Log.d("MyLocation: ", "("+location.getLongitude()+","+location.getLatitude()+")");
                }
            }
        }
    };

    PendingIntent geofencePendingIntent;
    LatLng home = new LatLng(50.666657, -120.348992);
    LatLng location1 = new LatLng(50.666707, -120.348493);
    LatLng CAC = new LatLng(50.672682, -120.366000);
    LatLng OM = new LatLng(50.671174, -120.363291);
    LatLng HOL = new LatLng(50.672000, -120.365257);
    ArrayList<LatLng> locations = new ArrayList<>();
//
//    private PendingIntent getGeofencePendingIntent() {
//        // Reuse the PendingIntent if we already have it.
//        if (geofencePendingIntent != null) {
//            return geofencePendingIntent;
//        }
//        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
//        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
//        // calling addGeofences() and removeGeofences().
//        geofencePendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.
//                FLAG_UPDATE_CURRENT);
//        return geofencePendingIntent;
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        Log.d("Location services:", "GoogleLocationAPI connected.");
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        Log.d("Location services:", "Suspending to connect to GoogleLocationAPI.");
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Log.d("Location services:", "Failed to connect to GoogleLocationAPI.");
                    }
                })
        .build();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //create Geofancing Client
        geofencingClient = LocationServices.getGeofencingClient(this);

//        locations.add(CAC);
//        locations.add(HOL);
//        locations.add(OM);

        locations.add(location1);


    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("main, on start:", "reconnect google api client.");
        googleApiClient.reconnect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("main, on stop:", "disconnect google api client.");
        googleApiClient.disconnect();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if(mMap == null){
//            mMap = GoogleMap;
//        }
        if(hasPermission() == true) {
            int response = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
            if(response != ConnectionResult.SUCCESS){
                Log.d("Google play services:", "use download it");
            }else{
                Log.d("google play services:", "no required.");
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED && mMap != null) {
                mMap.setMyLocationEnabled(true);
            }

//            startLocationUpdate();
            //updateUI();
        } else {

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Location_PERMISSION_CODE);
        }
    }

    GeofencingRequest getRequest;
    @SuppressLint("MissingPermission")
    private void presetGeoFancing(){
        for(LatLng location : locations){
            Geofence geofence = new Geofence.Builder()
                    .setRequestId("test")
                    .setCircularRegion(location.latitude, location.longitude, 100)
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                    .build();

            GeofencingRequest geofencingRequest = new GeofencingRequest.Builder()
                    .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                    .addGeofence(geofence)
                    .build();

            Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
            PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent,
                    PendingIntent.FLAG_CANCEL_CURRENT);

            geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                    .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            drawGeofance();
                            Log.d("mapsActivity", "geofence set successfully.");
                        }
                    });
        }
    }

    Circle geofanceArea;
    private void drawGeofance(){
        CircleOptions circleOptions = new CircleOptions()
                .center(location1)
                .strokeColor(Color.argb(50,66,24,137))
                .fillColor(Color.argb(100, 66, 134, 244))
                .radius(100);
        geofanceArea = mMap.addCircle(circleOptions);
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdate(){
//        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("onmapready: ", "map ready to use");
        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED && mMap != null) {
            mMap.setMyLocationEnabled(true);
        }

        //zoom in
        LocationManager locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        Location location = locationManager.getLastKnownLocation(locationManager
                .getBestProvider(criteria, false));
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        LatLng currentLocation = new LatLng(latitude, longitude);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));

//preset 3 geofancings
        presetGeoFancing();
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);

    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == Location_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission GRANTED", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean hasPermission(){
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }
}
