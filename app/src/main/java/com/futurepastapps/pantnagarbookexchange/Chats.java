package com.futurepastapps.pantnagarbookexchange;

/**
 * Created by HP on 23-06-2018.
 */

public class Chats {
    private long seenTimestamp , notSeenTimestamp;

    public Chats() {

    }

    public Chats(long seenTimestamp, long notSeenTimestamp) {
        this.seenTimestamp = seenTimestamp;
        this.notSeenTimestamp = notSeenTimestamp;
    }

    public long getSeenTimestamp() {
        return seenTimestamp;
    }

    public void setSeenTimestamp(long seenTimestamp) {
        this.seenTimestamp = seenTimestamp;
    }

    public long getNotSeenTimestamp() {
        return notSeenTimestamp;
    }

    public void setNotSeenTimestamp(long notSeenTimestamp) {
        this.notSeenTimestamp = notSeenTimestamp;
    }
}
