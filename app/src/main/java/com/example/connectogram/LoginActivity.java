package com.example.connectogram;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    Button login;
    TextView no_account ,forgot;
    EditText email, password;
    private static final String TAG = "LoginActivity";
    ProgressDialog pd;
    SignInButton googleSignIn;


    private FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        login=findViewById(R.id.login_btn);
        no_account=findViewById(R.id.no_account);
        email=findViewById(R.id.emailID);
        password=findViewById(R.id.password);
        pd=new ProgressDialog(this);
        pd.setMessage("Loggin In");
        forgot=findViewById(R.id.forot_password);
        //  googleSignIn=findViewById(R.id.google_singIn);

        auth=FirebaseAuth.getInstance();
        no_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
                finish();
            }
        });

//        googleSignIn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
////                proivide implementioan for google sign in ................................................... video 3
//
//
//                /* if you are   using login using ggole faciluy tthen it is needed to put this code  do not add the google sign in butotn
//                *
//                *
//                * FirebaseUser user = auth.getCurrentUser();
//                        String em=user.getEmail();
//                        String uid=user.getUid();
//                        HashMap<Object, String> hashmap=new HashMap<>();
//                        hashmap.put("email",em);
//                        hashmap.put("uid",uid);
//                        hashmap.put("name","");
//                        hashmap.put("phone"," ");
//                        hashmap.put("bio","");
//                        hashmap.put("image","");
//                        hashmap.put("year","");
//                        hashmap.put("sem", "");
//                        FirebaseDatabase database=FirebaseDatabase.getInstance();
//                        DatabaseReference reference=database.getReference("Users");
//                        reference.child(uid).setVa lue(hashmap);
//                *   in the addOnCOMPLE mehtod , if(succsufflll) becuase when we are signing in using google we register directly and that time we need to make an enty to realtime database...
//                *
//                *
//                 * */
//
//            }
//        });
        forgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRecoverDialog();
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String em=email.getText().toString().trim();
                String pas=password.getText().toString().trim();
                if (!Patterns.EMAIL_ADDRESS.matcher(em).matches()) {
                    email.setError("Invalid email");
                    email.setFocusable(true);
                } else if (pas.length() < 6) {
                    password.setError("At least 6 characters are needed");
                    password.setFocusable(true);
                } else {
                    loginUser(em, pas);
                }
            }
        });

    }
    public void loginUser(String email,String password)
    {
        pd.show();
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            pd.dismiss();
                            // Sign in success, update UI with the signed-in user's information
                            Toast.makeText(LoginActivity.this, "Authentication success.",
                                    Toast.LENGTH_SHORT).show();
                            FirebaseUser user=auth.getCurrentUser();
                            startActivity(new Intent(LoginActivity.this,ProfileActivity.class));
                            finish();
                            // You can redirect to another activity or perform other actions here
                        } else {
                            // If sign in fails, display a message to the user.
                            pd.dismiss();
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(LoginActivity.this, "Authentication failed."+e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
    public  void showRecoverDialog()

    {

        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("RECOVER PASSWORD");
        EditText e=new EditText(this);
        e.setHint("email");
        e.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);


        LinearLayout ll=new LinearLayout(this);
        ll.addView(e);
        ll.setPadding(10,10,10,10);
        builder.setView(ll);

        builder.setPositiveButton("RECOVER", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String email=e.getText().toString().trim();
                beginRecovery(email);
            }
        });

        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();;
            }
        });
        builder.create().show();

    }
    public void beginRecovery(String email)
    {
        pd.setMessage("SENDING EMAIL");
        pd.show();
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Password reset email sent", Toast.LENGTH_SHORT).show();
                            pd.dismiss();
                        } else {
                            Toast.makeText(LoginActivity.this, "Failed to send password reset email", Toast.LENGTH_SHORT).show();
                            pd.dismiss();
                        }

                    }
                });

    }

}