package com.example.connectogram;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import  android.Manifest;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.sql.SQLOutput;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AddPostActivity extends AppCompatActivity {
ActionBar actionBar;
EditText titleEt,descEv;
Button pUploadBtn;
DatabaseReference userDb;
ImageView pImageIv;
ProgressDialog progreess;
LinearLayout profilelayout;
private  static  final  int   CAMERA_REQUEST_CODE=100;
    private static  final  int STORAGE_REQUEST_CODE=200;
    private  static  final  int IMAGE_PICK_CAMERA_CODE=300;
    private  static  final int IMAGE_PICK_GALLERY_CODE=400;

    String [] cameraPermissions;
    String [] storagePermissions;
    Uri imageuri=null;
    String name,email,dp,uid;
String editTitle,editDesc,editImage;
FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_post);

         actionBar=getSupportActionBar();
         if(actionBar!=null) {
             actionBar.setTitle("Add New Post");
             actionBar.setDisplayShowHomeEnabled(true);
             ;
             actionBar.setDisplayHomeAsUpEnabled(true);
         }
        firebaseAuth=FirebaseAuth.getInstance();
        checkUserStatus();;
        titleEt=findViewById(R.id.pTitleEt);
        descEv=findViewById(R.id.pDescEt);
        pUploadBtn=findViewById(R.id.pUploadBtn);
        pImageIv=findViewById(R.id.pImageIv);

         Intent i=getIntent();
         String isUpdateKey=""+i.getStringExtra("key");
         String editPostId=""+i.getStringExtra("editPostId");

         if(isUpdateKey.equals("editPost")) {
           // actionBar.setTitle("Edit Post");
            pUploadBtn.setText("Save");
            loadPostData(editPostId);
         }
         else {
//actionBar.setTitle("Add Post");
         }







        userDb=FirebaseDatabase.getInstance().getReference("Users");
        Query q=userDb.orderByChild("email").equalTo(email);
        progreess=new ProgressDialog(this);
        q.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for(DataSnapshot ds:snapshot.getChildren())
                {
                    name=""+ds.child("name").getValue();
                    email=""+ds.child("email").getValue();
                    dp=""+ds.child("image").getValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });




///int permisson
        cameraPermissions=new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE};

pImageIv.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        showImagePickDialog();
    }
});

