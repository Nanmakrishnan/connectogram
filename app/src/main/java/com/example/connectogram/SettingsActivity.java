package com.example.connectogram;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;

import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

public class SettingsActivity extends AppCompatActivity {


    Switch postSwitch,killSwitch;
    SharedPreferences sp;
    SharedPreferences.Editor editor;


    private static final String TOPIC_POST_NOTIFICATION = "POST";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        postSwitch = findViewById(R.id.postSwitch);
        killSwitch=findViewById(R.id.killSwitch);
        sp = getSharedPreferences("Notification_SP", MODE_PRIVATE);
        boolean isPostEnabled = sp.getBoolean( TOPIC_POST_NOTIFICATION, false);
        postSwitch.setChecked(isPostEnabled);

        postSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor = sp.edit();
                editor.putBoolean( TOPIC_POST_NOTIFICATION, isChecked);
                editor.apply();
                ;


                if (isChecked) {
                    subscribePostNotification();

                } else {
                   unsubscribePostNotification();
                }

            }
        });

        killSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Call the method to toggle notifications based on the state of the switch
                toggleNotificationsForChannel("admin_channel", isChecked);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    private void unsubscribePostNotification() {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(TOPIC_POST_NOTIFICATION).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                String msg = "DISABLED POST NOTIFICATION";
                if (!task.isSuccessful()) {
                    msg = "Unsubscription failed";
                }
                Toast.makeText(SettingsActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void toggleNotificationsForChannel(String channelId, boolean enableNotifications) {
        // Get the NotificationManager service
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Check if the device is running Android Oreo or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // If enableNotifications is true, create the notification channel
            if (enableNotifications) {
                // Create the notification channel with desired settings
                NotificationChannel channel = new NotificationChannel(channelId, "All Notification", NotificationManager.IMPORTANCE_DEFAULT);
                // Add additional settings if needed
                // channel.setDescription("Channel Description");
                // channel.set... // Other settings

                // Register the notification channel with the system
                notificationManager.createNotificationChannel(channel);
            } else {
                // Disable notifications by deleting the notification channel
                notificationManager.deleteNotificationChannel(channelId);
            }
        }
    }

    private void subscribePostNotification() {
        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC_POST_NOTIFICATION).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                String msg = "ENABLED POST NOTIFICATION";
                if (!task.isSuccessful()) {
                    msg = "Subscription failed";
                }
                Toast.makeText(SettingsActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
}