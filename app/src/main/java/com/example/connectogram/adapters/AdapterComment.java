package com.example.connectogram.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import  java.util.*;

import com.example.connectogram.PostDetailsActivity;
import com.example.connectogram.R;
import com.example.connectogram.models.ModelComment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

public class AdapterComment extends  RecyclerView.Adapter<AdapterComment.Myholder>{

    Context context;
    List<ModelComment> commentList;
    String myUid,postId;

    public AdapterComment(Context context, List<ModelComment> commentList,String myUid,String postId) {
        this.context = context;
        this.commentList = commentList;
        this.myUid=myUid;
        this.postId=postId;
    }

    @NonNull
    @Override
    public Myholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view= LayoutInflater.from(context).inflate(R.layout.row_comments,parent,false);

        return new Myholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Myholder holder, int position) {
        String uid=commentList.get(position).getUid();
        String cid=commentList.get(position).getcId();
        String uName=commentList.get(position).getUname();
        String uEmail=commentList.get(position).getuEmail();
        String comment=commentList.get(position).getComment();
        String timestamp=commentList.get(position).getTimestamp();
        String uDp=commentList.get(position).getuDp();

        Calendar cal=Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(Long.parseLong(timestamp));
        String  pTime= DateFormat.format("dd/MM/yyyy hh:mm aa",cal).toString();

holder.commentTv.setText(comment);;
holder.nameTv.setText(uName);
holder.timeTv.setText(pTime);;
try
{
    Picasso.get().load(uDp).placeholder(R.drawable.ic_profile).into(holder.avatarIv);
}
catch ( Exception e)
{
    System.out.println(e);
}
holder.itemView.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        if(myUid.equals(uid))
        {
            AlertDialog.Builder builder=new AlertDialog.Builder(v.getRootView().getContext());
            builder.setTitle("Delete");
            builder.setMessage("Are you sure to delete Comment");
            builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
//   delte comment

                    
                    deleteComment(cid);
                    

                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create().show();;
        }
        else {

            Toast.makeText(context,"Cant Delete Others Comment",Toast.LENGTH_SHORT).show();

        }
    }
});

    }

    private void deleteComment(String cid) {
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        ref.child("Comments").child(cid).removeValue();

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String comments=""+snapshot.child("pComments").getValue();
                int newCommentBal=Integer.parseInt(comments)-1;
                ref.child("pComments").setValue(""+newCommentBal);


                //whenver an commment is dleleted it shoud reflect in the comments section

//                Intent i=new Intent(context, PostDetailsActivity.class);
//                i.putExtra("postId",postId);
//                context.startActivity(i);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return commentList.size()  ;
    }

    class Myholder extends RecyclerView.ViewHolder
    {
        ImageView avatarIv;
        TextView nameTv,timeTv,commentTv;
        public Myholder(@NonNull View itemView) {
            super(itemView);
            avatarIv=itemView.findViewById(R.id.avatarIv);
            nameTv=itemView.findViewById(R.id.nameTv);
            timeTv=itemView.findViewById(R.id.timeTv);
            commentTv=itemView.findViewById(R.id.commentTv);

        }
    }
}
