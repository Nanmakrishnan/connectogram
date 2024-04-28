package com.example.connectogram;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import org.apache.commons.io.FileUtils;


import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.connectogram.models.ModelAnnounce;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.InputStream;

public class AddAnnouncementActivity extends AppCompatActivity {
    private static final int PICK_FILE_REQUEST_CODE =300 ;
    Button publish,file;
EditText titleEt,descEt;
    Uri fileuri=null;
    String uName;
    String uDp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_announcement);
        titleEt=findViewById(R.id.titleEt);
        descEt=findViewById(R.id.descEt);
        file=findViewById(R.id.fileBtn);
        publish=findViewById(R.id.publishBtn);
        publish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call a method to validate and upload data
                validateAndUploadData();
            }
        });

        // Set onClickListener for the file button to allow users to attach a file
        file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Implement logic to allow users to attach a file
                // For example, open a file picker dialog
                openFilePicker();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    private void openFilePicker() {
        // Create an intent to open the file picker
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");  // Set MIME type to allow any file type

        // Start the activity with the intent
        startActivityForResult(Intent.createChooser(intent, "Select File"), PICK_FILE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable  Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == RESULT_OK) {
            // Retrieve the file URI from the intent data
            Uri fileUri = data.getData();

            // Convert the file URI to file path

            // Store the file path for later use
            this.fileuri=fileUri;

            // Show a toast message indicating the selected file
            Toast.makeText(this, "File selected: " + fileUri.toString(), Toast.LENGTH_SHORT).show();
        }
    }
    private void validateAndUploadData() {
        // Get title and description from EditText fields
        String title = titleEt.getText().toString().trim();
        String desc = descEt.getText().toString().trim();

        // Validate title and description fields
        if (TextUtils.isEmpty(title)) {
            // Show toast message if title is empty
            Toast.makeText(this, "Enter a title", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(desc)) {
            // Show toast message if description is empty
            Toast.makeText(this, "Enter a description", Toast.LENGTH_SHORT).show();
            return;
        }

        // If validation successful, proceed to upload data to Firebase
        // Implement uploadDataToFirebase() method to upload title, description, and file path (if available)
        uploadDataToFirebase(title, desc,fileuri );
    }

    private void uploadDataToFirebase(String title, String desc, Uri filePath) {

        if(filePath!=null)
        {
           uploadWithFile();
           

        }
        else {

            uploadWithoutFile();
        }

    }

    private void uploadWithoutFile() {

        long timestamp = System.currentTimeMillis();
        Toast.makeText(AddAnnouncementActivity.this,""+"withoud file",Toast.LENGTH_SHORT).show();;


        // If no file URI is available, proceed with uploading data to Firebase without a file
        // Implement the logic to upload title, description, timestamp, and UID to Firebase without the file
        // For example:
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Announcements");
        String announcementId = String.valueOf(timestamp); // Set announcement ID as current timestamp
       String uid= FirebaseAuth.getInstance().getCurrentUser().getUid();
       String email=FirebaseAuth.getInstance().getCurrentUser().getEmail();


     DatabaseReference   userDb=FirebaseDatabase.getInstance().getReference("Users");
        Query q=userDb.orderByChild("email").equalTo(email);
     ProgressDialog   progreess=new ProgressDialog(this);
        q.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for(DataSnapshot ds:snapshot.getChildren())
                {
                    uName=""+ds.child("name").getValue();
                    uDp=""+ds.child("image").getValue();
                }


                ModelAnnounce announce= new ModelAnnounce(announcementId,uid,uName,email,titleEt.getText().toString(),descEt.getText().toString(),announcementId,uDp,"null"); // Pass null for fileUrl
                ref.child(announcementId).setValue(announce);

                // Show a success message to the user
                //Toast.makeText(AddAnnouncementActivity.this, "Data uploaded successfully without file", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void uploadWithFile() {
        // Get the current timestamp
        int fsize=0;
        try {
            InputStream inputStream = getContentResolver().openInputStream(fileuri);
             fsize = inputStream.available();
          //  Toast.makeText(this, "File siz"+fsize, Toast.LENGTH_SHORT).show();
        }
            catch(Exception e)
            {
                return;
            }


        if (fileuri != null &&fsize <= (5 * 1024 * 1024)&&fsize>0) {

            long timestamp = System.currentTimeMillis();
            ProgressDialog progress = new ProgressDialog(this);
            progress.setMessage("Uploading file...");
            progress.show();

            // Get the Firebase Storage reference
            StorageReference storageRef = FirebaseStorage.getInstance().getReference();

            // Create a reference to the file location in Firebase Storage
            StorageReference fileRef = storageRef.child("AnnouncementFiles").child("file_" + timestamp);

            // Upload file to Firebase Storage
            UploadTask uploadTask = fileRef.putFile(fileuri);

            // Register observers to listen for when the upload is done or if it fails
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                // File uploaded successfully
                progress.dismiss();
                // Get the download URL of the uploaded file
                fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String fileUrl = uri.toString();
                    // Proceed to upload announcement data with file URL
                    uploadAnnouncementData(timestamp, fileUrl);
                }).addOnFailureListener(e -> {
                    // Handle any errors retrieving the download URL
                    progress.dismiss();
                    Toast.makeText(AddAnnouncementActivity.this, "Failed to get file URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }).addOnFailureListener(e -> {
                // Handle unsuccessful uploads
                progress.dismiss();
                Toast.makeText(AddAnnouncementActivity.this, "Failed to upload file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
        else
        {
            Toast.makeText(this, "File size exceeds 5 MB limit", Toast.LENGTH_SHORT).show();

        }
    }

    private void uploadAnnouncementData(long timestamp, String fileUrl) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Announcements");
        String announcementId = String.valueOf(timestamp); // Set announcement ID as current timestamp
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        DatabaseReference userDb = FirebaseDatabase.getInstance().getReference("Users");
        Query q = userDb.orderByChild("email").equalTo(email);
        ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage("Uploading announcement data...");
        progress.show();
        q.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    uName = "" + ds.child("name").getValue();
                    uDp = "" + ds.child("image").getValue();
                }

                ModelAnnounce announce = new ModelAnnounce(announcementId, uid, uName, email, titleEt.getText().toString(), descEt.getText().toString(), announcementId, uDp, fileUrl);
                ref.child(announcementId).setValue(announce).addOnCompleteListener(task -> {
                    progress.dismiss();
                    if (task.isSuccessful()) {
                        // Show a success message to the user
                       // Toast.makeText(AddAnnouncementActivity.this, "Data uploaded successfully with file", Toast.LENGTH_SHORT).show();
                    } else {
                        // Handle unsuccessful upload
                        Toast.makeText(AddAnnouncementActivity.this, "Failed to upload announcement data: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progress.dismiss();
                Toast.makeText(AddAnnouncementActivity.this, "Failed to upload announcement data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



}