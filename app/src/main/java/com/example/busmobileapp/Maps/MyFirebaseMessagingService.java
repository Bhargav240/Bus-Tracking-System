package com.example.busmobileapp.Maps;


import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMessagingServ";

    private static GoogleMap mMap;

    public static void setMap(GoogleMap map) {
        mMap = map;
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Handle incoming FCM messages here
        // This method is called when the app receives a message

        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if the message contains data
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            // Extract data from the message
            double latitude = Double.parseDouble(remoteMessage.getData().get("latitude"));
            double longitude = Double.parseDouble(remoteMessage.getData().get("longitude"));

            // Update the map with the new bus location
            updateMapWithBusLocation(latitude, longitude);
        }

        // Check if the message contains a notification payload
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }
    }

    private void updateMapWithBusLocation(double latitude, double longitude) {
        // TODO: Update the map with the new bus location
        // For example, add a marker at the specified latitude and longitude
        if (mMap != null) {
            LatLng busLocation = new LatLng(latitude, longitude);
            mMap.addMarker(new MarkerOptions().position(busLocation).title("Bus Location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(busLocation, 15)); // Adjust zoom level as needed
        }
    }
}
