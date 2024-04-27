package com.example.connectogram;

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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AddAnnouncementActivity extends AppCompatActivity {
    private static final int PICK_FILE_REQUEST_CODE =300 ;
    Button publish,file;
EditText titleEt,descEt;
    Uri fileuri=null;

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

        if(filePath==null)
        {
            Toast.makeText(this,"no file",Toast.LENGTH_SHORT).show();

        }
        Toast.makeText(this,"ther is file file",Toast.LENGTH_SHORT).show();

    }


}