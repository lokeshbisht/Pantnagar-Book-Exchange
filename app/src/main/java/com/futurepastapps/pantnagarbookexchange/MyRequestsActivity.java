package com.futurepastapps.pantnagarbookexchange;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyRequestsActivity extends AppCompatActivity {


    private EmptyRecyclerView myRequestsView;
    private Toolbar mToolbar;

    private String cUserName;
    private boolean connected;

    private FirebaseAuth mAuth;
    private DatabaseReference dbRef, requestsRef, bookDetailsRef;

    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_requests);

        myRequestsView = findViewById(R.id.myRequestsView);
        myRequestsView.setLayoutManager(new LinearLayoutManager(this));

        View emptyView = findViewById(R.id.myRequestsEmptyLayout);
        myRequestsView.setEmptyView(emptyView);
        myRequestsView.setHasFixedSize(true);

        mToolbar = findViewById(R.id.myRequestsBar);
        mToolbar.setTitle("My Requests");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.home);
        getSupportActionBar().setHomeButtonEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        cUserName = mAuth.getCurrentUser().getDisplayName();

        dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.keepSynced(true);

        connected = false;

        mInterstitialAd = new InterstitialAd(MyRequestsActivity.this);
        mInterstitialAd.setAdUnitId(getResources().getString(R.string.remove_interstitial));

        requestsRef = dbRef.child("BookRequests").child(cUserName).child("sent");
        requestsRef.keepSynced(true);

        bookDetailsRef = dbRef.child("Books");
        bookDetailsRef.keepSynced(true);

    }

    @Override
    protected void onStart() {
        super.onStart();

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        connected = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;

        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        final FirebaseRecyclerAdapter<BookRequests , BookRequestsViewHolder> requestRecyleview = new FirebaseRecyclerAdapter<BookRequests , BookRequestsViewHolder>(
                BookRequests.class , R.layout.bookrequestlayout , BookRequestsViewHolder.class , requestsRef) {

            @Override
            protected void populateViewHolder(final BookRequestsViewHolder viewHolder, final BookRequests model, int position) {

                final String bookName = getRef(position).getKey();

                viewHolder.setBookName(bookName);
                viewHolder.setTime(model.getTime());
                viewHolder.setName(model.getUserName());
                viewHolder.setRequestStatus(model.getRequestStatus());

                dbRef.child("Users").child(model.getUserName()).child("dpThumbnail").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()) {
                            viewHolder.setThumb(dataSnapshot.getValue().toString());
                        }

                        viewHolder.mview.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                if(model.getRequestStatus().equals("pending")) {

                                    CharSequence string[] = new CharSequence[]{"Cancel Request", "View User"};

                                    final AlertDialog.Builder builder = new AlertDialog.Builder(MyRequestsActivity.this);

                                    builder.setTitle("Select Options");
                                    builder.setItems(string, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                            if (i == 0) {

                                                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                                                connected = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                                                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;

                                                if (connected == false) {
                                                    Toast.makeText(MyRequestsActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Map cancelMap = new HashMap();
                                                    cancelMap.put("BookRequests/" + cUserName + "/sent/" + bookName , null);
                                                    cancelMap.put("BookRequests/" + model.getUserName() + "/received/" + bookName , null);

                                                    dbRef.updateChildren(cancelMap).addOnCompleteListener(new OnCompleteListener() {
                                                        @Override
                                                        public void onComplete(@NonNull Task task) {
                                                            if(task.isSuccessful()) {
                                                                Toast.makeText(MyRequestsActivity.this, "Book Request Cancelled", Toast.LENGTH_SHORT).show();
                                                            } else {
                                                                Toast.makeText(MyRequestsActivity.this, "An error occured", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                                }
                                            }

                                            if (i == 1) {
                                                if(model.getUserName().equals("Student Library")) {
                                                    Intent userIntent = new Intent(MyRequestsActivity.this, AboutUsActivity.class);
                                                    userIntent.putExtra("User Name", model.getUserName());
                                                    startActivity(userIntent);
                                                } else {
                                                    Intent userIntent = new Intent(MyRequestsActivity.this, UserProfileActivity.class);
                                                    userIntent.putExtra("User Name", model.getUserName());
                                                    startActivity(userIntent);
                                                }
                                            }

                                        }
                                    });

                                    builder.show();
                                } else if(model.getRequestStatus().equals("accepted")) {

                                    CharSequence string[] = new CharSequence[]{"View User", "Remove"};

                                    final AlertDialog.Builder builder = new AlertDialog.Builder(MyRequestsActivity.this);

                                    builder.setTitle("Select Options");
                                    builder.setItems(string, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                            if (i == 0) {
                                                if(model.getUserName().equals("Student Library")) {
                                                    Intent userIntent = new Intent(MyRequestsActivity.this, AboutUsActivity.class);
                                                    userIntent.putExtra("User Name", model.getUserName());
                                                    startActivity(userIntent);
                                                } else {
                                                    Intent userIntent = new Intent(MyRequestsActivity.this, UserProfileActivity.class);
                                                    userIntent.putExtra("User Name", model.getUserName());
                                                    startActivity(userIntent);
                                                }
                                            }
                                            if (i == 1) {

                                                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                                                connected = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                                                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;

                                                if (connected == false) {
                                                    Toast.makeText(MyRequestsActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
                                                } else {

                                                    AlertDialog.Builder builder = new AlertDialog.Builder(MyRequestsActivity.this);

                                                    builder.setTitle("Remove");
                                                    builder.setMessage("You will no longer be able to view this request");

                                                    builder.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {

                                                            requestsRef.child(bookName).removeValue(new DatabaseReference.CompletionListener() {
                                                                @Override
                                                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                                    if (databaseError == null) {
                                                                        mInterstitialAd.show();
                                                                        mInterstitialAd.setAdListener(new AdListener() {
                                                                            @Override
                                                                            public void onAdLoaded() {

                                                                            }

                                                                            @Override
                                                                            public void onAdFailedToLoad(int errorCode) {
                                                                                mInterstitialAd.loadAd(new AdRequest.Builder().build());
                                                                            }

                                                                            @Override
                                                                            public void onAdOpened() {

                                                                            }

                                                                            @Override
                                                                            public void onAdLeftApplication() {

                                                                            }

                                                                            @Override
                                                                            public void onAdClosed() {
                                                                                mInterstitialAd.loadAd(new AdRequest.Builder().build());
                                                                            }
                                                                        });
                                                                        Toast.makeText(MyRequestsActivity.this, "Book request removed", Toast.LENGTH_SHORT);
                                                                    } else
                                                                        Toast.makeText(MyRequestsActivity.this, "An error occured", Toast.LENGTH_SHORT).show();
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
                                            }
                                        }
                                    });
                                    builder.show();
                                }
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };

        requestRecyleview.notifyDataSetChanged();
        myRequestsView.setAdapter(requestRecyleview);

    }



    public static class BookRequestsViewHolder extends RecyclerView.ViewHolder {

        View mview;

        public BookRequestsViewHolder(View itemView) {
            super(itemView);

            mview = itemView;
        }

        public void setName(String userName) {

            TextView name = mview.findViewById(R.id.bookRequestLayoutUserName);
            name.setText(userName);
        }

        public void setBookName(String bookName) {

            TextView book = mview.findViewById(R.id.bookRequestLayoutBookName);
            book.setText(bookName);
        }

        public void setThumb(String thumb_dp) {

            CircleImageView userDp = mview.findViewById(R.id.bookRequestLayoutUserDp);
            Picasso.get().load(thumb_dp).placeholder(R.drawable.defaultuser).into(userDp);
        }

        public void setTime(long time) {
            TextView requestTime = mview.findViewById(R.id.bookRequestLayoutTime);

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


        public void setRequestStatus(String requestStatus) {
            ImageView requestStatusView = mview.findViewById(R.id.bookRequestRequestStatus);
            if(requestStatus.equals("pending"))
                requestStatusView.setImageResource(R.drawable.requestpending);
            else if(requestStatus.equals("accepted"))
                requestStatusView.setImageResource(R.drawable.requestaccepted);
        }
    }

}