package com.futurepastapps.pantnagarbookexchange;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileActivity extends AppCompatActivity {

    private TextView userName , numberOfBooks , college;
    private Button sendMessage , viewBooks;
    private Toolbar mToolbar;
    private ImageView userDpFull , userDp;
    private CircleImageView closeImage;
    private RelativeLayout userProfileLayout , fullImageLayout;

    private String viewUserName , sNumberOfBooks , sDp , cUserName , sCollege;
    int iNumberOfBooks;

    private DatabaseReference dbRef;
    private FirebaseAuth mAuth;

    private boolean connected , blockedUser , blockedByUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        viewUserName = getIntent().getStringExtra("User Name");

        mToolbar = findViewById(R.id.userProfileBar);
        mToolbar.setTitle("User Profile");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.home);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        cUserName = mAuth.getCurrentUser().getDisplayName();

        userName = findViewById(R.id.userProfileUserName);
        numberOfBooks = findViewById(R.id.userProfileBooks);
        college = findViewById(R.id.userProfileCollege);
        sendMessage = findViewById(R.id.userProfileSendMessageButton);
        viewBooks = findViewById(R.id.userProfileViewBooksButton);
        userDp = findViewById(R.id.userProfileDp);
        userDpFull = findViewById(R.id.userProfileFullImage);
        closeImage = findViewById(R.id.userProfileCloseImage);
        userProfileLayout = findViewById(R.id.userProfileLayout);
        fullImageLayout = findViewById(R.id.userProfileImageLayout);

        dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.keepSynced(true);

        connected = false;
        blockedByUser = false;
        blockedUser = false;

        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                connected = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;

                if (connected == false) {
                    Toast.makeText(UserProfileActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
                } else {

                    if(blockedByUser)
                        Toast.makeText(UserProfileActivity.this, "Cannot message the user. The user has blocked you", Toast.LENGTH_SHORT).show();
                    else if(blockedUser)
                        Toast.makeText(UserProfileActivity.this, "Cannot message the user. You have blocked the user", Toast.LENGTH_SHORT).show();
                    else {
                        Intent chatIntent = new Intent(UserProfileActivity.this, ChatActivity.class);
                        chatIntent.putExtra("User Name", viewUserName);
                        startActivity(chatIntent);
                    }
                }
            }
        });

        viewBooks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(iNumberOfBooks == 0) {
                    Toast.makeText(UserProfileActivity.this , viewUserName + " has not added any book yet" , Toast.LENGTH_SHORT).show();
                } else {
                    Intent bookListIntent = new Intent(UserProfileActivity.this, BookListActivity.class);
                    bookListIntent.putExtra("User Name" , viewUserName);
                    startActivity(bookListIntent);
                }
            }
        });

        userDp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fullImageLayout.setVisibility(View.VISIBLE);
                userProfileLayout.setVisibility(View.GONE);
            }
        });

        closeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userProfileLayout.setVisibility(View.VISIBLE);
                fullImageLayout.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        dbRef.child("Users").child(cUserName).child("blockedUsers").child(viewUserName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                    blockedUser = true;
                else
                    blockedUser = false;

                dbRef.child("Users").child(cUserName).child("blockedByUsers").child(viewUserName).addListenerForSingleValueEvent(new ValueEventListener() {
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

        dbRef.child("Users").child(viewUserName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    sNumberOfBooks = dataSnapshot.child("numberOfBooks").getValue().toString();
                    sCollege = dataSnapshot.child("college").getValue().toString();

                    iNumberOfBooks = Integer.parseInt(sNumberOfBooks);
                    sDp = dataSnapshot.child("dp").getValue().toString();

                    userName.setText(viewUserName);
                    numberOfBooks.setText(sNumberOfBooks);

                    if(sCollege.equals("none"))
                        college.setText("Not specified");
                    else
                        college.setText(sCollege);

                    Picasso.get().load(sDp).placeholder(R.drawable.defaultuser).into(userDp);
                    Picasso.get().load(sDp).placeholder(R.drawable.defaultuser).into(userDpFull);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
