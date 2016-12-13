package com.colornaut.colornaut;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PaletteGalleryActivity extends AppCompatActivity {
    // Views for this class
    private static Context mContext;

    // views/variables for palette gallery
    PGListAdapter listAdapter;

    private final static String TAG = "COLORNAUT:gallery";
    private ArrayList<ColorPalette> colornautData;

    private static final int MENU_RESET = Menu.FIRST;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_palette_gallery);
        mContext = getApplicationContext();

        Log.i(TAG, "loading");
        Intent intent = getIntent();
        colornautData = (ArrayList<ColorPalette>) intent.getSerializableExtra("colornautData");
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
        listAdapter = new PGListAdapter(mContext, listView, colornautData);
        listView.setAdapter(listAdapter);
    }

    // CUSTOM LIST ADAPTER
    private class PGListAdapter extends BaseAdapter {
        public List<ColorPalette> paletteList = new ArrayList<>();
        public LayoutInflater mInflater;
        public Context mContext;
        public ListView listView;

        //Constructor
        PGListAdapter(Context context, ListView listView, ArrayList<ColorPalette> items) {
            this.mContext = context;
            this.paletteList = items;
            this.listView = listView;
            mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void makeImage(Dialog checks, ColorPalette palette, Bitmap image) {
            Bitmap orig = palette.loadImageFromStorage();
            int width = orig.getWidth() * 2, height = (int) (orig.getHeight() * 1.33);
            image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas();
            canvas.setBitmap(image);
            canvas.drawColor(Color.WHITE);
            Paint paint = new Paint();
            paint.setTextSize(16f);
            paint.setColor(Color.BLACK);
            if ( ((CheckBox) checks.findViewById(R.id.img)).isChecked() ) {
                canvas.drawBitmap(orig, width / 6, height / 10, null);
            }
            if ( ((CheckBox) checks.findViewById(R.id.name)).isChecked() ) {
                canvas.drawText(palette.getPaletteName(), (int) (width / 2.3), height / 10, paint);
            }
//            if ( ((CheckBox) checks.findViewById(R.id.rgb)).isChecked() ) {
//                canvas.drawText(palette.makeRgbString(), 00, 00, paint);
//            }
//            if ( ((CheckBox) checks.findViewById(R.id.hex)).isChecked() ) {
//                canvas.drawText(palette.makeHexString(), 00000, 0000, paint);
//            }
//            if ( ((CheckBox) findViewById(R.id.time)).isChecked() ) {
//
//            }
//            if ( ((CheckBox) findViewById(R.id.loc)).isChecked() ) {
//                canvas.drawText(palette.getLocation(), 00000, 0000, paint);
//            }
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

        public void clear() {
            paletteList.clear();
            notifyDataSetChanged();
        }

        class ViewHolder {
            private ImageView originalImageImageView;
            private TextView paletteNameTextView;
            private TextView paletteLocationTextView;
            private TextView paletteRgbValuesTextView;
            private TextView paletteHexValuesTextView;
            private GridView paletteColorsGridView;
            private Button sharePaletteButton;
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
                holder.sharePaletteButton = (Button) itemLayout.findViewById(R.id.shareButton);
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

            // set up share button
            holder.sharePaletteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    makeShareDialog(colorPalette, colorPalette.loadImageFromStorage());

                }
            });

            return itemLayout;
        }

        private void makeShareDialog(final ColorPalette palette, Bitmap orig) {

            final Bitmap image = Bitmap.createBitmap(orig.getWidth()  * 2, (int) (orig.getHeight() * 1.33), Bitmap.Config.ARGB_8888);

            final Dialog dialog = new Dialog(PaletteGalleryActivity.this);
            dialog.setContentView(R.layout.select_share);
            dialog.setTitle("Share Palette");

            final Dialog shareTo = new Dialog(PaletteGalleryActivity.this);
            shareTo.setContentView(R.layout.share_to);

            Button confirm = (Button) dialog.findViewById(R.id.share_info);
            confirm.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    makeImage(dialog, palette, image);
                    dialog.dismiss();
                    ImageButton fb = (ImageButton) shareTo.findViewById(R.id.fb_btn);
                    fb.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            share("com.facebook.katana", image);
                            shareTo.dismiss();
                            Toast.makeText(mContext, "Image Shared! :)", Toast.LENGTH_SHORT).show();
                        }
                    });
                    ImageButton twit = (ImageButton) shareTo.findViewById(R.id.twitter_btn);
                    twit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            share("com.twitter.android", image);
                            shareTo.dismiss();
                            Toast.makeText(mContext, "Image Shared! :)", Toast.LENGTH_SHORT).show();
                        }
                    });
                    ImageButton ig = (ImageButton) shareTo.findViewById(R.id.instagram_btn);
                    ig.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            share("com.instagram.android", image);
                            shareTo.dismiss();
                            Toast.makeText(mContext, "Image Shared! :)", Toast.LENGTH_SHORT).show();
                        }
                    });
                    ImageButton pin = (ImageButton) shareTo.findViewById(R.id.pinterest_btn);
                    pin.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            share("com.pinterest", image);
                            shareTo.dismiss();
                            Toast.makeText(mContext, "Image Shared! :)", Toast.LENGTH_SHORT).show();
                        }
                    });
                    ImageButton sav = (ImageButton) shareTo.findViewById(R.id.save_btn);
                    sav.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            save(image);
                            shareTo.dismiss();
                            Toast.makeText(mContext, "Image Shared! :)", Toast.LENGTH_SHORT).show();
                        }
                    });
                    shareTo.show();
                }
            });
            dialog.show();
        }

        private void share(String application, Bitmap image) {
            Intent intent = mContext.getPackageManager().getLaunchIntentForPackage(application);
            if (intent != null) {
                String pathOfBmp = MediaStore.Images.Media.insertImage(mContext.getContentResolver(), image,"title", null);
                Uri bmpUri = Uri.parse(pathOfBmp);
                // The application exists
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.setPackage(application);
                shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
                shareIntent.setType("image/png");
                // Start the specific social application
                mContext.startActivity(shareIntent);
            }
        }

        public void save(Bitmap input) {
            String fName = UUID.randomUUID().toString() + ".png";
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), fName);
            try {
                boolean compressSucceeded = input.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(file));
                addImageToGallery(file.getAbsolutePath(), mContext);
                Toast.makeText(mContext, "Saved to " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        public void addImageToGallery(final String filePath, final Context context) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            values.put(MediaStore.MediaColumns.DATA, filePath);
            context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        }


    }
}
