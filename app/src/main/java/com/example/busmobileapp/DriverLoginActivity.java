package com.example.busmobileapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DriverLoginActivity extends AppCompatActivity {
    Button dlogin;

    ProgressBar progressBar;
    private EditText DID, DPassword;
    private DatabaseReference databaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login);
        dlogin = (Button) findViewById(R.id.buttonDLogin);
        DID = findViewById(R.id.Did);
        DPassword = findViewById(R.id.Dpass);
        progressBar = findViewById(R.id.progressBarD);
        databaseReference = FirebaseDatabase.getInstance().getReference("Drivers");



        dlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
    }
    public void login() {
        final String id = DID.getText().toString().trim();
        final String password = DPassword.getText().toString().trim();

        if (TextUtils.isEmpty(id)) {
            DID.setError("ID Number is Required");
            DID.requestFocus();
            return;
        } else if (TextUtils.isEmpty(password)) {
            DPassword.setError("Password is Required");
            DPassword.requestFocus();
            return;
        } else {
            progressBar.setVisibility(View.VISIBLE);
        }

        // Query the database for the entered ID
        databaseReference.orderByChild("ID").equalTo(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    progressBar.setVisibility(View.GONE);
                    // User ID found in the database
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String storedPassword = snapshot.child("Password").getValue(String.class);
                        String routeNo = snapshot.child("RouteNo").getValue(String.class);

                        if (password.equals(storedPassword)) {
                            // Passwords match, login successful
                            SharedPreferences preferences = getSharedPreferences("user_data", MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString("loggedInUserID", id);
                            editor.putString("loggedInUserRoute", routeNo);
                            editor.apply();

                            Toast.makeText(DriverLoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(DriverLoginActivity.this, DriverMapsActivity.class);
                            intent.putExtra("driverId", id);
                            intent.putExtra("driverRoute", routeNo);
                            startActivity(intent);
                            finish();
                            finishAffinity();// Close the current activity to prevent going back with the back button
                        } else {
                            // Passwords do not match
                            Toast.makeText(DriverLoginActivity.this, "Invalid password", Toast.LENGTH_SHORT).show();
                            DPassword.setError("Incorrect Password");
                            DPassword.requestFocus();
                        }
                    }
                } else {
                    // User ID not found in the database
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(DriverLoginActivity.this, "Invalid ID", Toast.LENGTH_SHORT).show();
                    DID.setError("Invalid ID Number");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors here
                Log.e("InchargeLoginActivity", "Database error: " + databaseError.getMessage());
                Toast.makeText(DriverLoginActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}