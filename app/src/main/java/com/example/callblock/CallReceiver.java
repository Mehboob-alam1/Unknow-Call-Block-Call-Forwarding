package com.example.callblock;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class CallReceiver extends BroadcastReceiver {

    private static Set<String> savedContacts = new HashSet<>();

    public static final String BLOCKED_NOTIFICATION_CHANNEL_NAME = "Alert Notification";
    public static final String BLOCKED_NOTIFICATION_CHANNEL_ID = "IncomingCallReceiver";

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent) {
        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);

        if (TelephonyManager.EXTRA_STATE_RINGING.equals(state) && incomingNumber != null) {
            // Fetch saved contacts from the Contacts Provider
            loadSavedContacts(context);

            boolean isBlocked = true;

            // Check if the incoming number is in the saved contacts
            if (savedContacts.contains(incomingNumber)) {
                isBlocked = false;
            }

            if (isBlocked) {
                Toast.makeText(context, "Blocking call from: " + incomingNumber, Toast.LENGTH_SHORT).show();
                // Implement call blocking logic if applicable

                TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);


                try {
                    Log.d("TAG", "BlockedCall from: " + incomingNumber);
                    //How to end call programmatically
                    //https://stackoverflow.com/questions/18065144/end-call-in-android-programmatically
                    if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                        Method method = telephonyManager.getClass().getMethod("endCall", null);
                        method.invoke(telephonyManager);
                    } else {
                        TelecomManager tm = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);

                        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ANSWER_PHONE_CALLS) != PackageManager.PERMISSION_GRANTED) {

                        }
                        tm.endCall();

                    }
                    notifyUser(context, incomingNumber);
                } catch (NoSuchMethodException |
                         IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    @SuppressLint("Range")
    private void loadSavedContacts(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = ContactsContract.Contacts.CONTENT_URI;
        Cursor cursor = contentResolver.query(uri, null, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
        String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                // Check if the contact has phone numbers
                int hasPhoneNumber = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                if (hasPhoneNumber > 0) {
                    Cursor phoneCursor = contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id},
                            null);

                    if (phoneCursor != null) {
                        while (phoneCursor.moveToNext()) {
                            String phoneNumber = phoneCursor.getString(
                                    phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            savedContacts.add(phoneNumber.replaceAll("\\s+", "")); // Remove spaces
                        }
                        phoneCursor.close();
                    }
                }
            }
            cursor.close();
        }
    }

    private void notifyUser(Context context, String incomingNumber) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notificationCompat = new NotificationCompat.Builder(context, BLOCKED_NOTIFICATION_CHANNEL_ID)
                .setContentTitle("Blocked Call From")
                .setContentText(incomingNumber)
                .setSmallIcon(R.drawable.notify)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.notify))
                .build();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(BLOCKED_NOTIFICATION_CHANNEL_ID,
                    BLOCKED_NOTIFICATION_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_LOW);

            notificationChannel.enableVibration(false);
            notificationChannel.enableLights(false);
            

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        if (notificationManager != null) {

            notificationManager.notify((int) System.currentTimeMillis(), notificationCompat);

        }
    }

}
