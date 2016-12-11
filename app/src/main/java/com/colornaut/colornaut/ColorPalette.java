package com.colornaut.colornaut;

import android.app.ListActivity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.graphics.Palette;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
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
    private String paletteName = "Untitled Palette";
    private String location = "No Location";

    // HashMap to store Swatches found from palette as String and Integer values so that Serialization can work
    // String key is just a name for that swatch, right now its just Color<number>
    // ArrayList<Integer> is organized as: [0]rgbValue, [1]titleTextColor, [2]bodyTextColor, [3]population
    // where titleTextColor and bodyTextColor are contrasting colors to rgbValue (useful for displaying text over color)
    // and population is the number of pixels on image that contain that color (might be useful)
    // Info on Palette.Swatch - https://developer.android.com/reference/android/support/v7/graphics/Palette.Swatch.html
    private HashMap<String, ArrayList<Integer>> palette = new HashMap<String, ArrayList<Integer>>();


    public ColorPalette(Bitmap image) {

        // TODO: Save image to internal memory and store image path
        // this.imagePath = getImagePath(image);

        // TODO - Asynchronous color palette builder. Works but is always one picture behind for some reason...
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

    public String getPaletteName() { return paletteName; }
    public void setPaletteName(String name) { this.paletteName = name; }
    public void setLocation(String location) { this.location = location; }


//    public List<Palette.Swatch> getPaletteSwatches() { return paletteSwatches; }
//    public ArrayList<Integer> getSwatchesRgb() {
//        ArrayList<Integer> list = new ArrayList<>();
//        for (Palette.Swatch ps : paletteSwatches) {
//            list.add(ps.getRgb());
//        }
//        return list;
//    }
//    public ArrayList<Integer> getRgbValues() { return rgbValues; }
//    public Integer getRgbValue(int position) { return rgbValues.get(position); }
//    public ArrayList<String> getHexValuess() { return hexValues; }
//    public String getHexValue(int position) { return hexValues.get(position); }
//    public String getPaletteName() { return paletteName; }
//    public String getLocation() { return location; }
//    public Bitmap getImage() { return image; }


//    @Override
//    public String toString() {
//        String s = "";
//
//        if (paletteName == null) {
//            s = s + "Color Palette" + "\n";
//        } else {
//            s = s + this.paletteName + "\n";
//        }
//
//        //s = s + bitmapToString(this.image) + "\n";
//        if (location == null) {
//            s = s + "no location" + "\n";
//        } else {
//            s = s + this.location + "\n";
//        }
//        s = s + "swatches:\n";
//        for (Palette.Swatch ps : this.paletteSwatches) {
//            s = s + ps.getRgb() + "\n";
//            Log.i(TAG, "SAVING population: " + ps.getPopulation());
//            s = s + ps.getPopulation() + "\n";
//        }
//        s = s + "\n";
//        return s;
//    }


    public final static String bitmapToString(Bitmap in){
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        in.compress(Bitmap.CompressFormat.PNG, 100, bytes);
        return Base64.encodeToString(bytes.toByteArray(),Base64.DEFAULT);
    }
    public final static Bitmap stringToBitmap(String in){
        byte[] bytes = Base64.decode(in, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }





}
