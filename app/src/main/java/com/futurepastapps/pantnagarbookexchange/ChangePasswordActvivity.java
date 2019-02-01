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
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Map;

public class ChangePasswordActvivity extends AppCompatActivity {

    private TextInputLayout newPassword, verifyPassword;
    private Button changePasswordButton;
    private Toolbar mToolbar;
    private ProgressBar loggingInProgress;
    private RelativeLayout changePasswordLayout , loggingInLayout;

    private String sPhoneNumber, sUserName, sDummyEmail, sNewPassword, sVerifyPassword , sOldPassword;
    private boolean connected;

    private DatabaseReference dbRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        sPhoneNumber = getIntent().getStringExtra("Phone Number");
        sUserName = getIntent().getStringExtra("User Name");
        sOldPassword = getIntent().getStringExtra("Password");

        mToolbar = findViewById(R.id.changePasswordBar);
        mToolbar.setTitle("Pantnagar Book Exchange");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        newPassword = findViewById(R.id.changePasswordPassword);
        verifyPassword = findViewById(R.id.changePasswordConfirmPassword);
        changePasswordButton = findViewById(R.id.changePasswordButton);
        loggingInProgress = findViewById(R.id.changePasswordLoginProgress);
        loggingInProgress.setIndeterminate(true);
        changePasswordLayout = findViewById(R.id.changePasswordNewPasswordLayout);
        loggingInLayout = findViewById(R.id.changePasswordLoggingInLayout);


        dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.keepSynced(true);

        connected = false;

        mAuth = FirebaseAuth.getInstance();

        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                connected = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;

                if (connected == false) {
                    Toast.makeText(ChangePasswordActvivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
                } else {

                    newPassword.setErrorEnabled(false);
                    verifyPassword.setErrorEnabled(false);

                    sNewPassword = newPassword.getEditText().getText().toString();
                    sVerifyPassword = verifyPassword.getEditText().getText().toString();

                    if (TextUtils.isEmpty(sNewPassword)) {
                        newPassword.setError("Please type your new Password");
                        newPassword.setErrorEnabled(true);
                        newPassword.requestFocus();
                    } else if (sNewPassword.length() < 8) {
                        newPassword.setError("Password must be atleast 8 characters long");
                        newPassword.setErrorEnabled(true);
                        newPassword.requestFocus();
                    } else if (TextUtils.isEmpty(sVerifyPassword)) {
                        verifyPassword.setError("Please confirm your Password");
                        verifyPassword.setErrorEnabled(true);
                        verifyPassword.requestFocus();
                    } else if (!sVerifyPassword.equals(sNewPassword)) {
                        verifyPassword.setError("Passwords do not match");
                        verifyPassword.setErrorEnabled(true);
                        verifyPassword.requestFocus();
                    } else {

                        changePasswordLayout.setVisibility(View.GONE);
                        loggingInLayout.setVisibility(View.VISIBLE);

                        sDummyEmail = sUserName + "@mydomain.com";

                        mAuth.signInWithEmailAndPassword(sDummyEmail, sOldPassword).addOnCompleteListener(ChangePasswordActvivity.this , new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = task.getResult().getUser();
                                    user.updatePassword(sNewPassword);

                                    String deviceToken = FirebaseInstanceId.getInstance().getToken();

                                    Map changePasswordMap = new HashMap();
                                    changePasswordMap.put("Numbers/" + sPhoneNumber + "/password", sNewPassword);
                                    changePasswordMap.put("Users/" + sUserName + "/deviceToken" , deviceToken);

                                    dbRef.updateChildren(changePasswordMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                loggingInLayout.setVisibility(View.GONE);
                                                changePasswordLayout.setVisibility(View.VISIBLE);

                                                Toast.makeText(ChangePasswordActvivity.this , "Your password has been changed successfully" , Toast.LENGTH_SHORT).show();

                                                Intent mainactivity = new Intent(ChangePasswordActvivity.this, HomeActivity.class);
                                                mainactivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(mainactivity);
                                                finish();
                                            }
                                        }
                                    });

                                } else {
                                    loggingInLayout.setVisibility(View.GONE);
                                    changePasswordLayout.setVisibility(View.VISIBLE);
                                    FirebaseAuth.getInstance().signOut();
                                    Toast.makeText(ChangePasswordActvivity.this, "An error occured", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

    }
}
