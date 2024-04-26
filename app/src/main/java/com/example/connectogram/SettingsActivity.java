package com.example.connectogram;

import android.content.SharedPreferences;
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

import com.example.connectogram.notifications.FIrebaseMessaging;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

public class SettingsActivity extends AppCompatActivity {

    Switch postSwitch;
    SharedPreferences sp;
    SharedPreferences.Editor editor;

    private static final String TOPIC_POST_NOTIFICATION = "POST";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        postSwitch = findViewById(R.id.postSwitch);
        sp = getSharedPreferences("Notification_SP", MODE_PRIVATE);
        boolean isPostEnabled = sp.getBoolean("" + TOPIC_POST_NOTIFICATION, false);
        if (isPostEnabled) {
            postSwitch.setChecked(true);

        } else {
            postSwitch.setChecked(false);
        }

        postSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor = sp.edit();
                editor.putBoolean("" + TOPIC_POST_NOTIFICATION, isChecked);
                editor.apply();
                ;


                if (isChecked) {
                    subscribePostNotification();

                } else {
                    unSubscribePostNotificaton();
                }

            }
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void unSubscribePostNotificaton() {
        FirebaseMessaging.getInstance().unsubscribeFromTopic("" + TOPIC_POST_NOTIFICATION).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                String msg = "DISABLED POST NOTIFICATOIN";
                if (!task.isSuccessful()) {
                    msg = " un subscription failed";

                }
                Toast.makeText(SettingsActivity.this, msg, Toast.LENGTH_SHORT).show();
                ;
            }
        });

    }

    private void subscribePostNotification() {
        FirebaseMessaging.getInstance().subscribeToTopic("" + TOPIC_POST_NOTIFICATION).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                String msg = "ENABLED POST NOTIFICATOIN";
                if (!task.isSuccessful()) {
                    msg = "subscription failed";

                }
                Toast.makeText(SettingsActivity.this, msg, Toast.LENGTH_SHORT).show();
                ;
            }
        });
    }
}