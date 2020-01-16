package com.retail.android.star.rabbitmqdemo.push;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.AppUtils;
import com.retail.android.star.rabbitmqdemo.Constants;
import com.retail.android.star.rabbitmqdemo.R;

import org.greenrobot.eventbus.EventBus;

/**
 * Time:2020/1/16
 * Author:toyk1hz1
 * Des:
 */
public class RabbitService extends Service {
    private static final String TAG = "RabbitService";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        RabbitManager.initService(Constants.HOST, Constants.PORT, Constants.USERNAME, Constants.PASSWORD);
        RabbitManager.initExchange(Constants.EXCHANGE_NAME, Constants.EXCHANGE_TYPE_FANOUT);
        RabbitManager.getInstance().receiveQueueMessage(Constants.QUEUE_NAME, new RabbitManager.ReceiveMessageListener() {
            @Override
            public void receiveMessage(String message) {
                Log.d(TAG, "receiveMessage: " + message);
                EventBus.getDefault().post(new EventMessage(message));
            }
        });
        initNotification();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RabbitManager.getInstance().close();
    }

    private void initNotification() {
        String CHANNEL_ONE_NAME = "Channel One";

        NotificationChannel notificationChannel = null;
        Notification.Builder notificationBuilder = new Notification.Builder(this.getApplicationContext());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(AppUtils.getAppPackageName(),
                    CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setShowBadge(true);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(notificationChannel);

            notificationBuilder.setChannelId(AppUtils.getAppPackageName());
        }
        notificationBuilder.setContentTitle(getString(R.string.app_name))
                .setContentText("服务")
                .setSmallIcon(R.mipmap.ic_launcher);

        Notification notification = notificationBuilder.build();
        notification.flags |= Notification.FLAG_NO_CLEAR;

        startForeground(1, notification);
    }

}
