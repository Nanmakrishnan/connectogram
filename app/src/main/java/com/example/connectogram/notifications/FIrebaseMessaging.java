package com.example.connectogram.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.connectogram.ChatActivity;
import com.example.connectogram.PostDetailsActivity;
import com.example.connectogram.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;


public class FIrebaseMessaging extends FirebaseMessagingService {
    private static final String ADMIN_CHANNEL_ID ="admin_channel";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);



        SharedPreferences sp=getSharedPreferences("SP_USER",MODE_PRIVATE);
        String savedCurrentUser=sp.getString("Current_USERID","None");

        String NotificationType=message.getData().get("notificationType");
        if(NotificationType.equals("PostNotification"))
        {
            String sender=message.getData().get("sender");
            String pId=message.getData().get("pId");
            String pTitle=message.getData().get("pTitle");
            String pDesc=message.getData().get("pDesc");
            if(!sender.equals(savedCurrentUser))

            {
                showPostNotificatoin(""+pId,""+pTitle,""+pDesc);
            }


        }
        else if(NotificationType.equals("ChatNotification"))
        {
            String send=message.getData().get("send");
            String user=message.getData().get("user");
            FirebaseUser Fuser= FirebaseAuth.getInstance().getCurrentUser();
            if(Fuser!=null && send.equals(Fuser.getUid()))

            {
                if(!savedCurrentUser.equals(user))
                {
                    if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
                    {
                        SendOAndAboveNotification(message);

                    }
                    else {
                        sendNormalNotification(message);
                    }
                }
            }

        }




    }

    private void showPostNotificatoin(String pId, String pTitle, String pDesc) {

        NotificationManager notificationManager=(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int notficatoinId=new Random().nextInt(3000 );

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
        {
            setupPostNotificationChannel(notificationManager);
        }
        Intent i=new Intent(this, PostDetailsActivity.class);
        i.putExtra("postId",pId);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi=PendingIntent.getActivity(this,0,i, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
        //CHANGE WHEN LOGO NEED TO BE CHANGED
        Bitmap largeicon= BitmapFactory.decodeResource(getResources(), R.drawable.ic_profile);

        Uri notificatoinUri=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificatoinbuilder=new NotificationCompat.Builder(this,""+ADMIN_CHANNEL_ID).setSmallIcon(R.drawable.ic_profile).setContentTitle(pTitle).setContentText(pDesc).setSound(notificatoinUri).setContentIntent(pi);

        notificationManager.notify(notficatoinId,notificatoinbuilder.build());


    }

    private void setupPostNotificationChannel(NotificationManager notificationManager) {

        CharSequence channelname="New Notification";
        String channelDescription="Device to Device post Notification";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel adminChanel=new NotificationChannel(ADMIN_CHANNEL_ID,channelname,NotificationManager.IMPORTANCE_LOW);
            adminChanel.setDescription(channelDescription);;
            adminChanel.enableLights(true);
            adminChanel.setLightColor(Color.RED);;
            adminChanel.enableVibration(true);
            if(notificationManager!=null)
            {
                notificationManager.createNotificationChannel(adminChanel);
            }
        }

    }

    private void sendNormalNotification(RemoteMessage message) {
        String user=message.getData().get("user");
        String icon=message.getData().get("icon");
        String title=message.getData().get("title");
        String body=message.getData().get("body");

        RemoteMessage.Notification notification=message.getNotification();
        int i=Integer.parseInt(user.replaceAll("[\\D]",""));
        Intent      intent=new Intent(this, ChatActivity.class);
        Bundle bundle=new Bundle();
        bundle.putString("hisUid",user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi=PendingIntent.getActivity(this,i,intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
Uri defSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder=new NotificationCompat.Builder(this).setSmallIcon(Integer.parseInt(icon)).setContentText(body).setContentTitle(title).setAutoCancel(true).setSound(defSoundUri).setContentIntent(pi);
        NotificationManager notificationManager=(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        int j=0;
        if(i>0)
        {
            j=i;
        }
        notificationManager.notify(j,builder.build());


    }

    private void SendOAndAboveNotification(RemoteMessage message) {

        String user=message.getData().get("user");
        String icon=message.getData().get("icon");
        String title=message.getData().get("title");
        String body=message.getData().get("body");

        RemoteMessage.Notification notification=message.getNotification();
        int i=Integer.parseInt(user.replaceAll("[\\D]",""));
        Intent      intent=new Intent(this, ChatActivity.class);
        Bundle bundle=new Bundle();
        bundle.putString("hisUid",user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi=PendingIntent.getActivity(this,i,intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
        Uri defSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        OreoAndAboveNotification notification1=new OreoAndAboveNotification(this);
        Notification.Builder builder=notification1.getOtNotifications(title,body,pi,defSoundUri,icon);

        int j=0;
        if(i>0)
        {
            j=i;
        }
        notification1.getManager().notify(j,builder.build());

    }
    @Override
   public void  onNewToken(@NonNull String s)
    {
FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();;
if(user!=null)
{
    updateToken(s);

}

    }

    private void updateToken(String tokenrefresh) {
        FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Tokens");
        Token token=new Token(tokenrefresh );

        ref.child(user.getUid()).setValue(token);


    }


}
