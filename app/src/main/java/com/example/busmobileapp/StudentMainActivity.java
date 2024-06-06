package com.example.busmobileapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.busmobileapp.Adapters.studentRecAdapter;
import com.example.busmobileapp.Models.studentrecmodel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class StudentMainActivity extends AppCompatActivity {
    RecyclerView rcv;
    private ProgressBar progressBar;
    studentRecAdapter adapter;
    private FirebaseAuth auth;
    private TextView welname;

    private String fullname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rcv = findViewById(R.id.recycleViewStudent);
        progressBar=(ProgressBar) findViewById(R.id.progressBarStum);
        welname =(TextView) findViewById(R.id.stwelname);

        adapter = new studentRecAdapter(dataqueue(), getApplicationContext(), FirebaseAuth.getInstance());
        rcv.setAdapter(adapter);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        rcv.setLayoutManager(gridLayoutManager);
        //Email Verification of user
        auth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if(!firebaseUser.isEmailVerified()){
            firebaseUser.sendEmailVerification();
            auth.signOut();
            showAlertDialog();
        }else {
            showUserProfile(firebaseUser);
        }
    }

    public ArrayList<studentrecmodel> dataqueue() {
        ArrayList<studentrecmodel> holder = new ArrayList<>();

        studentrecmodel ob1 = new studentrecmodel();
        ob1.setHeader("Profile");
        ob1.setImgname(R.drawable.profile2);
        holder.add(ob1);
        studentrecmodel ob2 = new studentrecmodel();
        ob2.setHeader("Bus Location");
        ob2.setImgname(R.drawable.location);
        holder.add(ob2);
        studentrecmodel ob3 = new studentrecmodel();
        ob3.setHeader("Bus Pass");
        ob3.setImgname(R.drawable.buspass);
        holder.add(ob3);
        studentrecmodel ob4 = new studentrecmodel();
        ob4.setHeader("Logout");
        ob4.setImgname(R.drawable.logout);
        holder.add(ob4);


        return holder;
    }
    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(StudentMainActivity.this);
        builder.setTitle("Email Not Verified");
        builder.setMessage("Please Verify Your Email now. You will be not able to access until you verify your email.");
        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent i = new Intent(Intent.ACTION_MAIN);
                i.addCategory(Intent.CATEGORY_APP_EMAIL);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                finishAffinity();

            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                // Prevent dialog close on back press button
                return keyCode == KeyEvent.KEYCODE_BACK;
            }
        });
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

                    welname.setText(fullname+"!");

                }else{
                    Toast.makeText(StudentMainActivity.this, "Something Went Wrong!!", Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StudentMainActivity.this, "Something Went Wrong!!", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}
