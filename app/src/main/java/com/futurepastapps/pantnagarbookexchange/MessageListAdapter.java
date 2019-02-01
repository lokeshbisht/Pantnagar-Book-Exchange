package com.futurepastapps.pantnagarbookexchange;

import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.List;

/**
 * Created by HP on 14-08-2018.
 */

public class MessageListAdapter extends RecyclerView.Adapter {

    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

    private List<Messages> mMessageList;

    private FirebaseAuth mAuth;

    private String cUserName;

    public MessageListAdapter(List<Messages> messageList) {
        mMessageList = messageList;

        mAuth = FirebaseAuth.getInstance();
        cUserName = mAuth.getCurrentUser().getDisplayName();

    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    // Determines the appropriate ViewType according to the sender of the message.
    @Override
    public int getItemViewType(int position) {
        Messages message = mMessageList.get(position);

        if (message.getFrom().equals(cUserName)) {
            return VIEW_TYPE_MESSAGE_SENT;
        } else {
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view;

        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.sentmessagelayout, parent, false);
            return new SentMessageHolder(view);
        } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.receivedmessagelayout, parent, false);
            return new ReceivedMessageHolder(view);
        }

        return null;
    }

    // Passes the message object to a ViewHolder so that the contents can be bound to UI.
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Messages message = mMessageList.get(position);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                ((SentMessageHolder) holder).bind(message);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ReceivedMessageHolder) holder).bind(message);
        }
    }

    private class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText;

        SentMessageHolder(View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.sentMessageText);
            timeText = itemView.findViewById(R.id.sentMessageTime);
        }

        void bind(Messages message) {
            messageText.setText(message.getMessage());

            String time;
            if(DateUtils.isToday(message.getTime())) {
                time = DateFormat.format("hh:mm a", message.getTime()).toString();
            } else if(DateUtils.isToday(message.getTime() + DateUtils.DAY_IN_MILLIS)) {
                time = "Yes " + DateFormat.format("hh:mm a", message.getTime()).toString();
            } else {
                time = DateFormat.format("dd-MM hh:mm a", message.getTime()).toString();
            }
            timeText.setText(time);
        }
    }

    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText;

        ReceivedMessageHolder(View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.receivedMessageText);
            timeText = itemView.findViewById(R.id.receivedMessageTime);
        }

        void bind(Messages message) {
            messageText.setText(message.getMessage());

            String time;
            if(DateUtils.isToday(message.getTime())) {
                time = DateFormat.format("hh:mm a", message.getTime()).toString();
            } else if(DateUtils.isToday(message.getTime() + DateUtils.DAY_IN_MILLIS)) {
                time = "Yes " + DateFormat.format("hh:mm a", message.getTime()).toString();
            } else
                time = DateFormat.format("dd-MM hh:mm a", message.getTime()).toString();
            timeText.setText(time);
        }
    }
}