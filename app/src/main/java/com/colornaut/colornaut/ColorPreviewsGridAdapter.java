package com.colornaut.colornaut;


import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * Created by vdesouza on 12/6/16.
 */

public class ColorPreviewsGridAdapter extends BaseAdapter {

    private Activity mContext;

    // color values
    public ArrayList<Integer> mColors;

    // Constructor
    public ColorPreviewsGridAdapter(MainActivity mainActivity, ArrayList<Integer> items) {
        this.mContext = mainActivity;
        this.mColors = items;
    }

    @Override
    public int getCount() {
        return mColors.size();
    }

    @Override
    public Object getItem(int position) {
        return mColors.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView = new ImageView(mContext);
        imageView.setBackgroundColor((Integer) getItem(position));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setLayoutParams(new GridView.LayoutParams(70, 70));
        return imageView;
    }

}
