package com.futurepastapps.pantnagarbookexchange;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by HP on 22-06-2018.
 */

public class SectionsPager extends FragmentStatePagerAdapter {

    public SectionsPager(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0 :
                ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;
            case 1 :
                BooksFragment booksFragment = new BooksFragment();
                return booksFragment;
            case 2 :
                BookRequsetsFragment bookRequsetsFragment = new BookRequsetsFragment();
                return bookRequsetsFragment;
            default :
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }


    @Override
    public CharSequence getPageTitle(int position) {
        return null;
    }


}
