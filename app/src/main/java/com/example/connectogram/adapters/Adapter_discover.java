package com.example.connectogram.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.connectogram.ChatActivity;
import com.example.connectogram.OthersProfileActivity;
import com.example.connectogram.R;
import com.example.connectogram.models.Model_discover;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;

public class Adapter_discover extends RecyclerView.Adapter<Adapter_discover.myholder> {

    Context context;
    List<Model_discover> userlist;

    public Adapter_discover(Context context, List<Model_discover> userlist) {
        this.context = context;
        this.userlist = userlist;
    }

    @NonNull
    @Override
    public myholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view= LayoutInflater.from(context).inflate(R.layout.row_users,parent,false);
        return new myholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull myholder holder, int position) {
        Model_discover model = userlist.get(position);

        String hisUID=userlist.get(position).getUid();

        // Set the data to the views
        holder.nameIv.setText(model.getName());
        if(model.getName()==null||model.getName().equals(""))
            holder.nameIv.setText("Unnamed User");
        holder.emailIv.setText(model.getEmail());
        try {
        if(model.getImage().equals(""))
        {
            Picasso.get().load(R.drawable.ic_profile).placeholder(R.drawable.ic_profile).into(holder.avatarIv);
        }
        else {
            Glide.with(context)
                    .load(model.getImage())
                    .placeholder(R.drawable.ic_profile)
                    .into(holder.avatarIv);
        //    Picasso.get().load(model.getImage()).placeholder(R.drawable.ic_profile).fit().centerCrop().into(holder.avatarIv);

        }



        }
        catch (Exception e)
        {

        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //

                AlertDialog.Builder builder=new AlertDialog.Builder(context);
                builder.setItems(new String[]{"Profile", "Chat"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which==0)
                        {
                            Intent intent=new Intent(context, OthersProfileActivity.class);
                            intent.putExtra("uid",hisUID);
                            context.startActivity(intent);


                        }
                        if(which==1)
                        {
                            Intent intent=new Intent(context,ChatActivity.class);
                            intent.putExtra("hisUid",hisUID);
                            context.startActivity(intent);


                        }

                    }
                });
                builder.create().show();;

            }
        });
    }
    private Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.postRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.postRotate(180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.postRotate(270);
                break;
            default:
                return bitmap;
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }
    @Override
    public int getItemCount() {
        return userlist.size();
    }

    class  myholder extends RecyclerView.ViewHolder
    {
        ImageView avatarIv;
        TextView nameIv,emailIv;

        public myholder(@NonNull View itemView) {
            super(itemView);
            avatarIv=itemView.findViewById(R.id.avatarIv);
            nameIv=itemView.findViewById(R.id.nameIv);
            emailIv=itemView.findViewById(R.id.emailIv);
        }
    }
}
