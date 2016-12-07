package com.colornaut.colornaut;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutCompat;
import android.test.LoaderTestCase;
import android.text.InputType;
import android.util.ArraySet;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.support.v7.app.AppCompatActivity;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
    private Context mContext;
    private FrameLayout mLayoutPreview;

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

}
