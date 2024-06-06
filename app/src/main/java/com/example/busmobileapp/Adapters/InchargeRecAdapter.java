package com.example.busmobileapp.Adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.busmobileapp.Incharge.InchargeMapsActivity;
import com.example.busmobileapp.Incharge.InchargeProfileActivity;
import com.example.busmobileapp.Incharge.InchargeStudentsActivity;
import com.example.busmobileapp.InchargeMainActivity;
import com.example.busmobileapp.Models.InchargerecModel;
import com.example.busmobileapp.R;
import com.example.busmobileapp.StartActivity;
import com.example.busmobileapp.Student.BusPassActivity;
import com.example.busmobileapp.ViewHolders.InchargeViewHolder;

import java.util.ArrayList;

public class InchargeRecAdapter extends RecyclerView.Adapter<InchargeViewHolder> {
    ArrayList<InchargerecModel> data;
    Context context;

    private String inRoute;
    private String inID;

    public InchargeRecAdapter(ArrayList<InchargerecModel> data, Context context, String inRoute, String inID) {
        this.context = context;
        this.data = data;
        this.inRoute = inRoute;
        this.inID = inID;
    }

    @NonNull
    @Override
    public InchargeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.single_item, parent, false);
        return new InchargeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InchargeViewHolder holder, int position) {
        final InchargerecModel temp = data.get(position);
        holder.t1.setText(data.get(position).getHeader());
        holder.img.setImageResource(data.get(position).getImgname());

        // Set click listener for RecyclerView item
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the text from holder.t1 for comparison
                String buttonText = holder.t1.getText().toString();

                if ("Reset Password".equals(buttonText)) {
                    //  for Reset Password
                    Intent intent = new Intent(context, InchargeProfileActivity.class);
                    intent.putExtra("InRoute", inRoute);
                    intent.putExtra("InID", inID);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);

                } else if ("Bus Location".equals(buttonText) ) {
                    // Intent for Bus Location or Bus Pass
                    Intent intent = new Intent(context, InchargeMapsActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);

                } else if ("Students".equals(buttonText)) {
                    Intent intent = new Intent(context, InchargeStudentsActivity.class);
                    intent.putExtra("InRoute", inRoute);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);

                }
                else if ("Logout".equals(buttonText)) {
                    // Logout logic
                    SharedPreferences preferences = context.getSharedPreferences("Incharge_data", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.clear();
                    editor.apply();

                    // Redirect to the StartActivity
                    Intent intent = new Intent(context, StartActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);

                }
            }
        });
    }



    @Override
    public int getItemCount() {
        return data.size();
    }
}