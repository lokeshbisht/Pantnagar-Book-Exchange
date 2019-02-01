package com.futurepastapps.pantnagarbookexchange;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class BlockedUsersActivity extends AppCompatActivity {

    private EmptyRecyclerView blockedUsersView;
    private Toolbar mToolbar;

    private String cUserName;

    private FirebaseAuth mAuth;
    private DatabaseReference dbRef, blockedUsersRef, userDetailsRef;

    private boolean connected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocked_users);

        blockedUsersView = findViewById(R.id.blockedUserView);
        blockedUsersView.setLayoutManager(new LinearLayoutManager(this));

        View emptyView = findViewById(R.id.blockedUsersEmptyLayout);
        blockedUsersView.setEmptyView(emptyView);
        blockedUsersView.setHasFixedSize(true);

        mToolbar = findViewById(R.id.blockedUsersBar);
        mToolbar.setTitle("Blocked Users");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.home);
        getSupportActionBar().setHomeButtonEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        cUserName = mAuth.getCurrentUser().getDisplayName();

        dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.keepSynced(true);

        blockedUsersRef = dbRef.child("Users").child(cUserName).child("blockedUsers");
        blockedUsersRef.keepSynced(true);

        userDetailsRef = dbRef.child("Users");
        userDetailsRef.keepSynced(true);

    }

    @Override
    protected void onStart() {
        super.onStart();

        final FirebaseRecyclerAdapter<BlockedUsers, BlockedUsersViewHolder> blockedUsersRecyclerAdapter = new FirebaseRecyclerAdapter<BlockedUsers, BlockedUsersViewHolder>(
                BlockedUsers.class, R.layout.chatsusersview, BlockedUsersViewHolder.class, blockedUsersRef) {

            @Override
            protected void populateViewHolder(final BlockedUsersViewHolder viewHolder, final BlockedUsers model, int position) {

                final String blockedUserName = getRef(position).getKey();
                final long time = model.getTime();


                userDetailsRef.child(blockedUserName).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()) {

                            String userThumb = dataSnapshot.child("dpThumbnail").getValue().toString();

                            viewHolder.setName(blockedUserName);
                            viewHolder.setTime(time);
                            viewHolder.setThumb(userThumb);

                            viewHolder.mview.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    CharSequence string[] = new CharSequence[]{"Unblock"};

                                    final AlertDialog.Builder builder = new AlertDialog.Builder(BlockedUsersActivity.this);

                                    builder.setTitle("Select Options");
                                    builder.setItems(string, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                            if (i == 0) {

                                                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                                                connected = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                                                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;

                                                if(connected == false)
                                                    Toast.makeText(BlockedUsersActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
                                                blockedUsersRef.child(blockedUserName).removeValue(new DatabaseReference.CompletionListener() {
                                                    @Override
                                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                        if (databaseError == null) {
                                                            Toast.makeText(BlockedUsersActivity.this, "User Unblocked", Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            Toast.makeText(BlockedUsersActivity.this, "Error unblocking", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    });
                                    builder.show();
                                }
                            });
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }
        };

        blockedUsersRecyclerAdapter.notifyDataSetChanged();
        blockedUsersView.setAdapter(blockedUsersRecyclerAdapter);

    }

    public static class BlockedUsersViewHolder extends RecyclerView.ViewHolder {

        View mview;

        public BlockedUsersViewHolder(View itemView) {
            super(itemView);

            mview = itemView;
        }

        public void setName(String userName) {

            TextView name = mview.findViewById(R.id.usersViewName);
            name.setText(userName);
        }

        public void setThumb(String thumb_dp) {

            CircleImageView userDp = mview.findViewById(R.id.usersViewDp);
            Picasso.get().load(thumb_dp).placeholder(R.drawable.defaultuser).into(userDp);
        }

        public void setTime(long time) {
            TextView requestTime = mview.findViewById(R.id.usersViewLastMessage);

            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
            cal.setTimeInMillis(time);

            String date;
            if(DateUtils.isToday(time)) {
                date = DateFormat.format("hh:mm a", cal).toString();
            } else if((time - System.currentTimeMillis()) < DateUtils.WEEK_IN_MILLIS) {
                date = DateFormat.format("EE hh:mm a", cal).toString();
            } else {
                date = DateFormat.format("dd-MM hh:mm a", cal).toString();
            }
            requestTime.setText(date);
        }
    }
}
