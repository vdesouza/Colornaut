package com.colornaut.colornaut;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.hardware.Camera;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IntegerRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.support.v7.app.AppCompatActivity;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("deprecation")
public class MainActivity extends AppCompatActivity {

    private final static String TAG = "COLORNAUT";
    private final static String FILENAME = "ColornautData.srl";

    // the main data structure that holds all saved color palettes taken
    private ArrayList<ColorPalette> colornautData;

    // views for main screen
    private Camera mCamera;
    private CameraPreview mCameraPreview;
    private Button mCaptureButton;
    private Button mPaletteGalleryButton;
    private Context mContext;
    private FrameLayout mLayoutPreview;
    private Button mShareButton;
    //private ShareView shareView;

    //bitmap to display the captured image
    private Bitmap mBitmapTaken;
    private ColorPalette colorPalette;

    // views for edit panel
    private SeekbarWithIntervals seekbarWithIntervals = null;
    private ViewGroup editPanel;
    private LinearLayout editPanelLinearLayout;
    private View editPanelBorder;
    private EditText inputPaletteName;
    private Button saveButton;
    private ColorPreviewsGridAdapter mAdapter;
    private String locationString = "";
    private TextView locationTextView;
    private boolean isPanelShown;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mContext = this;

        // Load save data from memory
        if (null == (colornautData = load())) {
            colornautData = new ArrayList<ColorPalette>();
        }

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

