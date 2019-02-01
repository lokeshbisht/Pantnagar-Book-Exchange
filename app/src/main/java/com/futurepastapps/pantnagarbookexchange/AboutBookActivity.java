package com.futurepastapps.pantnagarbookexchange;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class AboutBookActivity extends AppCompatActivity {

    private TextView bookName, authorName, genreAndLanguage , description , availability;
    private Button requestBookButton, userProfileButton;
    private ImageView descriptionDropDown;
    private ProgressBar requestBookProgress;

    private String sBookName, sAuthorName, sGenre, sLanguage, sBookUserName, sCurrentUserName , sDescription , userThumb , currentUserThumb;
    private boolean showMore;

    private DatabaseReference dbRef;

    private boolean available , connected , blockedUser , blockedByUser;

    private android.support.v7.widget.Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_book);

        mToolbar = findViewById(R.id.aboutBookBar);
        mToolbar.setTitle("Pantnagar Book Exchange");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.home);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        bookName = findViewById(R.id.aboutBookBookName);
        authorName = findViewById(R.id.aboutBookAuthor);
        genreAndLanguage = findViewById(R.id.aboutBookGenreAndLanguage);
        description = findViewById(R.id.aboutBookDescription);
        availability = findViewById(R.id.aboutBookAvailability);
        requestBookButton = findViewById(R.id.aboutBookRequestButton);
        userProfileButton = findViewById(R.id.aboutBookUserProfileButton);
        descriptionDropDown = findViewById(R.id.aboutBookShowMore);
        requestBookProgress = findViewById(R.id.aboutBookRequestProgress);
        requestBookProgress.setIndeterminate(true);

        showMore = true;
        connected = false;

        sBookName = getIntent().getStringExtra("Book Name");
        sAuthorName = getIntent().getStringExtra("Author Name");
        sGenre = getIntent().getStringExtra("Genre");
        sLanguage = getIntent().getStringExtra("Language");
        sDescription = getIntent().getStringExtra("Description");
        available = getIntent().getBooleanExtra("Available" , true);
        userThumb = getIntent().getStringExtra("User Thumb");
        sCurrentUserName = getIntent().getStringExtra("User Name");
        sBookUserName = getIntent().getStringExtra("Book User Name");

        dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.keepSynced(true);

        if(sBookUserName.equals("Student Library")) {
            userProfileButton.setText("About Us");
        }

        bookName.setText(sBookName);
        authorName.setText(sAuthorName);
        genreAndLanguage.setText(sGenre + " | " + sLanguage);
        if(sDescription.equals("none")) {
            description.setText("No Description");
            descriptionDropDown.setVisibility(View.GONE);
        } else
            description.setText(sDescription);

        if(available) {
            availability.setText("Available");
        }
        else {
            availability.setText("Currently unavailable");
        }

        descriptionDropDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(showMore) {
                    showMore = false;
                    descriptionDropDown.setImageResource(R.drawable.showless);
                    description.setMaxLines(8);
                } else {
                    showMore = true;
                    descriptionDropDown.setImageResource(R.drawable.showmore);
                    description.setMaxLines(2);
                }
            }
        });

        requestBookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                connected = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;

                if (connected == false) {
                    Toast.makeText(AboutBookActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
                } else {

                    if (available) {

                        if (blockedByUser)
                            Toast.makeText(AboutBookActivity.this, "Cannot request book. The user has blocked you", Toast.LENGTH_SHORT).show();
                        else if (blockedUser)
                            Toast.makeText(AboutBookActivity.this, "Cannot request book. You have blocked the user", Toast.LENGTH_SHORT).show();
                        else {
                            requestBookProgress.setVisibility(View.VISIBLE);
                            if (requestBookButton.getText().equals("Request Book")) {
                                Map addRequestMap = new HashMap();
                                addRequestMap.put(sBookUserName + "/" + "received/" + sBookName + "/" + "userName", sCurrentUserName);
                                addRequestMap.put(sBookUserName + "/" + "received/" + sBookName + "/" + "time", ServerValue.TIMESTAMP);
                                addRequestMap.put(sBookUserName + "/" + "received/" + sBookName + "/" + "requestStatus", "pending");
                                addRequestMap.put(sCurrentUserName + "/" + "sent/" + sBookName + "/" + "userName", sBookUserName);
                                addRequestMap.put(sCurrentUserName + "/" + "sent/" + sBookName + "/" + "time", ServerValue.TIMESTAMP);
                                addRequestMap.put(sCurrentUserName + "/" + "sent/" + sBookName + "/" + "requestStatus", "pending");

                                dbRef.child("BookRequests").updateChildren(addRequestMap, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                        if (databaseError == null) {

                                            requestBookProgress.setVisibility(View.GONE);
                                            Toast.makeText(AboutBookActivity.this, "Book Request Sent", Toast.LENGTH_SHORT).show();
                                            requestBookButton.setText("Cancel Request");
                                        } else {
                                            requestBookProgress.setVisibility(View.GONE);
                                            Toast.makeText(AboutBookActivity.this, "Error sending Book Request", Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                });
                            } else if (requestBookButton.getText().equals("Cancel Request")) {
                                Map cancelMap = new HashMap();
                                cancelMap.put("BookRequests/" + sCurrentUserName + "/sent/" + sBookName, null);
                                cancelMap.put("BookRequests/" + sBookUserName + "/received/" + sBookName, null);

                                dbRef.updateChildren(cancelMap).addOnCompleteListener(new OnCompleteListener() {
                                    @Override
                                    public void onComplete(@NonNull Task task) {
                                        if (task.isSuccessful()) {
                                            requestBookProgress.setVisibility(View.GONE);
                                            Toast.makeText(AboutBookActivity.this, "Book Request Cancelled", Toast.LENGTH_SHORT).show();
                                            requestBookButton.setText("Request Book");
                                        } else {
                                            requestBookProgress.setVisibility(View.GONE);
                                            Toast.makeText(AboutBookActivity.this, "An error occured", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }
                    }else
                        Toast.makeText(AboutBookActivity.this, "Book is not available.Try messaging the user", Toast.LENGTH_SHORT).show();
                }
            }
        });

        userProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(sBookUserName.equals("Student Library")) {
                    Intent userIntent = new Intent(AboutBookActivity.this, AboutUsActivity.class);
                    userIntent.putExtra("User Name", sBookUserName);
                    startActivity(userIntent);
                } else {
                    Intent userIntent = new Intent(AboutBookActivity.this, UserProfileActivity.class);
                    userIntent.putExtra("User Name", sBookUserName);
                    startActivity(userIntent);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        dbRef.child("Users").child(sCurrentUserName).child("blockedUsers").child(sBookUserName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                    blockedUser = true;
                else
                    blockedUser = false;

                dbRef.child("Users").child(sCurrentUserName).child("blockedByUsers").child(sBookUserName).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists())
                            blockedByUser = true;
                        else
                            blockedByUser = false;
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        dbRef.child("BookRequests").child(sCurrentUserName).child("sent").child(sBookName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    String user_name = dataSnapshot.child("userName").getValue().toString();
                    String requestStatus = dataSnapshot.child("requestStatus").getValue().toString();
                    if(user_name.equals(sBookUserName) && requestStatus.equals("pending")) {
                        requestBookButton.setText("Cancel Request");
                    }
                }

                else {
                    requestBookButton.setText("Request Book");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}

