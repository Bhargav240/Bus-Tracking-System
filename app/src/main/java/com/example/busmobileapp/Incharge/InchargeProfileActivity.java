package com.example.busmobileapp.Incharge;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.busmobileapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class InchargeProfileActivity extends AppCompatActivity {
    private TextView inRouteT, inIDT;
    private EditText newPassword1, newPassword2;
    private Button updatePasswordButton;
    private ProgressBar progressBar;

    private DatabaseReference inchargeRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incharge_profile);

        inRouteT = findViewById(R.id.inbus);
        inIDT = findViewById(R.id.inId);

        // Initialize Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        inchargeRef = database.getReference("Incharges");
        progressBar = findViewById(R.id.progressBari);
        newPassword1 = findViewById(R.id.newpass1);
        newPassword2 = findViewById(R.id.newpass2);
        updatePasswordButton = findViewById(R.id.reset);

        updatePasswordButton.setOnClickListener(view -> {
            String newPassword = newPassword1.getText().toString();
            String confirmPassword = newPassword2.getText().toString();

            if (!newPassword.isEmpty() && !confirmPassword.isEmpty()) {
                if (newPassword.equals(confirmPassword)) {
                    updatePassword(newPassword);
                } else {
                    Toast.makeText(InchargeProfileActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    newPassword2.setError("Passwords do not match");
                    newPassword2.requestFocus();
                }
            } else {
                Toast.makeText(InchargeProfileActivity.this, "Please enter both passwords", Toast.LENGTH_SHORT).show();
                newPassword1.setError("Enter Passwords");
                newPassword1.requestFocus();
            }
        });

        Intent intent = getIntent();
        if (intent != null) {
            String inID = intent.getStringExtra("InID");
            String inRoute = intent.getStringExtra("InRoute");
            if (inID != null && inRoute != null) {
                inIDT.setText(inID);
                inRouteT.setText(inRoute);
            }
        }
    }

    private void updatePassword(String newPassword) {

        progressBar.setVisibility(View.VISIBLE);
        // Update the password in the database
        String inchargeKey = "Incharge" + inRouteT.getText().toString();
        inchargeRef.child(inchargeKey).child("Password").setValue(newPassword)
                .addOnCompleteListener(new OnCompleteListener<Void>() {

                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            Toast.makeText(InchargeProfileActivity.this, "Password updated successfully", Toast.LENGTH_LONG).show();
                            newPassword1.getText().clear();
                            newPassword2.getText().clear();
                        } else {
                            Toast.makeText(InchargeProfileActivity.this, "Failed to update password", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}
