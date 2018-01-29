package com.alan.bledemo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.nutspace.nut.api.BleDeviceConsumer;
import com.nutspace.nut.api.BleDeviceManager;
import com.nutspace.nut.api.ble.util.TypeConvertUtils;
import com.nutspace.nut.api.callback.ConnectStateChangedCallback;
import com.nutspace.nut.api.callback.EventCallback;
import com.nutspace.nut.api.model.BleDevice;

public class ConnectActivity extends BaseActivity implements BleDeviceConsumer, ConnectStateChangedCallback, EventCallback, View.OnClickListener {

    BleDeviceManager mManager;

    BleDevice mDevice;

    TextView mTvName;

    TextView mTvMacAddress;

    CheckBox mCbAutoConnect;

    Button mBtnConnect;

    Button mBtnCall;

    Button mBtnShutdown;

    CheckBox mCbAntiLost;

    TextView mTvTips;

    TextView mTvRssi;

    TextView mTvBattery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        mManager = BleDeviceManager.getInstance(this);
        mDevice = getIntent().getParcelableExtra("device");
        mTvName = (TextView) findViewById(R.id.tv_name);
        mTvMacAddress = (TextView) findViewById(R.id.tv_address);
        mCbAutoConnect = (CheckBox) findViewById(R.id.cb_auto_connect);
        mBtnConnect = (Button) findViewById(R.id.btn_connect);
        mBtnCall = (Button) findViewById(R.id.btn_call);
        mBtnShutdown = (Button) findViewById(R.id.btn_shutdown);
        mCbAntiLost = (CheckBox) findViewById(R.id.cb_anti_lost);
        mTvBattery = (TextView) findViewById(R.id.tv_battery);
        mTvTips = (TextView) findViewById(R.id.tv_tips);
        mTvRssi = (TextView) findViewById(R.id.tv_rssi);

        mTvName.setText(mDevice.name);
        mTvMacAddress.setText(mDevice.address);

        mBtnConnect.setOnClickListener(this);
        mBtnCall.setOnClickListener(this);
        mBtnShutdown.setOnClickListener(this);
        mCbAntiLost.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mManager.enableAntiLost(mDevice, isChecked);
            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_connect:
                if (mBtnConnect.getText().equals("connect")) {
                    if (mManager.isBound(this)) {
                        mManager.connect(this, mDevice, mCbAutoConnect.isChecked());
                        mTvTips.setText("connecting...");
                    } else {
                        mTvTips.setText("service not bound");
                    }
                } else {
                    mTvTips.setText("disconnecting...");
                    mManager.disconnect(this, mDevice);
                }

                break;

            case R.id.btn_call:
                if (mBtnCall.getText().equals("call")) {
                    mManager.changeRingState(mDevice, BleDevice.STATE_RING);
                } else {
                    mManager.changeRingState(mDevice, BleDevice.STATE_QUIT);
                }

                break;

            case R.id.btn_shutdown:
                mManager.shutdown(this, mDevice);
//                mManager.forceShutdown(this, mDevice);
//                mManager.readBattery(this, mDevice);
                break;
        }
    }

    @Override
    public void onServiceBound() {

    }

    @Override
    public void onConnect(BleDevice device) {
        mBtnCall.setVisibility(View.VISIBLE);
        mBtnShutdown.setVisibility(View.VISIBLE);
        mCbAntiLost.setVisibility(View.VISIBLE);
        mTvRssi.setVisibility(View.VISIBLE);
        mTvTips.setText("connected");
        mBtnConnect.setText("disconnect");
        Log.e("TEST", "isDeviceConnect:" + isDeviceConnect(device.id));
    }

    @Override
    public void onDisconnect(BleDevice device, int error) {
        mBtnCall.setVisibility(View.GONE);
        mBtnShutdown.setVisibility(View.GONE);
        mCbAntiLost.setVisibility(View.GONE);
        mTvRssi.setVisibility(View.GONE);
        mTvTips.setText("disconnect error code is " + error);
        mBtnConnect.setText("connect");
    }

    @Override
    public void onClickEvent(BleDevice device, int action) {
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
        Toast.makeText(this, "receive " + ac + " action", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRssiChangedEvent(BleDevice device, int rssi) {
        if (device.equals(mDevice)) {
            mTvRssi.setText("rssi=" + rssi);
        }
    }

    @Override
    public void onBatteryChangedEvent(BleDevice device, int battery) {
        mTvBattery.setVisibility(View.VISIBLE);
        mTvBattery.setText("battery: " + battery);
    }

    @Override
    public void onDeviceRingStateChangedEvent(BleDevice device, int state, int error) {
        if (state == BleDevice.STATE_RING) {
            if (error == 0) {
                mBtnCall.setText("quit");
            } else {
                mTvTips.setText("call device error " + error);
            }
        } else {
            if (error == 0) {
                mBtnCall.setText("call");
            } else {
                mTvTips.setText("quit device error " + error);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mManager.addConnectStateChangedCallaback(this);
        mManager.addEventCallaback(this);
        mManager.bind(this);
    }

    @Override
    protected void onStop() {
        mManager.removeConnectStateChangedCallback(this);
        mManager.removeEventCallback(this);
        mManager.unbind(this);
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        mManager.disconnect(this, mDevice);
        super.onBackPressed();
    }

    /**
     * Bluetooth device connection status
     * @param deviceID
     * @return
     */
    private boolean isDeviceConnect(String deviceID) {
        BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = mBluetoothManager.getAdapter();
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(TypeConvertUtils.convertDeviceIdToByteArray(Long.parseLong(deviceID)));
        int state = mBluetoothManager.getConnectionState(device, BluetoothProfile.GATT);
        return state == BluetoothProfile.STATE_CONNECTED;
    }
}
