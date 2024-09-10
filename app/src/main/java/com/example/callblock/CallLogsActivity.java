package com.example.callblock;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CallLogsActivity extends AppCompatActivity {

    private DatabaseReference db;
    private RecyclerView recyclerView;
    private LogsAdapter adapter;
    private ArrayList<CallLog> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_logs);

        recyclerView = findViewById(R.id.recyclerView);
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String phoneNumber = sharedPreferences.getString("PHONE_NUMBER", null);
        db = FirebaseDatabase.getInstance().getReference("user").child(phoneNumber).child("callLogs");

        list = new ArrayList<>();
        adapter = new LogsAdapter(this, list);

        readCallLogs();

    }

    private void readCallLogs() {

        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    for (DataSnapshot snap : snapshot.getChildren()) {


                        CallLog log = snap.getValue(CallLog.class);

                        list.add(log);


                    }
                    adapter.setList(list);
                    adapter.notifyDataSetChanged();

                    recyclerView.setLayoutManager(new LinearLayoutManager(CallLogsActivity.this));
                    recyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}