package com.colornaut.colornaut;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
    private Canvas canvas = new Canvas();
    private Dialog checks, shareTo;
    private Context context;
    private Bitmap image;


    public ShareView(Context context, ColorPalette palette) {
        super(context);
        this.context = context;
        this.palette = palette;

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

                    break;
                case R.id.twitter_btn:

                    break;
                case R.id.instagram_btn:

                    break;
                case R.id.pinterest_btn:

                    break;
                case R.id.save_btn:

                            String fName = UUID.randomUUID().toString() + ".png";
                            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), fName);
                            try {
                                boolean compressSucceeded = image.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(file));
                                addImageToGallery(file.getAbsolutePath(), context);
                                Toast.makeText(context, "Saved to " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                    break;
            }
            shareTo.dismiss();
            Toast.makeText(context, "Image Shared! :)", Toast.LENGTH_SHORT).show();
        }
    };

    public static void addImageToGallery(final String filePath, final Context context) {

        ContentValues values = new ContentValues();

        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.MediaColumns.DATA, filePath);

        context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    public void makeImage() {
        Paint paint = new Paint();
        paint.setTextSize(16f);
        paint.setColor(Color.BLACK);
        if ( ((CheckBox) checks.findViewById(R.id.img)).isChecked() ) {
            canvas.drawBitmap(palette.getImage(),00000,000000, null);
        }
        if ( ((CheckBox) checks.findViewById(R.id.name)).isChecked() ) {
            canvas.drawText(palette.getPaletteName(), 0000000, 0000000, paint);
        }
        if ( ((CheckBox) checks.findViewById(R.id.rgb)).isChecked() ) {
            for (Integer rgb : palette.getRgbValues()) {
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = rgb & 0xff;

                String output = "RGB(" + r + ", " + g + ", " + b + ")";
                canvas.drawText(output, 00000, 000000, paint);
            }

        }
        if ( ((CheckBox) checks.findViewById(R.id.hex)).isChecked() ) {
            for (String hex : palette.getHexValuess()) {
                canvas.drawText(hex, 00000, 0000, paint);
            }

        }
        if ( ((CheckBox) findViewById(R.id.time)).isChecked() ) {

        }
        if ( ((CheckBox) findViewById(R.id.loc)).isChecked() ) {

        }
    }

}
