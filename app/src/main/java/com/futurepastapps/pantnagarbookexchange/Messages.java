package com.futurepastapps.pantnagarbookexchange;

/**
 * Created by HP on 06-07-2018.
 */

public class Messages {

    private String message , from;
    private long time;

    public Messages(){
    }

    public Messages(String message, String from, long time) {
        this.message = message;
        this.from = from;
        this.time = time;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
