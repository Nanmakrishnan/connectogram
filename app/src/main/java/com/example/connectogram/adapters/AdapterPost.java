package com.example.connectogram.adapters;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.collection.ArraySet;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.connectogram.AddPostActivity;
import com.example.connectogram.OthersProfileActivity;
import com.example.connectogram.PostDetailsActivity;
import com.example.connectogram.R;
import com.example.connectogram.models.ModelPost;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.example.connectogram.notifications.Data;
import com.example.connectogram.notifications.Sender;
import com.example.connectogram.notifications.Token;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.text.Format.*;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdapterPost extends  RecyclerView.Adapter<AdapterPost.Myholder> {


    DatabaseReference pLikesref;
    DatabaseReference postref;
    private int currentScrollPosition = RecyclerView.NO_POSITION;
    // view holder class
    Context context;
    List<ModelPost>postlist;
    String  myUid,myName;
    boolean mProcesLike=false;
    public AdapterPost(Context context, List<ModelPost> postlist) {
        this.context = context;
        this.postlist = postlist;
        myUid= FirebaseAuth.getInstance().getCurrentUser().getUid();
        pLikesref=FirebaseDatabase.getInstance().getReference().child("Likes");
        postref=FirebaseDatabase.getInstance().getReference().child("Posts");

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
        usersRef.child(myUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    myName = dataSnapshot.child("name").getValue(String.class);
                    // Now you have your name (myName), you can use it as needed

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle onCancelled
            }
        });



    }



    @NonNull
    @Override
    public Myholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view= LayoutInflater.from(context).inflate(R.layout.row_posts,parent,false);


        return new Myholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Myholder holder, @SuppressLint("RecyclerView") int position) {
        String uid=postlist.get(position).getUid();
        String uEmail=postlist.get(position).getuEmail();
        String uName=postlist.get(position).getuName();
        String uDp=postlist.get(position).getuDp();
        String pId=postlist.get(position).getpId();
        String pTitle=postlist.get(position).getpTitle();
        String pDesc=postlist.get(position).getpDesc();
        String pImage=postlist.get(position).getpImage();
        String pTimestamp=postlist.get(position).getpTime();
        String pLikes=postlist.get(position).getpLikes();
        String pComments=postlist.get(position).getpComments();

        Calendar cal=Calendar.getInstance(Locale.getDefault());
 cal.setTimeInMillis(Long.parseLong(pTimestamp));
 String  pTime= DateFormat.format("dd/MM/yyyy hh:mm aa",cal).toString();

 // set data;
      holder.uNameTV.setText(uName);;
      if(uName.equals("")) {
          holder.uNameTV.setText("Unnamed User");
      }
        holder.pTimeTv.setText(pTime);
        holder.pTitleTv.setText(pTitle);
        holder.pDescTv.setText(pDesc);
        holder.pLikeTv.setText(pLikes+" Likes");
        holder.pCommentTv.setText(pComments+" comments");

        setLikes(holder,pId);


        try{
            if(!uDp.equals(""))
            {
            Picasso.get().load(uDp).placeholder(R.drawable.ic_profile).into(holder.uPictureIv);}
            else {
                Picasso.get().load(R.drawable.ic_profile).placeholder(R.drawable.ic_profile).into(holder.uPictureIv);
            }
        }
        catch ( Exception e)
        {
            System.out.println(e);
        }

            if (pImage.equals("noImage")) {
                holder.pImgageIv.setVisibility(View.GONE);
            } else {
                if(holder.pImgageIv.equals("")==false &&holder.pImgageIv!=null){
                holder.pImgageIv.setVisibility(View.VISIBLE);
                try {
                    Picasso.get().load(pImage).placeholder(R.drawable.ic_photo).resize(1000, 1200).onlyScaleDown().centerCrop().into(holder.pImgageIv);
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
            }



        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptions(holder.moreBtn,uid,myUid,pId,pImage);
            }
        });
        holder.likeBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                currentScrollPosition = position;
                int pLikes=Integer.parseInt(postlist.get(position).getpLikes());
                mProcesLike=true;
                //get id of the post clickde

                String postid=postlist.get(position).getpId();
                pLikesref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(mProcesLike)
                        {
                            if(snapshot.child(postid).hasChild(myUid))
                            {
                                postref.child(postid).child("pLikes").setValue(""+(pLikes-1));
                                pLikesref.child(postid).child(myUid).removeValue();
                                mProcesLike=false;
                            }
                            else {
                                // not liked
                                postref.child(postid).child("pLikes").setValue(""+(pLikes+1));
                                pLikesref.child(postid).child(myUid).setValue("Liked");
                                mProcesLike=false;

                                sendLikeNotification(uid,myName);


                            }
                            postlist.get(position).setpLikes(String.valueOf(pLikes + (snapshot.child(postid).hasChild(myUid) ? -1 : 1)));
                            notifyItemChanged(position);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });

        holder.commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               Intent i=new Intent(context, PostDetailsActivity.class);
               i.putExtra("postId",pId);
               context.startActivity(i);
            }
        });

        holder.profileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(context, OthersProfileActivity.class);
                intent.putExtra("uid",uid);
                context.startActivity(intent);
            }
        });

        holder.shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              // what deos share button do

                BitmapDrawable bitmapDrawable=(BitmapDrawable)holder.pImgageIv.getDrawable()    ;
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










    }


        private void sendLikeNotification(String postOwnerUid, String senderName) {
            Toast.makeText(context,"Sending notificatoin TO "+postOwnerUid,Toast.LENGTH_SHORT).show();;
            DatabaseReference tokensRef = FirebaseDatabase.getInstance().getReference("Tokens").child(postOwnerUid);
            tokensRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Token token = dataSnapshot.getValue(Token.class);
                        if (token != null) {

                            Data data = new Data(
                                    "" + myUid,
                                    "LikeNotification",
                                    senderName + " liked your post.",
                                    R.drawable.ic_liked,
                                    "New Like",
                                    postOwnerUid+""
                            );
                            Sender sender = new Sender(data, token.getToken());

                            try {
                                JSONObject senderJsonObj = new JSONObject(new Gson().toJson(sender));
                                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                                        "https://fcm.googleapis.com/fcm/send",
                                        senderJsonObj,
                                        response -> {
                                            // Handle successful response
                                            Log.d("JSON_RESPONSE", "onResponse: " + response.toString());
                                            Toast.makeText(context, "Notification Sent Successfully", Toast.LENGTH_SHORT).show();
                                        },
                                        error -> {
                                            // Handle error
                                            Log.d("JSON_RESPONSE", "onError: " + error.toString());
                                            Toast.makeText(context, "Failed to send notification", Toast.LENGTH_SHORT).show();
                                        }
                                ) {
                                    @Override
                                    public Map<String, String> getHeaders() throws AuthFailureError {
                                        // Add headers required for the request, such as Content-Type and Authorization
                                        Map<String, String> headers = new HashMap<>();
                                        headers.put("Content-Type", "application/json");
                                        headers.put("Authorization", "key=AAAAenyNnco:APA91bElB1Mr3OvgkWme4uMYLUrkPbllU0kle1z8lIQUQrXP0v_3x1_-DD6blJAc4pASjFvmI7GOvovcbIHMF8XeU40rxNdqd9RPCagQu61o-HnsXnzJBOlnnv8Kqz07mquPpNMjCpwM");
                                        return headers;
                                    }
                                };

                                // Add the request to the Volley request queue
                                RequestQueue requestQueue= Volley.newRequestQueue(context);
                                requestQueue.add(jsonObjectRequest);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle onCancelled
                }
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
        context.startActivity(Intent.createChooser(i,"Share Via"));



    }

    private Uri saveImageToShare(Bitmap bitmap) {
        File imageFolder=new File(context.getCacheDir(),"images");
        Uri uri=null;
        try {
            imageFolder.mkdirs();
            File file=new File(imageFolder,"shared_image.png");

            FileOutputStream stream=new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG,90,stream);
            stream.flush();
            stream.close();;
            uri= FileProvider.getUriForFile(context,"com.example.connectogram.fileprovider",file);

        }
        catch ( Exception e)
        {
Toast.makeText(context,e.getMessage(),Toast.LENGTH_SHORT );

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
        context.startActivity(Intent.createChooser(i,"Share Via"));



    }

    private void setLikes(Myholder holder, String postkey) {
   pLikesref.addValueEventListener(new ValueEventListener() {
       @Override
       public void onDataChange(@NonNull DataSnapshot snapshot) {
           if(snapshot.child(postkey).hasChild(myUid))
           {
                    holder.likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_liked,0,0,0);
                   // holder.likeBtn.setText("Liked");
           }
           else {
               holder.likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_notliked,0,0,0);
              // holder.likeBtn.setText("Like");
           }
       }

       @Override
       public void onCancelled(@NonNull DatabaseError error) {

       }
   });
    }

    private void showMoreOptions(ImageButton moreBtn, String uid, String myUid, String pId, String pImage) {
        PopupMenu popupMenu=new PopupMenu(context,moreBtn, Gravity.END);
        if(uid.equals(myUid)) {

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
                    beginDelete(pId,pImage);
                }
                if(id==1)
                {
                    Intent i=new Intent(context, AddPostActivity.class);
                    i.putExtra("key","editPost");
                    i.putExtra("editPostId",pId);
                    context.startActivity(i);

                }
                if(id==2)
                {
                    Intent i=new Intent(context, PostDetailsActivity.class);
                    i.putExtra("postId",pId);
                    context.startActivity(i);

                }
                return false;
            }
        });
