package com.dotpad.logic;

import android.content.Context;

import com.dotpad.model.Dot;
import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import java.util.List;
import java.util.UUID;

/*
http://www.seas.upenn.edu/~cdmurphy/cit595/labs/lab06-pebble.html

// good tutorial
https://ninedof.wordpress.com/pebble-sdk-tutorial/

// two windows
https://github.com/pebble/pebble-sdk-examples/blob/master/watchapps/app_font_browser/src/app_font_browser.c

 */

/**
 * Created by INFOR PL on 2015-04-14.
 */
public class Pebble {

    public static final UUID APP_UUID = UUID.fromString("795feb20-f497-40f9-a237-4a8a528310f5");

    public static final int ACTION_KEY = 5;
    public static final int ID_KEY = 6;

    public static final int ACTION_DOTS = 1;
    public static final int ACTION_DOT = 2;

    public static final int DICTIONARY_DOTS = 1;
    public static final int DICTIONARY_DOT = 2;

    private Context mContext;

    public Pebble(Context context) {
        this.mContext = context;
    }

    public boolean isConnected() {
        return PebbleKit.isWatchConnected(this.mContext);
    }

    public void sendDot(List<Dot> dots, int index) {

        if (dots == null)
            return;

        Dot dot = dots.get(index);
        PebbleDictionary dictionary = new PebbleDictionary();
        dictionary.addBytes(DICTIONARY_DOTS, this.prepareData(dots));
        dictionary.addString(DICTIONARY_DOT, dot.text.substring(0, Math.min(dot.text.length(), 150)));

        PebbleKit.sendDataToPebble(this.mContext, APP_UUID, dictionary);
    }

    public void sendDisplay(List<Dot> dots) {

        PebbleDictionary dictionary = new PebbleDictionary();
        dictionary.addBytes(DICTIONARY_DOTS, this.prepareData(dots));

        PebbleKit.sendDataToPebble(this.mContext, APP_UUID, dictionary);
    }

    private byte[] prepareData(List<Dot> dots) {

        // scale from 720x1134 to 144x168 -> 107x168, ratio = 0.148
        final double ratio = 0.148;

        byte[] array = new byte[dots.size() * 3];
        int index = 0;

        for (Dot dot : dots) {

            // for negative values use: 128 + (128 + value)
            byte x = (byte)((double)dot.position.x * ratio);
            byte y = (byte)((double)dot.position.y * ratio);

            array[index++] = x;
            array[index++] = y;
            array[index++] = (byte)dot.size;
        }

        return array;
    }
}