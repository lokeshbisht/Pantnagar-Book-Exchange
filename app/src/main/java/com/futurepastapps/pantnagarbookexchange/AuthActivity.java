package com.futurepastapps.pantnagarbookexchange;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

public class AuthActivity extends AppCompatActivity {


    private Button login , register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        login = findViewById(R.id.authLogin);
        register = findViewById(R.id.authCreateAccount);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent registerIntent = new Intent(AuthActivity.this , RegisterActivity.class);
                startActivity(registerIntent);
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent loginIntent = new Intent(AuthActivity.this , LoginActivity.class);
                startActivity(loginIntent);
            }
        });
    }
}
