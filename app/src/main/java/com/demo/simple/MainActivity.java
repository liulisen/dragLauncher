package com.demo.simple;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.demo.simple.databinding.MainActivityBinding;

public class MainActivity extends AppCompatActivity {

    MainActivityBinding mBinding = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.main_activity);
        MyViewPagerAdapter adapter = new MyViewPagerAdapter(getSupportFragmentManager(), mBinding.dragLayerLayout);
        mBinding.dragLayerLayout.setDragCallback(adapter);
        mBinding.viewPager.setAdapter(adapter);
        mBinding.viewPager.setOffscreenPageLimit(adapter.getCount());
        mBinding.tabLayout.setupWithViewPager(mBinding.viewPager);
    }

}
