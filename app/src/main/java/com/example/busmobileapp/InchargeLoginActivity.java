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

public class InchargeLoginActivity extends AppCompatActivity {

    Button Ilogin;
    ProgressBar progressBar;
    private EditText IID, IPassword;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incharge_login);
        IID = findViewById(R.id.inUserId);
        IPassword = findViewById(R.id.inpass);
        progressBar = findViewById(R.id.progressBar);
        databaseReference = FirebaseDatabase.getInstance().getReference("Incharges");

        // Check for existing login


        Ilogin = findViewById(R.id.buttonILogin);
        Ilogin.setOnClickListener(v -> login());
    }

    public void login() {
        final String id = IID.getText().toString().trim();
        final String password = IPassword.getText().toString().trim();

        if (TextUtils.isEmpty(id)) {
            IID.setError("ID Number is Required");
            IID.requestFocus();
            return;
        } else if (TextUtils.isEmpty(password)) {
            IPassword.setError("Password is Required");
            IPassword.requestFocus();
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
                            SharedPreferences preferences = getSharedPreferences("Incharge_data", MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString("InchargeInUserID", id);
                            editor.putString("InchargeInUserRoute", routeNo);
                            editor.apply();

                            Toast.makeText(InchargeLoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(InchargeLoginActivity.this, InchargeMainActivity.class);
                            intent.putExtra("InchargeInUserID", id);
                            intent.putExtra("InchargeInUserRoute", routeNo);
                            startActivity(intent);
                            finish();
                            finishAffinity();// Close the current activity to prevent going back with the back button
                        } else {
                            // Passwords do not match
                            Toast.makeText(InchargeLoginActivity.this, "Invalid password", Toast.LENGTH_SHORT).show();
                            IPassword.setError("Incorrect Password");
                            IPassword.requestFocus();
                        }
                    }
                } else {
                    // User ID not found in the database
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(InchargeLoginActivity.this, "Invalid ID", Toast.LENGTH_SHORT).show();
                    IID.setError("Invalid ID Number");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors here
                Log.e("InchargeLoginActivity", "Database error: " + databaseError.getMessage());
                Toast.makeText(InchargeLoginActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
