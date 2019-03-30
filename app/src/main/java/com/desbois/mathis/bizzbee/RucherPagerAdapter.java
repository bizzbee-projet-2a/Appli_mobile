package com.desbois.mathis.bizzbee;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

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
                fragment = new RucherGrapheRuchesFragment();
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
