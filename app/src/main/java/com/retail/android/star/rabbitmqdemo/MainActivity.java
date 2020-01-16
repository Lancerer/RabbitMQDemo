package com.retail.android.star.rabbitmqdemo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.retail.android.star.rabbitmqdemo.push.EventMessage;

import com.retail.android.star.rabbitmqdemo.push.RabbitService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeoutException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String QUEUE_NAME = "hello_rabbit";
    private final static String EXCHANGE_NAME = "test_exchange_topic";
    ConnectionFactory mFactory;
    private String mMessage;
    Thread subscribeThread;
    @SuppressLint("HandlerLeak")
    final Handler incomingMessageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String message = msg.getData().getString("msg");

            Date now = new Date();
            @SuppressLint("SimpleDateFormat") SimpleDateFormat ft = new SimpleDateFormat("hh:mm:ss");
            mTv.append(ft.format(now) + "消息" + message + '\n');
            Log.i("test", "msg:" + message);
        }
    };
    private TextView mTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTv = findViewById(R.id.textView);
        startService(new Intent(this, RabbitService.class));
        //开启消费者线程
      //  subscribe(incomingMessageHandler);
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getMessage(EventMessage message) {
        Date now = new Date();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat ft = new SimpleDateFormat("hh:mm:ss");
        mTv.append(ft.format(now) + "消息:" + message.getMessage() + '\n');
        Log.i("test", "msg:" + message);
    }

    /**
     * 消费者线程
     */
    void subscribe(final Handler handler) {
        // 设置 RabbitMQ 的主机名
        mFactory = new ConnectionFactory();
        mFactory.setUsername("test");
        mFactory.setPassword("123456");
        mFactory.setVirtualHost("/");
        mFactory.setPort(5678);
        //设置 RabbitMQ 地址
        mFactory.setHost("101.132.121.56");
        subscribeThread = new Thread(() -> {
            try {
                //使用之前的设置，建立连接
                Connection connection = mFactory.newConnection();

                //创建一个通道
                Channel channel = connection.createChannel();

              /*  channel.exchangeDeclare(EXCHANGE_NAME, "topic");//这里要根据实际改
                String mQueue = channel.queueDeclare().getQueue();
                channel.queueBind(mQueue, EXCHANGE_NAME, "");//这里要根据实际改*/
                channel.queueDeclare(QUEUE_NAME, false, false, false, null);

                Consumer consumer = new DefaultConsumer(channel) {
                    @Override
                    public void handleDelivery(String consumerTag, Envelope envelope,
                                               AMQP.BasicProperties properties, byte[] body) throws IOException {
                        mMessage = new String(body, "UTF-8");
                        System.out.println(" [x] Received '" + envelope.getRoutingKey() + "':'" + mMessage + "'");
//                                从message池中获取msg对象更高效
                        Message msg = handler.obtainMessage();
                        Bundle bundle = new Bundle();
                        bundle.putString("msg", mMessage);
                        msg.setData(bundle);
                        handler.sendMessage(msg);
                    }
                };
                channel.basicConsume(QUEUE_NAME, true, consumer);

            } catch (Exception e1) {
                Log.d("", "Connection broken: " + e1.getClass().getName());
                try {
                    Thread.sleep(2000); //sleep and then try again
                } catch (InterruptedException e) {
                }
            }
            Log.i("1111111111111111111111", "run: ");
        });
        subscribeThread.start();
    }

    private void initdata() {
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
                            System.out.println("Received Message '" + message + "'");
                        }
                    };
                    channel.basicConsume(QUEUE_NAME, true, consumer);
                } catch (IOException | TimeoutException e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }


}
