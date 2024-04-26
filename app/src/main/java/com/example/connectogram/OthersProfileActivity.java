package com.example.connectogram;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.MenuItemCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectogram.adapters.AdapterPost;
import com.example.connectogram.models.ModelPost;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class OthersProfileActivity extends AppCompatActivity {
RecyclerView postRecycleView;
    List<ModelPost> postList;
    AdapterPost adapterPost;
    String uid;
    ImageView avatarTv;
    DatabaseReference reference;
    TextView nameTv,emailTv,phoneTv,yearTv,semTv,bioTv;
FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_others_profile);
        postRecycleView=findViewById(R.id.recycleview_posts);
        firebaseAuth=FirebaseAuth.getInstance();
        uid=getIntent().getStringExtra("uid");
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users");
        nameTv=findViewById(R.id.nameTv);
        avatarTv=findViewById(R.id.avatarTv);
        emailTv=findViewById(R.id.emailTv);
        phoneTv=findViewById(R.id.phoneTv);
        yearTv=findViewById(R.id.yearTv);
        semTv=findViewById(R.id.semTv);
        bioTv=findViewById(R.id.bioTv);
        postList=new ArrayList<>();
        System.out.println("Recived uid is "+uid);
        Query query=reference.orderByChild("uid").equalTo(uid);
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

        checkUserStatus();

        loadUserPosts();


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    private void loadUserPosts() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        postRecycleView.setLayoutManager(linearLayoutManager);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");

        Query q = ref.orderByChild("uid").equalTo(uid);
        q.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelPost modelPost = ds.getValue(ModelPost.class);
                    if (modelPost != null && modelPost.getUid().equals(uid)) {

                        postList.add(modelPost);
                    }
                }
                adapterPost = new AdapterPost(OthersProfileActivity.this, postList);
                postRecycleView.setAdapter(adapterPost);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OthersProfileActivity.this, "" + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    private  void searchUserPost(String searchquery)
    {
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(OthersProfileActivity.this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        postRecycleView.setLayoutManager(linearLayoutManager);


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

                    adapterPost=new AdapterPost(OthersProfileActivity.this,postList);

                    postRecycleView.setAdapter(adapterPost);


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(OthersProfileActivity.this,""+error,Toast.LENGTH_SHORT).show();


            }
        });





    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        menu.findItem(R.id.action_add_post).setVisible(false);
        MenuItem  item =menu.findItem(R.id.action_search);

        SearchView searchView=(SearchView) MenuItemCompat.getActionView(item);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(!TextUtils.isEmpty(query))
                {
                    searchUserPost(query);
                }
                else
                {
                   loadUserPosts();
                }
                return  false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(!TextUtils.isEmpty(newText))
                {
                    searchUserPost(newText);
                }
                else
                {
                   loadUserPosts();
                }
                return false;
            }
        });
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
            //email.setText(user.getEmail());
           // uid=user.getUid();
        }
        else {
            startActivity(new Intent(this,MainActivity.class));
           finish();
        }

    }


}