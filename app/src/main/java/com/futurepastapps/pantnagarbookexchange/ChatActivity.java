package com.futurepastapps.pantnagarbookexchange;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private TextView uName , uLastSeen;
    private CircleImageView userImage;
    private ImageView sendButton;
    private EditText message;
    private EmptyRecyclerView messageView;
    private SwipeRefreshLayout swipeRefreshLayout;

    private LinearLayoutManager linearLayoutManager;
    private String cUserName , chatUserName;

    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;

    private MessageListAdapter messageListAdapter;
    private final List<Messages> messagesList = new ArrayList<>();

    private boolean newMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mToolbar = findViewById(R.id.chatBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(true);

        dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.keepSynced(true);
        mAuth = FirebaseAuth.getInstance();
        cUserName = mAuth.getCurrentUser().getDisplayName();

        chatUserName = getIntent().getStringExtra("User Name");

        LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = inflater.inflate(R.layout.chatlayout , null);

        getSupportActionBar().setCustomView(R.layout.chatlayout);

        uName = findViewById(R.id.chatName);
        uLastSeen = findViewById(R.id.chatLastSeen);
        userImage = findViewById(R.id.chatImage);

        sendButton = findViewById(R.id.chatSend);
        message = findViewById(R.id.chatMessage);
        swipeRefreshLayout = findViewById(R.id.chatSwipe);

        linearLayoutManager = new LinearLayoutManager(this);
        messageListAdapter = new MessageListAdapter(messagesList);

        messageView = findViewById(R.id.chatMsgView);
        View mView = findViewById(R.id.chatsNoChatsLayout);
        messageView.setEmptyView(mView);
        messageView.setHasFixedSize(true);
        messageView.setLayoutManager(linearLayoutManager);
        messageView.setAdapter(messageListAdapter);

        newMessage = false;

        loadMessages();

        uName.setText(chatUserName);

        dbRef.child("Users").child(chatUserName).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String online = dataSnapshot.child("online").getValue().toString();
                    String image = dataSnapshot.child("dpThumbnail").getValue().toString();

                    if(!image.equals("default")) {
                        Picasso.get().load(image).placeholder(R.drawable.defaultuser).into(userImage);
                    }
                    if (online.equals("true")) {
                        uLastSeen.setText("online");
                    } else {
                        TimeSinceOffline timeSinceOffline = new TimeSinceOffline();
                        long time = Long.parseLong(online);
                        String lastSeen = TimeSinceOffline.getTimeAgo(time);
                        uLastSeen.setText(lastSeen);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sendMessage();
                message.setText("");
            }
        });

        mToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent viewUserIntent = new Intent(ChatActivity.this , UserProfileActivity.class);
                viewUserIntent.putExtra("User Name" , chatUserName);
                startActivity(viewUserIntent);
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void loadMessages() {

        DatabaseReference messageRef = dbRef.child("Messages").child(cUserName).child(chatUserName);

        Query query = messageRef.orderByChild("time");

        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Messages message = dataSnapshot.getValue(Messages.class);

                messagesList.add(message);

                messageListAdapter.notifyDataSetChanged();

                messageView.scrollToPosition(messagesList.size() - 1);

                dbRef.child("Chats").child(cUserName).child(chatUserName).child("seenTimestamp").setValue(ServerValue.TIMESTAMP);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void sendMessage() {

        String msg = message.getText().toString();

        if(!TextUtils.isEmpty(msg)) {

            messageView.scrollToPosition(messagesList.size() - 1);

            dbRef.child("Chats").child(cUserName).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(!dataSnapshot.hasChild(chatUserName)) {
                        Map chatMap = new HashMap();
                        chatMap.put("seenTimestamp" , ServerValue.TIMESTAMP);
                        chatMap.put("notSeenTimestamp" , ServerValue.TIMESTAMP);

                        Map chatUserMap = new HashMap();
                        chatUserMap.put("Chats/" + cUserName + '/' + chatUserName , chatMap);
                        chatUserMap.put("Chats/" + chatUserName + '/' + cUserName , chatMap);

                        dbRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                if(databaseError != null) {
                                    Toast.makeText(ChatActivity.this, "An error occured", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            dbRef.child("Chats").child(chatUserName).child(cUserName).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()) {
                        Chats chats = dataSnapshot.getValue(Chats.class);

                        long seenTimeStamp = chats.getSeenTimestamp();
                        long notSeenTimeStamp = chats.getNotSeenTimestamp();

                        newMessage = seenTimeStamp > notSeenTimeStamp;
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            String cUser = "Messages/" + cUserName + '/' + chatUserName;
            String chatUser = "Messages/" + chatUserName + '/' + cUserName;

            DatabaseReference messagePush = dbRef.child("Messages")
                    .child(cUserName).child(chatUserName).push();

            String pushId = messagePush.getKey();

            Map messageMap = new HashMap();
            messageMap.put("message" , msg);
            messageMap.put("time" , ServerValue.TIMESTAMP);
            messageMap.put("from" , cUserName);

            Map messageUserMap = new HashMap();
            messageUserMap.put(cUser + '/' + pushId , messageMap);
            messageUserMap.put(chatUser + '/' + pushId , messageMap);
            if(newMessage)
                messageUserMap.put("Chats/" + chatUserName + "/" + cUserName + "/notSeenTimestamp" , ServerValue.TIMESTAMP);

            dbRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                    if(databaseError != null) {
                        Toast.makeText(ChatActivity.this, "An error occcured", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.chatactivitymenu , menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId() == R.id.blockUserOption) {

            AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);

            builder.setTitle("Block User");
            builder.setMessage("After you block a user you will no longer be able to send message or request book from the user. Your chat history will also be deleted.");

            builder.setPositiveButton("Block", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            Map blockUserMap = new HashMap();
                            blockUserMap.put("Users/" + cUserName + "/blockedUsers/" + chatUserName + "/time", ServerValue.TIMESTAMP);
                            blockUserMap.put("Users/" + chatUserName + "/blockedByUsers/" + cUserName + "/time", ServerValue.TIMESTAMP);
                            blockUserMap.put("Chats/" + cUserName + "/" + chatUserName, null);
                            blockUserMap.put("Chats/" + chatUserName + "/" + cUserName, null);
                            blockUserMap.put("Messages/" + cUserName + "/" + chatUserName, null);
                            blockUserMap.put("Messages/" + chatUserName + "/" + cUserName, null);

                            dbRef.updateChildren(blockUserMap, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    if (databaseError == null) {
                                        Toast.makeText(ChatActivity.this, "User Blocked", Toast.LENGTH_SHORT).show();

                                        Intent homeIntent = new Intent(ChatActivity.this, HomeActivity.class);
                                        startActivity(homeIntent);
                                        finish();

                                    } else {
                                        Toast.makeText(ChatActivity.this, "Could not block user", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            builder.show();
        }

        if(item.getItemId() == R.id.viewUserOption) {
            Intent viewUserIntent = new Intent(ChatActivity.this , UserProfileActivity.class);
            viewUserIntent.putExtra("User Name" , chatUserName);
            startActivity(viewUserIntent);
        }

        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(0);
    }
}