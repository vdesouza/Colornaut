package com.colornaut.colornaut;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;

/**
 * Created by vdesouza on 12/7/16.
 */

public class ColornautDataAsyncTask extends AsyncTask<ArrayList<ColorPalette>, Void, ArrayList<ColorPalette>> {

    private final static String TAG = "COLORNAUT";
    public static final String MODE_SAVE = "save";
    public static final String MODE_LOAD = "load";
    private final static String FILENAME = "ColornautData.srl";

    private Context mContext;
    private String mode;

    public ColornautDataAsyncTask(Context context, String mode) {
        this.mContext = context;
        this.mode = mode;
    }

    @Override
    protected ArrayList<ColorPalette> doInBackground(ArrayList<ColorPalette>... params) {
        switch (mode) {
            case MODE_SAVE:
                ObjectOutput out = null;
                try {
                    out = new ObjectOutputStream(new FileOutputStream(new File(mContext.getFilesDir(), "") + File.separator + FILENAME));
                    out.writeObject(params[0]);
                    out.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case MODE_LOAD:
                ObjectInputStream input;
                try {
                    input = new ObjectInputStream(new FileInputStream(new File(new File(mContext.getFilesDir(),"")+File.separator+FILENAME)));
                    params[0] = (ArrayList<ColorPalette>) input.readObject();
                    input.close();


                    // Testing loaded data after saving to storage
                    Log.i(TAG, "Load complete: " + params[0].toString() + " Count: " + params[0].size());
                    for (ColorPalette loadedPalette : params[0]) {
                        Log.i(TAG, "Palette Name: " + loadedPalette.getPaletteName());
                        for (int i = 0; i < loadedPalette.getPaletteSize() - 1; i++) {
                            ArrayList<Integer> swatch = loadedPalette.getSwatch(i);
                            Log.i(TAG, "Color" + i + ": " + swatch.get(0));
                            Log.i(TAG, "TitleColor" + i + ": " + swatch.get(1));
                            Log.i(TAG, "BodyColor" + i + ": " + swatch.get(2));
                            Log.i(TAG, "Population" + i + ": " + swatch.get(3));
                        }
                    }



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
                break;
        }
        return params[0];
    }

    @Override
    protected void onProgressUpdate(Void... progress) {
    }

    @Override
    protected void onPostExecute(ArrayList<ColorPalette> result) {
        switch (mode) {
            case MODE_SAVE:
                Toast.makeText(mContext, "Palette Saved!", Toast.LENGTH_SHORT).show();
                break;

        }
    }
}
