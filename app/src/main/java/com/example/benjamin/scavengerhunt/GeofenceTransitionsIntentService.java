package com.example.benjamin.scavengerhunt;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;


public class GeofenceTransitionsIntentService extends IntentService {

//    private ResultReceiver mResultReceiver;
    public GeofenceTransitionsIntentService() {
        super(GeofenceTransitionsIntentService.class.getName());
    }

    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
//        mResultReceiver = intent.getParcelableExtra("RECEIVER");



//        if(mResultReceiver == null){
//            Log.d("intent", "resultreceiver is null");
//        }
        if (geofencingEvent.hasError()) {
            Log.d("IntentService: ", "geofancing event error.");
            return;
        } else {
            // Get the transition type.
            int geofenceTransition = geofencingEvent.getGeofenceTransition();
            Log.d("IntentService: ", "in services..");
            // Test that the reported transition was of interest.
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {

                // Get the geofences that were triggered. A single event can trigger
                // multiple geofences.
                List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
                // Get the transition details as a String.
                getGeofenceTransitionDetails(
                        geofenceTransition,
                        triggeringGeofences
                );

                // Send notification and log the transition details.
                Log.d("IntentService: ", "DWELL triggered.");
            } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                // Log the error.
                Log.d("IntentService: ", "***ENTER");
            }
        }
    }

    private void getGeofenceTransitionDetails(int geofenceTransition, List<Geofence> triggeringGeofences) {
//        Bundle bundle = new Bundle();
        Intent lbcIntent = new Intent("googlegeofence"); //Send to any reciever listening for this
        for (Geofence geofance : triggeringGeofences){
            Log.d("IntentService: ", geofance.getRequestId());
//            bundle.putString("ID", geofance.getRequestId());
            lbcIntent.putExtra("ID", geofance.getRequestId());  //Put whatever it is you want the activity to handle
        }
//        mResultReceiver.send(1,bundle);

        LocalBroadcastManager.getInstance(this).sendBroadcast(lbcIntent);  //Send the intent
    }
}

