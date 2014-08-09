package com.cwc.mobilecloud_star.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

public class TabsPagerAdapter extends FragmentPagerAdapter {

	public TabsPagerAdapter(FragmentManager fm) {
		super(fm);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Fragment getItem(int index) {
		// TODO Auto-generated method stub

		switch(index) {
		case 0:
			// Node Files fragment activity
			return new NodeFiles();


		case 1:
			// Network Files fragment activity
			return new NetworkFiles();


		case 2:
			// Streaming fragment activity
			return new Streaming();

		}
		return null;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return 3;
	}

	@Override
	public int getItemPosition(Object object) {
		return POSITION_NONE;
	}


}
