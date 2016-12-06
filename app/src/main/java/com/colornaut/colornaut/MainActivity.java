package com.colornaut.colornaut;

import java.io.ByteArrayOutputStream;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;


@SuppressWarnings("deprecation")
public class MainActivity extends AppCompatActivity {

    private final static String TAG = "COLORNAUT";

    // views for main screen
    private Camera mCamera;
    private CameraPreview mCameraPreview;
    private Button mCaptureButton;
    private Context mContext;
    private FrameLayout mLayoutPreview;

    //bitmap to display the captured image
    private Bitmap mBitmapTaken;

    // views for edit panel
    private ViewGroup editPanel;
    private boolean isPanelShown;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mContext = this;

        // get camera if available
        mCamera = getCameraInstance();

        // set up layout and camera view
        mLayoutPreview = (FrameLayout) findViewById(R.id.camera_preview);
        mCameraPreview = new CameraPreview(this, mCamera);
        mLayoutPreview.addView(mCameraPreview, 0);

        editPanel = (ViewGroup) findViewById(R.id.edit_panel);
        editPanel.setVisibility(View.INVISIBLE);
        isPanelShown = false;

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

                //TODO - Make ColorPalette object here

                Palette.Builder paletteBuilder = Palette.from(mBitmapTaken);
                paletteBuilder.generate(new Palette.PaletteAsyncListener() {
                    public void onGenerated(Palette palette) {
                        // picture taken and pallet created
                        launchEditPanel();
                    }
                });


            }
        });
    }

    public void slideDown(View v) {
        launchEditPanel();
    }

    private void launchEditPanel() {
        if (!isPanelShown) {
            Log.i(TAG, "Launching edit");
            Animation bottomUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bottom_up);
            editPanel.startAnimation(bottomUp);
            editPanel.setVisibility(View.VISIBLE);
            editPanel.bringToFront();
            Log.i(TAG, "Launched edit");
            isPanelShown = true;
        } else {
            Log.i(TAG, "Closing edit");
            Animation bottomDown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bottom_down);
            editPanel.startAnimation(bottomDown);
            editPanel.setVisibility(View.INVISIBLE);
            Log.i(TAG, "Closed edit");
            isPanelShown = false;
        }

    }


}
