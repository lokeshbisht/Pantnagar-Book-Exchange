package com.futurepastapps.pantnagarbookexchange;


import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {


    private EmptyRecyclerView convView;
    private DatabaseReference convdb , msgdb , userdb;
    private RelativeLayout mainLayout;

    private String cuser;
    private boolean sentMessage , messageSeen;

    private View mainview;

    private AdView bannerAdView;

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mainview =  inflater.inflate(R.layout.fragment_chats, container, false);


        mainLayout = mainview.findViewById(R.id.chatMainLayout);

        convView = mainLayout.findViewById(R.id.chatsView);
        convView.setLayoutManager(new LinearLayoutManager(getContext()));

        View emptyView = mainLayout.findViewById(R.id.chatsLayout);
        convView.setEmptyView(emptyView);

        cuser = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();

        convdb = FirebaseDatabase.getInstance().getReference().child("Chats")
                .child(cuser);
        convdb.keepSynced(true);

        userdb = FirebaseDatabase.getInstance().getReference().child("Users");
        userdb.keepSynced(true);

        msgdb = FirebaseDatabase.getInstance().getReference().child("Messages")
                .child(cuser);
        msgdb.keepSynced(true);

        convView.setHasFixedSize(true);
        convView.setLayoutManager(new LinearLayoutManager(getContext()));

        sentMessage = true;
        messageSeen = true;

        bannerAdView = mainLayout.findViewById(R.id.chatBanner);
        final AdRequest adRequest = new AdRequest.Builder().build();
        bannerAdView.loadAd(adRequest);
        bannerAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                bannerAdView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                bannerAdView.loadAd(adRequest);
            }

            @Override
            public void onAdOpened() {

            }

            @Override
            public void onAdLeftApplication() {
            }

            @Override
            public void onAdClosed() {
                bannerAdView.loadAd(adRequest);
            }
        });

        return mainview;
    }

    @Override
    public void onStart() {
        super.onStart();

        Query convquery = convdb.orderByChild("seenTimestamp");

        final FirebaseRecyclerAdapter<Chats , ChatsViewHolder> chatsRecycleView = new FirebaseRecyclerAdapter<Chats , ChatsViewHolder>(
                Chats.class , R.layout.chatsusersview, ChatsViewHolder.class , convquery) {

            @Override
            protected void populateViewHolder(final ChatsViewHolder viewHolder, final Chats model, int position) {

                final String listuserid = getRef(position).getKey();

                Query lastmsgquery = msgdb.child(listuserid).limitToLast(1);

                lastmsgquery.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                        if (dataSnapshot.exists()) {
                            String data = dataSnapshot.child("message").getValue().toString();
                            String from = dataSnapshot.child("from").getValue().toString();
                            String time = dataSnapshot.child("time").getValue().toString();
                            long messageTime = Long.parseLong(time);

                            long seenTime = model.getSeenTimestamp();

                            if (from.equals(cuser)) {
                                sentMessage = true;
                            } else {
                                sentMessage = false;
                                messageSeen = messageTime <= seenTime;
                            }
                            viewHolder.setMessage(data, sentMessage, messageSeen);
                        }
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

                userdb.child(listuserid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()) {

                            String userthumb = dataSnapshot.child("dpThumbnail").getValue().toString();

                            if (dataSnapshot.hasChild("online")) {

                                String useronline = dataSnapshot.child("online").getValue().toString();
                                viewHolder.setOnline(useronline);
                            }

                            viewHolder.setName(listuserid);
                            viewHolder.setThumb(userthumb);

                            viewHolder.mview.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                    chatIntent.putExtra("User Name", listuserid);
                                    startActivity(chatIntent);
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        convView.setAdapter(chatsRecycleView);

    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder {

        View mview;

        public ChatsViewHolder(View itemView) {
            super(itemView);

            mview = itemView;
        }

       public void setMessage(String msg , boolean sentMessage , boolean seen) {

           TextView message = mview.findViewById(R.id.usersViewLastMessage);
           message.setText(msg);

           if (sentMessage) {
               message.setTypeface(message.getTypeface() , Typeface.NORMAL);
           } else {
               if (!seen) {
                   message.setTypeface(message.getTypeface() , Typeface.BOLD_ITALIC);
               } else {
                   message.setTypeface(message.getTypeface() , Typeface.NORMAL);
               }
           }
        }
        public void setName(String name) {

            TextView userName = mview.findViewById(R.id.usersViewName);
            userName.setText(name);
        }

        public void setThumb(String thumb_dp) {

              CircleImageView circleImageView = mview.findViewById(R.id.usersViewDp);
              Picasso.get().load(thumb_dp).placeholder(R.drawable.defaultuser).into(circleImageView);

        }

        public void setOnline(String onlineStatus) {

            ImageView online = mview.findViewById(R.id.usersViewOnline);
            if(onlineStatus.equals("true")) {
                online.setVisibility(View.VISIBLE);
            }
            else {
                online.setVisibility(View.INVISIBLE);
            }
        }
    }
}
