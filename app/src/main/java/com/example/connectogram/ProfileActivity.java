package com.example.connectogram;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;

import com.example.connectogram.notifications.FIrebaseMessaging;
import com.example.connectogram.notifications.Token;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

public class ProfileActivity extends AppCompatActivity {
FirebaseAuth auth;
TextView email;
    Toolbar toolbar;
    String mUID;
BottomNavigationView bottomNavigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth=FirebaseAuth.getInstance();
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        //email=findViewById(R.id.email);
         toolbar = findViewById(R.id.toolbar);
        bottomNavigationView=findViewById(R.id.bottom_navigation_bar);
        setSupportActionBar(toolbar);

        bottomNavigationView.setOnNavigationItemSelectedListener(selectedlistener);
        HomeFragment hm=new HomeFragment();
        FragmentTransaction ft=getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container,hm," ");
        ft.commit();
checkUserStatus();;





    }

    @Override
    protected void onResume() {
        checkUserStatus();
        super.onResume();
    }

    public void checkUserStatus()
    {
        FirebaseUser user= auth.getCurrentUser();
        if(user!=null)
        {
            //email.setText(user.getEmail());
            mUID= user.getUid();;

            SharedPreferences sp=getSharedPreferences("SP_USER",MODE_PRIVATE);
            SharedPreferences.Editor editor=sp.edit();
            editor.putString("Current_USERID",mUID);
            editor.apply();;
            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            String token = task.getResult();
                            // Update the token in the database
                            updateToken(token);
                        } else {
                            //Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                        }
                    });

        }
        else {
            startActivity(new Intent(ProfileActivity.this,MainActivity.class));
        }

    }
    @Override
    protected  void onStart()
    {
        checkUserStatus();
        super.onStart();

    }

    public void updateToken(String token)
    {
        DatabaseReference dref= FirebaseDatabase.getInstance().getReference("Tokens");
        Token mToken=new Token(token);
        dref.child(mUID).setValue(mToken);


    }


private  BottomNavigationView.OnNavigationItemSelectedListener selectedlistener=new BottomNavigationView.OnNavigationItemSelectedListener() {
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int itemid=menuItem.getItemId();

        if(itemid==R.id.nav_home) {

                HomeFragment hm = new HomeFragment();
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.container, hm, " ");
                ft.commit();
                toolbar.setTitle("Home");

        }
                //frament
    else if (itemid==R.id.nav_profile)
        {
            ProfileFragment p=new ProfileFragment();
            FragmentTransaction ft=getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.container,p,"");
            ft.commit();
            toolbar.setTitle("Profile");
        }

    else if(itemid==R.id.nav_discover)
        {
            DiscoverFragment d=new DiscoverFragment();
            FragmentTransaction ft=getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.container,d,"");
            ft.commit();
            toolbar.setTitle("Discover");
        }
        else if(itemid==R.id.nav_chat)
        {
            ChatlistFragment d=new ChatlistFragment();
            FragmentTransaction ft=getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.container,d,"");
            ft.commit();
            toolbar.setTitle("Chats");
        }
        else if (itemid==R.id.nav_announcement)
        {
            //if user presses the anoucements title
            AnnouncementFragment announcementFragment=new AnnouncementFragment();
            FragmentTransaction ft=getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.container,announcementFragment,"");
            ft.commit();
            toolbar.setTitle("Announcements");




        }
        return true;
    }
};
    @Override
    public void onBackPressed() {

        super.onBackPressed();
        finish();
    }
}