package com.futurepastapps.pantnagarbookexchange;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class BookRequsetsFragment extends Fragment {


    private EmptyRecyclerView requestView;
    private DatabaseReference userRequestDb , requestDb , booksDb , dbRef;
    private String cUserName;
    private View mainview;
    private RelativeLayout mainLayout;

    private boolean connected;
    private boolean showWarning;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private InterstitialAd mInterstitialAd;
    private AdView bannerAdView;

    public BookRequsetsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mainview =  inflater.inflate(R.layout.fragment_book_requsets, container, false);

        mainLayout = mainview.findViewById(R.id.requestMainLayout);

        requestView = mainLayout.findViewById(R.id.bookRequestView);
        requestView.setLayoutManager(new LinearLayoutManager(getContext()));

        View emptyView = mainLayout.findViewById(R.id.bookRequestsLayout);
        requestView.setEmptyView(emptyView);

        cUserName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();

        userRequestDb = FirebaseDatabase.getInstance().getReference().child("BookRequests").child(cUserName).child("received");
        userRequestDb.keepSynced(true);

        booksDb = FirebaseDatabase.getInstance().getReference().child("Books");
        booksDb.keepSynced(true);

        requestDb = FirebaseDatabase.getInstance().getReference().child("BookRequests");
        requestDb.keepSynced(true);

        dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.keepSynced(true);

        requestView.setHasFixedSize(true);
        requestView.setLayoutManager(new LinearLayoutManager(getContext()));

        connected = false;
        showWarning = true;

        sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        showWarning = sharedPreferences.getBoolean("showWarning" , true);

        mInterstitialAd = new InterstitialAd(getContext());
        mInterstitialAd.setAdUnitId(getResources().getString(R.string.remove_interstitial));

        bannerAdView = mainLayout.findViewById(R.id.requestBanner);
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

        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        final FirebaseRecyclerAdapter<BookRequests , BookRequestsViewHolder> requestRecyleview = new FirebaseRecyclerAdapter<BookRequests , BookRequestsViewHolder>(
                BookRequests.class , R.layout.bookrequestlayout , BookRequestsViewHolder.class , userRequestDb) {

            @Override
            protected void populateViewHolder(final BookRequestsViewHolder viewHolder, final BookRequests model, final int position) {

                final String bookName = getRef(position).getKey();

                viewHolder.setRequestStatus(model.getRequestStatus());
                viewHolder.setBookName(bookName);
                viewHolder.setTime(model.getTime());
                viewHolder.setName(model.getUserName());

                dbRef.child("Users").child(model.getUserName()).child("dpThumbnail").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            viewHolder.setThumb(dataSnapshot.getValue().toString());
                        }

                        viewHolder.mview.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                if (model.getRequestStatus().equals("pending")) {

                                    CharSequence string[] = new CharSequence[]{"Accept", "Reject", "View User"};

                                    final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                                    builder.setTitle("Select Options");
                                    builder.setItems(string, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                            if (i == 0) {

                                                ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                                                connected = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                                                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;

                                                if (connected == false) {
                                                    Toast.makeText(getContext(), "No internet connection", Toast.LENGTH_SHORT).show();
                                                } else if (showWarning == true) {

                                                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                    View mView = getLayoutInflater().inflate(R.layout.dialoglayout, null);
                                                    final CheckBox checkBox = mView.findViewById(R.id.dialogCheckBox);

                                                    builder.setTitle("Accept Request");
                                                    builder.setMessage("Accepting a book request will mark the book as unavailable");

                                                    builder.setView(mView);

                                                    builder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {

                                                            if (checkBox.isChecked()) {
                                                                editor = sharedPreferences.edit();
                                                                editor.putBoolean("showWarning", false).apply();
                                                                showWarning = false;
                                                            }
                                                            acceptRequest(bookName, model.getUserName());
                                                        }
                                                    });

                                                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {

                                                            if (checkBox.isChecked()) {
                                                                editor = sharedPreferences.edit();
                                                                editor.putBoolean("showWarning", false).apply();
                                                                showWarning = false;
                                                            }
                                                        }
                                                    });
                                                    builder.show();
                                                } else {
                                                    acceptRequest(bookName, model.getUserName());
                                                }
                                            }
                                            if (i == 1) {

                                                ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                                                connected = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                                                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;

                                                if (connected == false) {
                                                    Toast.makeText(getContext(), "No internet connection", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Map cancelMap = new HashMap();
                                                    cancelMap.put("BookRequests/" + model.getUserName() + "/sent/" + bookName, null);
                                                    cancelMap.put("BookRequests/" + cUserName + "/received/" + bookName, null);

                                                    dbRef.updateChildren(cancelMap).addOnCompleteListener(new OnCompleteListener() {
                                                        @Override
                                                        public void onComplete(@NonNull Task task) {
                                                            if (task.isSuccessful())
                                                                Toast.makeText(getActivity(), "Book request rejected", Toast.LENGTH_SHORT).show();
                                                            else
                                                                Toast.makeText(getActivity(), "An Error occured", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                }
                                            }
                                            if (i == 2) {
                                                Intent userIntent = new Intent(getContext(), UserProfileActivity.class);
                                                userIntent.putExtra("User Name", model.getUserName());
                                                startActivity(userIntent);
                                            }

                                        }
                                    });
                                    builder.show();
                                } else if (model.getRequestStatus().equals("accepted")) {

                                    CharSequence string[] = new CharSequence[]{"View User", "Remove"};

                                    final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                                    builder.setTitle("Select Options");
                                    builder.setItems(string, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                            if (i == 0) {
                                                if (model.getUserName().equals("Student Library")) {
                                                    Intent userIntent = new Intent(getContext(), AboutUsActivity.class);
                                                    userIntent.putExtra("User Name", model.getUserName());
                                                    startActivity(userIntent);
                                                } else {
                                                    Intent userIntent = new Intent(getContext(), UserProfileActivity.class);
                                                    userIntent.putExtra("User Name", model.getUserName());
                                                    startActivity(userIntent);
                                                }
                                            }
                                            if (i == 1) {

                                                ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                                                connected = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                                                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;

                                                if (connected == false) {
                                                    Toast.makeText(getContext(), "No internet connection", Toast.LENGTH_SHORT).show();
                                                } else {

                                                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                                                    builder.setTitle("Remove");
                                                    builder.setMessage("You will no longer be able to view this request");

                                                    builder.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
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
                                                            Map removeMap = new HashMap();
                                                            removeMap.put("BookRequests/" + cUserName + "/received/" + bookName, null);
                                                            removeMap.put("AcceptedRequests/" + cUserName + "/" + model.getUserName() + "/" + bookName, null);

                                                            dbRef.updateChildren(removeMap).addOnCompleteListener(new OnCompleteListener() {
                                                                @Override
                                                                public void onComplete(@NonNull Task task) {
                                                                    if (task.isSuccessful())
                                                                        Toast.makeText(getActivity(), "Book request removed", Toast.LENGTH_SHORT).show();
                                                                    else
                                                                        Toast.makeText(getActivity(), "An Error occured", Toast.LENGTH_SHORT).show();
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
        requestView.setAdapter(requestRecyleview);

    }

    private void acceptRequest(String bookName , String requestUser) {
        Map acceptRequestMap = new HashMap();
        acceptRequestMap.put("AcceptedRequests/" + cUserName + "/" + requestUser + "/" + bookName , ServerValue.TIMESTAMP);
        acceptRequestMap.put("Books/" + bookName + "/available", false);
        acceptRequestMap.put("BookRequests/" + requestUser + "/sent/" + bookName + "/requestStatus", "accepted");
        acceptRequestMap.put("BookRequests/" + cUserName + "/received/" + bookName + "/requestStatus", "accepted");

        dbRef.updateChildren(acceptRequestMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getActivity(), "Book request accepted", Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(getActivity(), "An error occured", Toast.LENGTH_SHORT).show();
            }
        });
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
