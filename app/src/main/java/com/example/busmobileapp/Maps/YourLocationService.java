package com.example.busmobileapp.Maps;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.example.busmobileapp.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class YourLocationService extends Service {

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private PowerManager.WakeLock wakeLock;
    private int routeNumber;
    private static boolean isLocationSharingEnabled = false;
    private static boolean isServiceRunning = false;
    private static final int NOTIFICATION_ID = 123; // Notification ID

    @Override
    public void onCreate() {
        super.onCreate();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        acquireWakeLock();
        startLocationUpdates();
        startForeground(NOTIFICATION_ID, createNotification());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        routeNumber = intent.getIntExtra("routeNumber", -1);
        return START_STICKY;
    }

    private void acquireWakeLock() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        if (powerManager != null) {
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    "YourApp::WakeLock");
            if (!wakeLock.isHeld()) {
                wakeLock.acquire();
            }
        }
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(3000); // Update every 3 seconds
        locationRequest.setFastestInterval(1000); // Fastest update every 1 second
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult != null && isLocationSharingEnabled) {
                        LatLng driverLocation = new LatLng(
                                locationResult.getLastLocation().getLatitude(),
                                locationResult.getLastLocation().getLongitude()
                        );
                        updateDriverLocation(driverLocation);
                    }
                }
            };

            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    private void updateDriverLocation(LatLng driverLocation) {
        DatabaseReference driverLocationsRef = FirebaseDatabase.getInstance().getReference("driverLocations").child(String.valueOf(routeNumber));
        driverLocationsRef.setValue(driverLocation);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
        releaseWakeLock();
        stopForegroundService();
    }

    private void stopLocationUpdates() {
        if (locationCallback != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }

    public void setLocationSharingEnabled(boolean locationSharingEnabled) {
        this.isLocationSharingEnabled = locationSharingEnabled;
    }

    private void releaseWakeLock() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    private void startForegroundService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = createNotificationChannel("my_service", "My Background Service");
            Notification notification = new Notification.Builder(this, channelId)
                    .setContentTitle("My Background Service")
                    .setContentText("Location updates in the background")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .build();
            startForeground(NOTIFICATION_ID, notification);
            isServiceRunning = true;
        }
    }

    private void stopForegroundService() {
        if (isServiceRunning) {
            stopForeground(true);
            isServiceRunning = false;
        }
    }

    private Notification createNotification() {
        createNotificationChannel();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "YourAppChannel")
                .setContentTitle("Your App is Running")
                .setContentText("Location updates are active")
                .setSmallIcon(R.mipmap.ic_launcher);

        return builder.build();
    }

    private String createNotificationChannel(String channelId, String channelName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
            return channelId;
        }
        return "";
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel("YourAppChannel", "Your App Channel", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}