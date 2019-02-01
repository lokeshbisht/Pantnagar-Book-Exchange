package com.futurepastapps.pantnagarbookexchange;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.Scroller;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.reginald.editspinner.EditSpinner;

import java.util.HashMap;
import java.util.Map;

public class EditBookActivity extends AppCompatActivity {


    private Toolbar mToolbar;
    private TextInputLayout bookName , authorName , bookDescription;
    private EditSpinner genre , language;
    private Button saveButton;
    private ProgressBar saveProgress;

    private String action , userName;
    private String sNewBookName , sOldBookName , sAuthorName , sGenre , sLanguage , sDescription , numberOfBooks;
    private int iNumberOfBooks;
    private boolean connected;

    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_book);

        action = getIntent().getStringExtra("Action");
        userName = getIntent().getStringExtra("User Name");

        dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.keepSynced(true);

        connected = false;

        mToolbar = findViewById(R.id.editBookBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.home);
        getSupportActionBar().setHomeButtonEnabled(true);

        bookName = findViewById(R.id.editBookBookName);
        authorName = findViewById(R.id.editBookAuthorName);
        bookDescription = findViewById(R.id.editBookDescription);
        bookDescription.getEditText().setScroller(new Scroller(getApplicationContext()));
        bookDescription.getEditText().setVerticalScrollBarEnabled(true);

        saveProgress = findViewById(R.id.editBookProgress);
        saveProgress.setIndeterminate(true);

        genre = findViewById(R.id.editBookGenre);
        final ArrayAdapter<String> genreAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,
                getResources().getStringArray(R.array.genre_array));
        genre.setAdapter(genreAdapter);
        genre.setEditable(false);
        genre.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                sGenre = genreAdapter.getItem(position);
                genre.setText(sGenre);
            }
        });
        genre.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
            }
        });
        genre.setShowSoftInputOnFocus(true);

        language = findViewById(R.id.editBookLanguage);
        final ArrayAdapter<String> languageAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,
                getResources().getStringArray(R.array.book_language_array));
        language.setAdapter(languageAdapter);
        language.setEditable(false);
        language.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                sLanguage = languageAdapter.getItem(position);
                language.setText(sLanguage);
            }
        });
        language.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
            }
        });
        language.setShowSoftInputOnFocus(true);

        saveButton = findViewById(R.id.editBookSaveButton);

        if(action.equals("edit")) {
            sOldBookName = getIntent().getStringExtra("Book Name");
            sAuthorName = getIntent().getStringExtra("Author Name");
            sGenre = getIntent().getStringExtra("Genre");
            sLanguage = getIntent().getStringExtra("Language");
            sDescription = getIntent().getStringExtra("Description");


            bookName.getEditText().setText(sOldBookName);
            bookName.getEditText().setSelection(bookName.getEditText().getText().length());

            authorName.getEditText().setText(sAuthorName);
            authorName.getEditText().setSelection(authorName.getEditText().getText().length());

            if(sDescription.equals("none"))
                bookDescription.getEditText().setText("");
            else {
                bookDescription.getEditText().setText(sDescription);
                bookDescription.getEditText().setSelection(bookDescription.getEditText().getText().length());
            }

            language.setText(sLanguage);
            genre.setText(sGenre);

            mToolbar.setTitle("Edit Book Details");
            saveButton.setText("Save Changes");
        }

        else if(action.equals("add")) {
            mToolbar.setTitle("Add Book");
            saveButton.setText("Add Book");
        }

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                connected = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;

                if (connected == false) {
                    Toast.makeText(EditBookActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
                } else {

                    bookName.setErrorEnabled(false);
                    authorName.setErrorEnabled(false);

                    if (TextUtils.isEmpty(bookName.getEditText().getText())) {
                        bookName.setErrorEnabled(true);
                        bookName.setError("Enter a Book Name");
                        bookName.requestFocus();
                    } else if (TextUtils.isEmpty(authorName.getEditText().getText())) {
                        authorName.setErrorEnabled(true);
                        authorName.setError("Enter the Author Name");
                        authorName.requestFocus();
                    } else if (TextUtils.isEmpty(sGenre)) {
                        genre.setError("Select a genre");
                        genre.requestFocus();
                    } else if (TextUtils.isEmpty(sLanguage)) {
                        language.setError("Select a language");
                        language.requestFocus();
                    }else if (action.equals("add")) {

                        saveProgress.setVisibility(View.VISIBLE);

                        sNewBookName = bookName.getEditText().getText().toString();
                        sAuthorName = authorName.getEditText().getText().toString();
                        sDescription = bookDescription.getEditText().getText().toString();
                        if(sDescription.isEmpty())
                            sDescription = "none";

                        dbRef.child("Users").child(userName).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                numberOfBooks = dataSnapshot.child("numberOfBooks").getValue().toString();
                                iNumberOfBooks = Integer.parseInt(numberOfBooks);
                                iNumberOfBooks += 1;
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        dbRef.child("Books").child(sNewBookName).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    saveProgress.setVisibility(View.GONE);
                                    Toast.makeText(EditBookActivity.this, "A book with the same name already exist.Try adding an index to the book name", Toast.LENGTH_SHORT).show();
                                } else {

                                    Map addBookMap = new HashMap();
                                    Map putMap = new HashMap();
                                    addBookMap.put("authorName", sAuthorName);
                                    addBookMap.put("genre", sGenre);
                                    addBookMap.put("language", sLanguage);
                                    addBookMap.put("userName", userName);
                                    addBookMap.put("available", true);
                                    addBookMap.put("description", sDescription);

                                    putMap.put("Books/" + sNewBookName, addBookMap);
                                    putMap.put("Users/" + userName + "/" + "numberOfBooks", iNumberOfBooks);
                                    putMap.put("Users/" + userName + "/" + "books/" + sNewBookName + "/" + "timeStamp", ServerValue.TIMESTAMP);

                                    dbRef.updateChildren(putMap, new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                            if (databaseError == null) {

                                                saveProgress.setVisibility(View.GONE);
                                                Toast.makeText(EditBookActivity.this, "Book added to My books", Toast.LENGTH_SHORT).show();

                                                Intent myBooksIntent = new Intent(EditBookActivity.this, MyBooksActivity.class);
                                                myBooksIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(myBooksIntent);
                                            } else {
                                                saveProgress.setVisibility(View.GONE);
                                                Toast.makeText(EditBookActivity.this, "Error adding book", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    } else if (action.equals("edit")) {
                        if (sOldBookName.equals(bookName.getEditText().getText().toString())
                                && sAuthorName.equals(authorName.getEditText().getText().toString())
                                && sDescription.equals(bookDescription.getEditText().getText().toString())
                                && sLanguage.equals(language.getText().toString())
                                && sGenre.equals(genre.getText().toString())) {
                            Toast.makeText(EditBookActivity.this, "No Changes Were Made", Toast.LENGTH_SHORT).show();
                        } else {

                            saveProgress.setVisibility(View.VISIBLE);

                            sNewBookName = bookName.getEditText().getText().toString();
                            sAuthorName = authorName.getEditText().getText().toString();
                            sDescription = bookDescription.getEditText().getText().toString();
                            if(sDescription.isEmpty())
                                sDescription = "none";

                            dbRef.child("Books").child(sNewBookName).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()&&(!sOldBookName.equals(sNewBookName))) {
                                        saveProgress.setVisibility(View.GONE);
                                        Toast.makeText(EditBookActivity.this, "A book with the same name already exist.Try adding an index to the book name", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Map addBookMap = new HashMap();
                                        final Map putMap = new HashMap();
                                        Map deleteMap = new HashMap();

                                        addBookMap.put("authorName", sAuthorName);
                                        addBookMap.put("genre", sGenre);
                                        addBookMap.put("language", sLanguage);
                                        addBookMap.put("userName", userName);
                                        addBookMap.put("available", true);
                                        addBookMap.put("description", sDescription);

                                        putMap.put("Books/" + sNewBookName, addBookMap);
                                        putMap.put("Users/" + userName + "/" + "books/" + sNewBookName + "/" + "timeStamp", ServerValue.TIMESTAMP);

                                        deleteMap.put("Books/" + sOldBookName , null);
                                        deleteMap.put("Users/" + userName + "/books/" + sOldBookName , null);

                                        dbRef.updateChildren(deleteMap, new DatabaseReference.CompletionListener() {
                                            @Override
                                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                if(databaseError == null) {
                                                    dbRef.updateChildren(putMap, new DatabaseReference.CompletionListener() {
                                                        @Override
                                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                            if (databaseError == null) {

                                                                saveProgress.setVisibility(View.GONE);
                                                                Toast.makeText(EditBookActivity.this, "Book details edited successfully", Toast.LENGTH_SHORT).show();

                                                                Intent myBooksIntent = new Intent(EditBookActivity.this, MyBooksActivity.class);
                                                                myBooksIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                startActivity(myBooksIntent);
                                                            } else {
                                                                saveProgress.setVisibility(View.GONE);
                                                                Toast.makeText(EditBookActivity.this, "An error occured", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                                } else {
                                                    saveProgress.setVisibility(View.GONE);
                                                    Toast.makeText(EditBookActivity.this, "An error occured", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    saveProgress.setVisibility(View.GONE);
                                    Toast.makeText(EditBookActivity.this, "An error occured", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
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
