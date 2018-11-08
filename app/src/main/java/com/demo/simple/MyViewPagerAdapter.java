package com.demo.simple;

import android.graphics.RectF;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import android.view.View;

/**
 * Created by Lisen.Liu on 2018/11/2.
 */

public class MyViewPagerAdapter extends FragmentStatePagerAdapter implements DragLayerLayout.IDragActionCallback<DataBean> {
    private DragLayerLayout mDragLayerLayout;

    public MyViewPagerAdapter(FragmentManager fm, DragLayerLayout dragLayerLayout) {
        super(fm);
        mDragLayerLayout = dragLayerLayout;
    }

    private SparseArray<Fragment> mFragments = new SparseArray<>();
    private View originView;

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = mFragments.get(position);
        if (fragment == null) {
            fragment = MyFragment.getInstance(mDragLayerLayout, position, 5 + position);
            mFragments.put(position, fragment);
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return 20;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "TAB-" + position;
    }

    // ----- BEGIN DragLayerLayout.IDragActionCallback<String> -----

    @Override
    public void onStartDrag(View dragView, int dragPage, int dragPosition, DataBean data) {
        dragView.setVisibility(View.GONE);
        this.originView = dragView;
    }

    @Override
    public void onSlide2Page(View dragView, int dragPage, int dragPosition, int currentPage, DataBean data) {

    }

    @Override
    public void onDrop(View dragView, int dragPage, int dragPosition, int dropPage, DataBean data, RectF rectF) {
        Fragment dragFragment = mFragments.get(dragPage);
        Fragment dropFragment = mFragments.get(dropPage);
        if (dragFragment != null && dragFragment instanceof DragLayerLayout.IDragDataCallback
                && dropFragment != null && dropFragment instanceof DragLayerLayout.IDragDataCallback) {
            if (dragPage != dropPage) {
                ((DragLayerLayout.IDragDataCallback) dragFragment).removeItem(data);
                ((DragLayerLayout.IDragDataCallback) dropFragment).addItem(data, rectF);
            } else {
                ((DragLayerLayout.IDragDataCallback) dragFragment).swipItem(dragPosition, rectF);
            }
        }
        if (dragPage == dropPage) {
            dragView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onMove(View dragView, int dragPage, int dragPosition, int dropPage, DataBean data, RectF rectF) {
        if (dragPage == dropPage) {
            Fragment dragFragment = mFragments.get(dragPage);
            Fragment dropFragment = mFragments.get(dropPage);
            if (dragFragment != null && dragFragment instanceof DragLayerLayout.IDragDataCallback
                    && dropFragment != null && dropFragment instanceof DragLayerLayout.IDragDataCallback) {
                ((DragLayerLayout.IDragDataCallback) dragFragment).onMove(originView, dragPosition, rectF);
            }
        }
    }


// ----- END DragLayerLayout.IDragActionCallback<String> -----
}
