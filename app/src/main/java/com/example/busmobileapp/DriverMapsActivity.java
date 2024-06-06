package com.example.busmobileapp;



import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;


import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import com.example.busmobileapp.Maps.YourLocationService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DriverMapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1 ;
    private GoogleMap mMap;
    private TextView driverIdTextView;
    private TextView routeNumberTextView;
    private SwitchCompat locationSharingSwitch;
    private float lastBearing = 0.0f;
    private String driverId = "211013";
    private int routeNumber = 13; // Set your default route number
    private DatabaseReference busLocationsRef;
    private DatabaseReference driverLocationsRef;
    private Marker driverMarker;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;

    // Create an instance of YourLocationService
     private YourLocationService yourLocationService;

    TextView DRouteText, DIDText;
    Button dlogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_maps);
        dlogout = findViewById(R.id.DLogout);
        locationSharingSwitch = findViewById(R.id.switch1);
        DIDText = findViewById(R.id.driverID);
        DRouteText = findViewById(R.id.DriverRoute);

        Intent intent = getIntent();
        if (intent != null) {
            String inID = intent.getStringExtra("driverId");
            String inRoute = intent.getStringExtra("driverRoute");
            if (inID != null && inRoute != null) {
                DIDText.setText("ID No: " + inID);
                DRouteText.setText("Route No." + inRoute);

                // Update driverId and routeNumber with values from the Intent
                driverId = inID;
                routeNumber = Integer.parseInt(inRoute);
            }
        }
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Start the launcher activity (home)
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        };

        getOnBackPressedDispatcher().addCallback(this, callback);

        // LogOut of Driver Activity
        dlogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAlertDialog(DriverMapsActivity.this, "Are you sure, you want to Logout?");
            }
        });

        // Initialize yourLocationService
        yourLocationService = new YourLocationService();

        // Get references to UI elements
        driverIdTextView = findViewById(R.id.driverID);
        routeNumberTextView = findViewById(R.id.DriverRoute);
        locationSharingSwitch = findViewById(R.id.switch1);

        // Initialize the map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.drivermaps);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Set up Firebase reference for bus locations
        busLocationsRef = FirebaseDatabase.getInstance().getReference("BusRoutes");
        // Set up Firebase reference for driver locations
        driverLocationsRef = FirebaseDatabase.getInstance().getReference("driverLocations").child(String.valueOf(routeNumber));

        // Initialize FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Set a listener for the Switch
        locationSharingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Handle switch state change
                if (isChecked) {
                    startLocationService(routeNumber);
                    addInitialMarkers();
                    startLocationUpdates();
                    // Call the non-static method on yourLocationService instance
                    yourLocationService.setLocationSharingEnabled(true);
                } else {
                    stopLocationUpdates();
                    driverLocationsRef.setValue(new LatLng(0, 0));
                    mMap.clear();
                    // Call the non-static method on yourLocationService instance
                    yourLocationService.setLocationSharingEnabled(false);
                }
            }
        });

        // Initialize location callback
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    LatLng driverLocation = new LatLng(
                            locationResult.getLastLocation().getLatitude(),
                            locationResult.getLastLocation().getLongitude()
                    );
                    updateDriverLocation(driverLocation);
                }
            }
        };

        checkLocationPermission();
    }
    private void checkLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);
            } else {
                startLocationUpdates();
            }
        } else {
            startLocationUpdates();
        }
    }
    private void startLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(3000); // Update every 5 seconds
        locationRequest.setFastestInterval(1000); // Fastest update every 3 seconds
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
            }
        }
    }

    private void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    private void updateDriverLocation(LatLng driverLocation) {
        // Calculate bearing between the last known location and the current location
        float bearing = lastBearing; // Set an initial value

        // Update driver's live location on the map and in the database
        if (driverMarker != null) {
            Location lastLocation = new Location("last");
            lastLocation.setLatitude(driverMarker.getPosition().latitude);
            lastLocation.setLongitude(driverMarker.getPosition().longitude);

            Location newLocation = new Location("new");
            newLocation.setLatitude(driverLocation.latitude);
            newLocation.setLongitude(driverLocation.longitude);

            bearing = lastLocation.bearingTo(newLocation);
        }

        // Load the custom bus icon from the resources
        BitmapDescriptor busIcon;
        int markerWidth = 250;
        int markerHeight = 250;

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(getResources(), R.drawable.bus1_3d),
                markerWidth, markerHeight,
                false
        );

        busIcon = BitmapDescriptorFactory.fromBitmap(scaledBitmap);
        if (driverMarker != null) {
            driverMarker.remove();
        }

        driverMarker = mMap.addMarker(new MarkerOptions()
                .position(driverLocation)
                .title("Bus Location")
                .icon(busIcon)
                .anchor(0.5f, 0.5f) // Center of the icon
                .rotation(bearing) // Set the rotation angle
        );

        lastBearing = bearing;

        mMap.moveCamera(CameraUpdateFactory.newLatLng(driverLocation));

        // Update driver's location in the Firebase Realtime Database
        driverLocationsRef.setValue(driverLocation);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    private void addInitialMarkers() {
        // Add a marker for the driver's initial location
        LatLng driverLocation = new LatLng(0, 0); // Set initial coordinates
        mMap.addMarker(new MarkerOptions().position(driverLocation).title("Driver Marker").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        // Retrieve bus locations for the specified route number
        busLocationsRef.child(String.valueOf(routeNumber)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot locationSnapshot : dataSnapshot.getChildren()) {
                    String busName = locationSnapshot.getKey();
                    // Get latitude and longitude from the snapshot
                    double latitude = locationSnapshot.child("Latitude").getValue(Double.class);
                    double longitude = locationSnapshot.child("Longitude").getValue(Double.class);

                    // Add a red marker for each bus location
                    LatLng busLocation = new LatLng(latitude, longitude);
                    mMap.addMarker(new MarkerOptions().position(busLocation).title(busName).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                }

                // Move the camera to the first bus location
                if (dataSnapshot.getChildrenCount() > 0) {
                    DataSnapshot firstLocationSnapshot = dataSnapshot.getChildren().iterator().next();
                    double firstLatitude = firstLocationSnapshot.child("Latitude").getValue(Double.class);
                    double firstLongitude = firstLocationSnapshot.child("Longitude").getValue(Double.class);
                    LatLng firstBusLocation = new LatLng(firstLatitude, firstLongitude);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstBusLocation, 15));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    private void startLocationService(int routeNumber) {
        Intent serviceIntent = new Intent(this, YourLocationService.class);
        serviceIntent.putExtra("routeNumber", routeNumber);
        startService(serviceIntent);
    }

    private void showAlertDialog(Context context, String message) {
        // Create an AlertDialog Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        // Set the dialog title and message
        builder.setTitle("Logout")
                .setMessage(message)
                .setCancelable(false)

                // Add a positive button and its click listener
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Handle the button click, if needed
                        SharedPreferences preferences = DriverMapsActivity.this.getSharedPreferences("user_data", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.clear();
                        editor.apply();
                        Toast.makeText(DriverMapsActivity.this, "Logged Out successful", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(DriverMapsActivity.this, StartActivity.class);
                        startActivity(intent);
                        finish();
                        finishAffinity();
                        dialog.dismiss(); // Close the dialog
                        driverLocationsRef.setValue(new LatLng(0, 0));
                    }
                })

                // Add a negative button and its click listener
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Handle the button click, if needed
                        dialog.dismiss(); // Close the dialog
                    }
                });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
        yourLocationService.setLocationSharingEnabled(false);
    }
}
