package com.example.busmobileapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.busmobileapp.Models.studentrecmodel;
import com.example.busmobileapp.R;
import com.example.busmobileapp.Student.BusPassActivity;
import com.example.busmobileapp.Student.ProfileActivity;
import com.example.busmobileapp.Student.StudentMapsActivity;
import com.example.busmobileapp.StudentLoginActivity;
import com.example.busmobileapp.StudentMainActivity;
import com.example.busmobileapp.ViewHolders.stuRecViewHolder;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class studentRecAdapter extends RecyclerView.Adapter<stuRecViewHolder> {
    ArrayList<studentrecmodel> data;
    Context context;
    FirebaseAuth auth;

    public studentRecAdapter(ArrayList<studentrecmodel> data, Context context, FirebaseAuth auth) {
        this.context = context;
        this.data = data;
        this.auth = auth; // Initialize FirebaseAuth instance
    }

    @NonNull
    @Override
    public stuRecViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.single_item, parent, false);
        return new stuRecViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull stuRecViewHolder holder, int position) {
        final studentrecmodel temp = data.get(position);
        holder.t1.setText(data.get(position).getHeader());
        holder.img.setImageResource(data.get(position).getImgname());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.t1.getText().equals("Profile")) {
                    Intent intent = new Intent(context, ProfileActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                } else if (holder.t1.getText().equals("Bus Location")) {
                    Intent intent = new Intent(context, StudentMapsActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
                else if (holder.t1.getText().equals("Bus Pass")) {
                    Intent intent = new Intent(context, BusPassActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
                else if (holder.t1.getText().equals("Logout")) {
                    auth.signOut();
                    Toast.makeText(context,"Logged out", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(context,StudentLoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK
                            | Intent.FLAG_ACTIVITY_NEW_TASK);
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