pUploadBtn.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        String title=titleEt.getText().toString().trim();
        String desc=descEv.getText().toString().trim();
        if(TextUtils.isEmpty(title))
        {
            Toast.makeText(AddPostActivity.this,"Enter Title",Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(desc))
        {
            Toast.makeText(AddPostActivity.this,"Enter Description",Toast.LENGTH_SHORT).show();
            return;
        }
        System.out.println("Image url is"+imageuri);

        if(isUpdateKey.equals("editPost"))
        {
            beginUpdate(title,desc,editPostId);

        }
        else {
            uploadData(title,desc);
        }


    }
});
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void beginUpdate(String title, String desc, String editPostId) {

progreess.setMessage("Updating POST");
progreess.show();
if(!editImage.equals("noImage"))
{
    updateWithImage(title,desc,editPostId);

}
else if(pImageIv.getDrawable()!=null) {
updateWithoutImage(title,desc,editPostId);
}
else {
    updatewasNoImage(title,desc,editPostId);
}
    }

    private void updatewasNoImage(String title, String desc, String editPostId) {

        HashMap<String ,Object>hm=new HashMap<>();
        hm.put("uid",uid);
        hm.put("uName",name);
        hm.put("uEmail",email);
        hm.put("pTitle",title);
        hm.put("pDesc",desc);
        hm.put("uDp",dp);
        hm.put("pImage","noImage");


        DatabaseReference r=FirebaseDatabase.getInstance().getReference("Posts");
        r.child(editPostId).updateChildren(hm).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                progreess.dismiss();;
                Toast.makeText(AddPostActivity.this,"Upated post",Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progreess.dismiss();;
                Toast.makeText(AddPostActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void updateWithoutImage(String title, String desc, String editPostId) {
        String timestapm=String.valueOf(System.currentTimeMillis());
        String filepahtname="Posts/"+"post_"+timestapm;
        Bitmap bitmap=((BitmapDrawable)pImageIv.getDrawable()).getBitmap();
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,baos);
        byte[] data=baos.toByteArray();

        StorageReference ref=FirebaseStorage.getInstance().getReference().child(filepahtname);
        ref.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //
                Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                while(!uriTask.isSuccessful());
                String downloaduri=uriTask.getResult().toString();
                if(uriTask.isSuccessful())
                {
                    HashMap<String ,Object>hm=new HashMap<>();
                    hm.put("uid",uid);
                    hm.put("uName",name);
                    hm.put("uEmail",email);
                    hm.put("pTitle",title);
                    hm.put("pDesc",desc);
                    hm.put("uDp",dp);
                    hm.put("pImage",downloaduri);

                    DatabaseReference r=FirebaseDatabase.getInstance().getReference("Posts");
                    r.child(editPostId).updateChildren(hm).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            progreess.dismiss();;
                            Toast.makeText(AddPostActivity.this,"Upated post",Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progreess.dismiss();;
                            Toast.makeText(AddPostActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();

                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progreess.dismiss();
                Toast.makeText(AddPostActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void updateWithImage(String title, String desc, String editPostId) {
        StorageReference mpicref=FirebaseStorage.getInstance().getReferenceFromUrl(editImage);
        mpicref.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                    //image deleted
                String timestapm=String.valueOf(System.currentTimeMillis());
                String filepahtname="Posts/"+"post_"+timestapm;
                Bitmap bitmap=((BitmapDrawable)pImageIv.getDrawable()).getBitmap();
                ByteArrayOutputStream baos=new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG,100,baos);
                byte[] data=baos.toByteArray();

                StorageReference ref=FirebaseStorage.getInstance().getReference().child(filepahtname);
                ref.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //
                        Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                        while(!uriTask.isSuccessful());
                        String downloaduri=uriTask.getResult().toString();
                        if(uriTask.isSuccessful())
                        {
                            HashMap<String ,Object>hm=new HashMap<>();
                            hm.put("uid",uid);
                            hm.put("uName",name);
                                    hm.put("uEmail",email);
                            hm.put("pTitle",title);
                            hm.put("pDesc",desc);
                            hm.put("pImage",downloaduri);
                            hm.put("uDp",dp);

                            DatabaseReference r=FirebaseDatabase.getInstance().getReference("Posts");
                                          r.child(editPostId).updateChildren(hm).addOnSuccessListener(new OnSuccessListener<Void>() {
                                              @Override
                                              public void onSuccess(Void unused) {
                                                  progreess.dismiss();;
                                                  Toast.makeText(AddPostActivity.this,"Upated post",Toast.LENGTH_SHORT).show();
                                              }
                                          }).addOnFailureListener(new OnFailureListener() {
                                              @Override
                                              public void onFailure(@NonNull Exception e) {
    progreess.dismiss();;
                                                  Toast.makeText(AddPostActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();

                                              }
                                          });
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progreess.dismiss();
                        Toast.makeText(AddPostActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progreess.dismiss();
                Toast.makeText(AddPostActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void loadPostData(String editPostId) {

        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Posts");
        Query q=ref.orderByChild("pId").equalTo(editPostId);
        q.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds:snapshot.getChildren())
                {
                        editTitle=""+ds.child("pTitle").getValue();
                        editDesc=""+ds.child("pDesc").getValue();
                        editImage=""+ds.child("pImage").getValue();

                        titleEt.setText(editTitle);
                        descEv.setText(editDesc);

                        if(!editImage.equals("noImage"))
                        {
                            try
                            {
                                Picasso.get().load(editImage).into(pImageIv);
                            }
                            catch (Exception e )
                            {

                            }
                        }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void uploadData(String title, String desc) {
        progreess.setMessage("Publishing....");
        progreess.show();;
        String timestamp=String.valueOf(System.currentTimeMillis()) ;
        String filepath="Posts/"+"post_"+timestamp;

        if(pImageIv.getDrawable()!=null)
        {
            Bitmap bitmap=((BitmapDrawable)pImageIv.getDrawable()).getBitmap();
            ByteArrayOutputStream baos=new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG,100,baos);
            byte[] data=baos.toByteArray();

                                //post wth image
            StorageReference ref= FirebaseStorage.getInstance().getReference().child(filepath);
            ref.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());
                    String downloadUri=uriTask.getResult().toString();
                    if(uriTask.isSuccessful())
                    {
                        HashMap<Object,String >hm=new HashMap<>();
                        hm.put("uid",uid);
                        hm.put("uName",name);
                        hm.put("uEmail",email);
                        hm.put("uDp",dp);
                        hm.put("pId",timestamp);
                        hm.put("pTitle",title);
                        hm.put("pDesc",desc);
                        hm.put("pImage",downloadUri);
                        hm.put("pTime",timestamp);
                        hm.put("pLikes","0");
                        hm.put("pComments","0");

                        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Posts");
                        ref.child(timestamp).setValue(hm).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                        progreess.dismiss();
                        Toast.makeText(AddPostActivity.this,"Post published",Toast.LENGTH_SHORT).show();
                                titleEt.setText("");
                                descEv.setText("");
                                pImageIv.setImageURI(null);
                                imageuri=null;

                                //send notficaton
                                prepareNotification(""+timestamp,""+name+" added new post",""+title+"\n"+desc,"PostNotification","POST");
                                startActivity(new Intent(AddPostActivity.this,ProfileActivity.class));

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progreess.dismiss();;
                                Toast.makeText(AddPostActivity.this,"Failure To Publish Post",Toast.LENGTH_SHORT).show();

                            }
                        });

                    }


                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progreess.dismiss();;
                    Toast.makeText(AddPostActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();

                }
            });
        }
        else {
            HashMap<Object,String >hm=new HashMap<>();
            hm.put("uid",uid);
            hm.put("uName",name);
            hm.put("uEmail",email);
            hm.put("uDp",dp);
            hm.put("pId",timestamp);
            hm.put("pTitle",title);
            hm.put("pDesc",desc);
            hm.put("pImage","noImage");
            hm.put("pTime",timestamp);
            hm.put("pLikes","0");
            hm.put("pComments","0");

            DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Posts");
            ref.child(timestamp).setValue(hm).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    progreess.dismiss();
                    Toast.makeText(AddPostActivity.this,"Post published",Toast.LENGTH_SHORT).show();
                    titleEt.setText("");
                    descEv.setText("");
                    pImageIv.setImageURI(null);
                    imageuri=null;
                    prepareNotification(""+timestamp,""+name+"added new post",""+title+"\n"+desc,"PostNotification","POST");
                    startActivity(new Intent(AddPostActivity.this,ProfileActivity.class));

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progreess.dismiss();;
                    Toast.makeText(AddPostActivity.this,"Failure To Publish Post",Toast.LENGTH_SHORT).show();

                }
            });
        }

    }

    private boolean checkCameraPermission() {
        boolean cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        if (!cameraPermission) {
            // Camera permission is not granted, request it
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        }
        return cameraPermission;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    private  boolean checkStoragePermission()
    {
        boolean storagePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        if (!storagePermission) {
            // Storage permission is not granted, request it
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_REQUEST_CODE);
        }
        return storagePermission;
    }
    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this,storagePermissions, STORAGE_REQUEST_CODE);
    }
    private void showImagePickDialog() {
        String options[]={"Camera","Gallery"};
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Chose Image from");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which==0)
                {
                        //open camera
                    if(!checkCameraPermission())
                        requestCameraPermission();
                    else
                        pickFromCamera();
                }
                if(which==1)
                {
                    if(!checkStoragePermission()) {
                        requestStoragePermission();
                        pickFromGallery();
                    }
                    else 
                        pickFromGallery();

                }
            }
        });
        builder.create().show();;
    }

    private void pickFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera() {
        System.out.println("pirck From Camera Executed");
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");
        imageuri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        System.out.println("picked is"+imageuri);
        // setting by default
        pImageIv.setImageURI(imageuri);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageuri);
        startActivityForResult(cameraIntent,IMAGE_PICK_CAMERA_CODE);
    }
    private  void prepareNotification (String pId,String title,String desc,String notificationtype,String notificationTopic)
    {
        String NOTIFICATION_TOPIC="/topics/"+notificationTopic;
        String NOTIFICATION_TITLE=title;
        String NOTIFICATION_MESSAGE=desc;
        String NOTIFICATOIN_TYPE=notificationtype;

        JSONObject notificationjo=new JSONObject();
        JSONObject notificatoinBody=new JSONObject();
try {
    notificatoinBody.put("notificationType",NOTIFICATOIN_TYPE);
    notificatoinBody.put("sender",uid);
    notificatoinBody.put("pId",pId);
    notificatoinBody.put("pTitle",NOTIFICATION_TITLE);
    notificatoinBody.put("pDesc",NOTIFICATION_MESSAGE);
    notificatoinBody.put("to",NOTIFICATION_TOPIC);

    notificationjo.put("data",notificatoinBody);
    notificationjo.put("to",NOTIFICATION_TOPIC);


}
catch ( Exception e)
{
    e.printStackTrace();
}
sendPostNotification(notificationjo);



    }

    private void sendPostNotification(JSONObject notificationjo) {

        JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(Request.Method.POST,"https://fcm.googleapis.com/fcm/send",
                notificationjo,
                new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                Log.d("FCM_RESPONSE","onResponse :"+jsonObject.toString());

            }
        },
                new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
