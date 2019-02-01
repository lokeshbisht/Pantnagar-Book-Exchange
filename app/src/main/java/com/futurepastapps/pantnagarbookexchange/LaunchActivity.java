package com.futurepastapps.pantnagarbookexchange;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.util.Random;

public class LaunchActivity extends AppCompatActivity {

    private static int Timeout = 1500;

    private TextView quote;

    private int randomNumber;
    private Random random;
    private String[] quotes;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent homeIntent = new Intent(LaunchActivity.this , HomeActivity.class);
                startActivity(homeIntent);
                finish();
            }
        } , Timeout);

        quote = findViewById(R.id.launchQuote);

        random = new Random();

        quotes = getResources().getStringArray(R.array.quotes_array);

        randomNumber = random.nextInt(quotes.length);

        quote.setText("'" + quotes[randomNumber] + "'");

    }
}
