package com.futurepastapps.pantnagarbookexchange;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.ProviderQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.reginald.editspinner.EditSpinner;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout userName, password, confirmpass, phoneNumber;
    private String mUserName, mPassword, mConfirmPassword, mCollege, mPhoneNumber;
    private Button createAccount;
    private CheckBox agree;
    private EditSpinner college;
    private ProgressBar progressBar;

    private boolean connected;

    private Toolbar mToolbar;

    private FirebaseAuth mAuth;
    private DatabaseReference mref;


    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        connected = false;

        mAuth = FirebaseAuth.getInstance();
        mref = FirebaseDatabase.getInstance().getReference();
        mref.keepSynced(true);

        mToolbar = findViewById(R.id.registerBar);
        mToolbar.setTitle("Create Your Account");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressBar = findViewById(R.id.registerProgressBar);
        progressBar.setIndeterminate(true);

        agree = findViewById(R.id.regAgree);

        SpannableString spannableStringPolicy = new SpannableString("I have read the privacy policy");
        ClickableSpan clickableSpanPolicy = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://pantnagarbookexchagepolicy.blogspot.com/2018/09/privacy-policy-futurepast-apps-built.html"));
                startActivity(intent);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setUnderlineText(false);
                ds.setColor(getResources().getColor(R.color.colorHighlightedText));
            }
        };

        spannableStringPolicy.setSpan(clickableSpanPolicy,16,29, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableStringPolicy.setSpan(new StyleSpan(Typeface.BOLD),16,29,0);

        SpannableString spannableStringTerms = new SpannableString(" and agree to the Terms of use.");
        ClickableSpan clickableSpanTerms = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                startActivity(new Intent(RegisterActivity.this , TermsActivity.class));
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setUnderlineText(false);
                ds.setColor(getResources().getColor(R.color.colorHighlightedText));
            }
        };

        spannableStringTerms.setSpan(clickableSpanTerms,18,29, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableStringTerms.setSpan(new StyleSpan(Typeface.BOLD),18,29,0);

        agree.setText(TextUtils.concat(spannableStringPolicy , spannableStringTerms));
        agree.setMovementMethod(LinkMovementMethod.getInstance());

        agree.setHighlightColor(Color.TRANSPARENT);

        college = findViewById(R.id.regcollege);
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,
                getResources().getStringArray(R.array.college_name_array));
        college.setAdapter(adapter);
        college.setEditable(false);
        college.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mCollege = adapter.getItem(position);
                college.setText(mCollege);
            }
        });
        college.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                mCollege = "none";
            }
        });

        userName = findViewById(R.id.reguname);
        password = findViewById(R.id.regpassword);
        confirmpass = findViewById(R.id.regconfirmpass);
        phoneNumber = findViewById(R.id.regphone);
        createAccount = findViewById(R.id.regbutton);

        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                connected = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;

                if (!connected) {
                    Toast.makeText(RegisterActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
                } else {

                    progressBar.setVisibility(View.VISIBLE);

                    userName.setErrorEnabled(false);
                    password.setErrorEnabled(false);
                    confirmpass.setErrorEnabled(false);
                    phoneNumber.setErrorEnabled(false);

                    mUserName = userName.getEditText().getText().toString();
                    mPassword = password.getEditText().getText().toString();
                    mConfirmPassword = confirmpass.getEditText().getText().toString();
                    mPhoneNumber = phoneNumber.getEditText().getText().toString();

                    if (TextUtils.isEmpty(mUserName)) {
                        userName.setError("Please type in a User Name");
                        userName.setErrorEnabled(true);
                        userName.requestFocus();
                        progressBar.setVisibility(View.GONE);
                    } else if (!(mUserName.matches("[a-zA-Z0-9 ]*"))) {
                        userName.setError("Please use only letters and numbers");
                        userName.setErrorEnabled(true);
                        userName.requestFocus();
                        progressBar.setVisibility(View.GONE);
                    } else if (TextUtils.isEmpty(mPassword)) {
                        password.setError("Please type in a Password");
                        password.setErrorEnabled(true);
                        password.requestFocus();
                        progressBar.setVisibility(View.GONE);
                    } else if (mPassword.length() < 8) {
                        password.setError("Password must be atleast 8 characters long");
                        password.setErrorEnabled(true);
                        password.requestFocus();
                        progressBar.setVisibility(View.GONE);
                    } else if (TextUtils.isEmpty(mConfirmPassword)) {
                        confirmpass.setError("Please confirm your Password");
                        confirmpass.setErrorEnabled(true);
                        confirmpass.requestFocus();
                        progressBar.setVisibility(View.GONE);
                    } else if (!mPassword.equals(mConfirmPassword)) {
                        confirmpass.setError("Passwords do not match");
                        confirmpass.setErrorEnabled(true);
                        confirmpass.requestFocus();
                        progressBar.setVisibility(View.GONE);
                    } else if (mPhoneNumber.length() != 10) {
                        phoneNumber.setError("Please enter a 10 digit mobile number");
                        phoneNumber.setErrorEnabled(true);
                        phoneNumber.requestFocus();
                        progressBar.setVisibility(View.GONE);
                    } else if(!agree.isChecked()) {
                        Toast.makeText(RegisterActivity.this, "You must agree to the terms of use", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    } else {
                        String email = mUserName.replaceAll("\\s+", "");
                        String dummyEmail = email + "@mydomain.com";

                        mAuth.fetchProvidersForEmail(dummyEmail).addOnCompleteListener(new OnCompleteListener<ProviderQueryResult>() {
                            @Override
                            public void onComplete(@NonNull Task<ProviderQueryResult> task) {
                                if (task.isSuccessful()) {
                                    ProviderQueryResult result = task.getResult();

                                    if (result != null && result.getProviders() != null
                                            && result.getProviders().size() > 0) {
                                        userName.setError("User Name already exists. Choose another");
                                        userName.setErrorEnabled(true);
                                        userName.requestFocus();
                                        progressBar.setVisibility(View.GONE);
                                    } else {
                                        mref.child("Users").orderByChild("number").equalTo(mPhoneNumber).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.exists()) {
                                                    phoneNumber.setError("Account already exist. Try logging in");
                                                    phoneNumber.setErrorEnabled(true);
                                                    phoneNumber.requestFocus();
                                                    progressBar.setVisibility(View.GONE);
                                                } else {
                                                    progressBar.setVisibility(View.GONE);
                                                    Intent verifyCodeIntent = new Intent(RegisterActivity.this, VerifyCodeActivity.class);
                                                    verifyCodeIntent.putExtra("User Name", mUserName);
                                                    verifyCodeIntent.putExtra("Password", mPassword);
                                                    verifyCodeIntent.putExtra("College", mCollege);
                                                    verifyCodeIntent.putExtra("Number", mPhoneNumber);
                                                    startActivity(verifyCodeIntent);
                                                    finish();
                                                }
                                            }
                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                                Toast.makeText(RegisterActivity.this , "Error creating account" , Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                } else {
                                    Toast.makeText(RegisterActivity.this , "Error creating account" , Toast.LENGTH_SHORT).show();
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
