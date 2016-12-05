package com.colornaut.colornaut;

import java.io.ByteArrayOutputStream;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.support.v7.app.AppCompatActivity;


@SuppressWarnings("deprecation")
public class MainActivity extends AppCompatActivity {

    private final static String TAG = "COLORNAUT";

    private Camera mCamera;
    private CameraPreview mCameraPreview;
    private Button mCaptureButton;
    private Context mContext;
    private FrameLayout mLayoutPreview;

    //bitmap to display the captured image
    private Bitmap mBitmapTaken;

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
            }
        });
    }

    private void releaseCamera() {
        // stop and release camera
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

}
