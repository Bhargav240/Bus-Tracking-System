package com.example.busmobileapp.Maps;


import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class DataManager {

    private static final String BUS_LOCATIONS_NODE = "BusRoutes";

    public static void getBusDataFromFirebase(final BusDataCallback callback) {
        final Map<Integer, Map<String, LatLng>> busData = new HashMap<>();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(BUS_LOCATIONS_NODE);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot busSnapshot : dataSnapshot.getChildren()) {
                        int busNum = Integer.parseInt(busSnapshot.getKey());
                        Map<String, LatLng> locations = new HashMap<>();

                        for (DataSnapshot locationSnapshot : busSnapshot.getChildren()) {
                            String locationName = locationSnapshot.getKey();
                            double latitude = locationSnapshot.child("Latitude").getValue(Double.class);
                            double longitude = locationSnapshot.child("Longitude").getValue(Double.class);
                            locations.put(locationName, new LatLng(latitude, longitude));
                        }

                        busData.put(busNum, locations);
                    }

                    // Notify the callback with the fetched data
                    if (callback != null) {
                        callback.onBusDataReceived(busData);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors, if any
                if (callback != null) {
                    callback.onDataError(databaseError.getMessage());
                }
            }
        });
    }

    public interface BusDataCallback {
        void onBusDataReceived(Map<Integer, Map<String, LatLng>> busData);
        void onDataError(String errorMessage);
    }
}
