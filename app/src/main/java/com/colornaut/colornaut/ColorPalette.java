package com.colornaut.colornaut;

import android.graphics.Bitmap;
import android.support.v7.graphics.Palette;


/**
 * Created by vdesouza on 12/5/16.
 */

public class ColorPalette {

    private static final String TAG = "ColorPalette";

    // Swatches that make up a color palette
    // A Swatch is one type of color that appears the most
    // There are a max of 7 default colors but custom targets can be made http://stackoverflow.com/a/28776080
    // Swatches also carry various information about the color picked
    // https://developer.android.com/reference/android/support/v7/graphics/Palette.Swatch.html

    private Palette mPalette;

    public ColorPalette(Bitmap image) {
        Palette.Builder paletteBuilder = Palette.from(image);
        paletteBuilder.generate(new Palette.PaletteAsyncListener() {
            public void onGenerated(Palette palette) {
                mPalette = palette;
            }
        });
    }



}