Toast.makeText(AddPostActivity.this," error is"+volleyError.getMessage(),Toast.LENGTH_SHORT).show();;
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization","key=AAAAenyNnco:APA91bElB1Mr3OvgkWme4uMYLUrkPbllU0kle1z8lIQUQrXP0v_3x1_-DD6blJAc4pASjFvmI7GOvovcbIHMF8XeU40rxNdqd9RPCagQu61o-HnsXnzJBOlnnv8Kqz07mquPpNMjCpwM");
                return headers;
            }
        };
        Volley.newRequestQueue(this).add(jsonObjectRequest);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED&&grantResults[1]==PackageManager.PERMISSION_GRANTED) {
                    pickFromCamera();
                } else {
                    Toast.makeText(this, "Camera permission is required!", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case STORAGE_REQUEST_CODE: {
                if(grantResults.length>0)
                    System.out.println(grantResults[0]);
               // pickFromGallery();
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickFromGallery();
                } else {
                    Toast.makeText(this, "Storage permission is required!", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        checkUserStatus();;
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUserStatus();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
        if(id==R.id.action_logout)
        {
            firebaseAuth.signOut();
            checkUserStatus();
            return  true;
        }
        return super.onOptionsItemSelected(item);
    }
    public void checkUserStatus()
    {
        FirebaseUser user= firebaseAuth.getCurrentUser();
        if(user!=null)
        {
        email=user.getEmail();
        uid=user.getUid();

            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // User data exists, get name and profile picture URL
                        name = snapshot.child("name").getValue(String.class);
                        dp = snapshot.child("image").getValue(String.class);
                    } else {
                        // User data doesn't exist
                        // Handle the case where the user data doesn't exist
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Error occurred while fetching user data
                    // Handle the error if needed
                }
            });


        }
        else {
            startActivity(new Intent(this,MainActivity.class));
         finish();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK) {
            if(requestCode == IMAGE_PICK_CAMERA_CODE) {
                // Image captured from camera, set imageuri to the captured image URI
                pImageIv.setImageURI(imageuri);
            } else if(requestCode == IMAGE_PICK_GALLERY_CODE && data != null && data.getData() != null) {
                // Image picked from gallery, set imageuri to the selected image URI from data
                imageuri = data.getData();
                pImageIv.setImageURI(imageuri);
            }
        }
    }

}