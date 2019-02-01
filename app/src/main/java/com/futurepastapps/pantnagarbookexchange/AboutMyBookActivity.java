package com.futurepastapps.pantnagarbookexchange;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class AboutMyBookActivity extends AppCompatActivity {

    private TextView bookName, authorName, genreAndLanguage , description , availability;
    private Button removeFromMyBooksButton , editBookButton;
    private ToggleButton availabilityToggle;
    private ImageView descriptionDropDown;

    private String sBookName, sAuthorName, sGenre, sLanguage, sCurrentUserName , sDescription , userThumb;
    private boolean showMore;

    private DatabaseReference dbRef;

    private boolean available , connected;

    private android.support.v7.widget.Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_my_book);

        mToolbar = findViewById(R.id.aboutMyBookBar);
        mToolbar.setTitle("Pantnagar Book Exchange");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.home);
        getSupportActionBar().setHomeButtonEnabled(true);

        bookName = findViewById(R.id.aboutMyBookBookName);
        authorName = findViewById(R.id.aboutMyBookAuthor);
        genreAndLanguage = findViewById(R.id.aboutMyBookGenreAndLanguage);
        description = findViewById(R.id.aboutMyBookDescription);
        availability = findViewById(R.id.aboutMyBookAvailability);
        removeFromMyBooksButton = findViewById(R.id.aboutMyBookRemoveButton);
        availabilityToggle = findViewById(R.id.aboutMyBookAvailabilityToggle);
        editBookButton = findViewById(R.id.aboutMyBookEditButton);
        descriptionDropDown = findViewById(R.id.aboutMyBookShowMore);

        showMore = true;
        connected = false;

        sBookName = getIntent().getStringExtra("Book Name");
        sAuthorName = getIntent().getStringExtra("Author Name");
        sGenre = getIntent().getStringExtra("Genre");
        sLanguage = getIntent().getStringExtra("Language");
        sDescription = getIntent().getStringExtra("Description");
        available = getIntent().getBooleanExtra("Available" , true);
        userThumb = getIntent().getStringExtra("User Thumb");
        sCurrentUserName = getIntent().getStringExtra("User Name");

        dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.keepSynced(true);

        bookName.setText(sBookName);
        authorName.setText(sAuthorName);
        genreAndLanguage.setText(sGenre + " | " + sLanguage);
        if(sDescription.equals("none")) {
            description.setText("No description");
            descriptionDropDown.setVisibility(View.GONE);
        } else
            description.setText(sDescription);

        if(available) {
            availability.setText("Available");
            availabilityToggle.setChecked(true);
        }
        else {
            availability.setText("Currently unavailable");
            availabilityToggle.setChecked(false);
        }

        descriptionDropDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(showMore) {
                    showMore = false;
                    descriptionDropDown.setImageResource(R.drawable.showless);
                    description.setMaxLines(8);
                } else {
                        showMore = true;
                        descriptionDropDown.setImageResource(R.drawable.showmore);
                        description.setMaxLines(2);
                }
            }
        });

        availabilityToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                connected = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;

                if (connected == false) {
                    Toast.makeText(AboutMyBookActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
                    if(b)
                        availabilityToggle.setChecked(false);
                    else
                        availabilityToggle.setChecked(true);
                } else {
                    if (b) {
                        dbRef.child("Books").child(sBookName).child("available").setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()) {
                                    availability.setText("Available");
                                    Toast.makeText(AboutMyBookActivity.this, "Book marked available", Toast.LENGTH_SHORT).show();
                                } else
                                    Toast.makeText(AboutMyBookActivity.this, "An error occured", Toast.LENGTH_SHORT).show();
                            }
                        });

                    } else {
                        dbRef.child("Books").child(sBookName).child("available").setValue(false).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()) {
                                    availability.setText("Currently unavailable");
                                    Toast.makeText(AboutMyBookActivity.this, "Book marked unavailable", Toast.LENGTH_SHORT).show();
                                } else
                                    Toast.makeText(AboutMyBookActivity.this, "An error occured", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }
        });

        removeFromMyBooksButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                connected = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;

                if (connected == false) {
                    Toast.makeText(AboutMyBookActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
                }
                else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(AboutMyBookActivity.this);

                    builder.setTitle("Remove Book");
                    builder.setMessage("Are you sure you want to remove this book ?");

                    builder.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                                        dbRef.child("Users").child(sCurrentUserName).child("numberOfBooks").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.exists()) {
                                                    String numberOfBooks = dataSnapshot.getValue().toString();

                                                    int iNumberOfBooks = Integer.parseInt(numberOfBooks);
                                                    iNumberOfBooks -= 1;

                                                    Map removeBookMap = new HashMap();
                                                    removeBookMap.put("Users/" + sCurrentUserName + "/books/" + sBookName, null);
                                                    removeBookMap.put("Books/" + sBookName, null);
                                                    removeBookMap.put("Users/" + sCurrentUserName + "/numberOfBooks", iNumberOfBooks);

                                                    dbRef.updateChildren(removeBookMap).addOnCompleteListener(new OnCompleteListener() {
                                                        @Override
                                                        public void onComplete(@NonNull Task task) {
                                                            if (task.isSuccessful()) {
                                                                Toast.makeText(AboutMyBookActivity.this, "Book removed", Toast.LENGTH_SHORT).show();
                                                                finish();
                                                            } else
                                                                Toast.makeText(AboutMyBookActivity.this, "An error occured", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                }
                                            }
                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                                Toast.makeText(AboutMyBookActivity.this, "An error occured", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                            });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    builder.show();
                }
            }
        });

        editBookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent editBookIntent = new Intent(AboutMyBookActivity.this , EditBookActivity.class);
                editBookIntent.putExtra("Book Name", sBookName);
                editBookIntent.putExtra("Author Name", sAuthorName);
                editBookIntent.putExtra("Genre", sGenre);
                editBookIntent.putExtra("Language", sLanguage);
                editBookIntent.putExtra("User Name" , sCurrentUserName);
                editBookIntent.putExtra("Action" , "edit");
                editBookIntent.putExtra("User Thumb" , userThumb);
                editBookIntent.putExtra("Description" , sDescription);
                startActivity(editBookIntent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}

