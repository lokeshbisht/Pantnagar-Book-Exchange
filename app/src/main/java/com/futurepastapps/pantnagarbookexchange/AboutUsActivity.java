package com.futurepastapps.pantnagarbookexchange;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AboutUsActivity extends AppCompatActivity {

    private TextView numberOfBooks;
    private Toolbar mToolbar;
    private Button viewBooks , messageUs;

    private String sBooks;
    private int iNumberOfBooks;
    private DatabaseReference dbRef;

    private String cUserName;
    private boolean blockedUser , blockedByUser , connected;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        mToolbar = findViewById(R.id.aboutUsBar);
        mToolbar.setTitle("About Us");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.home);
        getSupportActionBar().setHomeButtonEnabled(true);

        numberOfBooks = findViewById(R.id.aboutUsNumberOfBooks);
        viewBooks = findViewById(R.id.aboutUsViewBooks);
        messageUs = findViewById(R.id.aboutUsMessageButton);

        dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.keepSynced(true);

        blockedByUser = false;
        blockedUser = false;
        connected = false;

        mAuth = FirebaseAuth.getInstance();
        cUserName = mAuth.getCurrentUser().getDisplayName();

        viewBooks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(iNumberOfBooks == 0) {
                    Toast.makeText(AboutUsActivity.this , "There are no books in the library" , Toast.LENGTH_SHORT).show();
                } else {
                    Intent bookListIntent = new Intent(AboutUsActivity.this, BookListActivity.class);
                    bookListIntent.putExtra("User Name" , "Student Library");
                    startActivity(bookListIntent);
                }
            }
        });

        messageUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                connected = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;

                if (connected == false) {
                    Toast.makeText(AboutUsActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
                } else {
                    if(blockedByUser)
                        Toast.makeText(AboutUsActivity.this, "Cannot message the user. The user has blocked you", Toast.LENGTH_SHORT).show();
                    else if(blockedUser)
                        Toast.makeText(AboutUsActivity.this, "Cannot message the user. You have blocked the user", Toast.LENGTH_SHORT).show();
                    else {
                        Intent chatIntent = new Intent(AboutUsActivity.this, ChatActivity.class);
                        chatIntent.putExtra("User Name", "Student Library");
                        startActivity(chatIntent);
                    }
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        dbRef.child("Users").child(cUserName).child("blockedUsers").child("Student Library").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                    blockedUser = true;
                else
                    blockedUser = false;

                dbRef.child("Users").child(cUserName).child("blockedByUsers").child("Student Library").addListenerForSingleValueEvent(new ValueEventListener() {
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

        dbRef.child("Users").child("Student Library").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    sBooks = dataSnapshot.child("numberOfBooks").getValue().toString();
                    numberOfBooks.setText(sBooks);
                    iNumberOfBooks = Integer.parseInt(sBooks);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
