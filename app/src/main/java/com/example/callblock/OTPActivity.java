package com.example.callblock;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class OTPActivity extends AppCompatActivity implements View.OnClickListener {
    EditText mob_no;
    Button submit;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otpactivity);

        mob_no = findViewById(R.id.etPhone);
        submit = findViewById(R.id.btnSignIn);

        // Set +91 as the prefix
        mob_no.setText("+91 ");

        // Move the cursor after the +91 prefix
        mob_no.setSelection(mob_no.getText().length());

        // Set the button's click listener
        submit.setOnClickListener(this);

        // Prevent user from deleting the +91 prefix
        mob_no.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                // Ensure +91 is always there and can't be deleted
                if (!charSequence.toString().startsWith("+91 ")) {
                    mob_no.setText("+91 ");
                    mob_no.setSelection(mob_no.getText().length());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    @Override
    public void onClick(View view) {
        String phoneNumber = mob_no.getText().toString().trim();

        // Check if the length of the entered phone number (excluding +91) is valid
        if (phoneNumber.length() > 4) {
            // Get only the part after +91 for validation
            String userPhonePart = phoneNumber.substring(4); // Skip "+91 "

            // Check if the remaining part is a valid Indian phone number
            if (isValidIndianPhoneNumber(userPhonePart)) {
                savePhoneNumberToSharedPref(phoneNumber);
                navigateToMainActivity();
            } else {
                Toast.makeText(OTPActivity.this, "Please enter a valid Indian phone number", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(OTPActivity.this, "Please enter a 10-digit phone number after +91", Toast.LENGTH_SHORT).show();
        }
    }

    private void savePhoneNumberToSharedPref(String phoneNumber) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("PHONE_NUMBER", phoneNumber);
        editor.apply();
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(OTPActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String phoneNumber = sharedPreferences.getString("PHONE_NUMBER", null);

        if (phoneNumber != null) {
            // Phone number exists, go to MainActivity
            navigateToMainActivity();
        }
    }

    // Function to validate the 10-digit part of the phone number
    private boolean isValidIndianPhoneNumber(String phoneNumber) {

        if (phoneNumber.length()==10)
            return true;
        else
            return false;
    }
}
