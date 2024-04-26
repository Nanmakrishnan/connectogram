package com.example.connectogram.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import  java.util.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectogram.ChatActivity;
import com.example.connectogram.HomeFragment;
import com.example.connectogram.R;
import com.example.connectogram.models.ModelChatlist;
import com.example.connectogram.models.Model_discover;
import com.squareup.picasso.Picasso;

public class AdapterChatlist  extends RecyclerView.Adapter<AdapterChatlist.Myholder> {
    Context context;
 List<Model_discover>userlist;
    private  HashMap<String,String>lastMessageMap;
    public AdapterChatlist(Context context, List<Model_discover> userlist) {
        this.context = context;
        this.userlist = userlist;
        this.lastMessageMap = new HashMap<>();
    }


    @NonNull
    @Override
    public Myholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.row_chatlist,parent,false);

        return new Myholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Myholder holder, int position) {

        String hisUid=userlist.get(position).getUid();
        String userImage=userlist.get(position).getImage();
        String userName=userlist.get(position).getName();
        String lastMessage=lastMessageMap.get(hisUid);


        holder.nameTv.setText(userName);;
        if(lastMessage!=null && lastMessage.equals("default"))
        {
            holder.lastMessageTv.setVisibility(View.GONE);

        }
        else {
            holder.lastMessageTv.setVisibility(View.VISIBLE);;
         holder.lastMessageTv.setText(lastMessage);
        }

        try
        {
            Picasso.get().load(userImage).placeholder(R.drawable.ic_profile).into(holder.profileIv);;
        }
        catch(Exception e)
        {
            Picasso.get().load(R.drawable.ic_profile).into(holder.profileIv);;
        }
        if(userlist.get(position).getOnlineStatus().equals("online"))
        {
          holder.onlineStatusIv.setImageResource(R.drawable.circle_online);
        }
        else {
            holder.onlineStatusIv.setImageResource(R.drawable.circle_offline);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start chatActiviy;

                Intent i=new Intent(context, ChatActivity.class);
                i.putExtra("hisUid",hisUid);
                context.startActivity(i);
            }
        });

    }
    public void setLastMessage(String userId,String lastMessage)
    {
        lastMessageMap.put(userId,lastMessage);

    }

    @Override
    public int getItemCount() {
        return userlist.size();
    }

    class Myholder extends RecyclerView.ViewHolder
    {
ImageView profileIv,onlineStatusIv;
TextView nameTv,lastMessageTv;


        public Myholder(@NonNull View itemView) {
            super(itemView);
            profileIv=itemView.findViewById(R.id.profileIv);
            onlineStatusIv=itemView.findViewById(R.id.onlineStatusIv);
            nameTv=itemView.findViewById(R.id.nameTv);
            lastMessageTv=itemView.findViewById(R.id.lastMessageTv);

        }
    }
}
