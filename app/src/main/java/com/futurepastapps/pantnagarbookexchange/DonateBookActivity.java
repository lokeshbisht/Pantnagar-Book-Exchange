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
import android.widget.Toast;

public class DonateBookActivity extends AppCompatActivity {

    private Button messageUsButton;
    private Toolbar mToolbar;

    private boolean connected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donate_book);

        mToolbar = findViewById(R.id.donateBookBar);
        mToolbar.setTitle("Donate Book");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.home);
        getSupportActionBar().setHomeButtonEnabled(true);

        connected = false;

        messageUsButton = findViewById(R.id.donateBookMessageUs);

        messageUsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                connected = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;

                if (connected == false) {
                    Toast.makeText(DonateBookActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
                } else {
                    Intent chatIntent = new Intent(DonateBookActivity.this, ChatActivity.class);
                    chatIntent.putExtra("User Name", "Student Library");
                    startActivity(chatIntent);
                }
            }
        });
    }
}
