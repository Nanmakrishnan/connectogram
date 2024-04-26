package com.example.connectogram.adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectogram.R;
import com.example.connectogram.models.ModelChat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.lang.reflect.InvocationTargetException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;


public class AdapterChat extends  RecyclerView.Adapter<AdapterChat.myholder>{

    private  static  final  int MSG_TYPE_LEFT=0;
    private  static  final  int MSG_TYPE_RIGHT=1;
    Context context;
List<ModelChat>chatlist;
String imageUrl;
FirebaseUser fuser;
    public AdapterChat(Context context, List<ModelChat> chatlist, String imageUrl) {
        this.context = context;
        this.chatlist = chatlist;
        this.imageUrl = imageUrl;
    }

    @NonNull
    @Override
    public myholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if(viewType==MSG_TYPE_RIGHT)
        {
            View view= LayoutInflater.from(context).inflate(R.layout.row_chat_right,parent,false);
            return  new myholder(view);

        }
        else
        {
            View view= LayoutInflater.from(context).inflate(R.layout.row_chat_left,parent,false);
            return  new myholder(view);


        }

    }

    @Override
    public void onBindViewHolder(@NonNull myholder holder, @SuppressLint("RecyclerView") int position) {

        //getting data
        String message=chatlist.get(position).getMessage();
        String timestamp=chatlist.get(position).getTimestamp();
        String type=chatlist.get(position).getType();
        Calendar cal=Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(Long.parseLong(timestamp));

        String datetime= DateFormat.format("dd/MM/yyyy hh:mm aa",cal).toString();
        System.out.println(holder);
       // holder.messageTv.setText(message);
        holder.timeTv.setText(datetime);
       //holder.isSeenTv.setText("");
if(type.equals("text"))
{
    holder.messageTv.setVisibility(View.VISIBLE);;
    holder.messageIv.setVisibility(View.GONE);
    holder.messageTv.setText(message);

}
else {
    holder.messageTv.setVisibility(View.GONE);;
    holder.messageIv.setVisibility(View.VISIBLE);

    try {
        Picasso.get().load(message).placeholder(R.drawable.ic_image_black).into(holder.messageIv);
    }
    catch ( Exception e)
    {

    }

}
        try{
            Picasso.get().load(imageUrl).placeholder(R.drawable.ic_person_blue).into(holder.profileIv);
        }
        catch ( Exception e)
        {
            System.out.println(e);
        }

    holder.messageLayout.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AlertDialog.Builder builder=new AlertDialog.Builder(context);
            builder.setTitle("Delete");
            builder.setMessage("Are you sure to delete this message ?");
            builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    deleteMessage(position);
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();;
                }
            });
            builder.create().show();
        }
    });

        if(position==chatlist.size()-1) {
            if (chatlist.get(position).isSeen())
                holder.isSeenTv.setText("Seen");
            else
                holder.isSeenTv.setText("Delived");

        }
        else
            holder.isSeenTv.setVisibility(View.GONE);



    }

    private void deleteMessage(int position) {
        String myuid=FirebaseAuth.getInstance().getCurrentUser().getUid();

        String timestamp=chatlist.get(position).getTimestamp();
        DatabaseReference dbref= FirebaseDatabase.getInstance().getReference("Chats");
        Query Q=dbref.orderByChild("timestamp").equalTo(timestamp);
            Q.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot ds:snapshot.getChildren())
                    {

                        if(ds.child("sender").getValue().equals(myuid)) {

                            // use the below line only the message should be compltely deleted (not even messaage)//no trace
                            //ds.getRef().removeValue();

                            HashMap<String, Object> hm = new HashMap<>();
                            hm.put("message", "This message was deleted");
                            ds.getRef().updateChildren(hm);
                            Toast.makeText(context,"Message Delted",Toast.LENGTH_SHORT).show();
                        }
                        else
                            Toast.makeText(context,"You Cant Delte others messages",Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
    }

    @Override
    public int getItemCount() {
        return chatlist.size();
    }

    @Override
    public int getItemViewType(int position) {
        fuser= FirebaseAuth.getInstance().getCurrentUser();
        if(chatlist.get(position).getSender().equals(fuser.getUid()))
        {
            return  MSG_TYPE_RIGHT;
        }
        else
            return  MSG_TYPE_LEFT;

    }

    class myholder extends RecyclerView.ViewHolder
    {

        ImageView profileIv,messageIv;
        TextView messageTv,timeTv,isSeenTv;
        LinearLayout messageLayout;


        public myholder(@NonNull View itemView) {
            super(itemView);
            profileIv=itemView.findViewById(R.id.profileIv);
            messageTv=itemView.findViewById(R.id.messageTv);
            timeTv=itemView.findViewById(R.id.timeTv);
            isSeenTv=itemView.findViewById(R.id.isSeenTv);
            messageLayout=itemView.findViewById(R.id.messageLayout);
            messageIv= itemView.findViewById(R.id.messageIv);



        }
    }
}
