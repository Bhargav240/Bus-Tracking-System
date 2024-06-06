package com.example.busmobileapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.example.busmobileapp.Adapters.InchargeRecAdapter;
import com.example.busmobileapp.Models.InchargerecModel;

import java.util.ArrayList;

public class InchargeMainActivity extends AppCompatActivity {
    private RecyclerView rcv;
    private TextView inRouteText;
    private TextView inIDText;
    private InchargeRecAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incharge_main);
        rcv = findViewById(R.id.recycleViewIncharge);
        inRouteText = findViewById(R.id.InchargeRoute);
        inIDText = findViewById(R.id.inchargeId);

        Intent intent = getIntent();
        if (intent != null) {
            String inID = intent.getStringExtra("InchargeInUserID");
            String inRoute = intent.getStringExtra("InchargeInUserRoute");
            if (inID != null && inRoute != null) {
                inIDText.setText( inID);
                inRouteText.setText( inRoute );
            }
        }

        adapter = new InchargeRecAdapter(dataqueue(), getApplicationContext(), inRouteText.getText().toString(), inIDText.getText().toString());
        rcv.setAdapter(adapter);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        rcv.setLayoutManager(gridLayoutManager);
    }

    public ArrayList<InchargerecModel> dataqueue() {
        ArrayList<InchargerecModel> holder = new ArrayList<>();

        InchargerecModel ob1 = new InchargerecModel();
        ob1.setHeader("Reset Password");
        ob1.setImgname(R.drawable.resetpass);
        holder.add(ob1);
        InchargerecModel ob2 = new InchargerecModel();
        ob2.setHeader("Bus Location");
        ob2.setImgname(R.drawable.location);
        holder.add(ob2);
        InchargerecModel ob3 = new InchargerecModel();
        ob3.setHeader("Students");
        ob3.setImgname(R.drawable.busstudents);
        holder.add(ob3);
        InchargerecModel ob4 = new InchargerecModel();
        ob4.setHeader("Logout");
        ob4.setImgname(R.drawable.logout);
        holder.add(ob4);

        return holder;
    }
}
