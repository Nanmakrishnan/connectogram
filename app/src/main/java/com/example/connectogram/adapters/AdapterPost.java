package com.example.connectogram.adapters;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.text.format.DateFormat;
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
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

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
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.text.Format.*;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterPost extends  RecyclerView.Adapter<AdapterPost.Myholder> {


    DatabaseReference pLikesref;
    DatabaseReference postref;

    // view holder class
    Context context;
    List<ModelPost>postlist;
    String  myUid;
    boolean mProcesLike=false;
    public AdapterPost(Context context, List<ModelPost> postlist) {
        this.context = context;
        this.postlist = postlist;
        myUid= FirebaseAuth.getInstance().getCurrentUser().getUid();
        pLikesref=FirebaseDatabase.getInstance().getReference().child("Likes");
        postref=FirebaseDatabase.getInstance().getReference().child("Posts");

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
      if(uName.equals(""))
          holder.uNameTV.setText("Default User");
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
        }
        catch ( Exception e)
        {
            System.out.println(e);
        }

            if (pImage.equals("noImage")) {
                holder.pImgageIv.setVisibility(View.GONE);
            } else {
                holder.pImgageIv.setVisibility(View.VISIBLE);
                try {
                    Picasso.get().load(pImage).resize(1000, 1200).onlyScaleDown().centerCrop().into(holder.pImgageIv);
                } catch (Exception e) {
                    System.out.println(e);
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

                            }
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
                    holder.likeBtn.setText("Liked");
           }
           else {
               holder.likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_notliked,0,0,0);
               holder.likeBtn.setText("Like");
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
