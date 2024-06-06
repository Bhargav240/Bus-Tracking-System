package com.example.busmobileapp.Incharge;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.busmobileapp.Adapters.InSudentsRecAdapter;
import com.example.busmobileapp.R;
import com.example.busmobileapp.ReadWriteUserDetails;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class InchargeStudentsActivity extends AppCompatActivity {

    private TextView busnum,total;
    private RecyclerView rcv;
    private SearchView searchView;
    private List<ReadWriteUserDetails> list;
    private DatabaseReference databaseReference;
    private InSudentsRecAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incharge_students);

        // Initialize UI components
        rcv = findViewById(R.id.isturecv);
        total = findViewById(R.id.totalstu);
        busnum = findViewById(R.id.busnum);
        searchView = findViewById(R.id.searchView);
        searchView.clearFocus();

        // Get bus number from Intent
        Intent intent = getIntent();
        String busnums = null;
        if (intent != null) {
            busnums = intent.getStringExtra("InRoute");
            if (busnum != null) {
                busnum.setText(busnums);
            }
        }

        // Initialize RecyclerView and Adapter
        rcv.setLayoutManager(new LinearLayoutManager(this));
        list = new ArrayList<>();
        adapter = new InSudentsRecAdapter(this, list);
        rcv.setAdapter(adapter);

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("Students");

        // Set up SearchView listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchList(newText);
                return true;
            }
        });


        // Set up ValueEventListener for Firebase Database
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                String busnums = busnum.getText().toString().trim();

                // Iterate through Firebase snapshot and add matching items to the list
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ReadWriteUserDetails r = dataSnapshot.getValue(ReadWriteUserDetails.class);

                    if (r != null && r.getRoute().equals(busnums)) {
                        list.add(r);
                    }
                }
                int totalSize = list.size();
                total.setText(String.valueOf(totalSize));
                // Notify adapter of data change
                adapter.notifyDataSetChanged();
                Log.d("Firebase", "Data changed. Filtered list size: " + list.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle errors, if any
            }
        });
    }
    public void searchList(String text){
        ArrayList<ReadWriteUserDetails> searchList = new ArrayList<>();
        for (ReadWriteUserDetails dataClass: list){
            if (dataClass.getRoll().toLowerCase().contains(text.toLowerCase())){
                searchList.add(dataClass);
            }
        }
        adapter.searchDataList(searchList);
    }
}
