package com.example.benjamin.scavengerhunt;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
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
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMyLocationButtonClickListener,
        OnMyLocationClickListener, OnMapReadyCallback {

    private GoogleMap mMap;
    private Location currentLocation = null;
    private PendingIntent pendingIntent = null;
    private GoogleApiClient googleApiClient = null;
    private int Location_PERMISSION_CODE = 1;
    private GeofencingClient geofencingClient;
    private int score = 0;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                if (location != null) {
                    currentLocation = location;
                    Log.d("MyLocation: ", "(" + location.getLatitude() + "," + location.getLongitude() + ")");
                }
            }
        }
    };

//    static class GoogleReceiver extends BroadcastReceiver {
//
//        MapsActivity mActivity;
//
//        public GoogleReceiver(Activity activity){
//            mActivity = (MapsActivity) activity;
//        }
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            //Handle the intent here
//            String id = intent.getStringExtra("ID");
//            Log.d("ID: ", id);
//            ArrayList<String> ids = new ArrayList<>();
//            removeGeofence(ids);
//        }
//    }

    private BroadcastReceiver listener = new BroadcastReceiver() {
        @Override
        public void onReceive( Context context, Intent intent ) {
            String id = intent.getStringExtra("ID");
            Log.d("ID: ", id);
        }
    };


    LatLng home = new LatLng(50.666657, -120.348992);
    LatLng location1 = new LatLng(50.666707, -120.348493);
    LatLng CAC = new LatLng(50.672682, -120.366000);
    LatLng OM = new LatLng(50.671174, -120.363291);
    LatLng BUS = new LatLng(50.671264, -120.368405);
    ArrayList<LatLng> locations = new ArrayList<>();
    ArrayList<Geofence> geofences = new ArrayList<>();

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

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createLocationRequest();

        //create Geofancing Client
        geofencingClient = LocationServices.getGeofencingClient(this);

        locations.add(CAC);
        locations.add(BUS);
        locations.add(OM);

        locations.add(location1);

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(listener, new IntentFilter("googlegeofence"));
//        LocalBroadcastManager lbc = LocalBroadcastManager.getInstance(this);
//        GoogleReceiver receiver = new GoogleReceiver(this);
//        lbc.registerReceiver(receiver, new IntentFilter("googlegeofence"));
        //Anything with this intent will be sent to this receiver
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
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

        if (currentLocation != null && mMap != null) {
            LatLng l = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(l, 15));
        }
    }

    private void buildGeofence() {
        geofences.add(new Geofence.Builder()
                .setRequestId("OM")
                .setCircularRegion(OM.latitude, OM.longitude, 100)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL)
                .setLoiteringDelay(50000)
                .build());
        geofences.add(new Geofence.Builder()
                .setRequestId("CAC")
                .setCircularRegion(CAC.latitude, CAC.longitude, 100)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL)
                .setLoiteringDelay(50000)
                .build());
        geofences.add(new Geofence.Builder()
                .setRequestId("BUS")
                .setCircularRegion(BUS.latitude, BUS.longitude, 100)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL)
                .setLoiteringDelay(50000)
                .build());
        geofences.add(new Geofence.Builder()
                .setRequestId("location1")
                .setCircularRegion(location1.latitude, location1.longitude, 100)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL)
                .setLoiteringDelay(50000)
                .build());
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL);
        builder.addGeofences(geofences);
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        if (pendingIntent != null) {
            return pendingIntent;
        }
        Intent intent = new Intent(MapsActivity.this, GeofenceTransitionsIntentService.class);
        pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);

        return pendingIntent;
    }

    private void drawGeofance(ArrayList<LatLng> list) {
        for(LatLng l : list){
            CircleOptions circleOptions = new CircleOptions()
                    .center(l)
                    .strokeColor(Color.argb(50, 66, 24, 137))
                    .fillColor(Color.argb(100, 66, 134, 244))
                    .radius(100);
            mMap.addCircle(circleOptions);
        }

    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdate() {
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
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
                == PackageManager.PERMISSION_GRANTED) {
            int response = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
            if (response != ConnectionResult.SUCCESS) {
                Log.d("Google play services:", "use download it");
            } else {
                Log.d("google play services:", "no required.");
            }
            startLocationUpdate();
            mMap.setMyLocationEnabled(true);
            LatLng l = new LatLng(50.666657, -120.348992);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(l, 15));
            buildGeofence();
            geofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                    .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            drawGeofance(locations);
                            Log.d("mapsActivity", "geofence set successfully.");
                        }
                    });

        } else {
            request_permission();
        }



        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);

    }

    private void request_permission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Location_PERMISSION_CODE);

        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_CALENDAR)) {

            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("To run this app, the location access is needed")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MapsActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Location_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Location_PERMISSION_CODE);
        }

    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {

    }

    @Override
    @SuppressLint("MissingPermission")
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == Location_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission GRANTED", Toast.LENGTH_SHORT).show();
                startLocationUpdate();
                if(mMap != null){
                    mMap.setMyLocationEnabled(true);
                    LatLng l = new LatLng(50.666657, -120.348992);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(l, 15));
                    buildGeofence();
                    geofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                            .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    drawGeofance(locations);
                                    Log.d("mapsActivity", "geofence set successfully.");
                                }
                            });
                }
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void removeGeofence(ArrayList<String> id){
        geofencingClient.removeGeofences(id).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                score = score + 10;
            }
        });
    }
}
