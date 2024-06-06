package com.example.busmobileapp.Incharge;


import static com.example.busmobileapp.Maps.DataManager.getBusDataFromFirebase;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;

import com.example.busmobileapp.Maps.DataManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.busmobileapp.R;
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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Map;

public class InchargeMapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean isFirstRun = true;
    private Spinner inchargeSpinner;
    private MaterialButton trackLocationButton;
    private FloatingActionButton fab;
    private FloatingActionButton fabDriverLocation;
    private float lastBearing = 0.5f;
    private static GoogleMap mMap;
    private LatLng selectedLocation;
    private int selectedBusNumber;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;

    private DatabaseReference busLocationsRef;
    private DatabaseReference driverLocationsRef;
    private DatabaseReference driverLocationSwitchRef;
    private static Marker driverMarker;
    ArrayList<String> routesSpinner;
    ArrayAdapter<String> RouteAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incharge_maps);

        inchargeSpinner = findViewById(R.id.inchargespinner);
        trackLocationButton = findViewById(R.id.button);
        fab = findViewById(R.id.fab);
        busLocationsRef = FirebaseDatabase.getInstance().getReference("BusRoutes");
        driverLocationsRef = FirebaseDatabase.getInstance().getReference("driverLocations");
        driverLocationSwitchRef = FirebaseDatabase.getInstance().getReference("driverLocationSwitch");

        routesSpinner = new ArrayList<>();
        RouteAdapter = new ArrayAdapter<>(InchargeMapsActivity.this, R.layout.spinner_layout, routesSpinner);
        RouteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        inchargeSpinner.setAdapter(RouteAdapter);
        // Fetch routes from the database
        fetchRoutesFromDatabase();
        inchargeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                String selectedRoute = (String) inchargeSpinner.getItemAtPosition(position);
                if (!selectedRoute.equals("Select Route")) {
                    selectedBusNumber = Integer.parseInt(selectedRoute);
                    subscribeToBusTopic(selectedBusNumber);
                    subscribeToDriverLocation(selectedBusNumber);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Do nothing
            }
        });

        trackLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSelectedLocationOnMap();
            }
        });

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

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.inchargemaps);
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

    private void fetchRoutesFromDatabase() {
        busLocationsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                routesSpinner.clear();
                routesSpinner.add(0, "Select Route");

                for (DataSnapshot item : snapshot.getChildren()) {
                    routesSpinner.add(item.getKey());
                }

                RouteAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("InchargeMapsActivity", "Failed to read value.", error.toException());
            }
        });
    }

    private void subscribeToBusTopic(int busNumber) {
        FirebaseMessaging.getInstance().subscribeToTopic(String.valueOf(busNumber));
    }

    private void subscribeToDriverLocation(int busNumber) {
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
                Log.e("InchargeMapsActivity", "Failed to read value.", databaseError.toException());
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

                    if (driverMarker != null) {
                        updateDriverMarker(driverMarker.getPosition());
                    }
                }

                @Override
                public void onDataError(String errorMessage) {
                    Log.e("InchargeMapsActivity", "Error fetching bus data: " + errorMessage);
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
            if (driverMarker != null && driverMarker.getPosition().latitude != 0 && driverMarker.getPosition().longitude != 0) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(driverMarker.getPosition(), 15));
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
                Log.e("InchargeMapsActivity", "Location permission denied");
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
}
