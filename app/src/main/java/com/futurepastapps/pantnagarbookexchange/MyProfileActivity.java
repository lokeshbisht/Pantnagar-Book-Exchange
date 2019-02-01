package com.futurepastapps.pantnagarbookexchange;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.reginald.editspinner.EditSpinner;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class MyProfileActivity extends AppCompatActivity {

    private String cUserName , cUserDp , cNumberOfBooks , cUserThumb , cCollege;
    private final int image_intent = 1;
    private boolean connected;

    private Toolbar mToolbar;
    private TextView mUserName , mNumberOfBooks , mSavingText , mCollege;
    private ImageView editCollege;
    private Button myBooksButton;
    private CircleImageView userDp , closeButton;
    private FloatingActionButton editDpButton;
    private RelativeLayout displayLayout, savingLayout , fullImageLayout;
    private ProgressBar savingProgress , savingCollegeProgress;
    private ImageView userDpFull;

    private DatabaseReference dbRef;
    private FirebaseAuth mAuth;
    private StorageReference storageRefDp , storageRefThumbnail;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.keepSynced(true);

        connected = false;

        mAuth = FirebaseAuth.getInstance();

        cUserName = mAuth.getCurrentUser().getDisplayName();
        storageRefDp = FirebaseStorage.getInstance().getReference().child("userDp").child(cUserName + ".jpg");
        storageRefThumbnail = FirebaseStorage.getInstance().getReference().child("userThumbnail").child(cUserName + ".jpg");

        mToolbar = findViewById(R.id.myProfileBar);
        mToolbar.setTitle("My Profile");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.home);
        getSupportActionBar().setHomeButtonEnabled(true);

        editDpButton = findViewById(R.id.myProfileEditImage);
        editCollege = findViewById(R.id.myProfileEditCollege);
        mUserName = findViewById(R.id.myProfileName);
        userDp = findViewById(R.id.myProfileDp);
        mCollege = findViewById(R.id.myProfileCollege);
        myBooksButton = findViewById(R.id.myProfileMyBooksButton);
        mNumberOfBooks = findViewById(R.id.myProfileNumberOfBooks);
        displayLayout = findViewById(R.id.myProfileDisplayLayout);
        savingLayout = findViewById(R.id.myProfileSavingLayout);
        fullImageLayout = findViewById(R.id.myProfileFullImageLayout);
        savingProgress = findViewById(R.id.myProfileSavingProgress);
        savingProgress.setIndeterminate(true);
        savingCollegeProgress = findViewById(R.id.myProfileSavingCollegeProgress);
        savingCollegeProgress.setIndeterminate(true);
        mSavingText = findViewById(R.id.myProfileSavingText);
        userDpFull = findViewById(R.id.myProfileFullImage);
        closeButton = findViewById(R.id.myProfileCloseImage);

        myBooksButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myBooksIntent = new Intent(MyProfileActivity.this , MyBooksActivity.class);
                myBooksIntent.putExtra("User Thumb" , cUserThumb);
                startActivity(myBooksIntent);
            }
        });

        userDp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayLayout.setVisibility(View.GONE);
                fullImageLayout.setVisibility(View.VISIBLE);
            }
        });

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayLayout.setVisibility(View.VISIBLE);
                fullImageLayout.setVisibility(View.GONE);
            }
        });

        editDpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                connected = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;

                if (connected == false) {
                    Toast.makeText(MyProfileActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
                } else {
                    Intent change = new Intent();
                    change.setType("image/*");
                    change.setAction(Intent.ACTION_GET_CONTENT);

                    startActivityForResult(Intent.createChooser(change, "imageselect"), image_intent);
                }
            }
        });

        editCollege.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                connected = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;

                if (connected == false) {
                    Toast.makeText(MyProfileActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
                } else {
                    final AlertDialog.Builder selectCollege = new AlertDialog.Builder(MyProfileActivity.this);
                    selectCollege.setTitle("Select college");

                    View mView = getLayoutInflater().inflate(R.layout.selectcollegelayout , null);
                    final EditSpinner selectCollegeSpinner = mView.findViewById(R.id.selectCollege);

                    selectCollege.setView(mView);

                    final ArrayAdapter<String> adapter = new ArrayAdapter<String>(MyProfileActivity.this , android.R.layout.simple_spinner_dropdown_item,
                            getResources().getStringArray(R.array.college_name_array));
                    selectCollegeSpinner.setAdapter(adapter);
                    selectCollegeSpinner.setEditable(false);
                    selectCollegeSpinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            cCollege = adapter.getItem(position);
                            selectCollegeSpinner.setText(cCollege);
                        }
                    });
                    selectCollegeSpinner.setOnDismissListener(new PopupWindow.OnDismissListener() {
                        @Override
                        public void onDismiss() {
                            cCollege = "none";
                        }
                    });

                    selectCollege.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            savingCollegeProgress.setVisibility(View.VISIBLE);
                            dbRef.child("Users").child(cUserName).child("college").setValue(cCollege).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()) {
                                        savingCollegeProgress.setVisibility(View.GONE);
                                        Toast.makeText(MyProfileActivity.this, "College update.", Toast.LENGTH_SHORT).show();
                                        mCollege.setText(cCollege);
                                        editCollege.setVisibility(View.GONE);
                                    } else {
                                        Toast.makeText(MyProfileActivity.this, "Error updating college", Toast.LENGTH_SHORT).show();
                                        savingCollegeProgress.setVisibility(View.GONE);
                                    }
                                }
                            });
                        }
                    });

                    selectCollege.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });

                    final AlertDialog selectCollegeDialog = selectCollege.show();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        dbRef.child("Users").child(cUserName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    cUserDp = dataSnapshot.child("dp").getValue().toString();
                    cCollege = dataSnapshot.child("college").getValue().toString();
                    cNumberOfBooks = dataSnapshot.child("numberOfBooks").getValue().toString();
                    cUserThumb = dataSnapshot.child("dpThumbnail").getValue().toString();

                    mUserName.setText(cUserName);
                    mNumberOfBooks.setText(cNumberOfBooks);

                    if(cCollege.equals("none")) {
                        mCollege.setText("Not Specified");
                        editCollege.setVisibility(View.VISIBLE);
                    }
                    else
                        mCollege.setText(cCollege);

                    if(!cUserDp.equals("default")) {
                        Picasso.get().load(cUserDp).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.defaultuser).into(userDp, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError(Exception e) {

                                Picasso.get().load(cUserDp).placeholder(R.drawable.defaultuser).into(userDp);
                            }
                        });

                        Picasso.get().load(cUserDp).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.defaultuser).into(userDpFull, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError(Exception e) {

                                Picasso.get().load(cUserDp).placeholder(R.drawable.defaultuser).into(userDpFull);
                            }
                        });
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == image_intent && resultCode == RESULT_OK) {

            Uri imageUri = data.getData();

            CropImage.activity(imageUri).setAspectRatio(1, 1).start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                Uri resultUri = result.getUri();
                File thumbFile = new File(resultUri.getPath());

                Bitmap thumbMap = null;
                try {
                    thumbMap = new Compressor(this).compressToBitmap(thumbFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumbMap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
                final byte[] thumbByte = baos.toByteArray();

                savingLayout.setVisibility(View.VISIBLE);
                displayLayout.setVisibility(View.GONE);

                storageRefDp.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if (task.isSuccessful()) {

                            final String downloadUrl = task.getResult().getDownloadUrl().toString();
                            UploadTask uploadTask = storageRefThumbnail.putBytes(thumbByte);

                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumbtask) {

                                    if (thumbtask.isSuccessful()) {

                                        String thumbNailUrl = thumbtask.getResult().getDownloadUrl().toString();
                                        Map dpPutMap = new HashMap<>();
                                        dpPutMap.put("Users/" + cUserName + "/" + "dp", downloadUrl);
                                        dpPutMap.put("Users/" + cUserName + "/" + "dpThumbnail", thumbNailUrl);

                                        dbRef.updateChildren(dpPutMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    displayLayout.setVisibility(View.VISIBLE);
                                                    savingLayout.setVisibility(View.GONE);

                                                    Picasso.get().load(downloadUrl).placeholder(R.drawable.defaultuser).into(userDp);
                                                    Picasso.get().load(downloadUrl).placeholder(R.drawable.defaultuser).into(userDpFull);

                                                    Toast.makeText(MyProfileActivity.this, "Profile picture updated", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(MyProfileActivity.this, "Failed to do update profile picture", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });

                                    } else {
                                        Toast.makeText(MyProfileActivity.this, "Failed to do update profile picture", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(MyProfileActivity.this, "Failed to do update profile picture", Toast.LENGTH_LONG).show();
                        }

                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(MyProfileActivity.this, "Failed to do update profile picture", Toast.LENGTH_LONG).show();
            }
        }
    }
}
