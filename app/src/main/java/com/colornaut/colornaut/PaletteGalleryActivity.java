package com.colornaut.colornaut;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class PaletteGalleryActivity extends AppCompatActivity {
    // Views for this class
    private static Context mContext;

    // views/variables for palette gallery
    PGListAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_palette_gallery);
        mContext = getApplicationContext();

        ArrayList<ColorPalette> list = (ArrayList<ColorPalette>) getIntent().getSerializableExtra("palette");

        // Sets listview for palette gallery
        ListView listView = (ListView) findViewById(R.id.listView);
        listAdapter = new PGListAdapter(mContext, null);
        listView.setAdapter(listAdapter);

        for (ColorPalette p : list) {
            listAdapter.add(p);
        }
    }

    // CUSTOM LIST ADAPTER
    public class PGListAdapter extends BaseAdapter {
        public List<ColorPalette> paletteList = new ArrayList<>();
        public LayoutInflater mInflater;
        public Context mContext;

        //Constructor
        PGListAdapter(Context context, ArrayList<ColorPalette> items) {
            this.mContext = context;
            this.paletteList = items;
            mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void add(ColorPalette palette) {
            paletteList.add(palette);
            notifyDataSetChanged();
        }


        @Override
        public int getCount() {
            return paletteList.size();
        }

        @Override
        public Object getItem(int i) {
            return paletteList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0; //Possibly unnecessary
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ColorPalette colorPalette = paletteList.get(i);
            ArrayList<Integer> swatches = colorPalette.getSwatchesRgb();

            RelativeLayout relativeLayout = (RelativeLayout) LayoutInflater.from(mContext).inflate(R.layout.palette_list_items, viewGroup, false);

            TextView nameTextView = (TextView) relativeLayout.findViewById(R.id.nameTextView);
            TextView locationTextView = (TextView) relativeLayout.findViewById(R.id.locationTextView);
            ImageView originalImageView = (ImageView) relativeLayout.findViewById(R.id.originalImage);
            ImageView paletteView1 = (ImageView) relativeLayout.findViewById(R.id.paletteView1);
            ImageView paletteView2 = (ImageView) relativeLayout.findViewById(R.id.paletteView2);
            ImageView paletteView3 = (ImageView) relativeLayout.findViewById(R.id.paletteView3);
            ImageView paletteView4 = (ImageView) relativeLayout.findViewById(R.id.paletteView4);
            ImageView paletteView5 = (ImageView) relativeLayout.findViewById(R.id.paletteView5);
            ImageView paletteView6 = (ImageView) relativeLayout.findViewById(R.id.paletteView6);
            ImageView paletteView7 = (ImageView) relativeLayout.findViewById(R.id.paletteView7);
            TextView paletteText1 = (TextView) relativeLayout.findViewById(R.id.paletteText1);
            TextView paletteText2 = (TextView) relativeLayout.findViewById(R.id.paletteText2);
            TextView paletteText3 = (TextView) relativeLayout.findViewById(R.id.paletteText3);
            TextView paletteText4 = (TextView) relativeLayout.findViewById(R.id.paletteText4);
            TextView paletteText5 = (TextView) relativeLayout.findViewById(R.id.paletteText5);
            TextView paletteText6 = (TextView) relativeLayout.findViewById(R.id.paletteText6);
            TextView paletteText7 = (TextView) relativeLayout.findViewById(R.id.paletteText7);

            nameTextView.setText(colorPalette.getPaletteName());
            locationTextView.setText(colorPalette.getLocation());
            originalImageView.setImageBitmap(colorPalette.getImage());
            paletteView1.setBackgroundColor(swatches.get(1));
            paletteView2.setBackgroundColor(swatches.get(2));
            paletteView3.setBackgroundColor(swatches.get(3));
            paletteView4.setBackgroundColor(swatches.get(4));
            paletteView5.setBackgroundColor(swatches.get(5));
            paletteView6.setBackgroundColor(swatches.get(6));
            paletteView7.setBackgroundColor(swatches.get(7));
            paletteText1.setText(swatches.get(1).toString());
            paletteText2.setText(swatches.get(2).toString());
            paletteText3.setText(swatches.get(3).toString());
            paletteText4.setText(swatches.get(4).toString());
            paletteText5.setText(swatches.get(5).toString());
            paletteText6.setText(swatches.get(6).toString());
            paletteText7.setText(swatches.get(7).toString());

            return relativeLayout;
        }
    }
}
