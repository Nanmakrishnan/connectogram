package com.example.connectogram;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.File;
import java.io.FileOutputStream;
import  java.util.*;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectogram.adapters.AdapterComment;
import com.example.connectogram.adapters.AdapterPost;
import com.example.connectogram.models.ModelComment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class PostDetailsActivity extends AppCompatActivity {

    EditText commentEt;
    ImageButton sendBtn,moreBtn;

    ImageView uPictueTv,pImageIv, pPictureIv;
    TextView nameTv,pTimeTv,pTitleTv,pDescTv,pLikesTv,pCommentsTv;
    Button likeBtn,shareBtn;
    LinearLayout profileLayout;
    ProgressDialog pd;
    boolean mProcessComment=false;
    boolean mProcesLike=false;
    RecyclerView recyclerView;
    AdapterComment adapterComment;
    List<ModelComment> commentList;
    String myUid,myEmail,myName,myDp,postId,pLikes,hisDp,hisName,hisUid, pImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_post_details);

        ///to get detail of user;


postId=getIntent().getStringExtra("postId");


        commentEt=findViewById(R.id.commentEt);
        sendBtn=findViewById(R.id.sendBtn);
        uPictueTv=findViewById(R.id.cAvatharIv);
        nameTv=findViewById(R.id.uNameTv);
        pLikesTv=findViewById(R.id.pLikeTv);
        pTimeTv=findViewById(R.id.pTimeTv);
        pTitleTv=findViewById(R.id.pTitleTv);
        pImageIv=findViewById(R.id.pImageIv);
        pDescTv=findViewById(R.id.pDescTv);
        likeBtn=findViewById(R.id.likeBtn);
        shareBtn=findViewById(R.id.shareBtn);
        moreBtn=findViewById(R.id.moreBtn);
        profileLayout=findViewById(R.id.profileLayout);
        pCommentsTv=findViewById(R.id.pCommentTv);
        pd=new ProgressDialog(this);
        pPictureIv=findViewById(R.id.uPictureIv);
        recyclerView=findViewById(R.id.recycleView);

        loadPostInfo();
        checkUserStaus();;

        loadUserInfo();
        setLikes();
        
        loadComments();



// neeed to set the subtitle for the actionbar

sendBtn.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        postComment();
    }
});

likeBtn.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
       likePost();
    }
});
moreBtn.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        showMoreOptions();
    }
});

