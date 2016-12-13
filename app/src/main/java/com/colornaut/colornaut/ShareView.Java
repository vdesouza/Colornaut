package com.colornaut.colornaut;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.UUID;

/**
 * Created by Moira on 12/6/2016.
 */

public class ShareView extends View {

    private ColorPalette palette;
    private Dialog checks, shareTo;
    private Context context;
    private Bitmap image, orig;
    private Canvas canvas;


    public ShareView(Context context, ColorPalette palette) {
        super(context);
        this.context = context;
        this.palette = palette;
        orig = palette.loadImageFromStorage();

        image = Bitmap.createBitmap(orig.getWidth()  * 2, (int) (orig.getHeight() * 1.33), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(image);

        checks = new Dialog(context);
        checks.setContentView(R.layout.select_share);
        checks.show();

        shareTo = new Dialog(context);
        shareTo.setContentView(R.layout.share_to);


        Button confirm = (Button) checks.findViewById(R.id.share_info);
        confirm.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                checks.dismiss();
                ImageButton fb = (ImageButton) shareTo.findViewById(R.id.fb_btn);
                fb.setOnClickListener(listener);
                ImageButton twit = (ImageButton)shareTo.findViewById(R.id.twitter_btn);
                twit.setOnClickListener(listener);
                ImageButton ig = (ImageButton)shareTo.findViewById(R.id.instagram_btn);
                ig.setOnClickListener(listener);
                ImageButton pin = (ImageButton)shareTo.findViewById(R.id.pinterest_btn);
                pin.setOnClickListener(listener);
                ImageButton sav = (ImageButton)shareTo.findViewById(R.id.save_btn);
                sav.setOnClickListener(listener);
                shareTo.show();
            }
        });
    }

    protected OnClickListener listener = new OnClickListener() {

        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.fb_btn:
                    share("com.facebook.katana");
                    break;
                case R.id.twitter_btn:
                    share("com.twitter.android");
                    break;
                case R.id.instagram_btn:
                    share("com.instagram.android");
                    break;
                case R.id.pinterest_btn:
                    share("com.pinterest");
                    break;
                case R.id.save_btn:
                    save(image);
                    break;
            }
            shareTo.dismiss();
            Toast.makeText(context, "Image Shared! :)", Toast.LENGTH_SHORT).show();
        }
    };

    public void save(Bitmap input) {
        String fName = UUID.randomUUID().toString() + ".png";
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), fName);
        try {
            boolean compressSucceeded = input.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(file));
            addImageToGallery(file.getAbsolutePath(), context);
            Toast.makeText(context, "Saved to " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void addImageToGallery(final String filePath, final Context context) {

        ContentValues values = new ContentValues();

        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.MediaColumns.DATA, filePath);

        context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    public void share(String application) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(application);
        if (intent != null) {
            String pathOfBmp = MediaStore.Images.Media.insertImage(context.getContentResolver(), image,"title", null);
            Uri bmpUri = Uri.parse(pathOfBmp);
            // The application exists
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.setPackage(application);
            shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
            shareIntent.setType("image/png");
            // Start the specific social application
            context.startActivity(shareIntent);
        }
    }

    public void makeImage() {
        Paint paint = new Paint();
        paint.setTextSize(16f);
        paint.setColor(Color.BLACK);
        if ( ((CheckBox) checks.findViewById(R.id.img)).isChecked() ) {
            canvas.drawBitmap(orig,00000,000000, null);
        }
        if ( ((CheckBox) checks.findViewById(R.id.name)).isChecked() ) {
            canvas.drawText(palette.getPaletteName(), 000, 0000000, paint);
        }
        if ( ((CheckBox) checks.findViewById(R.id.rgb)).isChecked() ) {
            canvas.drawText(palette.makeRgbString(), 00, 00, paint);
        }
        if ( ((CheckBox) checks.findViewById(R.id.hex)).isChecked() ) {
            canvas.drawText(palette.makeHexString(), 00000, 0000, paint);
        }
        if ( ((CheckBox) findViewById(R.id.time)).isChecked() ) {

        }
        if ( ((CheckBox) findViewById(R.id.loc)).isChecked() ) {
            canvas.drawText(palette.getLocation(), 00000, 0000, paint);
        }
    }

}
