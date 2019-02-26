package com.dotpad;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.dotpad.db.DotManager;
import com.dotpad.logic.Pebble;
import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

/**
 * Created by INFOR PL on 2015-04-20.
 */
public class PebbleService extends Service {

    private PebbleKit.PebbleDataReceiver mReceiver;
    private Pebble mPebble;

    public PebbleService() {
        this.mPebble = new Pebble(this);
    }

    @Override
    public void onCreate() {

        super.onCreate();

        this.mReceiver = new PebbleKit.PebbleDataReceiver(Pebble.APP_UUID) {
            @Override
            public void receiveData(Context context, int i, final PebbleDictionary pebbleDictionary) {

                long action = pebbleDictionary.getUnsignedIntegerAsLong(Pebble.ACTION_KEY);
                Long index = pebbleDictionary.getUnsignedIntegerAsLong(Pebble.ID_KEY);

                makeAction(action, index == null ? 0 : (long)index);
            }
        };

        PebbleKit.registerReceivedDataHandler(this, mReceiver);
    }

    @Override
    public void onDestroy() {

        super.onDestroy();

        if (this.mReceiver != null)
            this.unregisterReceiver(this.mReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void makeAction(long action, long index) {

        if (!this.mPebble.isConnected())
            return;

        DotManager manager = new DotManager(this);

        switch ((int)action) {

            case Pebble.ACTION_DOTS:
                if (this.mPebble != null && manager != null)
                    this.mPebble.sendDisplay(manager.dots(DotManager.Contents.ACTIVE));
                break;

            case Pebble.ACTION_DOT:
                if (this.mPebble != null && manager != null)
                    this.mPebble.sendDot(manager.dots(DotManager.Contents.ACTIVE), (int)index);
                break;
        }

        manager.close();
    }
}
