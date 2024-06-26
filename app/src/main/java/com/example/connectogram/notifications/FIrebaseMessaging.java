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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.connectogram.ChatActivity;
import com.example.connectogram.PostDetailsActivity;
import com.example.connectogram.ProfileActivity;
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


        SharedPreferences sp = getSharedPreferences("SP_USER", MODE_PRIVATE);
        String savedCurrentUser = sp.getString("Current_USERID", "None");

        String NotificationType = message.getData().get("notificationType");
        if (NotificationType != null && NotificationType.equals("PostNotification")) {
            String sender = message.getData().get("send");
            String pId = message.getData().get("pId");
            String pTitle = message.getData().get("pTitle");
            String pDesc = message.getData().get("pDesc");
            if (!sender.equals(savedCurrentUser)) {
                showPostNotificatoin("" + pId, "" + pTitle, "" + pDesc);
            }


        } else if (NotificationType != null && NotificationType.equals("ChatNotification")) {
            String send = message.getData().get("send");
            String user = message.getData().get("user");
            FirebaseUser Fuser = FirebaseAuth.getInstance().getCurrentUser();
            if (Fuser != null && send.equals(Fuser.getUid())) {
                if (!savedCurrentUser.equals(user)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        SendOAndAboveNotification(message);

                    } else {
                        sendNormalNotification(message);
                    }
                }
            }

        } else if (NotificationType != null && NotificationType.equals("LikeNotification")) {
            String to = message.getData().get("send");
            String sender = message.getData().get("user");
            String postTitle = message.getData().get("title");
            String postDescription = message.getData().get("body");
            if (to != null && to.equals(savedCurrentUser)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    sendOreoAndAboveLikeNotification(postTitle, postDescription);
                } else {
                    sendNormalLikeNotification(postTitle, postDescription);
                }
            }
        } else if (NotificationType.equals("CommentNotification")) {
            String to = message.getData().get("send");
            String sender = message.getData().get("user");
            String postId = message.getData().get("title");
            String commentDescription = message.getData().get("body");
            if (to != null && to.equals(savedCurrentUser)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    sendOreoAndAboveCommentNotification(postId, commentDescription);
                } else {
                    sendNormalCommentNotification(postId, commentDescription);
                }
            }

        }
    }
    private void sendNormalLikeNotification(String postTitle, String postDescription) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int notificationId = new Random().nextInt(3000);

        Intent intent = new Intent(this, ProfileActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_profile);
        Uri notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_liked)
                .setLargeIcon(largeIcon)
                .setContentTitle(postTitle)
                .setContentText(postDescription)
                .setSound(notificationUri)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    private void sendOreoAndAboveLikeNotification(String postTitle, String postDescription) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int notificationId = new Random().nextInt(3000);

        // Setup notification channel for Android Oreo and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setupLikeNotificationChannel(notificationManager);
        }

        Intent intent = new Intent(this, ProfileActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_profile);
        Uri notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Notification.Builder notificationBuilder = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationBuilder = new Notification.Builder(this, ADMIN_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_liked)
                    .setLargeIcon(largeIcon)
                    .setContentTitle(postTitle)
                    .setContentText(postDescription)
                    .setSound(notificationUri)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);
        }

        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    private void setupLikeNotificationChannel(NotificationManager notificationManager) {
        CharSequence channelName = "New Like Notification";
        String channelDescription = "Device to Device Like Notification";

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel adminChannel = new NotificationChannel(ADMIN_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_LOW);
            adminChannel.setDescription(channelDescription);
            adminChannel.enableLights(true);
            adminChannel.setLightColor(Color.RED);
            adminChannel.enableVibration(true);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(adminChannel);
            }
        }
    }


    private void sendNormalCommentNotification(String postId, String commentDescription) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int notificationId = new Random().nextInt(3000);

        Intent intent = new Intent(this, PostDetailsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("postId",postId);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_profile);
        Uri notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_comment)
                .setLargeIcon(largeIcon)
                .setContentTitle("New Comment")
                .setContentText(commentDescription)
                .setSound(notificationUri)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    private void sendOreoAndAboveCommentNotification(String postid, String commentDescription) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int notificationId = new Random().nextInt(3000);

        // Setup notification channel for Android Oreo and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setupCommentNotificationChannel(notificationManager);
        }

        Intent intent = new Intent(this, PostDetailsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("postId",postid);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_profile);
        Uri notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Notification.Builder notificationBuilder = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationBuilder = new Notification.Builder(this, ADMIN_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_comment)
                    .setLargeIcon(largeIcon)
                    .setContentTitle("new Comment")
                    .setContentText(commentDescription)
                    .setSound(notificationUri)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);
        }

        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    private void setupCommentNotificationChannel(NotificationManager notificationManager) {
        CharSequence channelName = "New Comment Notification";
        String channelDescription = "Device to Device Comment Notification";

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel adminChannel = new NotificationChannel(ADMIN_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_LOW);
            adminChannel.setDescription(channelDescription);
            adminChannel.enableLights(true);
            adminChannel.setLightColor(Color.RED);
            adminChannel.enableVibration(true);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(adminChannel);
            }
        }
    }











    private void showLikeNotification(String postTitle, String postDescription) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int notificationId = new Random().nextInt(3000);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setupLikeNotificationChannel(notificationManager);
        }

        Intent intent = new Intent(this, ProfileActivity.class);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_profile);
        Uri notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, ADMIN_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_liked)
                .setLargeIcon(largeIcon)
                .setContentTitle(postTitle)
                .setContentText(postDescription)
                .setSound(notificationUri)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationManager.notify(notificationId, notificationBuilder.build());
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
        NotificationCompat.Builder notificatoinbuilder=new NotificationCompat.Builder(this,""+ADMIN_CHANNEL_ID).setSmallIcon(R.drawable.ic_photo).setContentTitle(pTitle).setContentText(pDesc).setSound(notificatoinUri).setContentIntent(pi);

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
