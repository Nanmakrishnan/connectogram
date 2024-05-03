package com.example.connectogram;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.DecorToolbar;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import  android.Manifest;
import android.widget.Toast;

import com.example.connectogram.adapters.AdapterPost;
import com.example.connectogram.models.ModelPost;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static  final  int CAMERA_REQUEST_CODE=100;
    private static  final  int STORAGE_REQUEST_CODE=200;
    private static  final  int IMAGE_PICK_CAMERA_REQUEST_CODE=400;
    private static  final  int IMAGE_PICK_GALLERY_REQUEST_CODE=300;

    String camerPermissions[];
    String storagePermissions[];
    Uri imageuri;
    RecyclerView postrecycleview;
List<ModelPost> postList;
AdapterPost adapterPost;
String uid;


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    ImageView avatarTv;
    TextView nameTv,emailTv,phoneTv,yearTv,semTv,bioTv;
    ProgressDialog pd;
    FloatingActionButton fab;
FirebaseDatabase db;
FirebaseUser user;
FirebaseAuth auth;
    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
    View view=inflater.inflate(R.layout.fragment_profile, container, false);;
        auth=FirebaseAuth.getInstance();
        user=auth.getCurrentUser();
        db=FirebaseDatabase.getInstance();
        DatabaseReference reference=db.getReference("Users");
        nameTv=view.findViewById(R.id.nameTv);
        avatarTv=view.findViewById(R.id.avatarTv);
        emailTv=view.findViewById(R.id.emailTv);
        phoneTv=view.findViewById(R.id.phoneTv);
        yearTv=view.findViewById(R.id.yearTv);
        semTv=view.findViewById(R.id.semTv);
        fab=view.findViewById(R.id.fab);
        bioTv=view.findViewById(R.id.bioTv);
        pd=new ProgressDialog(getActivity());
        postrecycleview=view.findViewById(R.id.recycleview_posts);
        postList=new ArrayList<>();
        camerPermissions=new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        Query query=reference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot ds:snapshot.getChildren())
                {
                    String  name=""+ds.child("name").getValue();
                    String  email=""+ds.child("email").getValue();
                    String  phone=""+ds.child("phone").getValue();
                    String  year=""+ds.child("year").getValue();
                    String  sem=""+ds.child("sem").getValue();
                    String image=""+ds.child("image").getValue();
                    String bio=""+ds.child("bio").getValue();


                    nameTv.setText(name);
                    emailTv.setText(email);
                    phoneTv.setText(phone);
                    yearTv.setText(year);
                    semTv.setText(sem);
                    bioTv.setText(bio);

                    try {
                        Picasso.get().load(image).into(avatarTv);

                    }
                    catch (Exception e){
                    Picasso.get().load(R.drawable.ic_add_photo).into(avatarTv);
                }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogBox();

            }
        });
        checkUserStatus();;
        loadMyPosts();
        setHasOptionsMenu(true);
        return view;
    }

    private void loadMyPosts() {
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getActivity());
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        postrecycleview.setLayoutManager(linearLayoutManager);

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Posts");

        Query q= ref.orderByChild("uid").equalTo(uid);
            q.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();;
                for(DataSnapshot ds:snapshot.getChildren())
                {
                    ModelPost modelPost=ds.getValue(ModelPost.class);
                    postList.add(modelPost);




                }
                adapterPost=new AdapterPost(getActivity(),postList);

                postrecycleview.setAdapter(adapterPost);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(getActivity(),""+error,Toast.LENGTH_SHORT).show();


            }
        });






    }
    private void searchMyPosts(String searchquery) {
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getActivity());
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        postrecycleview.setLayoutManager(linearLayoutManager);


        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Posts");

        Query q= ref.orderByChild("uid").equalTo(uid);
        q.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();;
                for(DataSnapshot ds:snapshot.getChildren())
                {
                    ModelPost modelPost=ds.getValue(ModelPost.class);
                    if(modelPost.getpTitle().toLowerCase().contains(searchquery.toLowerCase())||modelPost.getpDesc().toLowerCase().contains(searchquery.toLowerCase())) {
                        postList.add(modelPost);
                    }

                    adapterPost=new AdapterPost(getActivity(),postList);

                    postrecycleview.setAdapter(adapterPost);


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(getActivity(),""+error,Toast.LENGTH_SHORT).show();


            }
        });






    }

    private boolean checkCameraPermission() {
        boolean result1= ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
       boolean result2= ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_DENIED);
       return  result2&&result1;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
    }

    private  boolean checkStoragePermission()
    {
        boolean result= ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_DENIED);

        return result;
    }
    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_REQUEST_CODE);
    }
    private  void showDialogBox()
    {
        String options[]={"Edit image","Edit Name","Edit Phone","Edit Sem","Edit year", "Edit bio"};
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setTitle("Edit");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if(which==0){
                    pd.setMessage("Changing image");
                    System.out.println("changing image");
                   editImage();
                }
                else if (which==1) {    pd.setMessage("changing name");   editName();                        }

                else if (which==2) { pd.setMessage("changing phone number");  editPhone();}

                else if (which==3) {pd.setMessage("changing  semester"); editSem();}

                else if (which==4) { pd.setMessage("changing  year"); edityear();}
                else if (which==5) {pd.setMessage("changing bio"); editBio();}
            }
        });
        builder.create().show();
    }

    private void edityear() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Edit Year");

        // Set up the input
        final EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint(yearTv.getText().toString()); // Set current year as hint
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newYear = input.getText().toString().trim();
                if (!TextUtils.isEmpty(newYear)) {
                    DatabaseReference userRef = db.getReference("Users").child(user.getUid());
                    userRef.child("year").setValue(newYear);
                    yearTv.setText(newYear);
                } else {
                    Toast.makeText(getActivity(), "Year cannot be empty!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void editBio() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Edit Bio");

        // Set up the input
        final EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setText(bioTv.getText().toString()); // Set current bio as hint
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newBio = input.getText().toString().trim();
                if (!TextUtils.isEmpty(newBio)) {
                    DatabaseReference userRef = db.getReference("Users").child(user.getUid());
                    userRef.child("bio").setValue(newBio);
                    bioTv.setText(newBio);
                } else {
                    Toast.makeText(getActivity(), "Bio cannot be empty!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void editSem() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Edit Semester");

        // Set up the input
        final EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint(semTv.getText().toString()); // Set current semester as hint
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newSemester = input.getText().toString().trim();
                if (!TextUtils.isEmpty(newSemester)) {
                    DatabaseReference userRef = db.getReference("Users").child(user.getUid());
                    userRef.child("sem").setValue(newSemester);
                    semTv.setText(newSemester);
                } else {
                    Toast.makeText(getActivity(), "Semester cannot be empty!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void editPhone() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Edit Phone Number");

        // Set up the input
        final EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_PHONE);
        input.setHint(phoneTv.getText().toString()); // Set current phone number as hint
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newPhone = input.getText().toString().trim();
                if (!TextUtils.isEmpty(newPhone)) {
                    DatabaseReference userRef = db.getReference("Users").child(user.getUid());
                    userRef.child("phone").setValue(newPhone);
                    phoneTv.setText(newPhone);
                } else {
                    Toast.makeText(getActivity(), "Phone number cannot be empty!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void editName() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Edit Name");

        // Set up the input
        final EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint(nameTv.getText().toString()); // Set current name as hint
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newName = input.getText().toString().trim();
                if (!TextUtils.isEmpty(newName)) {
                    DatabaseReference userRef = db.getReference("Users").child(user.getUid());
                    userRef.child("name").setValue(newName);
                    nameTv.setText(newName);

                    DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Posts");
                    System.out.println("uid during name chaingin "+uid);
                    Query q=ref.orderByChild("uid").equalTo(uid);
                    q.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for(DataSnapshot ds:snapshot.getChildren())
                            {
                                String postId = ds.getKey(); // Get the key of the post
                                String postUid = ds.child("uid").getValue(String.class); // Get the UID of the post's author

                               System.out.println(""+postId+"  "+postUid);

                                String child=ds.getKey();
                                System.out.println(" th name is"+ds.child("uName").getValue(String.class));
                                ds.getRef().child("uName").setValue(newName);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    //name changes so change name in the  comments that user hve posted

                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for(DataSnapshot ds:snapshot.getChildren())
                            {
                                String child=ds.getKey();
                                if(snapshot.child(child).hasChild("Comments"))
                                {
                                    String child1=""+snapshot.child(child).getKey();
                                    Query child2=FirebaseDatabase.getInstance().getReference("Posts").child(child1).child("Comments").orderByChild("uid").equalTo(uid);
                                    child2.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for(DataSnapshot ds:snapshot.getChildren())
                                            {
                                                String child=ds.getKey();
                                                snapshot.getRef().child(child).child("uName").setValue(newName);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });

                                }

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });


                } else {
                    Toast.makeText(getActivity(), "Name cannot be empty!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickFromCamera();
                } else {
                    Toast.makeText(getActivity(), "Camera permission is required!", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickFromGallery();
                } else {
                    Toast.makeText(getActivity(), "Storage permission is required!", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    private void pickFromCamera() {


            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, "Temp Pic");
            values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");

            imageuri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageuri);
            startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_REQUEST_CODE);

    }




    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_REQUEST_CODE) {
                imageuri = data.getData();
                uploadProfileImage(imageuri);
            } else if (requestCode == IMAGE_PICK_CAMERA_REQUEST_CODE) {
                uploadProfileImage(imageuri);
            }
        }
    }

    private void editImage() {
        String options[]={"camera","gallery"};
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setTitle("Pick Image From");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which==0)
                {
                    if(!checkCameraPermission())
                        requestCameraPermission();
                    else  pickFromCamera();;

                }
                else if(which==1)
                {
                    if(!checkStoragePermission())
                        requestStoragePermission();
                    else  pickFromGallery();;

                }
            }
        });
        builder.show();
    }
    private void pickFromGallery (){

        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_REQUEST_CODE);
    }

    private void uploadProfileImage(Uri imageUri) {
        pd.setMessage("Uploading Image...");
        pd.show();

        String filePathAndName = "profile_images/" + user.getUid();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
        storageReference.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) ;
                        Uri downloadUri = uriTask.getResult();

                        // Update image in the database
                        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("image", downloadUri.toString());
                        userRef.updateChildren(hashMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        pd.dismiss();
                                        Toast.makeText(getActivity(), "Image updated successfully!", Toast.LENGTH_SHORT).show();
                                        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Posts");
                                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                for(DataSnapshot ds:snapshot.getChildren()) {
                                                    String postUid = ds.child("uid").getValue(String.class);

                                                    // Check if the UID of the post matches the provided UID
                                                    if (uid.equals(postUid)) {
                                                        String postId = ds.getKey();
                                                        String downloadUriString = downloadUri.toString();

                                                        // Update the "uDp" field inside the post
                                                        ds.getRef().child("uDp").setValue(downloadUriString);
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                // Handle onCancelled
                                            }
                                        });


                                        //update image int the comments page


                                       //
                                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                for(DataSnapshot ds:snapshot.getChildren())
                                                {
                                                    String child=ds.getKey();
                                                    if(snapshot.child(child).hasChild("Comments"))
                                                    {
                                                        String child1=""+snapshot.child(child).getKey();
                                                        Query child2=FirebaseDatabase.getInstance().getReference("Posts").child(child1).child("Comments").orderByChild("uid").equalTo(uid);
                                                        child2.addValueEventListener(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                for(DataSnapshot ds:snapshot.getChildren())
                                                                {
                                                                    String child=ds.getKey();
                                                                    snapshot.getRef().child(child).child("uDp").setValue(downloadUri.toString());
                                                                }
                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError error) {

                                                            }
                                                        });

                                                    }

                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });




                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        pd.dismiss();
                                        Toast.makeText(getActivity(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(getActivity(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        // update user image in the comment secton of the posts


    }

    public void checkUserStatus()
    {
        FirebaseUser user= auth.getCurrentUser();
        if(user!=null)
        {
            //email.setText(user.getEmail());
            uid=user.getUid();
        }
        else {
            startActivity(new Intent(getActivity(),MainActivity.class));
            getActivity().finish();
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main,menu);
     MenuItem  item =menu.findItem(R.id.action_search);

        SearchView searchView=(SearchView) MenuItemCompat.getActionView(item);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(!TextUtils.isEmpty(query))
                {
                   searchMyPosts(query);
                }
                else
                {
                    loadMyPosts();
                }
                return  false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(!TextUtils.isEmpty(newText))
                {
                    searchMyPosts(newText);
                }
                else
                {
                    loadMyPosts();
                }
                return false;
            }
        });

        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
        if(id==R.id.action_logout)
        {
            auth.signOut();
            checkUserStatus();
            return  true;
        }
        if(id==R.id.action_settings)
        {
            startActivity(new Intent(getActivity(),SettingsActivity.class));


        }
        if(id==R.id.action_add_post)
        {
            startActivity(new Intent(getActivity(),AddPostActivity.class));

        }
        return super.onOptionsItemSelected(item);
    }
}