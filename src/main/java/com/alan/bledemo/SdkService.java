package com.alan.bledemo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.nutspace.nut.api.BleDeviceConsumer;
import com.nutspace.nut.api.BleDeviceManager;
import com.nutspace.nut.api.callback.BeaconResultCallback;
import com.nutspace.nut.api.callback.ConnectStateChangedCallback;
import com.nutspace.nut.api.callback.EventCallback;
import com.nutspace.nut.api.model.BleDevice;

/**
 * Created by alan on 2016/12/20.
 */

public class SdkService extends Service implements BleDeviceConsumer, ConnectStateChangedCallback, EventCallback, BeaconResultCallback {

    private static final String TAG = SdkService.class.getSimpleName();

    BleDeviceManager mManager;

    @Override
    public void onCreate() {
        super.onCreate();
        mManager = BleDeviceManager.getInstance(this);
        mManager.addConnectStateChangedCallback(this);
        mManager.addEventCallback(this);
        mManager.addBeaconResultCallback(this);
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
        Log.d(TAG, String.format("EventCallback: device %s connected", device.address));
        if (MyApplication.isAppInBackground()) {
            showNotification(device, "device connect");
        }
    }

    @Override
    public void onDisconnect(BleDevice device, int error) {
        Log.d(TAG, String.format("EventCallback: device %s disconnected", device.address));
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
                default:
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
        Log.d(TAG, String.format("EventCallback: device %s rssi= %d", device.address, rssi));
    }

    @Override
    public void onBatteryChangedEvent(BleDevice device, int battery) {
        Log.d(TAG, String.format("EventCallback: device %s battery= %d", device.address, battery));
    }

    @Override
    public void onDeviceRingStateChangedEvent(BleDevice device, int state, int error) {

    }

    @Override
    public void onDevicePairStateChangedEvent(BleDevice device, int state, int error) {

    }

    @Override
    public void onDeviceAlert(BleDevice device, boolean result) {

    }

    @Override
    public void onBeaconUUID(BleDevice device, String uuid, boolean result) {
        String tips = "Beacon uuid failure";
        if (result) {
            tips = String.format("beacon uuid= %s", uuid);
        }
        Log.d(TAG, String.format("BeaconResultCallback: device %s %s", device.address, tips));
    }

    @Override
    public void onBeaconMajorMinor(BleDevice device, int major, int minor, boolean result) {
        String tips = "Beacon Major Minor failure";
        if (result) {
            tips = String.format("beacon major= %d minor= %d", major, minor);
        }
        Log.d(TAG, String.format("BeaconResultCallback: device %s %s", device.address, tips));
    }

    @Override
    public void onBeaconAdvInterval(BleDevice device, int interval, boolean result) {
        String tips = "Beacon Adv Interval failure";
        if (result) {
            tips = String.format("beacon adv interval = %d", interval);
        }
        Log.d(TAG, String.format("BeaconResultCallback: device %s %s", device.address, tips));
    }

    @Override
    public void onSwitchDFUMode(BleDevice device, boolean result) {
        String tips = String.format("Switch DFU Mode %s", result ? "Success" : "Failure");
        Log.d(TAG, String.format("BeaconResultCallback: device %s %s", device.address, tips));
    }

    @Override
    public void onDestroy() {
        mManager.removeConnectStateChangedCallback(this);
        mManager.removeEventCallback(this);
        mManager.unbind(this);
        super.onDestroy();
    }
}
