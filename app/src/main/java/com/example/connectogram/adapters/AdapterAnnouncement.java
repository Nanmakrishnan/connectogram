package com.example.connectogram.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.connectogram.AddPostActivity;
import com.example.connectogram.PostDetailsActivity;
import com.example.connectogram.R;
import com.example.connectogram.models.ModelAnnounce;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterAnnouncement extends RecyclerView.Adapter
        <AdapterAnnouncement.MyViewHolder> {

    private Context context;
    private List<ModelAnnounce> announcementList;
    String contetype="default";

    public AdapterAnnouncement(Context context, List<ModelAnnounce> announcementList) {
        this.context = context;
        this.announcementList = announcementList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_announce, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ModelAnnounce announcement = announcementList.get(position);

        // Set your data to the views in the ViewHolder
        holder.titleTextView.setText(announcement.getaTitle());
        holder.descriptionTextView.setText(announcement.getaDesc());
        holder.uNameTv.setText(announcement.getuName());

        Calendar cal=Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(Long.parseLong(announcement.getaTime()));
        String  pTime= DateFormat.format("dd/MM/yyyy hh:mm aa",cal).toString();

        holder.aTimeTv.setText(pTime);
        holder.sendImage.setVisibility(View.GONE);
        holder.sendVdo.setVisibility(View.GONE);
        holder.sendpdf.setVisibility(View.GONE);
        holder.sendTxt.setVisibility(View.GONE);
        holder.sendWeb.setVisibility(View.GONE);
        String fileUrl = announcement.getaFile();
        if (!fileUrl.equals("null")) {
            Toast.makeText(context,fileUrl+" is the url",Toast.LENGTH_SHORT);
            StorageReference fileRef = FirebaseStorage.getInstance().getReferenceFromUrl(fileUrl);

            // Get metadata properties

            fileRef.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                @Override
                public void onSuccess(StorageMetadata storageMetadata) {
                    // Metadata now contains the metadata for the file
                    contetype = storageMetadata.getContentType();

                    try {
                        String contype=contetype.toLowerCase();
                        if(contype.endsWith("png")||contype.endsWith("jpg")||contype.endsWith("jpeg")) {
                            holder.sendImage.setVisibility(View.VISIBLE);
                            holder.icons.setVisibility(View.GONE);
                            Picasso.get().load(fileUrl).into(holder.sendImage);
                        }
                   else  if(contype.endsWith("mp4")||contype.endsWith("avi")||contype.endsWith("mov")) {
                      // holder.sendVdo.setVisibility(View.VISIBLE);
                       //holder.sendVdo.setVideoURI(Uri.parse(fileUrl));
                            holder.sendTxt.setText("Send A Video file click to open");
                            holder.sendTxt.setVisibility(View.VISIBLE);

                                holder.icons.setVisibility(View.VISIBLE);;
                            Picasso.get().load(R.drawable.ic_action_video).placeholder(R.drawable.ic_action_video).into(holder.icons);
                            holder.icons.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    // Get the file URL of the video
                                    String videoUrl = announcement.getaFile();

                                    // Create an intent to view the video
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setDataAndType(Uri.parse(videoUrl), "video/*");
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                                    // Verify that there's an app to handle this intent
                                    if (intent.resolveActivity(context.getPackageManager()) != null) {
                                        // Start the activity
                                        context.startActivity(intent);
                                    } else {
                                        // If no app can handle the intent, show a toast or dialog indicating so
                                        Toast.makeText(context, "No app found to open video", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        }
                     else if(contype.endsWith("pdf")) {
                            // PDF file
                           // holder.sendpdf.setVisibility(View.VISIBLE);
                    holder.sendImage.setVisibility(View.GONE);

                         //   Toast.makeText(context,"file is a pdf",Toast.LENGTH_SHORT).show();;
                            final long ONE_MEGABYTE = 1024 * 1024;
                            fileRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                @Override
                                public void onSuccess(byte[] bytes) {
                                    // PDF file retrieved successfully as byte array
                                    holder.sendpdf.fromBytes(bytes).scrollHandle(new DefaultScrollHandle(context)).load();
                                    holder.icons.setVisibility(View.VISIBLE);
                                    Picasso.get().load(R.drawable.ic_action_pdf).placeholder(R.drawable.ic_action_pdf).into(holder.icons);
                                    holder.icons.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            // Get the file URL
                                            String fileUrl = announcement.getaFile();

                                            // Create an intent to open PDF files
                                            Intent intent = new Intent(Intent.ACTION_VIEW);

                                            // Set the data and type for the intent
                                            intent.setDataAndType(Uri.parse(fileUrl), "application/pdf");

                                            // Set flags to grant read permissions and allow opening the file in other apps
                                            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                                            // Verify that there's an app to handle this intent
                                            if (intent.resolveActivity(context.getPackageManager()) != null) {
                                                // Start the activity
                                                context.startActivity(intent);
                                            } else {
                                                // If no app can handle the intent, show a toast or dialog indicating so
                                                Toast.makeText(context, "No app found to open PDF", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    // Handle any errors
                                    Log.e("TAG", "Error downloading PDF: " + exception.getMessage());
                                }
                            });
                        }
                  else       if(contype.endsWith("html")||contype.endsWith("htm")) {
                            holder.sendWeb.setVisibility(View.VISIBLE);
                            holder.sendWeb.loadUrl(fileUrl);
                        }
                  else      if(contype.endsWith("txt")) {
                            holder.sendTxt.setVisibility(View.VISIBLE);
                            holder.sendTxt.setText("Unsupported file type");
                        }
                        else{
                                // Unsupported file type
                                holder.sendTxt.setVisibility(View.VISIBLE);
                                holder.sendTxt.setText("Unsupported file type");

                        }
                    } catch (Exception e) {
                        // Handle any exceptions
                        e.printStackTrace();
                        Toast.makeText(context, "Error loading file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Uh-oh, an error occurred!
                    Toast.makeText(context,"erro occured",Toast.LENGTH_SHORT);
                    System.out.println("Error retrieving metadata: " + exception.getMessage());

                }
            });
          //  Toast.makeText(context,"File type is"+fileExtension,Toast.LENGTH_SHORT).show();;

        }

        // Set other data as needed
String dp= announcement.getuDp();
        try{
            if(!dp.equals(""))
            {
                Picasso.get().load(dp).placeholder(R.drawable.ic_profile).into(holder.uImage);}
        }
        catch ( Exception e)
        {
            System.out.println(e);
        }
        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMoreOptions(holder.moreBtn,announcement.getuId(),announcement.getaId(),announcement.getaFile());
            }
        });

    }



    private void showMoreOptions(ImageButton moreBtn, String uid,  String aId, String pFile) {
        PopupMenu popupMenu=new PopupMenu(context,moreBtn, Gravity.END);
        String myUid= FirebaseAuth.getInstance().getCurrentUser().getUid();
        if(uid.equals(myUid)) {

            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Delete");

        }
        //popupMenu.getMenu().add(Menu.NONE, 2, 0, "View Details");

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id=item.getItemId();
                if(id==0)
                {
                    beginDelete(aId,pFile);
                }

                return false;
            }
        });
        popupMenu.show();
    }

    private void beginDelete(String aId, String fileUrl) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Deleting announcement...");
        progressDialog.show();

        if (!fileUrl.equals("null")&&fileUrl != null && !fileUrl.isEmpty()) {
            // Delete the file from Firebase Storage
            StorageReference fileRef = FirebaseStorage.getInstance().getReferenceFromUrl(fileUrl);
            fileRef.delete().addOnSuccessListener(aVoid -> {
                // File deleted successfully, now delete the announcement data from Firebase Realtime Database
                deleteAnnouncementData(aId, progressDialog);
            }).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(context, "Failed to delete file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            // If there is no file associated with the announcement, directly delete the announcement data from Firebase Realtime Database
            deleteAnnouncementData(aId, progressDialog);
        }

    }
    private void deleteAnnouncementData(String announcementId, ProgressDialog progressDialog) {
        // Delete the announcement data from Firebase Realtime Database
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Announcements");
        ref.child(announcementId).removeValue().addOnSuccessListener(aVoid -> {
            progressDialog.dismiss();
            Toast.makeText(context, "Announcement deleted successfully", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(context, "Failed to delete announcement: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return announcementList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, descriptionTextView,aTimeTv,uNameTv;
        CircularImageView uImage;
        ImageButton moreBtn;
        PhotoView sendImage;
        VideoView sendVdo;
        WebView sendWeb;
        PDFView sendpdf;
        TextView sendTxt;
        ImageView icons;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            titleTextView = itemView.findViewById(R.id.aTitleTv);
            descriptionTextView = itemView.findViewById(R.id.aDescTv);
            uImage=itemView.findViewById(R.id.uPictureIv);
            aTimeTv=itemView.findViewById(R.id.pTimeTv  );
            uNameTv=itemView.findViewById(R.id.uNameTv);
            moreBtn=itemView.findViewById(R.id.moreBtn);
            sendpdf=itemView.findViewById(R.id.sendPdf);
            sendImage=itemView.findViewById(R.id.sendImg);
            sendTxt=itemView.findViewById(R.id.sendTxt);
            sendWeb=itemView.findViewById(R.id.sendWeb);
            sendVdo=itemView.findViewById(R.id.sendVdo);
            icons=itemView.findViewById(R.id.iconIv);




            // Initialize other views here
        }
    }
}







