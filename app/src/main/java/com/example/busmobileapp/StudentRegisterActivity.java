package com.example.busmobileapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.busmobileapp.Incharge.InchargeProfileActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StudentRegisterActivity extends AppCompatActivity {

    EditText stuname,stumail,stuphnm,sturoll,spass1,spass2;
    Button register;
    private ProgressBar progressBar;

    Spinner RouteSpinner, StopSpinner;
    String selectedRoute,selectedStop;
    String PassS = "Invalid";
    DatabaseReference routeref;
    FirebaseAuth auth;
    ArrayList<String> routesSpinner, stopsSpinner;
    ArrayAdapter<String> RouteAdapter, StopAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_register);
        //student details
        stuname =(EditText) findViewById(R.id.studentname);
        stumail = (EditText) findViewById(R.id.stuemail);
        stuphnm = (EditText) findViewById(R.id.stuphone);
        sturoll = (EditText) findViewById(R.id.sturollno);
        spass1 = (EditText) findViewById(R.id.spassword1);
        spass2 = (EditText) findViewById(R.id.spassword2);
        progressBar = (ProgressBar) findViewById(R.id.progressBarStu);
        register = (Button) findViewById(R.id.buttonregisters);

        RouteSpinner = findViewById(R.id.spinner_routes);
        StopSpinner = findViewById(R.id.spinner_stop);

        routeref = FirebaseDatabase.getInstance().getReference("BusRoutes");
        routesSpinner = new ArrayList<>();
        stopsSpinner = new ArrayList<>();

        RouteAdapter = new ArrayAdapter<>(StudentRegisterActivity.this, R.layout.spinner_layout, routesSpinner);
        StopAdapter = new ArrayAdapter<>(StudentRegisterActivity.this, R.layout.spinner_layout, stopsSpinner);

        RouteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        StopAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        RouteSpinner.setAdapter(RouteAdapter);
        StopSpinner.setAdapter(StopAdapter);

        routesSpinner.add(0, "Select Your Route");
        stopsSpinner.add(0, "Select Your Stop");

        RouteSpinner.setSelection(0);
        StopSpinner.setSelection(0);

        routeref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                routesSpinner.clear(); // Clear existing items
                routesSpinner.add(0, "Select Your Route");

                for (DataSnapshot item : snapshot.getChildren()) {
                    routesSpinner.add(item.getKey());
                }

                RouteAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        RouteSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedRoute = RouteSpinner.getSelectedItem().toString();

                // Fetch stops for the selected route
                routeref.child(selectedRoute).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        stopsSpinner.clear(); // Clear existing items
                        stopsSpinner.add(0, "Select Your Stop");

                        for (DataSnapshot stop : snapshot.getChildren()) {
                            stopsSpinner.add(stop.getKey());
                        }

                        StopAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        StopSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedStop = StopSpinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fname = stuname.getText().toString();
                String femail = stumail.getText().toString();
                String pnum = stuphnm.getText().toString();
                String froute = selectedRoute;
                String fstop = selectedStop;
                String froll = sturoll.getText().toString();
                String pass1 = spass1.getText().toString();
                String pass2 = spass2.getText().toString();
                String fbuspass = PassS;

                String mobile = "[6-9][0-9]{9}";
                Matcher mobilematcher;
                Pattern mobilePattern = Pattern.compile(mobile);
                mobilematcher = mobilePattern.matcher(pnum);

                if (TextUtils.isEmpty(fname)) {
                    Toast.makeText(StudentRegisterActivity.this, "Please Enter your Name", Toast.LENGTH_LONG).show();
                    stuname.setError("Name is Required");
                    stuname.requestFocus();
                } else if (TextUtils.isEmpty(femail)) {
                    Toast.makeText(StudentRegisterActivity.this, "Please Enter your Email", Toast.LENGTH_LONG).show();
                    stumail.setError("Email is Required");
                    stumail.requestFocus();
                }
                String emailPattern = "\\d{10}@klh.edu.in";
                if (!femail.matches(emailPattern)) {
                    Toast.makeText(StudentRegisterActivity.this, "Invalid email format", Toast.LENGTH_LONG).show();
                    stumail.setError("Invalid email format");
                    stumail.requestFocus();
                    return;
                }

                // Check if the number in the email matches the number in sturoll
                String[] emailParts = femail.split("@");
                String emailNumber = emailParts[0];

                if (!emailNumber.equals(froll)) {
                    Toast.makeText(StudentRegisterActivity.this, "Email number and Roll number do not match", Toast.LENGTH_LONG).show();
                    stumail.setError("Email number and Roll number do not match");
                    stumail.requestFocus();
                } else if (TextUtils.isEmpty(pnum)) {
                    Toast.makeText(StudentRegisterActivity.this, "Please Enter your Phone Number", Toast.LENGTH_LONG).show();
                    stuphnm.setError("Phone Number is Required");
                    stuphnm.requestFocus();
                } else if (!mobilematcher.find()) {
                    Toast.makeText(StudentRegisterActivity.this, "Please Enter Valid Phone Number", Toast.LENGTH_LONG).show();
                    stuphnm.setError("Phone Number is not Valid");
                    stuphnm.requestFocus();

                } else if (froute.equals("Select Your Route")) {
                    Toast.makeText(StudentRegisterActivity.this, "Please select a route", Toast.LENGTH_SHORT).show();
                    // Set an error on the RouteSpinner
                    ((TextView) RouteSpinner.getSelectedView()).setError("Route is Required");

                } else if (fstop.equals("Select Your Stop")) {
                    Toast.makeText(StudentRegisterActivity.this, "Please select a stop", Toast.LENGTH_SHORT).show();
                    // Set an error on the StopSpinner
                    ((TextView) StopSpinner.getSelectedView()).setError("Stop is Required");
                } else if (TextUtils.isEmpty(froll)) {
                    Toast.makeText(StudentRegisterActivity.this, "Please Enter Your ID Number", Toast.LENGTH_LONG).show();
                    sturoll.setError("ID Number is Required");
                    sturoll.requestFocus();
                } else if (TextUtils.isEmpty(pass1)) {
                    Toast.makeText(StudentRegisterActivity.this, "Please Enter the Passsword", Toast.LENGTH_LONG).show();
                    spass1.setError("Password is Required");
                    spass1.requestFocus();
                } else if (TextUtils.isEmpty(pass2)) {
                    Toast.makeText(StudentRegisterActivity.this, "Please Enter Confirm Passsword", Toast.LENGTH_LONG).show();
                    spass2.setError("Confirm Password is Required");
                    spass2.requestFocus();
                } else if (!pass1.equals(pass2)) {
                    Toast.makeText(StudentRegisterActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    spass2.setError("Passwords do not match");
                    spass2.requestFocus();
                } else if (pass1.length() < 6) {
                    Toast.makeText(StudentRegisterActivity.this, "Passsword must be atleast 6 characters", Toast.LENGTH_LONG).show();
                    spass1.setError("Password too weak");
                    spass1.requestFocus();
                } else {
                    DatabaseReference studentsRef = FirebaseDatabase.getInstance().getReference("StuRollnumbers");
                    studentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.hasChild(froll)) {
                                // The number exists in Firebase data, proceed with registration
                                progressBar.setVisibility(View.VISIBLE);
                                registerUser(fname, femail, pnum, froute, fstop, froll, pass2, fbuspass);
                            } else {
                                // The number does not exist in Firebase data
                                Toast.makeText(StudentRegisterActivity.this, "Invalid Roll Number", Toast.LENGTH_LONG).show();
                                sturoll.setError("Invalid Roll Number");
                                sturoll.requestFocus();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // Handle onCancelled
                        }
                    });
                }
            }

        });

    }
    private void registerUser(String fname, String femail, String pnum, String froute, String fstop , String froll, String pass2 , String fbuspass) {

        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword(femail,pass2).addOnCompleteListener(StudentRegisterActivity.this,
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){

                            FirebaseUser firebaseUser = auth.getCurrentUser();

                            UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(fname).build();
                            firebaseUser.updateProfile(profileChangeRequest);

                            ReadWriteUserDetails writeUserDetails = new ReadWriteUserDetails(fname,femail,pnum,froute,fstop,froll,pass2,fbuspass);

                            DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference().child("Students");
                            referenceProfile.child(firebaseUser.getUid()).setValue(writeUserDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        firebaseUser.sendEmailVerification();
                                        Toast.makeText(StudentRegisterActivity.this,"User Registered Successfully,Please Verify Your email",Toast.LENGTH_LONG).show();
                                        Intent intent =new Intent(StudentRegisterActivity.this, StudentLoginActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                        finishAffinity();
                                    }
                                    else {
                                        Toast.makeText(StudentRegisterActivity.this,"User Registration Failed",Toast.LENGTH_LONG).show();

                                    }
                                    progressBar.setVisibility(View.GONE);

                                }
                            });

                        }
                        else{
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthWeakPasswordException e){
                                stuname.setError("Password too weak");
                                stuname.requestFocus();
                            }catch (FirebaseAuthInvalidCredentialsException e){
                                spass2.setError("Your email is invalid or already in use. Kindly Re-enter");
                                spass2.requestFocus();
                            }
                            catch(FirebaseAuthUserCollisionException e){
                                spass2.setError("User is already registered with this email.Use an other email");
                                spass2.requestFocus();
                            }
                            catch (Exception e){
                                Toast.makeText(StudentRegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
    }
}
