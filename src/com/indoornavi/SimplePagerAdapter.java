package com.indoornavi;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

class SimplePagerAdapter extends FragmentPagerAdapter {

    private final List<Fragment> fragments = new ArrayList<Fragment>();
    private final List<String> fragmentsNames = new ArrayList<String>();

    public SimplePagerAdapter(FragmentManager fm) {
        super(fm);
        fragments.add(new MapPage());
        fragmentsNames.add("Map");

        fragments.add(new RSSIChartPage());
        fragmentsNames.add("RSSI Chart");
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return fragmentsNames.get(position);
    }
}