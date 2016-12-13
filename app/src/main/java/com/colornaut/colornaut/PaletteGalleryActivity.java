package com.colornaut.colornaut;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PaletteGalleryActivity extends AppCompatActivity {
    // Views for this class
    private static Context mContext;

    // views/variables for palette gallery
    PGListAdapter listAdapter;

    private final static String TAG = "COLORNAUT:gallery";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_palette_gallery);
        mContext = getApplicationContext();

        Log.i(TAG, "loading");
        Intent intent = getIntent();
        ArrayList<ColorPalette> colornautData = (ArrayList<ColorPalette>) intent.getSerializableExtra("colornautData");
        Log.i(TAG, "loaded");



        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.DKGRAY));
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.GRAY);
        }

        // Sets listview for palette gallery
        ListView listView = (ListView) findViewById(R.id.listView);
        listAdapter = new PGListAdapter(mContext, colornautData);
        listView.setAdapter(listAdapter);
    }

    // CUSTOM LIST ADAPTER
    private class PGListAdapter extends BaseAdapter {
        public List<ColorPalette> paletteList = new ArrayList<>();
        public LayoutInflater mInflater;
        public Context mContext;

        //Constructor
        PGListAdapter(Context context, ArrayList<ColorPalette> items) {
            this.mContext = context;
            this.paletteList = items;
            mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

        class ViewHolder {
            private ImageView originalImageImageView;
            private TextView paletteNameTextView;
            private TextView paletteLocationTextView;
            private TextView paletteRgbValuesTextView;
            private TextView paletteHexValuesTextView;
            private GridView paletteColorsGridView;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            Log.i(TAG, "Testing adapter");

            final ColorPalette colorPalette = paletteList.get(position);

            LinearLayout itemLayout = (LinearLayout) convertView;
            final ViewHolder holder;

            if (itemLayout == null) {
                itemLayout = (LinearLayout) LayoutInflater.from(mContext).inflate(
                        R.layout.palette_list_items, parent, false);
                holder = new ViewHolder();
                holder.originalImageImageView = (ImageView) itemLayout.findViewById(R.id.originalImage);
                holder.paletteNameTextView = (TextView) itemLayout.findViewById(R.id.paletteName);
                holder.paletteLocationTextView = (TextView) itemLayout.findViewById(R.id.paletteLocation);
                holder.paletteRgbValuesTextView = (TextView) itemLayout.findViewById(R.id.paletteRgbValues);
                holder.paletteHexValuesTextView = (TextView) itemLayout.findViewById(R.id.paletteHexValues);
                holder.paletteColorsGridView = (GridView) itemLayout.findViewById(R.id.paletteListItemGridView);
                itemLayout.setTag(holder);
            }
            else {
                itemLayout = (LinearLayout) convertView;
                holder = (ViewHolder) itemLayout.getTag();
            }

            // set up list item content
            itemLayout.setBackgroundColor(Color.parseColor("#F8F8F8"));
            // set up bitmap image
            Bitmap image = colorPalette.loadImageFromStorage();
            holder.originalImageImageView.setImageBitmap(image);
            // set up palette name, location, rgb values, hex values
            holder.paletteNameTextView.setText(colorPalette.getPaletteName());
            holder.paletteLocationTextView.setText(colorPalette.getLocation());
            holder.paletteLocationTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_location, 0, 0, 0);
            holder.paletteRgbValuesTextView.setText(colorPalette.makeRgbString());
            // listener for long click to copy values to clipboard
            holder.paletteRgbValuesTextView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("RGB Values", holder.paletteRgbValuesTextView.getText());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(mContext, "Copied RGB Values to clipboard.", Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
            holder.paletteHexValuesTextView.setText(colorPalette.makeHexString());
            // listener for long click to copy values to clipboard
            holder.paletteHexValuesTextView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("HEX Values", holder.paletteHexValuesTextView.getText());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(mContext, "Copied HEX Values to clipboard.", Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
            // set up gridview (same gridview used in the edit panel)
            int savedCount = colorPalette.getSavedNumber();
            ArrayList<Integer> rgbValues = new ArrayList<Integer>(colorPalette.getAllRgbValues().subList(0, savedCount));
            ColorPreviewsGridAdapter mAdapter = new ColorPreviewsGridAdapter(mContext, rgbValues);
            holder.paletteColorsGridView.setColumnWidth((int)(parent.getWidth() - 23.8) / mAdapter.getCount());
            holder.paletteColorsGridView.setAdapter(mAdapter);

            return itemLayout;
        }
    }
}
