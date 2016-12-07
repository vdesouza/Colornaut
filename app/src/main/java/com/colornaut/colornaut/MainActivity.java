package com.colornaut.colornaut;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.graphics.Palette;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.support.v7.app.AppCompatActivity;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;


@SuppressWarnings("deprecation")
public class MainActivity extends AppCompatActivity {

    private final static String TAG = "COLORNAUT";
//    private static final String FILE_NAME = "ColornautData.txt";
    private static final String PREFS_KEY = "ColornautData";
    private SharedPreferences prefs;

    // views for main screen
    private Camera mCamera;
    private CameraPreview mCameraPreview;
    private Button mCaptureButton;
    private Button mPaletteGalleryButton;
    private Context mContext;
    private FrameLayout mLayoutPreview;
    private Button mShareButton;
    private ShareView shareView;

    //bitmap to display the captured image
    private Bitmap mBitmapTaken;
    private ColorPalette colorPalette;

    // views for edit panel
    private ViewGroup editPanel;
    private LinearLayout editPanelLinearLayout;
    private EditText inputPaletteName;
    private Button saveButton;
    private ColorPreviewsGridAdapter mAdapter;
    private boolean isPanelShown;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mContext = this;

        prefs = mContext.getSharedPreferences("pref", Context.MODE_PRIVATE);

        // get camera if available
        mCamera = getCameraInstance();

        // set up layout and camera view
        mLayoutPreview = (FrameLayout) findViewById(R.id.camera_preview);
        if (mCamera != null) {
            mCameraPreview = new CameraPreview(this, mCamera);
            mLayoutPreview.addView(mCameraPreview, 0);
        }

        editPanel = (ViewGroup) findViewById(R.id.edit_panel);
        editPanel.setVisibility(View.INVISIBLE);
        isPanelShown = false;

        editPanelLinearLayout = (LinearLayout) findViewById(R.id.editPanelLinearLayout);

