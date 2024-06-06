package com.example.busmobileapp.Student;



import static com.example.busmobileapp.Maps.DataManager.getBusDataFromFirebase;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.example.busmobileapp.Maps.DataManager;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.busmobileapp.R;
import com.example.busmobileapp.ReadWriteUserDetails;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Map;

public class StudentMapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean isFirstRun = true;
    private static GoogleMap mMap;
    private FloatingActionButton fab;
    private FloatingActionButton fabDriverLocation;
    private LatLng selectedLocation;
    private float lastBearing = 0.5f;
    private static final double RADAR_RADIUS_METERS = 1500;
    private int selectedBusNumber; // Set dynamically from fullstop
    private String TARGET_BUS_LOCATION_NAME; // Set dynamically from fullroute

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;

    private DatabaseReference busLocationsRef,driverLocationsRef;
    private static Marker driverMarker;

    private FirebaseAuth auth;
    private TextView route, stop;
    private String fullroute, fullstop;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_maps);

        route = findViewById(R.id.stroute);
        stop = findViewById(R.id.ststop);
        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();

        if (firebaseUser == null) {
            Toast.makeText(this, "Something went Wrong, User details not available at the moment", Toast.LENGTH_SHORT).show();
        } else {
            String userId = firebaseUser.getUid();

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Students");
            reference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    ReadWriteUserDetails readUserDetails = snapshot.getValue(ReadWriteUserDetails.class);
                    if (readUserDetails != null) {
                        fullroute = readUserDetails.route;
                        fullstop = readUserDetails.stop;
                        route.setText(fullroute);
                        stop.setText(fullstop);

                        // Set selectedBusNumber from fullstop
                        selectedBusNumber = Integer.parseInt(fullroute);

                        // Set TARGET_BUS_LOCATION_NAME from fullroute
                        TARGET_BUS_LOCATION_NAME = fullstop;
                        subscribeToBusTopic(selectedBusNumber);
                        subscribeToDriverLocation(selectedBusNumber);

                        initializeMap();
                    } else {
                        Toast.makeText(StudentMapsActivity.this, "Something Went Wrong!!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(StudentMapsActivity.this, "Something Went Wrong!!", Toast.LENGTH_SHORT).show();
                }
            });
        }
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveToCurrentLocation();
            }
        });

        fabDriverLocation = findViewById(R.id.fabDriverLocation);
        fabDriverLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveToDriverLocation();
            }
        });
    }

    private void initializeMap() {
        busLocationsRef = FirebaseDatabase.getInstance().getReference("BusRoutes");

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.studentmaps);
        mapFragment.getMapAsync(this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        selectedLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        showSelectedLocationOnMap();
                    }
                }
            }
        };

        checkLocationPermission();
    }

    private void subscribeToBusTopic(int busNumber) {
        FirebaseMessaging.getInstance().subscribeToTopic("bus_" + busNumber);
    }

    private void subscribeToDriverLocation(int busNumber) {
        driverLocationsRef = FirebaseDatabase.getInstance().getReference("driverLocations");
        driverLocationsRef.child(String.valueOf(busNumber)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    double driverLatitude = dataSnapshot.child("latitude").getValue(Double.class);
                    double driverLongitude = dataSnapshot.child("longitude").getValue(Double.class);
                    LatLng driverLocation = new LatLng(driverLatitude, driverLongitude);
                    updateDriverMarker(driverLocation);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("StudentActivity", "Failed to read value.", databaseError.toException());
            }
        });
    }

    private void showSelectedLocationOnMap() {
        if (mMap != null) {
            getBusDataFromFirebase(new DataManager.BusDataCallback() {
                @Override
                public void onBusDataReceived(Map<Integer, Map<String, LatLng>> busData) {
                    Map<String, LatLng> busLocations = busData.get(selectedBusNumber);

                    mMap.clear();

                    if (busLocations != null && !busLocations.isEmpty()) {
                        for (Map.Entry<String, LatLng> entry : busLocations.entrySet()) {
                            String locationName = entry.getKey();
                            LatLng location = entry.getValue();

                            mMap.addMarker(new MarkerOptions()
                                    .position(location)
                                    .title(locationName)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                        }
                        if (isFirstRun) {
                            Map.Entry<String, LatLng> firstLocation = busLocations.entrySet().iterator().next();
                            LatLng location = firstLocation.getValue();
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
                            isFirstRun = false;
                        }
                    }

                    if (selectedLocation != null) {
                        mMap.addMarker(new MarkerOptions()
                                .position(selectedLocation)
                                .title("Live Location")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                    }


                    checkBusProximity();
                }

                @Override
                public void onDataError(String errorMessage) {
                    Log.e("StudentActivity", "Error fetching bus data: " + errorMessage);
                }
            });
        }
    }


    private void moveToCurrentLocation() {
        if (mMap != null) {
            if (selectedLocation != null) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(selectedLocation, 15));
            } else {
                Toast.makeText(this, "Current location not available", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void moveToDriverLocation() {
        if (mMap != null) {
            if (driverMarker != null && driverMarker.getPosition().latitude!=0 && driverMarker.getPosition().longitude!=0) {
//                if (isSwitchOn) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(driverMarker.getPosition(), 15));
//                } else {
//                    Toast.makeText(this, "Driver's location not available or switch is off", Toast.LENGTH_SHORT).show();
//                }
            } else {
                Toast.makeText(this, "Driver's location not available", Toast.LENGTH_SHORT).show();
            }
        }
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
        locationRequest.setInterval(3000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                Log.e("StudentMapsActivity", "Location permission denied");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fusedLocationProviderClient != null && locationCallback != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Disable the default click events on the map
        mMap.setOnMapClickListener(null);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        // Update the live location of the student instead of selected location
        if (mMap != null) {
            Location location = new Location("live");
            location.setLatitude(latLng.latitude);
            location.setLongitude(latLng.longitude);

            if (selectedBusNumber != 0) {
                selectedLocation = new LatLng(location.getLatitude(), location.getLongitude());
                showSelectedLocationOnMap();
            }
        }
    }

    private void checkBusProximity() {
        if (selectedLocation != null && driverMarker != null) {
            String busLocationName = driverMarker.getTitle();
            if (busLocationName != null) {
                Location driverLocation = new Location("driver");
                driverLocation.setLatitude(driverMarker.getPosition().latitude);
                driverLocation.setLongitude(driverMarker.getPosition().longitude);

                getBusDataFromFirebase(new DataManager.BusDataCallback() {
                    @Override
                    public void onBusDataReceived(Map<Integer, Map<String, LatLng>> busData) {
                        if (busData.containsKey(selectedBusNumber)) {
                            Map<String, LatLng> busLocations = busData.get(selectedBusNumber);

                            if (busLocations != null && busLocations.containsKey(TARGET_BUS_LOCATION_NAME)) {
                                LatLng targetLatLng = busLocations.get(TARGET_BUS_LOCATION_NAME);
                                Location targetBusLocation = new Location("bus");
                                targetBusLocation.setLatitude(targetLatLng.latitude);
                                targetBusLocation.setLongitude(targetLatLng.longitude);

                                float distance = driverLocation.distanceTo(targetBusLocation);

                                if (distance <= RADAR_RADIUS_METERS) {
                                    showHurryUpNotification();
                                }
                            } else {
                                Log.e("StudentActivity", "Target bus location not found.");
                            }
                        } else {
                            Log.e("StudentActivity", "Selected bus number not found in bus data.");
                        }
                    }

                    @Override
                    public void onDataError(String errorMessage) {
                        Log.e("StudentActivity", "Error fetching bus data: " + errorMessage);
                    }
                });
            }
        }
    }
    private void showHurryUpNotification() {
        Toast.makeText(this, "Hurry Up! Bus is nearby.", Toast.LENGTH_SHORT).show();
    }

    private void updateDriverMarker(LatLng driverLocation) {
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
//            driverMarker.remove();
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
    }
}