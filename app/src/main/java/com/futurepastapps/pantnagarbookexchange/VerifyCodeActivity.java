package com.futurepastapps.pantnagarbookexchange;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class VerifyCodeActivity extends AppCompatActivity {

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    private String mUserName , mPassword , mCollege , mPhoneNumber , mUserDummyEmail;

    private TextInputLayout code;
    private Button verifyCode , resendCode;
    private TextView verifyTimer , sendingCodeMessage;
    private ProgressBar verifyingProgressBar , sendingProgressBar;
    private RelativeLayout sendingCodeLayout , codeSentLayout;

    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private FirebaseAuth mAuth;

    private String verificationCode;
    private boolean connected;
    private Toolbar mToolbar;

    private DatabaseReference mRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_code);

        mToolbar = findViewById(R.id.verifyBar);
        mToolbar.setTitle("Verify Your Number");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        verifyingProgressBar = findViewById(R.id.verifyProgressBar);
        verifyingProgressBar.setIndeterminate(true);
        sendingProgressBar = findViewById(R.id.verifyCodeSendingProgress);
        sendingProgressBar.setIndeterminate(true);
        sendingCodeLayout = findViewById(R.id.sendingCodeLayout);
        codeSentLayout = findViewById(R.id.codeSentLayout);
        sendingCodeMessage = findViewById(R.id.verifyCodeSendingText);

        mAuth = FirebaseAuth.getInstance();
        mRef = FirebaseDatabase.getInstance().getReference();
        mRef.keepSynced(true);

        connected = false;

        mUserName = getIntent().getStringExtra("User Name");
        mPassword = getIntent().getStringExtra("Password");
        mCollege = getIntent().getStringExtra("College");
        mPhoneNumber = getIntent().getStringExtra("Number");

        if(mCollege.equals("none") || mCollege.equals(null))
            mCollege = "Not Specified";

        sendingCodeMessage.setText("Sending verification code to your number " + mPhoneNumber );

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Toast.makeText(VerifyCodeActivity.this , "Verification failed" , Toast.LENGTH_SHORT).show();
                Intent registerIntent = new Intent(VerifyCodeActivity.this , RegisterActivity.class);
                startActivity(registerIntent);
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {

                mVerificationId = verificationId;
                mResendToken = token;

                sendingCodeLayout.setVisibility(View.GONE);
                codeSentLayout.setVisibility(View.VISIBLE);

                Toast.makeText(VerifyCodeActivity.this, "Code sent", Toast.LENGTH_SHORT).show();

                new CountDownTimer(60000, 1000) {

                    public void onTick(long millisUntilFinished) {
                        if(millisUntilFinished/1000 > 9)
                            verifyTimer.setText("00:" + millisUntilFinished / 1000);
                        else
                            verifyTimer.setText("00:0" + millisUntilFinished / 1000);
                    }

                    public void onFinish() {
                        resendCode.setEnabled(true);
                        verifyTimer.setText("00:00");
                    }
                }.start();

            }
        };

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                mPhoneNumber,
                60,
                TimeUnit.SECONDS,
                VerifyCodeActivity.this,
                mCallbacks
        );

        code = findViewById(R.id.verificationCode);
        verifyCode = findViewById(R.id.verifyCode);
        verifyTimer = findViewById(R.id.verifyTimer);
        resendCode = findViewById(R.id.verifyresendcode);

        verifyCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                connected = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;

                if (connected == false) {
                    Toast.makeText(VerifyCodeActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
                } else {

                    verifyingProgressBar.setVisibility(View.VISIBLE);

                    code.setErrorEnabled(false);
                    verificationCode = code.getEditText().getText().toString();

                    if (verificationCode.length() != 6) {
                        code.setError("Please enter the 6 digit verification code");
                        code.setErrorEnabled(true);
                        code.requestFocus();
                        verifyingProgressBar.setVisibility(View.GONE);
                    } else {
                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                        signInWithPhoneAuthCredential(credential);
                    }
                }
            }
        });

        resendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                connected = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;

                if (connected == false) {
                    Toast.makeText(VerifyCodeActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
                } else {
                    verifyingProgressBar.setVisibility(View.GONE);
                    resendVerificationCode(mPhoneNumber, mResendToken);
                }
            }
        });



}

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            String email = mUserName.replaceAll("\\s+", "");

                            mUserDummyEmail = email + "@mydomain.com";
                            mAuth.createUserWithEmailAndPassword(mUserDummyEmail , mPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()) {
                                        FirebaseUser user = task.getResult().getUser();
                                        final String uid = user.getUid();

                                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                .setDisplayName(mUserName)
                                                .build();

                                        user.updateProfile(profileUpdates)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {

                                                            String devicetoken = FirebaseInstanceId.getInstance().getToken();
                                                            String userRef = "Users/" + mUserName;
                                                            String numberRef = "Numbers/" + mPhoneNumber;

                                                            mRef = FirebaseDatabase.getInstance().getReference();

                                                            int books = 0;

                                                            Map userMap = new HashMap();
                                                            userMap.put("college" , mCollege);
                                                            userMap.put("number" , mPhoneNumber);
                                                            userMap.put("deviceToken" , devicetoken);
                                                            userMap.put("online" , "true");
                                                            userMap.put("id" , uid);
                                                            userMap.put("numberOfBooks" , books);
                                                            userMap.put("books" , "none");
                                                            userMap.put("dpThumbnail" , "default");
                                                            userMap.put("dp" , "default");

                                                            Map numbersMap = new HashMap();
                                                            numbersMap.put("userName" , mUserName);
                                                            numbersMap.put("userId" , uid);
                                                            numbersMap.put("password" , mPassword);

                                                            Map putMap = new HashMap();
                                                            putMap.put(userRef , userMap);
                                                            putMap.put(numberRef , numbersMap);

                                                            mRef.updateChildren(putMap, new DatabaseReference.CompletionListener() {
                                                                @Override
                                                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                                    if(databaseError == null) {

                                                                        verifyingProgressBar.setVisibility(View.GONE);
                                                                        Toast.makeText(VerifyCodeActivity.this , "Account Created Successfully" , Toast.LENGTH_SHORT).show();

                                                                        Intent aboutAppActivity = new Intent(VerifyCodeActivity.this , AboutAppActivity.class);
                                                                        aboutAppActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                        startActivity(aboutAppActivity);
                                                                        finish();
                                                                    }
                                                                    else {
                                                                        FirebaseAuth.getInstance().signOut();
                                                                        Toast.makeText(VerifyCodeActivity.this , "Could Not Create Account" , Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }
                                                            });
                                                        } else {
                                                            FirebaseAuth.getInstance().signOut();
                                                            Toast.makeText(VerifyCodeActivity.this , "Error creating account" , Toast.LENGTH_LONG).show();
                                                        }
                                                    }
                                                });

                                    } else {
                                        FirebaseAuth.getInstance().signOut();
                                        Toast.makeText(VerifyCodeActivity.this , "Error creating account" , Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }
                        else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(VerifyCodeActivity.this , "Invalid Code", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }

    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS,
                this,
                mCallbacks,
                token);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}
