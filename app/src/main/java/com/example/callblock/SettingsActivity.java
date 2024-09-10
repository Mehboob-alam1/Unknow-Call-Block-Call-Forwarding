package com.example.callblock;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SettingsActivity extends AppCompatActivity {

    private EditText etGreetings, etMidPrompt, etPrompt, etForwardingNumber;
    private Button btnUpdate;

    private ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        etGreetings = findViewById(R.id.etGreetings);
        etMidPrompt = findViewById(R.id.etMidPrompt);
        etPrompt = findViewById(R.id.etPrompt);
        etForwardingNumber = findViewById(R.id.etForwardingNumber);

        btnUpdate = findViewById(R.id.btnUpdate);


        callToDataBase();


    }

    private void callToDataBase() {

        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String phoneNumber = sharedPreferences.getString("PHONE_NUMBER", null);
        DatabaseReference db = FirebaseDatabase.getInstance().getReference("user").child(phoneNumber).child("settings");


        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {

                    Settings settings = snapshot.getValue(Settings.class);
                    assert settings != null;
                    etGreetings.setText(settings.getGreeting());
                    etMidPrompt.setText(settings.getMidPrompt());
                    etPrompt.setText(settings.getCompletePrompt());
                    etForwardingNumber.setText(settings.getForwardingNumber());
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        btnUpdate.setOnClickListener(v -> {

            updateValues();
        });

    }

    private void updateValues() {

        if (etForwardingNumber.getText().toString().isEmpty()) {
            etForwardingNumber.setError("Field required");
        } else if (etGreetings.getText().toString().isEmpty()) {
            etGreetings.setError("Field required");
        } else if (etPrompt.getText().toString().isEmpty()) {
            etPrompt.setError("Field required");
        } else if (etMidPrompt.getText().toString().isEmpty()) {
            etMidPrompt.setError("Field required");
        } else {


            dialog= new ProgressDialog(this);
            dialog.setMessage("Please wait");
            dialog.setCancelable(false);
            dialog.show();

            // Update Shared Pref
            SharedPreferences forwardPref = getSharedPreferences("CallForwardPrefs", MODE_PRIVATE);
            forwardPref.edit().putString("forwardingNumber", etForwardingNumber.getText().toString()).apply();

            // Get updated value from pref
            SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
            String phoneNumber = sharedPreferences.getString("PHONE_NUMBER", null);
            DatabaseReference db = FirebaseDatabase.getInstance().getReference("user").child(phoneNumber).child("settings");


            Settings settings= new Settings(etPrompt.getText().toString(),etForwardingNumber.getText().toString(),etGreetings.getText().toString(),etMidPrompt.getText().toString());

            db.setValue(settings)
                    .addOnCompleteListener(task -> {
                        if (task.isComplete() && task.isSuccessful()){
                            dialog.dismiss();
                            Toast.makeText(this, "Values updated", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }).addOnFailureListener(e -> {
                        dialog.dismiss();

                    });


        }


    }


}