        List<String> seekbarIntervals = getIntervals();
        getSeekbarWithIntervals().setIntervals(seekbarIntervals);


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
                // shareView = new ShareView(mContext, null);
                //mLayoutPreview.addView(shareView);
            }
        });

    }

    private List<String> getIntervals() {
        return new ArrayList<String>() {{
            add("2");
            add("3");
            add("4");
            add("5");
            add("6");
            add("7");
            add("8");
        }};
    }

    private SeekbarWithIntervals getSeekbarWithIntervals() {
        if (seekbarWithIntervals == null) {
            seekbarWithIntervals = (SeekbarWithIntervals) findViewById(R.id.seekbarWithIntervals);
        }
        return seekbarWithIntervals;
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

                // change action bar and status bar color
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(colorPalette.getAllRgbValues().get(1)));
                getSupportActionBar().setDisplayShowTitleEnabled(false);
                getSupportActionBar().setDisplayShowTitleEnabled(true);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Window window = getWindow();
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    window.setStatusBarColor(colorPalette.getAllRgbValues().get(0));
                }

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

        // clear previous LinearLayout
        editPanelLinearLayout.removeView(locationTextView);
        editPanelLinearLayout.removeView(inputPaletteName);
        editPanelLinearLayout.removeView(saveButton);

        // set editPanel border
        editPanelBorder = (View) findViewById(R.id.editPanelBorder);
        editPanelBorder.setBackgroundColor(colorPalette.getAllRgbValues().get(0));

        // build gridview of palette colors
        ArrayList<Integer> rgbValues = new ArrayList<Integer>(colorPalette.getAllRgbValues().subList(0, 5));
        final GridView mPaletteGridView = (GridView) findViewById(R.id.paletteGridView);
        mAdapter = new ColorPreviewsGridAdapter(this, rgbValues);
        mPaletteGridView.setColumnWidth(editPanelLinearLayout.getWidth() / 6);
        mPaletteGridView.setAdapter(mAdapter);

        // set up seekbar
        seekbarWithIntervals.setProgress(3);
        // behaviors for seekbar to select number of items in palette
        seekbarWithIntervals.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 5;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                progress = progressValue + 2;
                ArrayList<Integer> rgbValues = new ArrayList<Integer>(colorPalette.getAllRgbValues().subList(0, progress));
                mAdapter = new ColorPreviewsGridAdapter(MainActivity.this, rgbValues);
                mAdapter.notifyDataSetChanged();
                mPaletteGridView.setColumnWidth((int)(editPanelLinearLayout.getWidth() - 3.8) / mAdapter.getCount());
                mPaletteGridView.setAdapter(mAdapter);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                ArrayList<Integer> rgbValues = new ArrayList<Integer>(colorPalette.getAllRgbValues().subList(0, progress));
                mAdapter = new ColorPreviewsGridAdapter(MainActivity.this, rgbValues);
                mAdapter.notifyDataSetChanged();
                mPaletteGridView.setColumnWidth((int)(editPanelLinearLayout.getWidth() - 3.8) / mAdapter.getCount());
                mPaletteGridView.setAdapter(mAdapter);
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Set an EditText view to get user input and Save button
        inputPaletteName = new EditText(mContext);
        inputPaletteName.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        inputPaletteName.setHint("Enter palette name");
        inputPaletteName.setPadding(20, 10, 20, 10);
        editPanelLinearLayout.addView(inputPaletteName);

        // set up location tracker and display location
        locationTextView = new TextView(mContext);
        locationString = "Getting Location Data...";
        locationTextView.setText(locationString);
        locationTextView.setPadding(20, 10, 20, 20);
        locationTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_location, 0, 0, 0);
        editPanelLinearLayout.addView(locationTextView);
        getLocation();

        saveButton = new Button(mContext);
        saveButton.setText("Save");
        saveButton.setBackgroundColor(colorPalette.getAllRgbValues().get(0));
        saveButton.setTextColor(Color.WHITE);
        saveButton.setTextSize(20);
        saveButton.setHeight(70);
        saveButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!inputPaletteName.getText().toString().isEmpty()) {
                    colorPalette.setPaletteName(inputPaletteName.getText().toString());
                }
                colornautData.add(colorPalette);
                save();
            }
        });
        editPanelLinearLayout.addView(saveButton);

    }

    public void slideDown(View v) { closeEditPanel(); }

    private void closeEditPanel() {
        if (isPanelShown) {
            Log.i(TAG, "Closing edit");
            Animation bottomDown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bottom_down);
            editPanel.startAnimation(bottomDown);
            editPanel.setVisibility(View.INVISIBLE);
            Log.i(TAG, "Closed edit");
            editPanelLinearLayout.removeView(locationTextView);
            editPanelLinearLayout.removeView(inputPaletteName);
            editPanelLinearLayout.removeView(saveButton);
            isPanelShown = false;
        }
    }

    private void save() {
        ObjectOutput out = null;
        try {
            out = new ObjectOutputStream(new FileOutputStream(new File(getFilesDir(),"")+File.separator+FILENAME));
            out.writeObject(colornautData);
            out.close();
            Toast.makeText(mContext, "Palette Saved!", Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // for testing loading and saving
        ArrayList<ColorPalette> loadedPalettes = load();
        Log.i(TAG, "Load complete: " + loadedPalettes.toString() + " Count: " + loadedPalettes.size());
        for (ColorPalette loadedPalette: loadedPalettes) {
            Log.i(TAG, "Palette Name: " + loadedPalette.getPaletteName());
            for (int i = 0; i < loadedPalette.getPaletteSize() - 1; i++) {
                ArrayList<Integer> swatch = loadedPalette.getSwatch(i);
                Log.i(TAG, "Color" + i + ": " + swatch.get(0));
                Log.i(TAG, "TitleColor" + i + ": " + swatch.get(1));
                Log.i(TAG, "BodyColor" + i + ": " + swatch.get(2));
                Log.i(TAG, "Population" + i + ": " + swatch.get(3));
            }
        }
        closeEditPanel();
    }

    private ArrayList<ColorPalette> load() {
        ObjectInputStream input;
        try {
            input = new ObjectInputStream(new FileInputStream(new File(new File(getFilesDir(),"")+File.separator+FILENAME)));
            ArrayList<ColorPalette> colorPalettes = (ArrayList<ColorPalette>) input.readObject();
            input.close();
            return colorPalettes;
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
            return null;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    // starts location services to get lat lon data from gps or network
    private void getLocation() {
        // adapted from http://stackoverflow.com/a/29658427
        OneTimeLocationProvider.requestSingleUpdate(mContext, new OneTimeLocationProvider.LocationCallback() {
            @Override
            public void onNewLocationAvailable(Location location) {
                getLocationString(location);
                Log.d("Location", "my location is " + location.getLatitude() + ", " + location.getLongitude());
            }
        });
    }

    // performs AsyncTask call to get name of location based on lat and lon
    private void getLocationString(Location location) {
        final String USERNAME = "vdesouza";
        URL url = null;
        // build URL with lat lon
        try {
            double lat = location.getLatitude();
            double lon = location.getLongitude();
            url = new URL("http://api.geonames.org/findNearbyPlaceNameJSON?lat="+lat+"&lng="+lon+"&username="+USERNAME);
        } catch (MalformedURLException e) {
            Log.i(TAG, "Bad URL");
        }
        if (url != null) {
            // AsyncTask call
            new GetLocationNameAsyncTask().execute(url);
        }
    }

    // AsyncTask to connect to geoname.org and get city name of where palette is created
    private class GetLocationNameAsyncTask extends AsyncTask<URL, Void, String> {
        @Override
        protected String doInBackground(URL... url) {
            Log.i(TAG, "Entered AsyncTask.");
            return buildLocationNameString(url[0]);
        }
        @Override
        protected void onPostExecute(String result) {
            // check if there were any errors found
            Log.i(TAG, "Result: " + result);
            // add location string to ColorPalette and to editPanel
            if (result != null) {
                locationString = result;
            } else {
                locationString = "Could not get location data at the moment.";
            }
            if (locationTextView != null) {
                locationTextView.setText(locationString);
            }
            colorPalette.setLocation(locationString);
        }
    }

    // parses and formats json recieved from connecting to geonames.org
    private String buildLocationNameString(URL url) {
        String locationName = "";
        HttpURLConnection connection = null;
        // make connection into an HttpUrlConnection for url
        try {
            // connect and read
            connection = (HttpURLConnection) url.openConnection();
            JSONObject jsonLocation = new JSONObject(read(connection));
            // parse JSON for errors or data
            if (jsonLocation.getJSONArray("geonames").length() != 0) {
                jsonLocation = jsonLocation.getJSONArray("geonames").getJSONObject(0);
                Log.i(TAG, jsonLocation.toString());
                // get json data
                String city = jsonLocation.getString("name");
                String country = jsonLocation.getString("countryName");
                Log.i(TAG, "Got JSON data: " + city + country);
                // build string
                locationName = city + ", " +  country + " on " + DateFormat.getDateTimeInstance().format(new Date());
                Log.i(TAG, locationName);
            }
        } catch (MalformedURLException e) {
            Log.i(TAG, "Malformed URL.");
        } catch (IOException e) {
            Log.i(TAG, "Invalid URL.");
        } catch (JSONException e) {
            Log.i(TAG, "Invalid JSON.");
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            return locationName;
        }
    }

    // helper to read data from http connection
    private static String read(HttpURLConnection connection) {
        StringBuffer stringBuffer = new StringBuffer();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String line = new String();
            while ((line = reader.readLine()) != null) {
                stringBuffer.append(line + "\n");
            }
        } catch (IOException e) {
            stringBuffer.append("Failed");
        }
        Log.i(TAG, stringBuffer.toString());
        return stringBuffer.toString();
    }

//    public class ShareView extends View {
//
//        private ColorPalette palette;
//        private Canvas canvas = new Canvas();
//        private Dialog checks, shareTo;
//        private Context context;
//        private Bitmap image;
//
//
//        public ShareView(Context context, ColorPalette palette) {
//            super(context);
//            this.context = context;
//            this.palette = palette;
//
//            checks = new Dialog(context);
//            checks.setContentView(R.layout.select_share);
//            checks.show();
//
//            shareTo = new Dialog(context);
//            shareTo.setContentView(R.layout.share_to);
//
//
//            Button confirm = (Button) checks.findViewById(R.id.share_info);
//            confirm.setOnClickListener(new OnClickListener() {
//                public void onClick(View view) {
//                    checks.dismiss();
//                    ImageButton fb = (ImageButton) shareTo.findViewById(R.id.fb_btn);
//                    fb.setOnClickListener(listener);
//                    ImageButton twit = (ImageButton)shareTo.findViewById(R.id.twitter_btn);
//                    twit.setOnClickListener(listener);
//                    ImageButton ig = (ImageButton)shareTo.findViewById(R.id.instagram_btn);
//                    ig.setOnClickListener(listener);
//                    ImageButton pin = (ImageButton)shareTo.findViewById(R.id.pinterest_btn);
//                    pin.setOnClickListener(listener);
//                    ImageButton sav = (ImageButton)shareTo.findViewById(R.id.save_btn);
//                    sav.setOnClickListener(listener);
//                    shareTo.show();
//                }
//            });
//        }
//
//        protected OnClickListener listener = new OnClickListener() {
//
//            public void onClick(View v) {
//                switch (v.getId()) {
//                    case R.id.fb_btn:
//
//                        break;
//                    case R.id.twitter_btn:
//
//                        break;
//                    case R.id.instagram_btn:
//
//                        break;
//                    case R.id.pinterest_btn:
//
//                        break;
//                    case R.id.save_btn:
//
//                        String fName = UUID.randomUUID().toString() + ".png";
//                        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), fName);
//                        try {
//                            boolean compressSucceeded = image.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(file));
//                            addImageToGallery(file.getAbsolutePath(), context);
//                            Toast.makeText(context, "Saved to " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
//                        } catch (FileNotFoundException e) {
//                            e.printStackTrace();
//                        }
//                        break;
//                }
//                shareTo.dismiss();
//                Toast.makeText(context, "Image Shared! :)", Toast.LENGTH_SHORT).show();
//            }
//        };
//
//        public void addImageToGallery(final String filePath, final Context context) {
//
//            ContentValues values = new ContentValues();
//
//            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
//            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
//            values.put(MediaStore.MediaColumns.DATA, filePath);
//
//            context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
//        }
//
//        public void makeImage() {
//            Paint paint = new Paint();
//            paint.setTextSize(16f);
//            paint.setColor(Color.BLACK);
//            if ( ((CheckBox) checks.findViewById(R.id.img)).isChecked() ) {
//                canvas.drawBitmap(palette.getImage(),00000,000000, null);
//            }
//            if ( ((CheckBox) checks.findViewById(R.id.name)).isChecked() ) {
//                canvas.drawText(palette.getPaletteName(), 0000000, 0000000, paint);
//            }
//            if ( ((CheckBox) checks.findViewById(R.id.rgb)).isChecked() ) {
//                for (Integer rgb : palette.getRgbValues()) {
//                    int r = (rgb >> 16) & 0xff;
//                    int g = (rgb >> 8) & 0xff;
//                    int b = rgb & 0xff;
//
//                    String output = "RGB(" + r + ", " + g + ", " + b + ")";
//                    canvas.drawText(output, 00000, 000000, paint);
//                }
//
//            }
//            if ( ((CheckBox) checks.findViewById(R.id.hex)).isChecked() ) {
//                for (String hex : palette.getHexValuess()) {
//                    canvas.drawText(hex, 00000, 0000, paint);
//                }
//
//            }
//            if ( ((CheckBox) findViewById(R.id.time)).isChecked() ) {
//
//            }
//            if ( ((CheckBox) findViewById(R.id.loc)).isChecked() ) {
//
//            }
//        }
//
//    }

}
