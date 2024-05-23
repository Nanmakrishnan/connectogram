package com.example.connectogram;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    EditText email, password,confirm,usn;
    Button register_btn;
    TextView haveAccount;
    private FirebaseAuth auth;
    ProgressDialog progress; // Renamed progess to progress

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize views after inflating the layout
        email = findViewById(R.id.emailID);

        password = findViewById(R.id.password);
        register_btn = findViewById(R.id.register_btn);
        haveAccount=findViewById(R.id.have_account);
        confirm=findViewById(R.id.cfrmpassword);
        usn=findViewById(R.id.usn);
        // Initialize FirebaseAuth instance
        auth = FirebaseAuth.getInstance();


        haveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
            }
        });
        // Set click listener for register button
        register_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String em = email.getText().toString().trim();

                String pas = password.getText().toString().trim();
                if (!Patterns.EMAIL_ADDRESS.matcher(em).matches()) {
                    email.setError("Invalid email");
                    email.setFocusable(true);
                } else if (pas.length() < 6) {
                    password.setError("At least 6 characters are needed");
                    password.setFocusable(true);
                } else if (!confirm.getText().toString().trim().equals(pas)) {
                    confirm.setError("Password mismatch");
                    confirm.setFocusable(true);
                }
                else if(TextUtils.isEmpty(usn.getText().toString())|| !usn.getText().toString().toLowerCase().startsWith("4kv"))
                {
                    usn.setError("Invalid Usn");
                    usn.setFocusable(true);

                }
                else {
                    registerUser(em, pas);
                    // if you want email to be fileter call checkEmailAndRegister(String email, String password) instead of registerUser

                }


            }
        });

        // Initialize progress dialog
        progress = new ProgressDialog(this);
        progress.setMessage("Registering");

        // Set up ActionBar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Create New Account");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Edge-to-edge
        EdgeToEdge.enable(this);

        // Apply window insets listener
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // code if email filtering has to be done ............
    private void checkEmailAndRegister(String email, String password) {
        progress.show();
        DatabaseReference allowedEmailsRef = FirebaseDatabase.getInstance().getReference("AllowedEmails");

        allowedEmailsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean isEmailAllowed = false;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String allowedEmail = snapshot.getValue(String.class);
                    if (allowedEmail != null && allowedEmail.equals(email)) {
                        isEmailAllowed = true;
                        break;
                    }
                }
                if (isEmailAllowed) {
                    registerUser(email, password);
                } else {
                    progress.dismiss();
                    Toast.makeText(RegisterActivity.this, "Email is not allowed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(
                    DatabaseError databaseError) {
                progress.dismiss();
                Toast.makeText(RegisterActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    // Method to register the user with Firebase Authentication
    public void registerUser(String email, String password) {



        progress.show();

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        // Sign up success, update UI accordingly
                        FirebaseUser user = auth.getCurrentUser();
                        String em=user.getEmail();
                        String uid=user.getUid();
                        HashMap<Object, String> hashmap=new HashMap<>();
                        hashmap.put("email",em);
                        hashmap.put("uid",uid);

                        hashmap.put("name","");
                        hashmap.put("phone"," ");
                        hashmap.put("bio","");
                        hashmap.put("image","");
                        hashmap.put("year","");
                        hashmap.put("sem", "");
                        hashmap.put("usn",usn.getText().toString());
                        hashmap.put("onlineStatus","online");
                        hashmap.put("typingTo","noOne");
                        FirebaseDatabase database=FirebaseDatabase.getInstance();
                        DatabaseReference reference=database.getReference("Users");
                        reference.child(uid).setValue(hashmap);


                        if (user != null) {
                            progress.dismiss();
                            Toast.makeText(RegisterActivity.this, "Registered \n" + user.getEmail(), Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterActivity.this, ProfileActivity.class));
                            finish(); // Finish the current activity
                        } else {
                            // User is null, handle this case
                            Toast.makeText(RegisterActivity.this, "User is null", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // If sign up fails, display a message to the user.
                        Toast.makeText(getApplicationContext(), "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        progress.dismiss();
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}