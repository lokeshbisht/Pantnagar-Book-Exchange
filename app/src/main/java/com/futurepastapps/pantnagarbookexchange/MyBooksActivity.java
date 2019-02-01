package com.futurepastapps.pantnagarbookexchange;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MyBooksActivity extends AppCompatActivity {


    private Toolbar mToolbar;
    private FloatingActionButton addBookButton;
    private EmptyRecyclerView booksView;

    private String cUserName , userThumb;

    private FirebaseAuth mAuth;
    private DatabaseReference dbRef , bookRef , bookDetailsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_books);

        mAuth = FirebaseAuth.getInstance();
        cUserName = mAuth.getCurrentUser().getDisplayName();

        userThumb = getIntent().getStringExtra("User Thumb");

        mToolbar = findViewById(R.id.myBooksBar);
        mToolbar.setTitle("My Books");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.home);
        getSupportActionBar().setHomeButtonEnabled(true);

        booksView = findViewById(R.id.myBooksView);
        addBookButton = findViewById(R.id.myBooksAddBookButton);

        booksView.setLayoutManager(new LinearLayoutManager(this));

        View emptyView = findViewById(R.id.myBooksNoBooksView);
        booksView.setEmptyView(emptyView);

        dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.keepSynced(true);
        bookRef = dbRef.child("Users").child(cUserName).child("books");
        bookRef.keepSynced(true);

        bookDetailsRef = dbRef.child("Books");
        bookDetailsRef.keepSynced(true);

        booksView.setHasFixedSize(true);

        addBookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addBookIntent = new Intent(MyBooksActivity.this , EditBookActivity.class);
                addBookIntent.putExtra("Action" , "add");
                addBookIntent.putExtra("User Name" , cUserName);
                startActivity(addBookIntent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        final FirebaseRecyclerAdapter<MyBooks , BooksViewHolder> booksRecyclerView = new FirebaseRecyclerAdapter<MyBooks, BooksViewHolder>(
                MyBooks.class , R.layout.mybooklayout , BooksViewHolder.class , bookRef) {

            @Override
            protected void populateViewHolder(final BooksViewHolder viewHolder, final MyBooks model, int position) {

                final String bookName = getRef(position).getKey();

                bookDetailsRef.child(bookName).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()) {

                            final String genre = dataSnapshot.child("genre").getValue().toString();
                            final String authorName = dataSnapshot.child("authorName").getValue().toString();
                            final String language = dataSnapshot.child("language").getValue().toString();
                            final String description = dataSnapshot.child("description").getValue().toString();
                            final String available = dataSnapshot.child("available").getValue().toString();
                            final boolean avail = Boolean.parseBoolean(available);

                            viewHolder.setAvailability(avail);
                            viewHolder.setGenreAndLanguage(genre, language);
                            viewHolder.setAuthorName(authorName);
                            viewHolder.setBookName(bookName);

                            viewHolder.mview.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent aboutBookIntent = new Intent(MyBooksActivity.this, AboutMyBookActivity.class);
                                    aboutBookIntent.putExtra("Book Name", bookName);
                                    aboutBookIntent.putExtra("User Name", cUserName);
                                    aboutBookIntent.putExtra("Author Name", authorName);
                                    aboutBookIntent.putExtra("Genre", genre);
                                    aboutBookIntent.putExtra("Language", language);
                                    aboutBookIntent.putExtra("Available", avail);
                                    aboutBookIntent.putExtra("Description", description);
                                    aboutBookIntent.putExtra("User Thumb", userThumb);
                                    startActivity(aboutBookIntent);
                                }
                            });

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };

        booksRecyclerView.notifyDataSetChanged();
        booksView.setAdapter(booksRecyclerView);

    }

    public static class BooksViewHolder extends RecyclerView.ViewHolder {

        View mview;

        public BooksViewHolder(View itemView) {
            super(itemView);

            mview = itemView;
        }

        public void setBookName(String bookName) {

            TextView bookNameView = mview.findViewById(R.id.myBookBookName);
            bookNameView.setText(bookName);

        }
        public void setAuthorName(String authorName) {

            TextView authorNameView = mview.findViewById(R.id.myBookAuthorName);
            authorNameView.setText(authorName);
        }

        public void setGenreAndLanguage(String genre , String language) {

            TextView genreAndLanguageView = mview.findViewById(R.id.myBookGenreAndLanguage);
            genreAndLanguageView.setText(genre + " | " + language);
        }

        public void setAvailability(boolean available) {

            ImageView availableView = mview.findViewById(R.id.myBookAvailability);
            if(available) {
                availableView.setImageResource(R.drawable.bookavailable);
            } else{
                availableView.setImageResource(R.drawable.bookunavailable);
            }
        }
    }
}