        // set up capture button
        mCaptureButton = (Button) findViewById(R.id.button_capture);
        mCaptureButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                captureImage();
            }
        });

        // set up palette gallery button
        mPaletteGalleryButton = (Button) findViewById(R.id.button_palette_gallery);
        mPaletteGalleryButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, PaletteGalleryActivity.class);
                intent.putExtra("list", load());
                startActivity(intent);
            }
        });

        mShareButton = (Button) findViewById(R.id.buttonShare);
        mShareButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                shareView = new ShareView(mContext, null);
                mLayoutPreview.addView(shareView);
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //when on Pause, release camera in order to be used from other applications
        releaseCamera();
    }

    private Camera getCameraInstance() {
        Camera camera = null;
        try {
            camera = Camera.open();
        } catch (Exception e) {
            Log.i(TAG, "Could not get camera: " + e.getMessage());
        }
        return camera;
    }

    private void releaseCamera() {
        // stop and release camera
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    private void captureImage() {
        // takes the preview on screen and puts it in a bitmap
        mCamera.setOneShotPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                // Convert to JPG - found at http://stackoverflow.com/a/7536405
                Camera.Size previewSize = camera.getParameters().getPreviewSize();
                YuvImage yuvimage = new YuvImage(data, ImageFormat.NV21, previewSize.width, previewSize.height, null);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                yuvimage.compressToJpeg(new Rect(0, 0, previewSize.width, previewSize.height), 80, baos);
                byte[] jdata = baos.toByteArray();
                // Convert to Bitmap
                mBitmapTaken = BitmapFactory.decodeByteArray(jdata, 0, jdata.length);
                Log.i(TAG, mBitmapTaken.toString());

                // create color palette
                colorPalette = new ColorPalette(mBitmapTaken);

                // open edit panel
                launchEditPanel();
            }
        });
    }

    private void launchEditPanel() {
        Log.i(TAG, "Launching edit");
        Animation bottomUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bottom_up);
        editPanel.startAnimation(bottomUp);
        editPanel.setVisibility(View.VISIBLE);
        editPanel.bringToFront();
        Log.i(TAG, "Launched edit");
        isPanelShown = true;

        editPanelLinearLayout.removeView(inputPaletteName);
        editPanelLinearLayout.removeView(saveButton);

        // build gridview of palette colors
        GridView mPaletteGridView = (GridView) findViewById(R.id.paletteGridView);
        mAdapter = new ColorPreviewsGridAdapter(this, colorPalette.getSwatchesRgb());
        mPaletteGridView.setAdapter(mAdapter);

        // Set an EditText view to get user input and Save button
        inputPaletteName = new EditText(mContext);
        inputPaletteName.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        inputPaletteName.setHint("Enter palette name");
        editPanelLinearLayout.addView(inputPaletteName);

        saveButton = new Button(mContext);
        saveButton.setText("Save");
        saveButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                savePalette(colorPalette);
            }
        });
        editPanelLinearLayout.addView(saveButton);

    }

    public void slideDown(View v) {
        closeEditPanel();
    }

    private void closeEditPanel() {
        if (isPanelShown) {
            Log.i(TAG, "Closing edit");
            Animation bottomDown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bottom_down);
            editPanel.startAnimation(bottomDown);
            editPanel.setVisibility(View.INVISIBLE);
            Log.i(TAG, "Closed edit");
            editPanelLinearLayout.removeView(inputPaletteName);
            editPanelLinearLayout.removeView(saveButton);
            isPanelShown = false;
        }
    }

    private void savePalette(ColorPalette colorPalette) {

        Set<String> paletteSet = prefs.getStringSet(PREFS_KEY, new HashSet<String>());
        SharedPreferences.Editor editor = prefs.edit();
        paletteSet.add(colorPalette.toString());
        editor.putStringSet(PREFS_KEY, paletteSet);
        editor.apply();

        Toast.makeText(mContext, "Palette Saved!", Toast.LENGTH_SHORT).show();

        ArrayList<ColorPalette> loadedlist = load();
        Log.i(TAG, "Load complete");
        for (ColorPalette cp : loadedlist) {
            Log.i(TAG, cp.toString());
        }



//        PrintWriter writer = null;
//        try {
//            FileOutputStream fos = openFileOutput(FILE_NAME, MODE_PRIVATE);
//            writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
//                    fos)));
//            Toast.makeText(mContext, "Saving...", Toast.LENGTH_LONG).show();
//            writer.println(colorPalette);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            if (null != writer) {
//                writer.close();
//                Toast.makeText(mContext, "Palette Saved!", Toast.LENGTH_SHORT).show();
//                ArrayList<ColorPalette> loadedlist = load();
//                Log.i(TAG, "Load complete");
//                for (ColorPalette cp : loadedlist) {
//
//                    Log.i(TAG, cp.toString());
//                }
//            }
//        }

        closeEditPanel();
    }

    private ArrayList<ColorPalette> load() {
        ArrayList<ColorPalette> listOfSavedPalettes = new ArrayList<ColorPalette>();

        BufferedReader reader = null;
        try {

            Set<String> paletteSet = prefs.getStringSet(PREFS_KEY, new HashSet<String>());

            for (String s : paletteSet) {
                reader = new BufferedReader(new StringReader(s));

                Log.i(TAG, "String: " + s);

                String name;
                //String bitmap;
                String location;
                String color;
                String population;
                String empty = "";

                while (null != (name = reader.readLine())) {
                    //bitmap = reader.readLine();
                    location = reader.readLine();
                    Log.i(TAG, "Loading: Name = "+name);
                    Log.i(TAG, "Loading: Location = "+location);
                    List<Palette.Swatch> swatchList = new ArrayList<Palette.Swatch>();
                    while (!empty.equals(reader.readLine())) {
                        color = reader.readLine();
                        Log.i(TAG, "Loading: Color = "+color);
                        population = reader.readLine();
                        Log.i(TAG, "Loading: Population = "+population);
                        Palette.Swatch ps = new Palette.Swatch(Integer.valueOf(color), Integer.valueOf(population));
                        swatchList.add(ps);
                    }
                    ColorPalette cp = new ColorPalette(name, location, swatchList);
                    listOfSavedPalettes.add(cp);
                }

            }
//            FileInputStream fis = openFileInput(FILE_NAME);
//            reader = new BufferedReader(new InputStreamReader(fis));
//
//            String name;
//            String bitmap;
//            String location;
//            String color;
//            String population;
//            String empty = "";
//
//            while (null != (name = reader.readLine())) {
//                bitmap = reader.readLine();
//                location = reader.readLine();
//                Log.i(TAG, "Loading: Name = "+name);
//                Log.i(TAG, "Loading: Location = "+location);
//                List<Palette.Swatch> swatchList = new ArrayList<Palette.Swatch>();
//                while (!empty.equals(reader.readLine())) {
//                    color = reader.readLine();
//                    Log.i(TAG, "Loading: Color = "+color);
//                    population = reader.readLine();
//                    Log.i(TAG, "Loading: Population = "+population);
//                    Palette.Swatch ps = new Palette.Swatch(Integer.valueOf(color), Integer.valueOf(population));
//                    swatchList.add(ps);
//                }
//                ColorPalette cp = new ColorPalette(name, bitmap, location, swatchList);
//                listOfSavedPalettes.add(cp);
//            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != reader) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return listOfSavedPalettes;
        }

    }

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

        public void addImageToGallery(final String filePath, final Context context) {

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

}
