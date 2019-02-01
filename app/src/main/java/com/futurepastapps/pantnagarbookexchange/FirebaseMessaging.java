package com.futurepastapps.pantnagarbookexchange;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by HP on 19-07-2018.
 */

public class FirebaseMessaging extends FirebaseMessagingService {

    Intent intent;
    int notificationId;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String title = remoteMessage.getData().get("title");
        String msg = remoteMessage.getData().get("body");
        String clickAction = remoteMessage.getData().get("click_action");
        String fromUser = remoteMessage.getData().get("from_user");
        String type = remoteMessage.getData().get("type");

        String channelId = "CH_ID";

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this , channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(msg)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setGroup(type)
                .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_CHILDREN);

        mBuilder.setDefaults(Notification.DEFAULT_SOUND);

        intent = new Intent(clickAction);
        intent.putExtra("User Name", fromUser);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        mBuilder.setContentIntent(pendingIntent);

        if(type.equals("message"))
            notificationId = 0;
        else
            notificationId = (int)System.currentTimeMillis();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        notificationManager.notify(notificationId, mBuilder.build());
    }
}
