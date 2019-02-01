package com.futurepastapps.pantnagarbookexchange;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
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
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;

public class ResetPasswordActivity extends AppCompatActivity {

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;

    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    private TextInputLayout phoneNumber , verificationCode;
    private Button sendCodeButton , verifyCode , resendCode;
    private ProgressBar verifyCodeProgress , sendingCodeProgress;
    private RelativeLayout enterNumberLayout , sendingCodeLayout , enterCodeLayout;
    private Toolbar mToolbar;
    private TextView errorText , verifyTimer;

    private String sPhoneNumber , sVerificationCode , sUserName , sPassword;
    private boolean connected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.keepSynced(true);

        mToolbar = findViewById(R.id.resetPasswordBar);
        mToolbar.setTitle("Forgot Password");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        phoneNumber = findViewById(R.id.resetPasswordNumber);
        verificationCode = findViewById(R.id.resetPasswordCode);
        sendCodeButton = findViewById(R.id.resetPasswordSendCodeButton);
        verifyCode = findViewById(R.id.resetPasswordVerifyCodeButton);
        errorText = findViewById(R.id.resetPasswordErrorText);
        verifyCodeProgress = findViewById(R.id.resetPasswrodVerifyCodeProgress);
        verifyCodeProgress.setIndeterminate(true);
        sendingCodeProgress = findViewById(R.id.resetPasswordVerifyCodeSendingProgress);
        enterNumberLayout = findViewById(R.id.resetPasswordPhoneNumberLayout);
        sendingCodeLayout = findViewById(R.id.resetPasswordSendingCodeLayout);
        enterCodeLayout = findViewById(R.id.resetPasswordEnterCodeLayout);
        resendCode = findViewById(R.id.resetResendcode);
        verifyTimer = findViewById(R.id.resetTimer);

        connected = false;

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {

                Toast.makeText(ResetPasswordActivity.this , "Verification failed" , Toast.LENGTH_SHORT).show();

                sendingCodeLayout.setVisibility(View.GONE);
                enterNumberLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {

                mVerificationId = verificationId;
                mResendToken = token;

                sendingCodeLayout.setVisibility(View.GONE);
                enterCodeLayout.setVisibility(View.VISIBLE);

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

        sendCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                connected = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;

                if (connected == false) {
                    Toast.makeText(ResetPasswordActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
                } else {

                    errorText.setVisibility(View.INVISIBLE);
                    phoneNumber.setErrorEnabled(false);

                    sPhoneNumber = phoneNumber.getEditText().getText().toString();

                    if (sPhoneNumber.length() < 10) {
                        phoneNumber.setErrorEnabled(true);
                        phoneNumber.setError("Please enter a 10 digit number");
                        phoneNumber.requestFocus();
                    } else {
                        dbRef.child("Numbers").child(sPhoneNumber).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    sUserName = dataSnapshot.child("userName").getValue().toString();
                                    sPassword = dataSnapshot.child("password").getValue().toString();

                                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                                            sPhoneNumber,
                                            60,
                                            TimeUnit.SECONDS,
                                            ResetPasswordActivity.this,
                                            mCallbacks);

                                    enterNumberLayout.setVisibility(View.GONE);
                                    sendingCodeLayout.setVisibility(View.VISIBLE);
                                } else {
                                    errorText.setVisibility(View.VISIBLE);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Toast.makeText(ResetPasswordActivity.this, "Error resetting password", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }
        });

        verifyCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                connected = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;

                if (connected == false) {
                    Toast.makeText(ResetPasswordActivity.this, "No internet connection.=", Toast.LENGTH_SHORT).show();
                } else {

                    verifyCodeProgress.setVisibility(View.VISIBLE);
                    verificationCode.setErrorEnabled(false);

                    sVerificationCode = verificationCode.getEditText().getText().toString();

                    if (sVerificationCode.length() != 6) {
                        verifyCodeProgress.setVisibility(View.GONE);
                        verificationCode.setError("Please enter the 6 digit verification code");
                        verificationCode.setErrorEnabled(true);
                        verificationCode.requestFocus();
                    } else {
                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, sVerificationCode);
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
                    Toast.makeText(ResetPasswordActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
                } else {
                    verifyCodeProgress.setVisibility(View.GONE);
                    resendVerificationCode(sPhoneNumber , mResendToken);
                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            verifyCodeProgress.setVisibility(View.INVISIBLE);
                            enterCodeLayout.setVisibility(View.GONE);
                            enterNumberLayout.setVisibility(View.VISIBLE);
                            Intent changePasswordIntent = new Intent(ResetPasswordActivity.this, ChangePasswordActvivity.class);
                            changePasswordIntent.putExtra("User Name" , sUserName);
                            changePasswordIntent.putExtra("Phone Number" , sPhoneNumber);
                            changePasswordIntent.putExtra("Password" , sPassword);
                            startActivity(changePasswordIntent);
                        }
                        else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(ResetPasswordActivity.this , "Invalid Code" , Toast.LENGTH_SHORT).show();
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
}
