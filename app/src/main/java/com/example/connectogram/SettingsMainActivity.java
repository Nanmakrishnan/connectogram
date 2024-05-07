package com.example.connectogram;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SettingsMainActivity extends AppCompatActivity {
    Button notificaton,help,accnt,privacy,logout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings_main);

        notificaton=findViewById(R.id.notification_settings );
        help=findViewById(R.id.help_and_support);
        accnt=findViewById(R.id.accnt_settings);
        privacy=findViewById(R.id.privacy_settings);


        logout=findViewById(R.id.logoutBtn);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth auth=FirebaseAuth.getInstance();

                if(auth.getCurrentUser()!=null)
                {
                    auth.signOut();;
                    startActivity(new Intent(SettingsMainActivity.this,MainActivity.class));

                }
            }
        });
        notificaton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsMainActivity.this,SettingsActivity.class));
            }
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}