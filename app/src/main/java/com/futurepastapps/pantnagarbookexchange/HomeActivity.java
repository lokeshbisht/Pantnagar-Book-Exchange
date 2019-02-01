package com.futurepastapps.pantnagarbookexchange;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class HomeActivity extends AppCompatActivity {


    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private String cUserName;

    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private SectionsPager mSectionsPager;

    private DatabaseReference dbRef;

    private int[] icons = {
            R.drawable.chats_toggle,
            R.drawable.book_toggle,
            R.drawable.book_request_toggle
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mToolbar = findViewById(R.id.mainBar);
        setSupportActionBar(mToolbar);

        mViewPager = findViewById(R.id.mainPager);
        mSectionsPager = new SectionsPager(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionsPager);
        mTabLayout = findViewById(R.id.mainTab);
        mTabLayout.setupWithViewPager(mViewPager);



        for (int i = 0; i < mTabLayout.getTabCount(); i++) {
            mTabLayout.getTabAt(i).setIcon(icons[i]);
        }

        dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.keepSynced(true);

        TabLayout.Tab tab = mTabLayout.getTabAt(1);
        tab.select();

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();

        currentUser = mAuth.getCurrentUser();

        if(currentUser == null) {

            Intent authIntent = new Intent(HomeActivity.this , AuthActivity.class);
            startActivity(authIntent);
            finish();
        }

        else {

            cUserName = currentUser.getDisplayName();
            dbRef = FirebaseDatabase.getInstance().getReference().child("Users").child(cUserName);
            dbRef.child("online").setValue(true);
        }

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                invalidateOptionsMenu();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();

        currentUser = mAuth.getCurrentUser();

        if(currentUser != null) {

            dbRef = FirebaseDatabase.getInstance().getReference().child("Users").child(cUserName);
            dbRef.child("online").setValue(ServerValue.TIMESTAMP);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        if(mTabLayout.getSelectedTabPosition() == 0) {
            getMenuInflater().inflate(R.menu.chatsmenu, menu);
        } else if(mTabLayout.getSelectedTabPosition() == 1) {
            getMenuInflater().inflate(R.menu.booksmenu, menu);
        } else if(mTabLayout.getSelectedTabPosition() == 2) {
            getMenuInflater().inflate(R.menu.bookrequestsmenu, menu);
        }

        return true;
    }




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId() == R.id.myProfileOption) {
            Intent myProfileIntent = new Intent(HomeActivity.this , MyProfileActivity.class);
            startActivity(myProfileIntent);
        }

        if(item.getItemId() == R.id.blockedUsersOption) {
            Intent blockedUsersIntent = new Intent(HomeActivity.this , BlockedUsersActivity.class);
            startActivity(blockedUsersIntent);

        }

        if(item.getItemId() == R.id.tellAFriendOption) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, "Fond of reading books and sharing them with friends??\nDownload Pantnagar Book Exchange App now and join the community of students like you, who love reading and love sharing!!!\nhttps://play.google.com/store/apps/details?id=com.futurepastapps.pantnagarbookexchange");
            sendIntent.setType("text/plain");
            startActivity(Intent.createChooser(sendIntent, "Tell friend using :"));
        }

        if(item.getItemId() == R.id.rateUsOption) {
            rateApp();
        }

        if(item.getItemId() == R.id.donateBookOption) {
            Intent donateBookIntent = new Intent(HomeActivity.this , DonateBookActivity.class);
            startActivity(donateBookIntent);
        }


        if(item.getItemId() == R.id.myRequestsOption) {
            Intent myRequestsIntent= new Intent(HomeActivity.this , MyRequestsActivity.class);
            startActivity(myRequestsIntent);

        }

        if(item.getItemId() == R.id.logoutOption) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle("Log Out");
            builder.setMessage("Are you sure you want to logout");

            builder.setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    String uid = currentUser.getUid();
                    dbRef = FirebaseDatabase.getInstance().getReference().child("Users").child(cUserName);
                    dbRef.child("online").setValue(ServerValue.TIMESTAMP);
                    FirebaseAuth.getInstance().signOut();
                    Intent logoutIntent = new Intent(HomeActivity.this , AuthActivity.class);
                    startActivity(logoutIntent);
                    finish();

                }
            });

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            builder.show();
        }

        return true;
    }

    public void rateApp()
    {
        try
        {
            Intent rateIntent = rateIntentForUrl("market://details");
            startActivity(rateIntent);
        }
        catch (ActivityNotFoundException e)
        {
            Intent rateIntent = rateIntentForUrl("https://play.google.com/store/apps/details");
            startActivity(rateIntent);
        }
    }

    private Intent rateIntentForUrl(String url)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("%s?id=%s", url, getPackageName())));
        int flags = Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK;
        if (Build.VERSION.SDK_INT >= 21)
        {
            flags |= Intent.FLAG_ACTIVITY_NEW_DOCUMENT;
        }
        else
        {
            flags |= Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET;
        }
        intent.addFlags(flags);
        return intent;
    }
}