popupMenu.show();
    }

    private void beginDelete(String pId, String pImage) {

        if(pImage.equals("noImage"))
        {
            deleteWithouImage(pId,pImage);
        }
        else {
            deleteWithImage(pId,pImage);
        }
    }

    private void deleteWithImage(String pId, String pImage) {
        ProgressDialog pd=new ProgressDialog(context);
        pd.setMessage("Deleting");
        StorageReference picref= FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        picref.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Query Q= FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
            Q.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot ds:snapshot.getChildren())
                    {
                        ds.getRef().removeValue();
                    }
                    Toast.makeText(context,"Deleted Post",Toast.LENGTH_SHORT).show();
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
        Toast.makeText(context,e+" ",Toast.LENGTH_SHORT).show();;
            }
        });

    }

    private void deleteWithouImage(String pId, String pImage) {
        ProgressDialog pd=new ProgressDialog(context);
        pd.setMessage("Deleting");
        Query Q= FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
        Q.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds:snapshot.getChildren())
                {
                    ds.getRef().removeValue();
                }
                Toast.makeText(context,"Deleted Post",Toast.LENGTH_SHORT).show();
                pd.dismiss();;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return postlist.size();
    }


    class  Myholder extends RecyclerView.ViewHolder
    {
            ImageView uPictureIv,pImgageIv;
            TextView uNameTV,pTimeTv,pTitleTv,pDescTv,pLikeTv,pCommentTv;
            ImageButton moreBtn;
            Button likeBtn,commentBtn,shareBtn;
            LinearLayout profileLayout;


        public Myholder(@NonNull View itemView) {
            super(itemView);


            uPictureIv=itemView.findViewById(R.id.uPictureIv);
            uNameTV=itemView.findViewById(R.id.uNameTv);
            pImgageIv=itemView.findViewById(R.id.pImageIv);
            pTitleTv=itemView.findViewById(R.id.pTitleTv);
            pTimeTv=itemView.findViewById(R.id.pTimeTv);
            pDescTv=itemView.findViewById(R.id.pDescTv);
            pLikeTv=itemView.findViewById(R.id.pLikeTv);
            moreBtn=itemView.findViewById(R.id.moreBtn);
            likeBtn=itemView.findViewById(R.id.likeBtn);
            commentBtn=itemView.findViewById(R.id.commentBtn);
            shareBtn=itemView.findViewById(R.id.shareBtn);
            profileLayout=itemView.findViewById(R.id.profileLayout);
            pCommentTv=itemView.findViewById(R.id.pCommentTv);


        }
    }
}
