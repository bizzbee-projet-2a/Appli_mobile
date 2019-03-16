package com.desbois.mathis.bizzbee;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

// Since this is an object collection, use a FragmentStatePagerAdapter,
// and NOT a FragmentPagerAdapter.
public class RucherPagerAdapter extends FragmentStatePagerAdapter {

    private String title[] = {
            BizzbeeApp.getAppResources().getString(R.string.graphes),
            BizzbeeApp.getAppResources().getString(R.string.ruches)};

    public RucherPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        Fragment fragment = null;
        switch (i) {
            case 0:
                fragment = new RucherGraphesFragment();
                break;
            case 1:
                fragment = new RucherListeRuchesFragment();
                break;
        }

        return fragment;
    }

    @Override
    public int getCount() {
        return title.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return title[position];
    }
}
