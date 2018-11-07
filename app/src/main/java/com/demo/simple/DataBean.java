package com.demo.simple;

import java.util.Random;

/**
 * Created by Lisen.Liu on 2018/11/2.
 */

public class DataBean {
    private static final int[] IMAGE_RES = new int[] {
            R.drawable.cat,
            R.drawable.panda,
            R.drawable.maps,
            R.drawable.water,
    };
    public int resId;
    public String name;


    public DataBean(String name) {
        Random random = new Random();
        resId = IMAGE_RES[random.nextInt(IMAGE_RES.length)];
        this.name = name;
    }
}
