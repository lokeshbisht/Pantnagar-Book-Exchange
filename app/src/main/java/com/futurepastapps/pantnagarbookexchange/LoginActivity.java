package com.futurepastapps.pantnagarbookexchange;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private TextInputLayout userName, password;
    private Button loginButton, resetPasswordButton;
    private ProgressBar loginProgress;

    private Boolean connected;

    private String uName, uPassword, uDummyEmail;

    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;

    private DatabaseReference mref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        connected = false;

        mToolbar = findViewById(R.id.loginBar);
        mToolbar.setTitle("Login");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userName = findViewById(R.id.loginUname);
        password = findViewById(R.id.loginPassword);
        loginButton = findViewById(R.id.loginButton);
        resetPasswordButton = findViewById(R.id.loginResetPasswordButton);
        loginProgress = findViewById(R.id.loginProgressBar);
        loginProgress.setIndeterminate(true);

        mref = FirebaseDatabase.getInstance().getReference();
        mref.keepSynced(true);

        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                connected = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;

                if (connected == false) {
                    Toast.makeText(LoginActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
                } else {

                    loginButton.setEnabled(false);
                    loginProgress.setVisibility(View.VISIBLE);

                    password.setErrorEnabled(false);
                    userName.setErrorEnabled(false);

                    uName = userName.getEditText().getText().toString();
                    uPassword = password.getEditText().getText().toString();

                    if (TextUtils.isEmpty(uName)) {
                        userName.setError("Please enter your User Name");
                        userName.setErrorEnabled(true);
                        userName.requestFocus();
                        loginProgress.setVisibility(View.GONE);
                        loginButton.setEnabled(true);
                    } else if (TextUtils.isEmpty(uPassword)) {
                        password.setError("Please enter your Password");
                        password.setErrorEnabled(true);
                        password.requestFocus();
                        loginProgress.setVisibility(View.GONE);
                        loginButton.setEnabled(true);
                    } else {
                        if (!(uName.matches("[a-zA-Z0-9 ]*"))) {
                            userName.setError("No such user exists");
                            userName.setErrorEnabled(true);
                            userName.requestFocus();
                            loginProgress.setVisibility(View.GONE);
                            loginButton.setEnabled(true);
                        } else {
                            mref.child("Users").child(uName).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (!dataSnapshot.exists()) {
                                        userName.setError("No such user exists");
                                        userName.setErrorEnabled(true);
                                        userName.requestFocus();
                                        loginProgress.setVisibility(View.GONE);
                                        loginButton.setEnabled(true);
                                    } else {
                                        String email = uName.replaceAll("\\s+", "");
                                        uDummyEmail = email + "@mydomain.com";

                                        mAuth.signInWithEmailAndPassword(uDummyEmail, uPassword).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                if (task.isSuccessful()) {

                                                    FirebaseUser user = task.getResult().getUser();
                                                    String uid = user.getUid();

                                                    String devicetoken = FirebaseInstanceId.getInstance().getToken();
                                                    dbRef.child("Users").child(mAuth.getCurrentUser().getDisplayName()).child("deviceToken").setValue(devicetoken).addOnSuccessListener(LoginActivity.this, new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            loginProgress.setVisibility(View.GONE);
                                                            loginButton.setEnabled(true);

                                                            Toast.makeText(LoginActivity.this, "Logged in successfully", Toast.LENGTH_SHORT).show();

                                                            Intent mainIntent = new Intent(LoginActivity.this, HomeActivity.class);
                                                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                            startActivity(mainIntent);
                                                            finish();
                                                        }
                                                    });
                                                } else {
                                                    loginProgress.setVisibility(View.GONE);
                                                    String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
                                                    switch (errorCode) {
                                                        case "ERROR_WRONG_PASSWORD":
                                                            password.setError("The password you entered is incorrect");
                                                            password.setErrorEnabled(true);
                                                            password.requestFocus();
                                                            break;
                                                        default:
                                                            Toast.makeText(LoginActivity.this, "Error logging in. Please try again", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            });
                        }
                    }
                }
            }
        });

        resetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginProgress.setVisibility(View.GONE);
                Intent resetPasswordIntent = new Intent(LoginActivity.this , ResetPasswordActivity.class);
                startActivity(resetPasswordIntent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

    }
}

