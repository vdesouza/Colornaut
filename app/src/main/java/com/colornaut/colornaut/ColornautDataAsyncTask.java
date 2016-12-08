package com.colornaut.colornaut;

import android.content.Context;
import android.os.AsyncTask;
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

public class ColornautDataAsyncTask extends AsyncTask<Void, Void, Void> {

    private final static String TAG = "COLORNAUT";
    public static final String MODE_SAVE = "save";
    public static final String MODE_LOAD = "load";
    private final static String FILENAME = "ColornautData.srl";

    private Context mContext;
    private String mode;
    private ArrayList<ColorPalette> colornautData;

    public ColornautDataAsyncTask(Context context, ArrayList<ColorPalette> colornautData, String mode) {
        this.mContext = context;
        this.mode = mode;
        this.colornautData = colornautData;
    }

    @Override
    protected Void doInBackground(Void... params) {
        switch (mode) {
            case MODE_SAVE:
                ObjectOutput out = null;
                try {
                    out = new ObjectOutputStream(new FileOutputStream(new File(mContext.getFilesDir(), "") + File.separator + FILENAME));
                    out.writeObject(colornautData);
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
                    colornautData = (ArrayList<ColorPalette>) input.readObject();
                    input.close();
                } catch (StreamCorruptedException e) {
                    e.printStackTrace();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                break;
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Void... progress) {
    }

    @Override
    protected void onPostExecute(Void result) {
        switch (mode) {
            case MODE_SAVE:
                Toast.makeText(mContext, "Palette Saved!", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
