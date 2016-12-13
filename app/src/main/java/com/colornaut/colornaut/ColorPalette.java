package com.colornaut.colornaut;

import android.app.ListActivity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.support.v7.graphics.Palette;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Created by vdesouza on 12/5/16.
 */

public class ColorPalette implements Serializable {

    private static final String TAG = "ColorPalette";

    private String imagePath;
    private String imageFileName;
    private String paletteName = "Untitled Palette";
    private String location = "No Location";
    private int savedNumber;

    // HashMap to store Swatches found from palette as String and Integer values so that Serialization can work
    // String key is just a name for that swatch, right now its just Color<number>
    // ArrayList<Integer> is organized as: [0]rgbValue, [1]titleTextColor, [2]bodyTextColor, [3]population
    // where titleTextColor and bodyTextColor are contrasting colors to rgbValue (useful for displaying text over color)
    // and population is the number of pixels on image that contain that color (might be useful)
    // Info on Palette.Swatch - https://developer.android.com/reference/android/support/v7/graphics/Palette.Swatch.html
    private HashMap<String, ArrayList<Integer>> palette = new HashMap<String, ArrayList<Integer>>();


    public ColorPalette(Bitmap image) {

        // TODO - if time - Asynchronous color palette builder. Works but is always one picture behind for some reason...
//        Palette.from(image).generate(new Palette.PaletteAsyncListener() {
//            public void onGenerated(Palette p) {
//                Palette mPalette = p;
//                List<Palette.Swatch> paletteSwatches = mPalette.getSwatches();
//                for (int i = 0; i <= paletteSwatches.size() - 1; i++) {
//                    ArrayList<Integer> swatch = new ArrayList<Integer>();
//                    Palette.Swatch paletteSwatch = paletteSwatches.get(i);
//                    swatch.add(0, paletteSwatch.getRgb());
//                    swatch.add(1, paletteSwatch.getTitleTextColor());
//                    swatch.add(2, paletteSwatch.getBodyTextColor());
//                    swatch.add(3, paletteSwatch.getPopulation());
//                    palette.put("Color " + i, swatch);
//                }
//            }
//        });

        Palette mPalette = Palette.from(image).generate();
        List<Palette.Swatch> paletteSwatches = mPalette.getSwatches();
        for (int i = 0; i <= paletteSwatches.size() - 1; i++) {
            ArrayList<Integer> swatch = new ArrayList<Integer>();
            Palette.Swatch paletteSwatch = paletteSwatches.get(i);
            swatch.add(0, paletteSwatch.getRgb());
            swatch.add(1, paletteSwatch.getTitleTextColor());
            swatch.add(2, paletteSwatch.getBodyTextColor());
            swatch.add(3, paletteSwatch.getPopulation());
            this.palette.put("Color " + i, swatch);
        }
    }

    public int getPaletteSize() { return palette.size(); }

    public ArrayList<Integer> getAllRgbValues() {
        ArrayList<Integer> allRgbValues = new ArrayList<>();
        for (ArrayList<Integer> color : palette.values()) {
            allRgbValues.add(color.get(0));
        }
        return allRgbValues;
    }

    public ArrayList<Integer> getSwatch(int colorIndex) {
        String key = "Color " + colorIndex;
        return palette.get(key);
    }

    public void setPaletteName(String name) { this.paletteName = name; }
    public String getPaletteName() { return paletteName; }
    public void setLocation(String location) { this.location = location; }
    public String getLocation() { return location; }
    public void setImagePath(String path) { this.imagePath = path; }
    public void setImageFileName(String filename) { this.imageFileName = filename; }
    public Bitmap loadImageFromStorage()  {
        Bitmap b = null;
        try {
            Log.i(TAG, imagePath);
            File f = new File(imagePath, imageFileName);
            b = BitmapFactory.decodeStream(new FileInputStream(f));
            // bitmap is saved at 90 degrees for some reason ¯\_(ツ)_/¯
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(b,b.getWidth(),b.getHeight(),true);
            return Bitmap.createBitmap(scaledBitmap , 0, 0, scaledBitmap .getWidth(), scaledBitmap .getHeight(), matrix, true);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return b;
    }
    public void setSavedNumber(int number) { this.savedNumber = number; }
    public int getSavedNumber() { return savedNumber; }

    public String makeRgbString() {
        String rgb = "";
        for (int i = 0; i < savedNumber; i++) {
            int r = Color.red(palette.get("Color " + i).get(0));
            int g = Color.green(palette.get("Color " + i).get(0));
            int b = Color.blue(palette.get("Color " + i).get(0));
            rgb = rgb + "RGB(" + r + "," + g + "," + b + ")";
            if (i != savedNumber-1 ) {
                rgb += ", ";
            }
        }
        return rgb;
    }

    public String makeHexString() {
        String hex = "";
        for (int i = 0; i < savedNumber; i++) {
            String hexColor = String.format("#%06X", (0xFFFFFF & palette.get("Color " + i).get(0)));
            hex = hex + hexColor;
            if (i != savedNumber-1 ) {
                hex += ", ";
            }
        }
        return hex;
    }

}
