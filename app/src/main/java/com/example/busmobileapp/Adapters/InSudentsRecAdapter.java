package com.example.busmobileapp.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.busmobileapp.R;
import com.example.busmobileapp.ReadWriteUserDetails;
import com.example.busmobileapp.ViewHolders.InStudentsViewHolder;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class InSudentsRecAdapter extends RecyclerView.Adapter<InStudentsViewHolder>   {
    private Context context;
    private List<ReadWriteUserDetails> list;

    public InSudentsRecAdapter(Context context, List<ReadWriteUserDetails> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public InStudentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.student_item, parent, false);
        return new InStudentsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InStudentsViewHolder holder, int position) {
        ReadWriteUserDetails userDetails = list.get(position);
        holder.isname.setText(userDetails.getName());
        holder.isroll.setText(userDetails.getRoll());
        holder.isstop.setText(userDetails.getStop());
        holder.ispass.setText(userDetails.getStatus());
        holder.isph.setText(userDetails.getPhnum());
        Log.d("Adapter", "onBindViewHolder called for position: " + position + ", Name: " + userDetails.getName());

        holder.approve.setOnClickListener(view -> {
            // Update the status in the local list
            userDetails.setStatus("VALID");
            // Update the status in the Firebase Realtime Database using email
            updateStatusInDatabaseByEmail(userDetails.getEmail(), "VALID");
        });

        holder.disapprove.setOnClickListener(view -> {
            // Update the status in the local list
            userDetails.setStatus("INVALID");
            // Update the status in the Firebase Realtime Database using email
            updateStatusInDatabaseByEmail(userDetails.getEmail(), "INVALID");
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private void updateStatusInDatabaseByEmail(String userEmail, String newStatus) {
        DatabaseReference studentsRef = FirebaseDatabase.getInstance().getReference("Students");
        studentsRef.orderByChild("email").equalTo(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot dataSnapshot) {
                for (com.google.firebase.database.DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String userKey = userSnapshot.getKey();
                    DatabaseReference userRef = studentsRef.child(userKey).child("status");
                    userRef.setValue(newStatus);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors, if any
            }
        });
    }

    public void searchDataList(ArrayList<ReadWriteUserDetails> searchList){
        list = searchList;
        notifyDataSetChanged();
    }
}
