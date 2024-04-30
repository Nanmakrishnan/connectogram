package com.example.connectogram;




import static android.app.PendingIntent.getActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.connectogram.adapters.AdapterChat;
import com.example.connectogram.models.ModelChat;
import com.example.connectogram.models.Model_discover;
import com.example.connectogram.notifications.Data;
import com.example.connectogram.notifications.Sender;
import com.example.connectogram.notifications.Token;
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
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    Toolbar toolbar;
    FirebaseAuth fauth;
    RecyclerView recyclerView;
    String myUid,hisUid,hisImage;
private RequestQueue requestQueue;
   private  boolean notfiy=false;
    ImageView profileTv;
    TextView nameTv,userStatusTv;
    EditText messageEt;
    ImageButton sendBtn,attachBtn;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    ValueEventListener seenLister;
    DatabaseReference userRefForSeen;
    List<ModelChat> chatList;
    AdapterChat adapterChat;
    private  static  final  int   CAMERA_REQUEST_CODE=100;
    private static  final  int STORAGE_REQUEST_CODE=200;
    private  static  final  int IMAGE_PICK_CAMERA_CODE=300;
    private  static  final int IMAGE_PICK_GALLERY_CODE=400;

    String [] cameraPermissions;
    String [] storagePermissions;
    Uri imageuri=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);

        toolbar=findViewById(R.id.toolbar_chat);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        profileTv=findViewById(R.id.profileIv);
        recyclerView=findViewById(R.id.chat_recycleView);
        nameTv=findViewById(R.id.nameTv);
        userStatusTv=findViewById(R.id.statusTv);
        sendBtn=findViewById(R.id.send_btn);
        attachBtn=findViewById(R.id.attachBtn);
        messageEt=findViewById(R.id.messageEt);
        fauth=FirebaseAuth.getInstance();
        Intent i=getIntent();
        hisUid=i.getStringExtra("hisUid");
        firebaseDatabase=FirebaseDatabase.getInstance();
        databaseReference=firebaseDatabase.getReference("Users");
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this    );
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);;
        requestQueue= Volley.newRequestQueue(getApplicationContext());

        cameraPermissions=new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions=new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};


        //create api sevices



        Query query=databaseReference.orderByChild("uid").equalTo(hisUid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for(DataSnapshot ds:snapshot.getChildren())
                {
                    String name=""+ds.child("name").getValue();
                    hisImage=""+ds.child("image").getValue();

                    String onlineStatus=""+ds.child("onlineStatus").getValue();
                    String typingStatus=""+ds.child("typingTo").getValue();

                    if(typingStatus.equals(myUid))
                        userStatusTv.setText("typing...");

                    else
                    {
                        if(onlineStatus.equals("online"))
                            userStatusTv.setText(onlineStatus);
                        else
                        {

                            Calendar cal=Calendar.getInstance(Locale.ENGLISH);
                            cal.setTimeInMillis(Long.parseLong(onlineStatus));
                            String datetime= DateFormat.format("dd/MM/yyyy hh:mm aa",cal).toString();
                            userStatusTv.setText("last seen at "+datetime);
                        }


                    }

                    nameTv.setText(name);
                  try {
                      Picasso.get().load(hisImage).placeholder(R.drawable.ic_person_blue).into(profileTv);
                  }
                  catch (Exception e)
                  {
                      System.out.println(e);
                      Picasso.get().load(R.drawable.ic_person_blue).into(profileTv);
                  }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notfiy=true;
                String mesage=messageEt.getText().toString().trim();
                if(TextUtils.isEmpty(mesage))
                {
                    Toast.makeText(ChatActivity.this,"Message is Empty",Toast.LENGTH_SHORT).show();;
                }
                else {
                sendmessage(mesage);
                }
                messageEt.setText("");
            }
        });
attachBtn.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
showImagePickDialog();
    }
});
        messageEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().trim().length()==0)
                {
                    chechTypingStatus("noOne");
                }
                else
                    chechTypingStatus(hisUid);

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

