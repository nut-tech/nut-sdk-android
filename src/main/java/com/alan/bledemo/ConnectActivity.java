package com.alan.bledemo;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.nutspace.nut.api.BleDeviceConsumer;
import com.nutspace.nut.api.BleDeviceManager;
import com.nutspace.nut.api.callback.BeaconResultCallback;
import com.nutspace.nut.api.callback.ConnectStateChangedCallback;
import com.nutspace.nut.api.callback.EventCallback;
import com.nutspace.nut.api.model.BleDevice;

/**
 * @author hanson
 */
public class ConnectActivity extends BaseActivity implements BleDeviceConsumer,
        ConnectStateChangedCallback, EventCallback, BeaconResultCallback, View.OnClickListener {

    BleDeviceManager mManager;

    BleDevice mDevice;

    TextView mTvName;

    TextView mTvMacAddress;

    CheckBox mCbAutoConnect;

    Button mBtnConnect;

    TextView mTvTips;

    TextView mTvRssi;

    TextView mTvBattery;

    TextView mTvBeaconUUID;

    TextView mTvBeaconMajor;

    TextView mTvBeaconMinor;

    Button mBtnCall;

    Button mBtnReadBattery;

    Button mBtnReadRssi;

    Button mBtnShutdown;

    CheckBox mCbAntiLost;

    Button mBtnWriteBeaconUUID;

    EditText mEtBeaconUUID;

    Button mBtnWriteBeaconMajorMinor;

    EditText mEtBeaconMajor;

    EditText mEtBeaconMinor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        mManager = BleDeviceManager.getInstance(this);
        mDevice = getIntent().getParcelableExtra("device");
        mTvName = findViewById(R.id.tv_name);
        mTvMacAddress = findViewById(R.id.tv_address);
        mCbAutoConnect = findViewById(R.id.cb_auto_connect);
        mBtnConnect = findViewById(R.id.btn_connect);
        mTvTips = findViewById(R.id.tv_tips);
        mTvRssi = findViewById(R.id.tv_rssi);
        mTvBattery = findViewById(R.id.tv_battery);
        mTvBeaconUUID = findViewById(R.id.tv_beacon_uuid);
        mTvBeaconMajor = findViewById(R.id.tv_beacon_major);
        mTvBeaconMinor = findViewById(R.id.tv_beacon_minor);
        mBtnCall = findViewById(R.id.btn_call);
        mBtnReadBattery = findViewById(R.id.btn_read_battery);
        mBtnReadRssi = findViewById(R.id.btn_read_rssi);
        mBtnShutdown = findViewById(R.id.btn_shutdown);
        mCbAntiLost = findViewById(R.id.cb_anti_lost);
        mBtnWriteBeaconUUID = findViewById(R.id.btn_write_uuid);
        mEtBeaconUUID = findViewById(R.id.et_beacon_uuid);
        mEtBeaconUUID.setText("10102233-4455-6677-8899-AABBCCDDEEFF");
        mBtnWriteBeaconMajorMinor = findViewById(R.id.btn_write_major_minor);
        mEtBeaconMajor = findViewById(R.id.et_beacon_major);
        mEtBeaconMajor.setText("12");
        mEtBeaconMinor = findViewById(R.id.et_beacon_minor);
        mEtBeaconMinor.setText("34");

        mTvName.setText(mDevice.name);
        mTvMacAddress.setText(mDevice.address);

        mBtnConnect.setOnClickListener(this);
        mBtnCall.setOnClickListener(this);
        mBtnReadBattery.setOnClickListener(this);
        mBtnReadRssi.setOnClickListener(this);
        mBtnShutdown.setOnClickListener(this);
        mCbAntiLost.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mManager.enableAntiLost(mDevice, isChecked);
            }
        });
        mBtnWriteBeaconUUID.setOnClickListener(this);
        mBtnWriteBeaconMajorMinor.setOnClickListener(this);
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
            case R.id.btn_read_battery:
                mManager.readBattery(this, mDevice);
                break;
            case R.id.btn_read_rssi:
                mManager.readRssi(mDevice);
                break;
            case R.id.btn_shutdown:
                mManager.shutdown(this, mDevice);
                //mManager.forceShutdown(this, mDevice);
                break;
            case R.id.btn_write_uuid:
                mTvBeaconUUID.setText("Beacon UUID: ");
                String beaconUUID = mEtBeaconUUID.getText().toString();
                mManager.setBeaconUUID(mDevice, beaconUUID);
                break;
            case R.id.btn_write_major_minor:
                mTvBeaconMajor.setText("Beacon Major: ");
                mTvBeaconMinor.setText("Beacon Minor: ");
                String major = mEtBeaconMajor.getText().toString();
                String minor = mEtBeaconMinor.getText().toString();
                mManager.setBeaconMajorMinor(mDevice, Integer.parseInt(major), Integer.parseInt(minor));
                break;
            default:
                break;
        }
    }

    @Override
    public void onServiceBound() {

    }

    @Override
    public void onConnect(BleDevice device) {
        //The device is connected successfully
        mBtnConnect.setText("disconnect");
        mTvTips.setText("connected");
        findViewById(R.id.ll_device_status).setVisibility(View.VISIBLE);
        findViewById(R.id.ll_device_control).setVisibility(View.VISIBLE);
        mManager.readRssi(device);
    }

    @Override
    public void onDisconnect(BleDevice device, int error) {
        mBtnConnect.setText("connect");
        mTvTips.setText("disconnect error code is " + error);
        findViewById(R.id.ll_device_status).setVisibility(View.GONE);
        findViewById(R.id.ll_device_control).setVisibility(View.GONE);
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
            default:
                break;
        }
        Toast.makeText(this, "receive " + ac + " action", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRssiChangedEvent(BleDevice device, int rssi) {
        if (device.equals(mDevice)) {
            mTvRssi.setText("RSSI: " + rssi);
        }
    }

    @Override
    public void onBatteryChangedEvent(BleDevice device, int battery) {
        mTvBattery.setVisibility(View.VISIBLE);
        mTvBattery.setText(String.format("Battery: %s%%", battery));
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
    public void onSwitchDFUMode(BleDevice bleDevice, boolean b) {

    }

    @Override
    public void onBeaconUUID(BleDevice device, String uuid, boolean result) {
        if (result) {
            mTvBeaconUUID.setText("Beacon UUID: " + uuid);
        } else {
            mTvBeaconUUID.setText("Beacon UUID: Error");
        }
    }

    @Override
    public void onBeaconMajorMinor(BleDevice device, int major, int minor, boolean result) {
        if (result) {
            mTvBeaconMajor.setText("Beacon Major: " + major);
            mTvBeaconMinor.setText("Beacon Minor: " + minor);
        } else {
            mTvBeaconMajor.setText("Beacon Major: Error");
            mTvBeaconMinor.setText("Beacon Minor: Error");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mManager.addConnectStateChangedCallback(this);
        mManager.addEventCallback(this);
        mManager.addBeaconResultCallback(this);
        mManager.bind(this);
    }

    @Override
    protected void onStop() {
        mManager.removeConnectStateChangedCallback(this);
        mManager.removeEventCallback(this);
        mManager.removeBeaconResultCallback(this);
        mManager.unbind(this);
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        mManager.disconnect(this, mDevice);
        super.onBackPressed();
    }
}