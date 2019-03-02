package com.example.benjamin.scavengerhunt;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;


public class GeofenceTransitionsIntentService extends IntentService {

    public GeofenceTransitionsIntentService() {
        super(GeofenceTransitionsIntentService.class.getName());
    }

    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            Log.d("IntentService: ", "geofancing event error.");
            return;
        } else {
            // Get the transition type.
            int geofenceTransition = geofencingEvent.getGeofenceTransition();
            Log.d("IntentService: ", "in services..");
            // Test that the reported transition was of interest.
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {

                // Get the geofences that were triggered. A single event can trigger
                // multiple geofences.
                List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
                // Get the transition details as a String.
                String geofenceTransitionDetails = getGeofenceTransitionDetails(
                        geofenceTransition,
                        triggeringGeofences
                );

                // Send notification and log the transition details.
                Log.d("IntentService: ", "enter triggered.");
            } else {
                // Log the error.
                Log.d("IntentService: ", "***dewll fail");
            }

            if(geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL){
                Log.d("IntentService: ", "dwell triggered.");
            }
        }
    }

    private String getGeofenceTransitionDetails(int geofenceTransition, List<Geofence> triggeringGeofences) {
        ArrayList<String> triggerfanceList = new ArrayList<>();
        for (Geofence geofance : triggeringGeofences){
            triggerfanceList.add(geofance.getRequestId());
        }

        String status = null;

        if(geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL){
            status = "inside geofance circle!!";
        } else {
            status = "not in the circle.";
        }

        return status;
    }
}

