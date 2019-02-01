package com.futurepastapps.pantnagarbookexchange;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class BooksFragment extends Fragment {


    private EmptyRecyclerView booksView;
    private View mainview;
    private FloatingActionButton addBook , searchBook , sortBook;
    private ImageView closeSearch;
    private TextView searchTextView;
    private RelativeLayout mainLayout;

    private String searchText , cUserName , userThumb;
    private int sortById;

    private DatabaseReference booksDb, userdb;

    private LinearLayoutManager linearLayoutManager;

    private boolean showAd;
    private int clickCount;

    private AdView bannerAdView;
    private InterstitialAd mInterstitialAd;

    public BooksFragment() {
        // Required empty public constructor
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mainview =  inflater.inflate(R.layout.fragment_books , container, false);

        mainLayout = mainview.findViewById(R.id.booksMainLayout);

        booksView = mainLayout.findViewById(R.id.booksView);
        booksView.setLayoutManager(new LinearLayoutManager(getContext()));

        View emptyView = mainLayout.findViewById(R.id.booksEmptyLayout);
        booksView.setEmptyView(emptyView);

        bannerAdView = mainLayout.findViewById(R.id.booksBanner);
        final AdRequest adRequest = new AdRequest.Builder().build();
        bannerAdView.loadAd(adRequest);
        bannerAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                bannerAdView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                bannerAdView.loadAd(adRequest);
            }

            @Override
            public void onAdOpened() {

            }

            @Override
            public void onAdLeftApplication() {
            }

            @Override
            public void onAdClosed() {
                bannerAdView.loadAd(adRequest);
            }
        });

        cUserName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();

        searchText = "";
        sortById = R.id.sortByBook;

        booksDb = FirebaseDatabase.getInstance().getReference().child("Books");
        booksDb.keepSynced(true);
        userdb = FirebaseDatabase.getInstance().getReference().child("Users");
        userdb.keepSynced(true);

        showAd = true;
        clickCount = 0;

        mInterstitialAd = new InterstitialAd(getContext());
        mInterstitialAd.setAdUnitId(getResources().getString(R.string.book_interstitial));

        addBook = emptyView.findViewById(R.id.emptyRecyclerButton);
        searchBook = mainview.findViewById(R.id.booksLayoutSearch);
        sortBook = mainview.findViewById(R.id.booksLayoutSort);
        closeSearch = mainview.findViewById(R.id.booksLayoutClose);
        searchTextView = mainview.findViewById(R.id.booksLayoutSearchText);

        addBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addBookIntent = new Intent(getContext() , EditBookActivity.class);
                addBookIntent.putExtra("Action" , "add");
                addBookIntent.putExtra("User Name" , cUserName);
                startActivity(addBookIntent);
            }
        });

        searchBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sortBook.setVisibility(View.GONE);
                searchBook.setVisibility(View.GONE);
                closeSearch.setVisibility(View.VISIBLE);
                searchTextView.setVisibility(View.VISIBLE);

                searchTextView.requestFocus();

                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(searchTextView , InputMethodManager.SHOW_IMPLICIT);

                searchTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                            searchText = searchTextView.getText().toString();
                            sortBook.setVisibility(View.GONE);
                            searchBook.setVisibility(View.GONE);
                            setLayout();
                            return true;
                        }
                        return false;
                    }
                });
            }
        });

        closeSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sortBook.setVisibility(View.VISIBLE);
                searchBook.setVisibility(View.VISIBLE);
                closeSearch.setVisibility(View.GONE);
                searchTextView.setVisibility(View.GONE);
                searchText = "";
                searchTextView.setText("");

                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchTextView.getWindowToken(), 0);
            }
        });

        sortBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder sortByBuilder = new AlertDialog.Builder(getContext());
                sortByBuilder.setTitle("Sort By :");

                View mView = getLayoutInflater().inflate(R.layout.sortbylayout , null);
                final RadioGroup sortByGroup = mView.findViewById(R.id.sortByGroup);

                sortByGroup.check(sortById);
                sortByBuilder.setView(mView);

                final AlertDialog sortByDialog = sortByBuilder.show();

                sortByGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup radioGroup, int i) {
                        sortById = sortByGroup.getCheckedRadioButtonId();
                        sortByDialog.dismiss();
                        setLayout();
                    }
                });
            }
        });

        linearLayoutManager = new LinearLayoutManager(getContext());

        booksView.setHasFixedSize(true);

        return mainview;
    }

    @Override
    public void onStart() {
        super.onStart();

        setLayout();
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
    }

    private void setLayout() {
        Query query = booksDb.orderByKey();

        linearLayoutManager.setReverseLayout(false);
        linearLayoutManager.setStackFromEnd(false);

        booksView.setLayoutManager(linearLayoutManager);

        if (sortById == R.id.sortByBook)
            query = booksDb.orderByKey().startAt(searchText).endAt(searchText + "\uf8ff");
        if (sortById == R.id.sortByAuthor)
            query = booksDb.orderByChild("authorName").startAt(searchText).endAt(searchText + "\uf8ff");
        if (sortById == R.id.sortByGenre)
            query = booksDb.orderByChild("genre").startAt(searchText).endAt(searchText + "\uf8ff");
        if (sortById == R.id.sortByLanguage)
            query = booksDb.orderByChild("language").startAt(searchText).endAt(searchText + "\uf8ff");
        if (sortById == R.id.sortByAvailability) {
            query = booksDb.orderByChild("available");
            linearLayoutManager.setReverseLayout(true);
            linearLayoutManager.setStackFromEnd(true);

            booksView.setLayoutManager(linearLayoutManager);
            searchBook.setVisibility(View.GONE);
        }
        if (sortById == R.id.sortByOwner)
            query = booksDb.orderByChild("userName").startAt(searchText).endAt(searchText + "\uf8ff");

        final FirebaseRecyclerAdapter<Books, BooksViewHolder> booksRecyclerView = new FirebaseRecyclerAdapter<Books, BooksViewHolder>(
                Books.class, R.layout.booklayout, BooksViewHolder.class, query) {

            @Override
            protected void populateViewHolder(final BooksViewHolder viewHolder, final Books model, int position) {

                final String bookName = getRef(position).getKey();

                userdb.child(model.getUserName()).child("dpThumbnail").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            userThumb = dataSnapshot.getValue().toString();
                        }

                        viewHolder.setAvailability(model.isAvailable());
                        viewHolder.setUserThumb(userThumb);
                        viewHolder.setValues(bookName, model.getAuthorName(), model.getGenre(), model.getLanguage(), sortById);

                        viewHolder.mview.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                if ((model.getUserName().equals(cUserName))) {
                                    Intent aboutBookIntent = new Intent(getContext(), AboutMyBookActivity.class);
                                    aboutBookIntent.putExtra("Book Name", bookName);
                                    aboutBookIntent.putExtra("Author Name", model.getAuthorName());
                                    aboutBookIntent.putExtra("Genre", model.getGenre());
                                    aboutBookIntent.putExtra("Language", model.getLanguage());
                                    aboutBookIntent.putExtra("User Name", cUserName);
                                    aboutBookIntent.putExtra("Available", model.isAvailable());
                                    aboutBookIntent.putExtra("Description", model.getDescription());
                                    aboutBookIntent.putExtra("User Thumb", model.getUserThumb());
                                    startActivity(aboutBookIntent);
                                } else {
                                    if (model.getUserName().equals("Student Library")) {
                                        if (showAd) {
                                            showAd = false;
                                            mInterstitialAd.show();
                                            mInterstitialAd.setAdListener(new AdListener() {
                                                @Override
                                                public void onAdLoaded() {

                                                }

                                                @Override
                                                public void onAdFailedToLoad(int errorCode) {
                                                    mInterstitialAd.loadAd(new AdRequest.Builder().build());
                                                }

                                                @Override
                                                public void onAdOpened() {

                                                }

                                                @Override
                                                public void onAdLeftApplication() {

                                                }

                                                @Override
                                                public void onAdClosed() {
                                                    mInterstitialAd.loadAd(new AdRequest.Builder().build());
                                                    Intent aboutBookIntent = new Intent(getContext(), AboutBookActivity.class);
                                                    aboutBookIntent.putExtra("Book Name", bookName);
                                                    aboutBookIntent.putExtra("Author Name", model.getAuthorName());
                                                    aboutBookIntent.putExtra("Genre", model.getGenre());
                                                    aboutBookIntent.putExtra("Language", model.getLanguage());
                                                    aboutBookIntent.putExtra("Book User Name", model.getUserName());
                                                    aboutBookIntent.putExtra("User Name", cUserName);
                                                    aboutBookIntent.putExtra("Available", model.isAvailable());
                                                    aboutBookIntent.putExtra("From", "other");
                                                    aboutBookIntent.putExtra("Description", model.getDescription());
                                                    aboutBookIntent.putExtra("User Thumb", model.getUserThumb());
                                                    startActivity(aboutBookIntent);
                                                }
                                            });
                                        } else if (!showAd) {
                                            showAd = true;
                                            Intent aboutBookIntent = new Intent(getContext(), AboutBookActivity.class);
                                            aboutBookIntent.putExtra("Book Name", bookName);
                                            aboutBookIntent.putExtra("Author Name", model.getAuthorName());
                                            aboutBookIntent.putExtra("Genre", model.getGenre());
                                            aboutBookIntent.putExtra("Language", model.getLanguage());
                                            aboutBookIntent.putExtra("Book User Name", model.getUserName());
                                            aboutBookIntent.putExtra("User Name", cUserName);
                                            aboutBookIntent.putExtra("Available", model.isAvailable());
                                            aboutBookIntent.putExtra("From", "other");
                                            aboutBookIntent.putExtra("Description", model.getDescription());
                                            aboutBookIntent.putExtra("User Thumb", model.getUserThumb());
                                            startActivity(aboutBookIntent);
                                        }
                                    } else {
                                        if (clickCount == 0) {
                                            clickCount++;
                                            mInterstitialAd.show();
                                            mInterstitialAd.setAdListener(new AdListener() {
                                                @Override
                                                public void onAdLoaded() {

                                                }

                                                @Override
                                                public void onAdFailedToLoad(int errorCode) {
                                                    mInterstitialAd.loadAd(new AdRequest.Builder().build());
                                                }

                                                @Override
                                                public void onAdOpened() {

                                                }

                                                @Override
                                                public void onAdLeftApplication() {

                                                }

                                                @Override
                                                public void onAdClosed() {
                                                    mInterstitialAd.loadAd(new AdRequest.Builder().build());
                                                    Intent aboutBookIntent = new Intent(getContext(), AboutBookActivity.class);
                                                    aboutBookIntent.putExtra("Book Name", bookName);
                                                    aboutBookIntent.putExtra("Author Name", model.getAuthorName());
                                                    aboutBookIntent.putExtra("Genre", model.getGenre());
                                                    aboutBookIntent.putExtra("Language", model.getLanguage());
                                                    aboutBookIntent.putExtra("Book User Name", model.getUserName());
                                                    aboutBookIntent.putExtra("User Name", cUserName);
                                                    aboutBookIntent.putExtra("Available", model.isAvailable());
                                                    aboutBookIntent.putExtra("From", "other");
                                                    aboutBookIntent.putExtra("Description", model.getDescription());
                                                    aboutBookIntent.putExtra("User Thumb", model.getUserThumb());
                                                    startActivity(aboutBookIntent);
                                                }
                                            });
                                        } else {
                                            clickCount++;
                                            if(clickCount == 3)
                                                clickCount = 0;
                                            Intent aboutBookIntent = new Intent(getContext(), AboutBookActivity.class);
                                            aboutBookIntent.putExtra("Book Name", bookName);
                                            aboutBookIntent.putExtra("Author Name", model.getAuthorName());
                                            aboutBookIntent.putExtra("Genre", model.getGenre());
                                            aboutBookIntent.putExtra("Language", model.getLanguage());
                                            aboutBookIntent.putExtra("Book User Name", model.getUserName());
                                            aboutBookIntent.putExtra("User Name", cUserName);
                                            aboutBookIntent.putExtra("Available", model.isAvailable());
                                            aboutBookIntent.putExtra("From", "other");
                                            aboutBookIntent.putExtra("Description", model.getDescription());
                                            aboutBookIntent.putExtra("User Thumb", model.getUserThumb());
                                            startActivity(aboutBookIntent);
                                        }
                                    }
                                }
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
            @Override
            public void onDataChanged() {
                super.onDataChanged();

                if(getItemCount() == 0 && !(searchText.isEmpty())) {
                    Toast.makeText(getContext() , "No results were found" , Toast.LENGTH_SHORT).show();
                    searchText = "";
                    searchBook.setVisibility(View.GONE);
                    sortBook.setVisibility(View.GONE);
                    setLayout();
                } else if(getItemCount() != 0 && searchTextView.getText().toString().isEmpty()) {
                    closeSearch.setVisibility(View.GONE);
                    searchTextView.setVisibility(View.GONE);
                    searchBook.setVisibility(View.VISIBLE);
                    sortBook.setVisibility(View.VISIBLE);
                } else if(getItemCount() == 0) {
                    searchBook.setVisibility(View.GONE);
                    sortBook.setVisibility(View.GONE);
                }

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

        public void setValues(String bookName , String authorName , String genre , String language , int sortBy) {

            TextView bookNameView = mview.findViewById(R.id.bookLayoutBookName);
            TextView authorNameView = mview.findViewById(R.id.bookLayoutAuthorName);
            TextView genreAndLanguageView = mview.findViewById(R.id.bookLayoutGenreAndLanguage);

            if(sortBy == R.id.sortByBook | sortBy == R.id.sortByOwner) {
                bookNameView.setText(bookName);
                authorNameView.setText(authorName);
                genreAndLanguageView.setText(genre + " | " + language);
            } else if(sortBy == R.id.sortByAuthor) {
                bookNameView.setText(authorName);
                authorNameView.setText(bookName);
                genreAndLanguageView.setText(genre + " | " + language);
            } else if(sortBy == R.id.sortByGenre) {
                bookNameView.setText(genre + " | " + language);
                authorNameView.setText(bookName);
                genreAndLanguageView.setText(authorName);
            } else if(sortBy == R.id.sortByLanguage) {
                bookNameView.setText(genre + " | " + language);
                authorNameView.setText(bookName);
                genreAndLanguageView.setText(authorName);
            } else  if(sortBy == R.id.sortByAvailability) {
                bookNameView.setText(bookName);
                authorNameView.setText(authorName);
                genreAndLanguageView.setText(genre + " | " + language);
            }

        }

        public void setUserThumb(String thumb_dp) {

            CircleImageView userDp = mview.findViewById(R.id.bookLayoutUserDp);
            if(thumb_dp.equals("default")) {
                userDp.setImageResource(R.drawable.defaultuser);
            } else {
                Picasso.get().load(thumb_dp).placeholder(R.drawable.defaultuser).into(userDp);
            }
        }

        public void setAvailability(boolean available) {

            CircleImageView availabilityView = mview.findViewById(R.id.bookLayoutAvailabilityImage);
            if(available) {
                availabilityView.setImageResource(R.drawable.bookavailable);
            } else
                availabilityView.setImageResource(R.drawable.bookunavailable);
        }
    }
}


