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
    TextView mTvName, mTvMacAddress;
    CheckBox mCbAutoConnect, mCbAntiLost;
    TextView mTvTips, mTvRssi, mTvBattery, mTvHardware, mTvFirmware, mTvBeaconUUID,
            mTvBeaconMajor, mTvBeaconMinor, mTvBeaconAdvInterval;
    Button mBtnConnect, mBtnCall, mBtnPairMode, mBtnShutdown, mBtnSwitchDFUMode, mBtnReadRSSI,
            mBtnReadBattery, mBtnWriteBeaconUUIDEnable, mBtnWriteBeaconUUIDDisable;
    EditText mEtBeaconUUIDEnable, mEtBeaconMajor, mEtBeaconMinor, mEtBeaconUUIDDisable, mEtBeaconAdvInterval;
    Button mBtnWriteBeaconMajorMinor, mBtnWriteBeaconAdvInterval;

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
        mTvHardware = findViewById(R.id.tv_hardware);
        mTvFirmware = findViewById(R.id.tv_firmware);
        mTvBeaconUUID = findViewById(R.id.tv_beacon_uuid);
        mTvBeaconMajor = findViewById(R.id.tv_beacon_major);
        mTvBeaconMinor = findViewById(R.id.tv_beacon_minor);
        mTvBeaconAdvInterval = findViewById(R.id.tv_beacon_adv_interval);
        mBtnCall = findViewById(R.id.btn_call);
        mBtnPairMode = findViewById(R.id.btn_pair);
        if (mDevice.pairState == BleDevice.PAIR_STATE_UNPAIR) {
            mBtnPairMode.setVisibility(View.VISIBLE);
            mBtnPairMode.setText("Set Paired");
        } else if (mDevice.pairState == BleDevice.PAIR_STATE_PAIRED) {
            mBtnPairMode.setVisibility(View.VISIBLE);
            mBtnPairMode.setText("Set Unpair");
        } else {
            mBtnPairMode.setVisibility(View.GONE);
        }
        mBtnShutdown = findViewById(R.id.btn_shutdown);
        mBtnSwitchDFUMode = findViewById(R.id.btn_switch_dfu);
        mBtnReadRSSI = findViewById(R.id.btn_read_rssi);
        mBtnReadBattery = findViewById(R.id.btn_read_battery);
        mCbAntiLost = findViewById(R.id.cb_anti_lost);
        mBtnWriteBeaconUUIDEnable = findViewById(R.id.btn_write_uuid_on);
        mEtBeaconUUIDEnable = findViewById(R.id.et_beacon_uuid_on);
        mEtBeaconUUIDEnable.setText("10102233-4455-6677-8899-AABBCCDDEEFF");
        mBtnWriteBeaconUUIDDisable = findViewById(R.id.btn_write_uuid_off);
        mEtBeaconUUIDDisable = findViewById(R.id.et_beacon_uuid_off);
        mEtBeaconUUIDDisable.setText("FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFFF");
        mBtnWriteBeaconMajorMinor = findViewById(R.id.btn_write_major_minor);
        mEtBeaconMajor = findViewById(R.id.et_beacon_major);
        mEtBeaconMajor.setText("12");
        mEtBeaconMajor.setHint("(0~65535)");
        mEtBeaconMinor = findViewById(R.id.et_beacon_minor);
        mEtBeaconMinor.setText("34");
        mEtBeaconMinor.setHint("(0~65535)");
        mBtnWriteBeaconAdvInterval = findViewById(R.id.btn_write_adv_interval);
        mEtBeaconAdvInterval = findViewById(R.id.et_beacon_adv_interval);
        mEtBeaconAdvInterval.setText("1000");
        mEtBeaconAdvInterval.setHint("(1000~10240ms)");

        mTvName.setText(mDevice.name);
        mTvMacAddress.setText(mDevice.address);

        mBtnConnect.setOnClickListener(this);
        mBtnCall.setOnClickListener(this);
        mBtnPairMode.setOnClickListener(this);
        mBtnReadBattery.setOnClickListener(this);
        mBtnReadRSSI.setOnClickListener(this);
        mBtnShutdown.setOnClickListener(this);
        mCbAntiLost.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mManager.setDeviceAlert(mDevice, isChecked);
                LoadingDialogFragment.show(ConnectActivity.this);
            }
        });
        mBtnWriteBeaconUUIDEnable.setOnClickListener(this);
        mBtnWriteBeaconUUIDDisable.setOnClickListener(this);
        mBtnWriteBeaconMajorMinor.setOnClickListener(this);
        mBtnWriteBeaconAdvInterval.setOnClickListener(this);
        mBtnSwitchDFUMode.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_connect) {
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
        } else if (id == R.id.btn_call) {
            if (mBtnCall.getText().equals("Find Device")) {
                mManager.changeRingState(mDevice, BleDevice.STATE_RING);
            } else {
                mManager.changeRingState(mDevice, BleDevice.STATE_QUIT);
            }
            LoadingDialogFragment.show(this);
        } else if (id == R.id.btn_pair) {
            if (mBtnPairMode.getText().equals("Set Paired")) {
                mManager.changePairState(mDevice, BleDevice.PAIR_STATE_PAIRED);
            } else {
                mManager.changePairState(mDevice, BleDevice.PAIR_STATE_UNPAIR);
            }
            LoadingDialogFragment.show(this);
        } else if (id == R.id.btn_shutdown) {
            mManager.shutdown(mDevice);
            //mManager.forceShutdown(this, mDevice);
        } else if (id == R.id.btn_read_rssi) {
            mManager.readRssi(mDevice);
            LoadingDialogFragment.show(this);
        } else if (id == R.id.btn_read_battery) {
            mManager.readBattery(mDevice);
            LoadingDialogFragment.show(this);
        } else if (id == R.id.btn_write_uuid_on) {
            mTvBeaconUUID.setText("Beacon UUID: ");
            String beaconUUID = mEtBeaconUUIDEnable.getText().toString();
            mManager.setBeaconUUID(mDevice, beaconUUID);
            LoadingDialogFragment.show(this);
        } else if (id == R.id.btn_write_uuid_off) {
            String beaconUUID;
            mTvBeaconUUID.setText("Beacon UUID: ");
            beaconUUID = mEtBeaconUUIDDisable.getText().toString();
            mManager.setBeaconUUID(mDevice, beaconUUID);
            LoadingDialogFragment.show(this);
        } else if (id == R.id.btn_write_major_minor) {
            mTvBeaconMajor.setText("Beacon Major: ");
            mTvBeaconMinor.setText("Beacon Minor: ");
            String major = mEtBeaconMajor.getText().toString();
            String minor = mEtBeaconMinor.getText().toString();
            mManager.setBeaconMajorMinor(mDevice, Integer.parseInt(major), Integer.parseInt(minor));
            LoadingDialogFragment.show(this);
        } else if (id == R.id.btn_write_adv_interval) {
            mTvBeaconAdvInterval.setText("Beacon AdvInterval: ");
            String interval = mEtBeaconAdvInterval.getText().toString();
            mManager.setBeaconAdvInterval(mDevice, Integer.parseInt(interval));
            LoadingDialogFragment.show(this);
        } else if (id == R.id.btn_switch_dfu) {
            mManager.switchToDFUMode(mDevice);
            LoadingDialogFragment.show(this);
        }
    }

    @Override
    public void onServiceBound() {

    }

    @Override
    public void onConnect(BleDevice device) {
        mBtnConnect.setText("disconnect");
        mTvTips.setText("connected");
        mTvHardware.setText(String.format("Hw:%s", device.hardware));
        mTvFirmware.setText(String.format("Fw:%s", device.firmware));
        findViewById(R.id.ll_device_status).setVisibility(View.VISIBLE);
        findViewById(R.id.ll_device_control).setVisibility(View.VISIBLE);
        mManager.readRssi(device);
        mManager.readBattery(mDevice);
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
        String ac = "unknown";
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
        LoadingDialogFragment.hide(this);
        if (device.equals(mDevice)) {
            mTvRssi.setText("RSSI:" + rssi);
        }
    }

    @Override
    public void onBatteryChangedEvent(BleDevice device, int battery) {
        LoadingDialogFragment.hide(this);
        mTvBattery.setVisibility(View.VISIBLE);
        mTvBattery.setText(String.format("Battery:%s%%(%s%%)", battery, device.getEstimateBatteryLevel()));
    }

    @Override
    public void onDeviceRingStateChangedEvent(BleDevice device, int state, int error) {
        LoadingDialogFragment.hide(this);
        if (state == BleDevice.STATE_RING) {
            if (error == 0) {
                mBtnCall.setText("Quit");
            } else {
                mTvTips.setText("call device error " + error);
            }
        } else {
            if (error == 0) {
                mBtnCall.setText("Find Device");
            } else {
                mTvTips.setText("quit device error " + error);
            }
        }
    }

    @Override
    public void onDevicePairStateChangedEvent(BleDevice device, int state, int error) {
        LoadingDialogFragment.hide(this);
        if (state == BleDevice.PAIR_STATE_PAIRED) {
            if (error == 0) {
                mBtnPairMode.setText("Set Unpair");
            } else {
                mTvTips.setText("set paired device error " + error);
            }
        } else {
            if (error == 0) {
                mBtnPairMode.setText("Set Paired");
            } else {
                mTvTips.setText("set unpair device error " + error);
            }
        }
    }

    @Override
    public void onDeviceAlert(BleDevice device, boolean result) {
        LoadingDialogFragment.hide(this);
        String tips = String.format("Set Device Alert %s", result ? "Success" : "Failure");
        Toast.makeText(this, tips, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBeaconUUID(BleDevice device, String uuid, boolean result) {
        LoadingDialogFragment.hide(this);
        if (result) {
            mTvBeaconUUID.setText("Beacon UUID: " + uuid);
        } else {
            mTvBeaconUUID.setText("Beacon UUID: Error");
        }
    }

    @Override
    public void onBeaconMajorMinor(BleDevice device, int major, int minor, boolean result) {
        LoadingDialogFragment.hide(this);
        if (result) {
            mTvBeaconMajor.setText("Beacon Major: " + major);
            mTvBeaconMinor.setText("Beacon Minor: " + minor);
        } else {
            mTvBeaconMajor.setText("Beacon Major: Error");
            mTvBeaconMinor.setText("Beacon Minor: Error");
        }
    }

    @Override
    public void onBeaconAdvInterval(BleDevice device, int interval, boolean result) {
        LoadingDialogFragment.hide(this);
        if (result) {
            mTvBeaconAdvInterval.setText("Beacon Adv Interval: " + interval);
        } else {
            mTvBeaconAdvInterval.setText("Beacon Adv Interval: Error");
        }
    }

    @Override
    public void onSwitchDFUMode(BleDevice device, boolean result) {
        LoadingDialogFragment.hide(this);
//        if (result) {
        mTvTips.setText("Device switch to DFU mode success.");
        mTvTips.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Exit to Main Activity and scan for devices in DFU mode
                Toast.makeText(ConnectActivity.this,
                        "Click on the DfuTarg device to enter the DFU process", Toast.LENGTH_LONG).show();
                finish();
            }
        }, 3 * 1000);
//        } else {
//            mTvTips.setText("Device switch to DFU mode failure.");
//        }
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
