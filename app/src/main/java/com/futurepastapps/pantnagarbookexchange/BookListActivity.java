package com.futurepastapps.pantnagarbookexchange;

import android.content.Intent;
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

public class BookListActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView booksView;
    private String bookUserName , cUserName;

    private FirebaseAuth mAuth;
    private DatabaseReference dbRef , bookRef , bookDetailsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_list);

        bookUserName = getIntent().getStringExtra("User Name");

        mToolbar = findViewById(R.id.bookListBar);
        mToolbar.setTitle(bookUserName + "'s Books");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.home);
        getSupportActionBar().setHomeButtonEnabled(true);

        booksView = findViewById(R.id.bookListView);

        mAuth = FirebaseAuth.getInstance();
        cUserName = mAuth.getCurrentUser().getDisplayName();

        dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.keepSynced(true);

        bookRef = dbRef.child("Users").child(bookUserName).child("books");
        bookRef.keepSynced(true);

        bookDetailsRef = dbRef.child("Books");
        bookDetailsRef.keepSynced(true);

        booksView.setHasFixedSize(true);
        booksView.setLayoutManager(new LinearLayoutManager(BookListActivity.this));

        final FirebaseRecyclerAdapter<Books , BooksViewHolder> booksRecyclerView = new FirebaseRecyclerAdapter<Books, BooksViewHolder>(
                Books.class , R.layout.mybooklayout , BooksViewHolder.class , bookRef) {

            @Override
            protected void populateViewHolder(final BooksViewHolder viewHolder, Books model, int position) {

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

                            dbRef.child("Users").child(bookUserName).child("dpThumbnail").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.exists()) {
                                        final String userThumb = dataSnapshot.getValue().toString();

                                        viewHolder.setAvailability(avail);
                                        viewHolder.setGenreAndLanguage(genre, language);
                                        viewHolder.setAuthorName(authorName);
                                        viewHolder.setBookName(bookName);

                                        viewHolder.mview.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                Intent aboutBookIntent = new Intent(BookListActivity.this, AboutBookActivity.class);
                                                aboutBookIntent.putExtra("Book Name", bookName);
                                                aboutBookIntent.putExtra("Book User Name", bookUserName);
                                                aboutBookIntent.putExtra("User Name", cUserName);
                                                aboutBookIntent.putExtra("Author Name", authorName);
                                                aboutBookIntent.putExtra("Genre", genre);
                                                aboutBookIntent.putExtra("Language", language);
                                                aboutBookIntent.putExtra("Description", description);
                                                aboutBookIntent.putExtra("Available", avail);
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
