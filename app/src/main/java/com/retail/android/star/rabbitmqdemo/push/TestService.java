package com.retail.android.star.rabbitmqdemo.push;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.AppUtils;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.retail.android.star.rabbitmqdemo.R;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

/**
 * Time:2020/1/11
 * Author:toyk1hz1
 * Des: 启动rabbitMq
 */
public class TestService extends Service {
    private static final String QUEUE_NAME = "hello";

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
        initNotification();
        startRabbitMQ();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void startRabbitMQ() {
        new Thread() {
            @Override
            public void run() {
                try {
                    // 创建连接
                    ConnectionFactory factory = new ConnectionFactory();
                    // 设置 RabbitMQ 的主机名
                    // 设置 RabbitMQ 的主机名
                    factory.setUsername("test");
                    factory.setPassword("123456");
                    factory.setVirtualHost("/");
                    factory.setPort(5678);
                    //设置 RabbitMQ 地址
                    factory.setHost("101.132.121.56");
                    // 创建一个连接
                    Connection connection = factory.newConnection();
                    // 创建一个通道
                    Channel channel = connection.createChannel();
                    // 指定一个队列
                    channel.queueDeclare(QUEUE_NAME, false, false, false, null);
                    // 创建队列消费者
                    com.rabbitmq.client.Consumer consumer = new DefaultConsumer(channel) {
                        @Override
                        public void handleDelivery(String consumerTag, Envelope envelope,
                                                   AMQP.BasicProperties properties, byte[] body) {
                            String message = new String(body, StandardCharsets.UTF_8);
                            EventBus.getDefault().post(new EventMessage(message));
                        }
                    };
                    channel.basicConsume(QUEUE_NAME, true, consumer);
                } catch (IOException | TimeoutException e) {
                    e.printStackTrace();
                }
            }
        }.start();

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
