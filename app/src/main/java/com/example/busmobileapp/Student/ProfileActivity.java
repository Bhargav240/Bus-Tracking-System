package com.example.busmobileapp.Student;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.busmobileapp.R;
import com.example.busmobileapp.ReadWriteUserDetails;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    ProgressBar progressBar;
    private TextView name,email,phone,roll,route,stop,bstatus;

    private String fullname,fullemail,phonenumber,fullroll,fullroute,fullstop,fullstatus;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        name = (TextView) findViewById(R.id.displayname);
        email = (TextView) findViewById(R.id.displaymail);
        roll =(TextView) findViewById(R.id.displayroll);
        phone = (TextView) findViewById(R.id.displayph);
        route =(TextView) findViewById(R.id.displayroute);
        stop =(TextView) findViewById(R.id.displaystop);
        bstatus =(TextView) findViewById(R.id.statusbus);
        progressBar=(ProgressBar) findViewById(R.id.progressBarD) ;

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        if(firebaseUser==null){
            Toast.makeText(this, "Something went Wrong, User details not available at the moment", Toast.LENGTH_SHORT).show();
        }
        else{
            progressBar.setVisibility(View.VISIBLE);
            showUserProfile(firebaseUser);
        }

    }

    private void showUserProfile(FirebaseUser firebaseUser) {

        String userId = firebaseUser.getUid();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Students");
        reference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ReadWriteUserDetails readUserDetails = snapshot.getValue(ReadWriteUserDetails.class);
                if(readUserDetails != null){
                    fullname = firebaseUser.getDisplayName();
                    fullemail = firebaseUser.getEmail();
                    phonenumber= readUserDetails.phnum;
                    fullroll = readUserDetails.roll;
                    fullroute = readUserDetails.route;
                    fullstop = readUserDetails.stop;
                    fullstatus = readUserDetails.status;

                    name.setText(fullname);
                    email.setText(fullemail);
                    phone.setText(phonenumber);
                    roll.setText(fullroll);
                    route.setText(fullroute);
                    stop.setText(fullstop);
                    bstatus.setText(fullstatus);

                }else{
                    Toast.makeText(ProfileActivity.this, "Something Went Wrong!!", Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Something Went Wrong!!", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}