package com.example.callblock;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PERMISSIONS = 1001;
    private static final int REQUEST_CONTACTS_PERMISSIONS = 1;

    public static final String ACTION_USER_DELETED_ENTRY = "userDeletedEntry";


    private Switch switchCallForward;
    private TextView textCallForward;
    private EditText editPhoneNumber;
    ImageView btnCallLogs, btnSettings;
    private SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("CallForwardPrefs", MODE_PRIVATE);

        switchCallForward = findViewById(R.id.switchCallForward);
        textCallForward = findViewById(R.id.textCallForward);
        editPhoneNumber = new EditText(this);
        btnCallLogs = findViewById(R.id.btnCallLogs);
        btnSettings = findViewById(R.id.btnSettings);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            // If the permission is not granted, request it.
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.READ_CONTACTS,
                            android.Manifest.permission.WRITE_CONTACTS},
                    REQUEST_CONTACTS_PERMISSIONS);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALL_LOG) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ANSWER_PHONE_CALLS) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CALL_LOG, Manifest.permission.CALL_PHONE, Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS, Manifest.permission.WRITE_CALL_LOG, Manifest.permission.ANSWER_PHONE_CALLS},
                    REQUEST_CODE_PERMISSIONS);
        }


        saveSimNumber();

        btnSettings.setOnClickListener(v -> {


            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        });


        btnCallLogs.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, CallLogsActivity.class));
        });

        if (!getSimNumberFromSharedPreferences().equals("Unknown")) {

            new LoadContactsTask().execute();
        }
        registerLocalBroadcastReceiver();


        // Load saved number and set the switch to off initially
        String savedNumber = sharedPreferences.getString("forwardingNumber", "");


        switchCallForward.setChecked(false);

        // Handle switch toggle
        switchCallForward.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Forward calls to the saved number
                if (!savedNumber.isEmpty()) {
                    callforward("*21*" + savedNumber + "#");
                } else {
                    Toast.makeText(this, "Please set a forwarding number first.", Toast.LENGTH_SHORT).show();
                    switchCallForward.setChecked(false); // Turn off the switch if no number is set
                }
            } else {
                // Disable call forwarding
                callforward("#21#");
            }
        });


        // Handle text click to set forwarding number
        textCallForward.setOnClickListener(v -> {
            // Create a new EditText every time you open the dialog
            EditText editPhoneNumber = new EditText(this);


            // Set the saved number in the EditText if it exists
            if (!savedNumber.isEmpty()) {
                editPhoneNumber.setText(savedNumber);
                editPhoneNumber.setSelection(savedNumber.length()); // Move the cursor to the end of the text
            }
            new AlertDialog.Builder(this)
                    .setTitle("Set Forwarding Number")
                    .setView(editPhoneNumber)
                    .setPositiveButton("Save", (dialog, which) -> {
                        String inputNumber = editPhoneNumber.getText().toString().trim();
                        if (!inputNumber.isEmpty()) {
                            sharedPreferences.edit().putString("forwardingNumber", inputNumber).apply();
                            Toast.makeText(this, "Forwarding number saved.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Number cannot be empty.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
            }
        } else if (requestCode == REQUEST_CONTACTS_PERMISSIONS) {
            // If the request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted, proceed with your operation.
            } else {
                // Permission denied, disable the functionality that depends on this permission.
            }
            return;
        }
    }

    private void registerLocalBroadcastReceiver() {
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
        localBroadcastManager.registerReceiver(receiver, new IntentFilter(ACTION_USER_DELETED_ENTRY));
    }


    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        }
    };


    private class LoadContactsTask extends AsyncTask<Void, Void, List<Contact>> {

        @SuppressLint("Range")
        @Override
        protected List<Contact> doInBackground(Void... voids) {

            ArrayList<Contact> list = new ArrayList<>();


            ContentResolver contentResolver = getContentResolver();
            Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null); //TODO/permission issue
            assert cursor != null;
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    @SuppressLint("Range")
                    String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                    if (cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                        Cursor cursorInfo = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);
                        InputStream inputStream = ContactsContract.Contacts.openContactPhotoInputStream(getContentResolver(),
                                ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.parseLong(id)));

                        Uri person = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.parseLong(id));
                        Uri pURI = Uri.withAppendedPath(person, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);

                        Bitmap photo = null;
                        if (inputStream != null) {
                            photo = BitmapFactory.decodeStream(inputStream);
                        }
                        while (cursorInfo != null && cursorInfo.moveToNext()) {
                            Contact info = new Contact();

                            info.setId(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID)));
                            info.setName(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
                            info.setPhoneNumber(cursorInfo.getString(cursorInfo.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));

                            list.add(info);
                        }

                        cursorInfo.close();
                    }
                }
                cursor.close();
            }
            return list;
        }

        @Override
        protected void onPostExecute(List<Contact> contacts) {
            SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
            String phoneNumber = sharedPreferences.getString("PHONE_NUMBER", null);
            DatabaseReference db = FirebaseDatabase.getInstance().getReference("user").child(phoneNumber).child("Contacts");

            // Write contacts to Firebase Realtime Database
            for (Contact contact : contacts) {
                db.child(contact.getId()).setValue(contact);
            }


            // uploadMessages();


        }
    }


    public String getAndroidID() {
        return Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    private void saveSimNumber() {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            @SuppressLint("HardwareIds")
            String simNumber = telephonyManager.getLine1Number();

            Log.d("PHONE", simNumber);
            Toast.makeText(this, "SIM NUMBER" + simNumber, Toast.LENGTH_LONG).show();
            //String simNumber = telephonyManager.gegettSimSerialNumber(); // Get the SIM number
            if (simNumber != null) {
                SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("SIM_NUMBER", simNumber);
                editor.apply();

            } else {
            }
        }
    }

    private void callforward(String callForwardString) {
        PhoneCallListener phoneListener = new PhoneCallListener();
        TelephonyManager telephonyManager = (TelephonyManager)
                this.getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);

        Intent intentCallForward = new Intent(Intent.ACTION_CALL);
        Uri mmiCode = Uri.fromParts("tel", callForwardString, "#");
        intentCallForward.setData(mmiCode);
        startActivity(intentCallForward);
    }

    private String getSimNumberFromSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String simNumber = sharedPreferences.getString("SIM_NUMBER", null);
        if (simNumber != null)
            return simNumber;


        return "Unknown";
    }

    private class PhoneCallListener extends PhoneStateListener {
        private boolean isPhoneCalling = false;

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            if (TelephonyManager.CALL_STATE_RINGING == state) {
                // phone ringing
            }

            if (TelephonyManager.CALL_STATE_OFFHOOK == state) {
                // active
                isPhoneCalling = true;
            }

            if (TelephonyManager.CALL_STATE_IDLE == state) {
                if (isPhoneCalling) {
                    // restart app
                    Intent i = getBaseContext().getPackageManager()
                            .getLaunchIntentForPackage(getBaseContext().getPackageName());
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                    isPhoneCalling = false;
                }
            }
        }
    }

}
