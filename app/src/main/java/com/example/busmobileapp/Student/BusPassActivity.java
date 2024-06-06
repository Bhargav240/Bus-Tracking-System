package com.example.busmobileapp.Student;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.ScrollView;
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

public class BusPassActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private FirebaseAuth auth;
    ProgressBar progressBar;
    SwipeRefreshLayout swipeRefreshLayout;

    private TextView name,phone,roll,route,stop,bstatus;

    CardView cardView;

    private String fullname,phonenumber,fullroll,fullroute,fullstop,fullstatus;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_pass);

        name = (TextView) findViewById(R.id.stuname);
        roll =(TextView) findViewById(R.id.stunroll);
        route = (TextView) findViewById(R.id.stubusnum);
        stop =(TextView) findViewById(R.id.stustop);
        phone =(TextView) findViewById(R.id.stuph);
        bstatus =(TextView) findViewById(R.id.status);
        progressBar=(ProgressBar) findViewById(R.id.progressBarB) ;
        cardView = findViewById(R.id.busspasscard);
        swipeRefreshLayout = findViewById(R.id.swipe);
        swipeRefreshLayout.setOnRefreshListener(this);

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        if(firebaseUser==null){
            Toast.makeText(this, "Something went Wrong, User details not available at the moment", Toast.LENGTH_SHORT).show();
        }
        else{
            progressBar.setVisibility(View.VISIBLE);
            showUserPass(firebaseUser);
        }

    }

    private void showUserPass(FirebaseUser firebaseUser) {

        String userId = firebaseUser.getUid();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Students");
        reference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ReadWriteUserDetails readUserDetails = snapshot.getValue(ReadWriteUserDetails.class);
                if(readUserDetails != null){
                    fullname = firebaseUser.getDisplayName();

                    phonenumber= readUserDetails.phnum;
                    fullroll = readUserDetails.roll;
                    fullroute = readUserDetails.route;
                    fullstop = readUserDetails.stop;
                    fullstatus = readUserDetails.status;
                    name.setText(fullname);
                    phone.setText(phonenumber);
                    roll.setText(fullroll);
                    route.setText(fullroute);
                    stop.setText(fullstop);
                    bstatus.setText(fullstatus);
                    String textFromTextView = bstatus.getText().toString();

                    if ("VALID".equals(textFromTextView))
                    {
                        cardView.setCardBackgroundColor(Color.parseColor("#A4F14A")); // Replace "#FF5733" with your desired color code
                    }
                    else {
                        cardView.setCardBackgroundColor(Color.parseColor("#FFEB3B")); // Replace "#FF5733" with your desired color code
                    }

                }else{
                    Toast.makeText(BusPassActivity.this, "Something Went Wrong!!", Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(BusPassActivity.this, "Something Went Wrong!!", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    public void onRefresh() {
        progressBar.setVisibility(View.VISIBLE);
        showUserPass(firebaseUser);
    }
}