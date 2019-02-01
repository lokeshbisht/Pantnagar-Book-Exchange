package com.futurepastapps.pantnagarbookexchange;

/**
 * Created by HP on 05-07-2018.
 */

public class BookRequests {

    private String requestStatus , userName;
    private long time;

    public BookRequests() {

    }

    public BookRequests(String requestStatus, String userName, long time) {
        this.requestStatus = requestStatus;
        this.userName = userName;
        this.time = time;
    }

    public String getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(String requestStatus) {
        this.requestStatus = requestStatus;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
