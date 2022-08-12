package com.osbornnick.jukebot;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class PushNotificationService extends FirebaseMessagingService {

    private static final String TAG = "PushNotificationService";
    private FirebaseFirestore db;
    private static String[] hostUID = new String[1];

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        Log.d(TAG, "From: " + message.getFrom());
        // Check if message contains a data payload.
        if (message.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + message.getData());

        }

        // Check if message contains a notification payload.
        if (message.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + message.getNotification().getBody());
            String s1 = message.getNotification().getBody();
            Log.d(TAG, "onMessageReceived: " + s1);
            String s2 = s1.substring(0,s1.indexOf(' '));
            Log.d(TAG, "onMessageReceived: " + s2);
            returnHostUID(s2);

        }


        String title = message.getNotification().getTitle();
        String text = message.getNotification().getBody();
        String CHANNEL_ID = "MESSAGE";
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Message Notification", NotificationManager.IMPORTANCE_HIGH);
        getSystemService(NotificationManager.class).createNotificationChannel(channel);

        Notification.Builder notification = new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true);
        NotificationManagerCompat.from(this).notify(1,notification.build());

    }

    private String returnHostUID(String s2){
        db = FirebaseFirestore.getInstance();
        db.collection("users").whereEqualTo("username",s2).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d(TAG, document.getId() + " => " + document.getData());
                        hostUID[0] = document.getId();
                        Log.d(TAG, "onComplete: the host document UID is " + hostUID[0]);
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });

        return hostUID[0];
    }

    public static String getHostUID(){
        Log.d(TAG, "getHostUID: " + hostUID[0]);
        return hostUID[0];
    }

}
