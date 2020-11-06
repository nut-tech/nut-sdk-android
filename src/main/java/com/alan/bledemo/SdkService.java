package com.alan.bledemo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.nutspace.nut.api.BleDeviceConsumer;
import com.nutspace.nut.api.BleDeviceManager;
import com.nutspace.nut.api.callback.ConnectStateChangedCallback;
import com.nutspace.nut.api.callback.EventCallback;
import com.nutspace.nut.api.model.BleDevice;

/**
 * Created by alan on 2016/12/20.
 */

public class SdkService extends Service implements BleDeviceConsumer, ConnectStateChangedCallback, EventCallback {

    BleDeviceManager mManager;

    @Override
    public void onCreate() {
        super.onCreate();
        mManager = BleDeviceManager.getInstance(this);
        mManager.addConnectStateChangedCallback(this);
        mManager.addEventCallback(this);
        mManager.bind(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return START_STICKY_COMPATIBILITY;
    }

    @Override
    public void onServiceBound() {

    }

    @Override
    public void onConnect(BleDevice device) {
        if (MyApplication.isAppInBackground()) {
            showNotification(device, "device connect");
        }
    }

    @Override
    public void onDisconnect(BleDevice device, int error) {
        if (MyApplication.isAppInBackground()) {
            showNotification(device, "device disconnect");
        }
    }

    @Override
    public void onClickEvent(BleDevice device, int action) {
        if (MyApplication.isAppInBackground()) {
            String ac = "unkown";
            switch (action) {
                case BleDevice.ACTION_SINGLE_CLICK:
                    ac = "single";
                    break;

                case BleDevice.ACTION_DOUBLECLICK:
                    ac = "double click";
                    break;

                case BleDevice.ACTION_LONGCLICK:
                    ac = "long click";
                    break;
            }
            showNotification(device, "receive " + ac + " action");
        }
    }

    private void showNotification(BleDevice device, String text) {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        Notification notification = builder.setSmallIcon(R.mipmap.ic_launcher).setContentTitle(device.address).setContentText(text).build();
        notification.defaults |= Notification.DEFAULT_SOUND;
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        manager.notify(device.id, 0, notification);
    }

    @Override
    public void onRssiChangedEvent(BleDevice device, int rssi) {

    }

    @Override
    public void onBatteryChangedEvent(BleDevice device, int battery) {

    }

    @Override
    public void onDeviceRingStateChangedEvent(BleDevice device, int state, int error) {

    }

    @Override
    public void onSwitchDFUMode(BleDevice bleDevice, boolean b) {

    }

    @Override
    public void onDestroy() {
        mManager.removeConnectStateChangedCallback(this);
        mManager.removeEventCallback(this);
        mManager.unbind(this);
        super.onDestroy();
    }
}
