package com.colornaut.colornaut;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

@SuppressWarnings("deprecation")
public class MainActivity extends AppCompatActivity {

    private final static String TAG = "COLORNAUT";

    private Camera mCamera;
    private CameraPreview mCameraPreview;
    private Button capture;
    private Context myContext;
    private FrameLayout mLayoutPreview;
    PictureCallback mCall;

    //a bitmap to display the captured image
    private Bitmap bmp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        myContext = this;

        mCamera = getCameraInstance();

        mLayoutPreview = (FrameLayout) findViewById(R.id.camera_preview);
        mCameraPreview = new CameraPreview(this, mCamera);

        mLayoutPreview.addView(mCameraPreview, 0);

        capture = (Button) findViewById(R.id.button_capture);
        capture.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                captureImage();
            }
        });
    }

    private Camera getCameraInstance() {
        Camera camera = null;
        try {
            camera = Camera.open();
        } catch (Exception e) {
            // cannot get camera or does not exist
        }
        return camera;
    }

    public void onResume() {
        super.onResume();
//        if (!hasCamera(myContext)) {
//            Toast toast = Toast.makeText(myContext, "Sorry, your phone does not have a camera!", Toast.LENGTH_LONG);
//            toast.show();
//            finish();
//        }
//        if (mCamera == null) {
//            //if the front facing camera does not exist
//            if (findFrontFacingCamera() < 0) {
//                Toast.makeText(this, "No front facing camera found.", Toast.LENGTH_LONG).show();
//                switchCamera.setVisibility(View.GONE);
//            }
//            mCamera = Camera.open(findBackFacingCamera());
//            mPicture = getPictureCallback();
//            mCameraPreview.refreshCamera(mCamera);
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //when on Pause, release camera in order to be used from other applications
        releaseCamera();
    }

    private void captureImage() {
        //sets what code should be executed after the picture is taken
        if (mCameraPreview.isSafeToTakePicture()) {
            mCamera.takePicture(null, null, mPicture);
            mCameraPreview.setSafeToTakePicture(false);
        }
        //Log.i(TAG, "captured image: " + bmp);
    }

    PictureCallback mPicture = new PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File pictureFile = getOutputMediaFile();
            if (pictureFile == null) {
                mCameraPreview.setSafeToTakePicture(true);
                return;
            }
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
                mCameraPreview.setSafeToTakePicture(true);
            } catch (FileNotFoundException e) {

            } catch (IOException e) {
            }
        }
    };


    private static File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyCameraApp");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator
                + "IMG_" + timeStamp + ".jpg");

        return mediaFile;
    }

    private void releaseCamera() {
        // stop and release camera
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

}
