package com.kotprog.notebook;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class NoteNotificationHandler {
    private static final String CHANNEL_ID = "note_notification_channel";
    private static final int DEFAULT_ID = 0;
    private final Context context;
    private final NotificationManager notificationManager;

    public NoteNotificationHandler(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createChannel();
    }

    @SuppressLint("ObsoleteSdkInt")
    private void createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            return;

        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Note notification", NotificationManager.IMPORTANCE_DEFAULT);
        channel.enableLights(true);
        channel.enableVibration(true);
        channel.setLightColor(R.color.button_background);
        channel.setDescription("Note notification when a note is updated or deleted");

        this.notificationManager.createNotificationChannel(channel);
    }

    public void send(String message) {
        Intent intent = new Intent(context, MainActivity.class);
        @SuppressLint("UnspecifiedImmutableFlag") PendingIntent pendingIntent = PendingIntent
                .getActivity(context, DEFAULT_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        long[] vibrationPattern = { 0, 500, 1000, 500, 1000 };

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("Note change has occurred")
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_add_note)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setVibrate(vibrationPattern);

        this.notificationManager.notify(DEFAULT_ID, builder.build());
    }
}
