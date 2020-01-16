package com.retail.android.star.rabbitmqdemo.push;

/**
 * Time:2020/1/16
 * Author:toyk1hz1
 * Des:
 */
public class EventMessage {
    private String message;

    public EventMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