readMessages();
seenMessage();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
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
       // pImageIv.setImageURI(imageuri);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageuri);
        startActivityForResult(cameraIntent,IMAGE_PICK_CAMERA_CODE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode==RESULT_OK)
        {
            if(requestCode==IMAGE_PICK_CAMERA_CODE)
            {
                // imageuri=data.getData();

                System.out.println("cliede image is"+imageuri);
                try {
                    sendImageMessage(imageuri);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
            else   if(requestCode==IMAGE_PICK_GALLERY_CODE)
            {
                imageuri=data.getData();
                System.out.println("pOCIED FROM GALLERY URL IS"+imageuri);
                try {
                    sendImageMessage(imageuri);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        }


    }

    private void sendImageMessage(Uri imageuri) throws IOException {
        notfiy=true;
        ProgressDialog pd=new ProgressDialog(this);
        pd.setMessage("Sending Image");

        pd.show();
        String timestamp=String.valueOf(System.currentTimeMillis());
        String fileNamePath="ChatImages/"+"post_"+timestamp;
        Bitmap bitmap=MediaStore.Images.Media.getBitmap(this.getContentResolver(),imageuri);
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,baos);
        byte[]data=baos.toByteArray();
        StorageReference ref= FirebaseStorage.getInstance().getReference().child(fileNamePath);
        ref.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                pd.dismiss();
                Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());
                String downloadeduri=uriTask.getResult().toString();
                if(uriTask.isSuccessful())
                {
                    DatabaseReference databaseReference1=FirebaseDatabase.getInstance().getReference();
                    HashMap<String,Object>hm=new HashMap<>();
                    hm.put("sender",myUid);
                    hm.put("receiver",hisUid);
                    hm.put("message",downloadeduri);
                    hm.put("timestamp",timestamp);
                    hm.put("type","image");
                    hm.put("isSeen",false);

                    databaseReference1.child("Chats").push().setValue(hm);

                    DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users").child(myUid);
                    ref.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Model_discover user=snapshot.getValue(Model_discover.class);
                            if(notfiy)
                            {
                                sendNotification(hisUid,user.getName(),"Send You A photo" );
                                notfiy=false;
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    //create a chatlist

                    DatabaseReference chatRef1= FirebaseDatabase.getInstance().getReference("Chatlist").child(myUid).child(hisUid);

                    chatRef1.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(!snapshot.exists())
                            {
                                chatRef1.child("id").setValue(hisUid);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    DatabaseReference chatRef2= FirebaseDatabase.getInstance().getReference("Chatlist").child(hisUid).child(myUid);


                    chatRef2.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(!snapshot.exists())
                            {
                                chatRef2.child("id").setValue(myUid);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });



                }


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
pd.dismiss();;
            }
        });


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


    private void seenMessage() {
        userRefForSeen=FirebaseDatabase.getInstance().getReference("Chats");
        seenLister=userRefForSeen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds:snapshot.getChildren())
                {
ModelChat chat=ds.getValue(ModelChat.class);
if(chat.getReceiver().equals(myUid)&&chat.getSender().equals((hisUid)))
{
    HashMap<String,Object>hasSeen=new HashMap<>();
    hasSeen.put("isSeen",true);
    ds.getRef().updateChildren(hasSeen);
}

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private  void chechOnlineStatus(String status)
    {
        DatabaseReference dbref=FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String,Object>hashMap=new HashMap<>();
        hashMap.put("onlineStatus",status);
        dbref.updateChildren(hashMap);

    }
    private  void chechTypingStatus(String typing)
    {
        DatabaseReference dbref=FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String,Object>hashMap=new HashMap<>();
        hashMap.put("typingTo",typing);
        dbref.updateChildren(hashMap);

    }
    private void readMessages() {
        chatList=new ArrayList<>();
        DatabaseReference dbref=FirebaseDatabase.getInstance().getReference("Chats");
        dbref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatList.clear();;

                for(DataSnapshot ds:snapshot.getChildren())
                {
                    ModelChat chat=ds.getValue(ModelChat.class);
                    if(chat.getReceiver().equals(myUid)&&chat.getSender().equals(hisUid)||chat.getReceiver().equals(hisUid)&&chat.getSender().equals(myUid))
                    {
                        chatList.add(chat);
                    }


                }
                adapterChat=new AdapterChat(ChatActivity.this,chatList,hisImage);
                adapterChat.notifyDataSetChanged();

                recyclerView.setAdapter(adapterChat);;
                if(adapterChat.getItemCount()>=1)
                {
                recyclerView.smoothScrollToPosition(adapterChat.getItemCount()-1);}
            }



            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        String timeStamp=String.valueOf(System.currentTimeMillis());
        chechOnlineStatus(timeStamp);
        chechTypingStatus("noOne");
        userRefForSeen.removeEventListener(seenLister);
    }

    @Override
    protected void onStop() {
        String timeStamp=String.valueOf(System.currentTimeMillis());
        chechOnlineStatus(timeStamp);
        chechTypingStatus("noOne");
        userRefForSeen.removeEventListener(seenLister);
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        chechOnlineStatus("online");
    }

    private void sendmessage(String mesage) {
        String timestamp=String.valueOf(System.currentTimeMillis());
DatabaseReference dr=FirebaseDatabase.getInstance().getReference();
        HashMap<String , Object> hm=new HashMap<>();
        hm.put("sender",myUid);
        hm.put("receiver",hisUid);
        hm.put("message",mesage);
        hm.put("timestamp",timestamp);
        hm.put("isSeen",false);
        hm.put("type","text");


        dr.child("Chats").push().setValue(hm);


        String msg=mesage;
        DatabaseReference database=FirebaseDatabase.getInstance().getReference("Users").child(myUid);
database.addValueEventListener(new ValueEventListener() {
    @Override
    public void onDataChange(@NonNull DataSnapshot snapshot) {
        Model_discover user=snapshot.getValue(Model_discover.class);
        if(notfiy)
        {
            sendNotification(hisUid,user.getName(),mesage);

        }
        notfiy=false;
    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {

    }
});
//create a chatlist

        DatabaseReference chatRef1= FirebaseDatabase.getInstance().getReference("Chatlist").child(myUid).child(hisUid);

chatRef1.addValueEventListener(new ValueEventListener() {
    @Override
    public void onDataChange(@NonNull DataSnapshot snapshot) {
        if(!snapshot.exists())
        {
            chatRef1.child("id").setValue(hisUid);
        }
    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {

    }
});
        DatabaseReference chatRef2= FirebaseDatabase.getInstance().getReference("Chatlist").child(hisUid).child(myUid);


        chatRef2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists())
                {
                    chatRef2.child("id").setValue(myUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void sendNotification(String hisUid, String name, String mesage) {
        DatabaseReference alltokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query q = alltokens.orderByKey().equalTo(hisUid);
        q.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Token token = ds.getValue(Token.class);
                    Data data = new Data(""+myUid,
                            "ChatNotification",
                            name + ":" + mesage,
                            R.drawable.ic_person_blue,
                            "New Messsage",
                            hisUid+""

                            );
                    Sender sender = new Sender(data, token.getToken());

                    // Create a JSONObjectRequest to send the notification
                    try {
                        JSONObject senderJsonObj = new JSONObject(new Gson().toJson(sender));
                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                                "https://fcm.googleapis.com/fcm/send",
                                senderJsonObj,
                                response -> {
                                    // Handle successful response
                                    Log.d("JSON_RESPONSE","onResponse: "+response.toString());
                                    System.out.println("send noti propey");
                                },
                                error -> {
                                    // Handle error
                                    Log.d("JSON_RESPONSE","onError: "+error.toString());
                                    System.out.println("cant send notification");
                                }
                        ) {
                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                // Add any headers required for your request, if needed
                                Map<String, String> headers = new HashMap<>();
                                headers.put("Content-Type", "application/json");
                                headers.put("Authorization","key=AAAAenyNnco:APA91bElB1Mr3OvgkWme4uMYLUrkPbllU0kle1z8lIQUQrXP0v_3x1_-DD6blJAc4pASjFvmI7GOvovcbIHMF8XeU40rxNdqd9RPCagQu61o-HnsXnzJBOlnnv8Kqz07mquPpNMjCpwM");
                                return headers;
                            }
                        };
                        // Add the request to the Volley request queue
                        requestQueue.add(jsonObjectRequest);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle onCancelled
            }
        });
    }


    @Override
    protected void onStart() {
        checkUserStatus();
            chechOnlineStatus("online");
        super.onStart();
    }

    public void checkUserStatus()
    {
        FirebaseUser user= fauth.getCurrentUser();
        if(user!=null)
        {
           myUid=user.getUid();
        }
        else {
            startActivity(new Intent(this,MainActivity.class));
            finish();
        }

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_add_post).setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
        if(id==R.id.action_logout)
        {
            fauth.signOut();
            checkUserStatus();
            return  true;
        }
        return super.onOptionsItemSelected(item);
    }



}