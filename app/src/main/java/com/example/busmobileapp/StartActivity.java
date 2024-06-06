package com.example.busmobileapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class StartActivity extends AppCompatActivity {

    Button student,incharge,driver;
    TextView register;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);


        // Check for existing login
        SharedPreferences preferences1 = getSharedPreferences("Incharge_data", MODE_PRIVATE);
        String InchargeInUserID = preferences1.getString("InchargeInUserID", "");
        String InchargeInUserRoute = preferences1.getString("InchargeInUserRoute", "");

        if (!TextUtils.isEmpty(InchargeInUserID)) {
            // User is already logged in, navigate to InchargeMainActivity
            Intent intent = new Intent(StartActivity.this, InchargeMainActivity.class);
            intent.putExtra("InchargeInUserID", InchargeInUserID);
            intent.putExtra("InchargeInUserRoute", InchargeInUserRoute);
            startActivity(intent);
            finish(); // Close the current activity
        }

        SharedPreferences preferences = getSharedPreferences("user_data", MODE_PRIVATE);
        String loggedInUserID = preferences.getString("loggedInUserID", "");
        String loggedInUserRoute = preferences.getString("loggedInUserRoute", "");

        if (!TextUtils.isEmpty(loggedInUserID)) {
            // User is already logged in, navigate to InchargeMainActivity
            Intent intent = new Intent(StartActivity.this, DriverMapsActivity.class);
            intent.putExtra("driverId", loggedInUserID);
            intent.putExtra("driverRoute", loggedInUserRoute);
            startActivity(intent);
            finish(); // Close the current activity
        }

        student = (Button) findViewById(R.id.button11);
        incharge = (Button) findViewById(R.id.button2);
        driver = (Button) findViewById(R.id.button3);
        register = (TextView) findViewById(R.id.textViewregister);

        student.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =new Intent(StartActivity.this,StudentLoginActivity.class);
                startActivity(intent);
            }
        });
        incharge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =new Intent(StartActivity.this,InchargeLoginActivity.class);
                startActivity(intent);
            }
        });
        driver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =new Intent(StartActivity.this,DriverLoginActivity.class);
                startActivity(intent);

            }
        });
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =new Intent(StartActivity.this,StudentRegisterActivity.class);
                startActivity(intent);

            }
        });

    }

}