shareBtn.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {

        String pTitle=pTitleTv.getText().toString().trim();
        String pDesc=pDescTv.getText().toString().trim();

        BitmapDrawable bitmapDrawable=(BitmapDrawable)pImageIv.getDrawable()    ;
        if(bitmapDrawable==null)
        {
            // share a post withouit image

            shareTextOnly(pTitle,pDesc);

        }
        else {
            Bitmap bitmap=bitmapDrawable.getBitmap();

            shareImageAndText(pTitle,pDesc,bitmap);
        }


    }
});

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    private void shareImageAndText(String pTitle, String pDesc, Bitmap bitmap) {
        String shareBody=pTitle+"\n"+pDesc;
        Uri uri=saveImageToShare(bitmap);

        // SHARE INTENT
        Intent i=new Intent(Intent.ACTION_SEND);
        i.putExtra(Intent.EXTRA_STREAM,uri  );
        i.putExtra(Intent.EXTRA_TEXT,shareBody);
        i.putExtra(Intent.EXTRA_SUBJECT,"Shared a post With Image");
        i.setType("image/png");
      startActivity(Intent.createChooser(i,"Share Via"));



    }

    private Uri saveImageToShare(Bitmap bitmap) {
        File imageFolder=new File(getCacheDir(),"images");
        Uri uri=null;
        try {
            imageFolder.mkdirs();
            File file=new File(imageFolder,"shared_image.png");

            FileOutputStream stream=new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG,90,stream);
            stream.flush();
            stream.close();;
            uri= FileProvider.getUriForFile(this,"com.example.connectogram.fileprovider",file);

        }
        catch ( Exception e)
        {
            Toast.makeText(this,e.getMessage(),Toast.LENGTH_SHORT );

        }
        return  uri;

    }

    private void shareTextOnly(String pTitle, String pDesc) {

        String shareBody=pTitle+"\n"+pDesc;

        Intent i=new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");

        // sharing via email or so
        i.putExtra(Intent.EXTRA_SUBJECT,"Subject here ");
        i.putExtra(Intent.EXTRA_TEXT,shareBody);
        startActivity(Intent.createChooser(i,"Share Via"));



    }
    private void loadComments() {
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        commentList=new ArrayList<>();

     DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");
ref.addValueEventListener(new ValueEventListener() {
    @Override
    public void onDataChange(@NonNull DataSnapshot snapshot) {
        commentList.clear();;
        for(DataSnapshot ds:snapshot.getChildren())
        {
            ModelComment modelComment=ds.getValue(ModelComment.class);
            commentList.add(modelComment);

            adapterComment=new AdapterComment(getApplicationContext(),commentList,myUid,postId);
            recyclerView.setAdapter(adapterComment);



        }
    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {

    }
});
    }

    private void showMoreOptions() {
        PopupMenu popupMenu=new PopupMenu(this,moreBtn, Gravity.END);
        if(hisUid.equals(myUid)) {

            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Delete");
            popupMenu.getMenu().add(Menu.NONE, 1, 0, "Edit");
        }
        popupMenu.getMenu().add(Menu.NONE, 2, 0, "View Details");

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id=item.getItemId();
                if(id==0)
                {
                    beginDelete();
                }
                if(id==1)
                {
                    Intent i=new Intent(PostDetailsActivity.this, AddPostActivity.class);
                    i.putExtra("key","editPost");
                    i.putExtra("editPostId",postId);
                    startActivity(i);

                }

                return false;
            }
        });
        popupMenu.show();
    }

    private void beginDelete() {
        if(pImage.equals("noImage"))
        {
            deleteWithouImage();
        }
        else {
            deleteWithImage();
        }
    }

    private void deleteWithImage() {

        ProgressDialog pd=new ProgressDialog(this);
        pd.setMessage("Deleting");
        StorageReference picref= FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        picref.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Query Q= FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);
                Q.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds:snapshot.getChildren())
                        {
                            ds.getRef().removeValue();
                        }
                        Toast.makeText(PostDetailsActivity.this,"Deleted Post",Toast.LENGTH_SHORT).show();
                        pd.dismiss();;
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();;
                Toast.makeText(PostDetailsActivity.this,e+" ",Toast.LENGTH_SHORT).show();;
            }
        });

    }

    private void deleteWithouImage() {
        ProgressDialog pd=new ProgressDialog(this);
        pd.setMessage("Deleting");
        Query Q= FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);
        Q.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds:snapshot.getChildren())
                {
                    ds.getRef().removeValue();
                }
                Toast.makeText(PostDetailsActivity.this,"Deleted Post",Toast.LENGTH_SHORT).show();
                pd.dismiss();;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void likePost() {


        mProcesLike=true;
        //get id of the post clickde

       DatabaseReference pLikesref=FirebaseDatabase.getInstance().getReference().child("Likes");
       DatabaseReference postref=FirebaseDatabase.getInstance().getReference().child("Posts");
        pLikesref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(mProcesLike)
                {
                    if(snapshot.child(postId).hasChild(myUid))
                    {
                        postref.child(postId).child("pLikes").setValue(""+(Integer.parseInt(pLikes)-1));
                        pLikesref.child(postId).child(myUid).removeValue();
                        mProcesLike=false;

                    }
                    else {
                        // not liked
                        postref.child(postId).child("pLikes").setValue(""+(pLikes+1));
                        pLikesref.child(postId).child(myUid).setValue("Liked");
                        mProcesLike=false;


                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void postComment() {
pd.setMessage("Adding comment");

String cmg=commentEt.getText().toString().trim();
if(TextUtils.isEmpty(cmg))
{
    Toast.makeText(this,"Empty commnet",Toast.LENGTH_SHORT).show();;
    return;
}
DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");
String timestamp=String.valueOf(System.currentTimeMillis());
        HashMap<String,Object>hm=new HashMap<>();
        hm.put("cId",timestamp);
        hm.put("comment",cmg);
        hm.put("timestamp",timestamp);
        hm.put("uid",myUid);
        hm.put("uEmail",myEmail);
        hm.put("uName",myName);
        hm.put("uDp",myDp);

       ref.child(timestamp).setValue(hm).addOnSuccessListener(new OnSuccessListener<Void>() {
           @Override
           public void onSuccess(Void unused) {

               pd.dismiss();;
               Toast.makeText(PostDetailsActivity.this,"comment Added",Toast
                       .LENGTH_SHORT).show();;
                       commentEt.setText("");
                       updateCommentCount();

           }
       }).addOnFailureListener(new OnFailureListener() {
           @Override
           public void onFailure(@NonNull Exception e) {
               pd.dismiss();;
               Toast.makeText(PostDetailsActivity.this,"unable to add comment",Toast
                       .LENGTH_SHORT).show();;
           }
       });



    }
    private void setLikes() {
        DatabaseReference pLikesref=FirebaseDatabase.getInstance().getReference().child("Likes");
        DatabaseReference postref=FirebaseDatabase.getInstance().getReference().child("Posts");
        pLikesref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(postId).hasChild(myUid))
                {
                    likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_liked,0,0,0);
                    likeBtn.setText("Liked");
                }
                else {likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_notliked,0,0,0);
                    likeBtn.setText("Like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void updateCommentCount() {
        mProcessComment=true;
     DatabaseReference r  = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
r.addValueEventListener(new ValueEventListener() {
    @Override
    public void onDataChange(@NonNull DataSnapshot snapshot) {
        if(mProcessComment)
        {
            String comments=""+snapshot.child("pComments").getValue();
            int newCommentBal=Integer.parseInt(comments)+1;
            r.child("pComments").setValue(""+newCommentBal);
            mProcessComment=false;

        }
    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {

    }
});


    }

    private void loadUserInfo() {
        Query q=FirebaseDatabase.getInstance().getReference("Users").orderByChild("uid").equalTo(myUid);
        q.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds:snapshot.getChildren())
                {
                myName=""+ds.child("name").getValue();
                myDp=""+ds.child("image").getValue();

                try {
                        Picasso .get().load(myDp).placeholder(R.drawable.ic_profile).into(uPictueTv);
                }
                catch (Exception e)
                {
                    System.out.println(e.getMessage());
                    Picasso.get().load(R.drawable.ic_profile).into(uPictueTv);;
                }


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadPostInfo() {
        DatabaseReference dref= FirebaseDatabase.getInstance().getReference("Posts");
        Query q= dref.orderByChild("pId").equalTo(postId);
        q.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds:snapshot.getChildren())
                {
                    String pTitle=""+ds.child("pTitle").getValue();
                    String pDSesc=""+ds.child("pDesc").getValue();
                     pImage=""+ds.child("pImage").getValue();
                     pLikes=""+ds.child("pLikes").getValue();
                    String pTimestamp=""+ds.child("pTime").getValue();
                    hisDp=""+ds.child("uDp").getValue();
                    hisUid=""+ds.child("uid").getValue();
                    String uEmail=""+ds.child("uEmail").getValue();
                    hisName=""+ds.child("uName").getValue();
                    String cmtcont=""+ds.child("pComments").getValue();




                    Calendar cal=Calendar.getInstance(Locale.getDefault());
                    cal.setTimeInMillis(Long.parseLong(pTimestamp));
                    String  pTime= DateFormat.format("dd/MM/yyyy hh:mm aa",cal).toString();

                    pTitleTv.setText(pTitle);;
                    pTimeTv.setText(pTime);;
                    pDescTv.setText(pDSesc);
                    pLikesTv.setText(pLikes+" Likes");
                    nameTv.setText(hisName);
                    pCommentsTv.setText(cmtcont+" Comments");

                    if(pImage.equals("noImage"))
                    {
                        pImageIv.setVisibility(View.GONE);
                    }
                    else {
                        pImageIv.setVisibility(View.VISIBLE);
                        try {
                            Picasso.get().load(pImage).into(pImageIv);
                            Picasso.get().load(hisDp).into(pPictureIv);;
                        } catch (Exception e) {
                            System.out.println(e);
                        }
                    }
                    try {
                Picasso.get().load(hisDp).placeholder(R.drawable.ic_profile).into(uPictueTv);
                    }
                    catch (Exception e)
                    {
                        Picasso.get().load(R.drawable.ic_profile).into(uPictueTv);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
private  void checkUserStaus()
{
    FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();;
    if(user!=null)
    {
        myEmail= user.getEmail();
        myUid=user.getUid();

    }
    else {
        startActivity(new Intent(this,MainActivity.class));
    }

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
            FirebaseAuth.getInstance().signOut();;
            checkUserStaus();;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();;
        return super.onSupportNavigateUp();
    }
}