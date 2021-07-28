package com.alan.bledemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nutspace.nut.api.BleDeviceConsumer;
import com.nutspace.nut.api.BleDeviceManager;
import com.nutspace.nut.api.ble.exception.BleNotAvailableException;
import com.nutspace.nut.api.callback.ScanResultCallback;
import com.nutspace.nut.api.model.BleDevice;

import java.util.ArrayList;

import no.nordicsemi.android.dfu.DfuProgressListener;
import no.nordicsemi.android.dfu.DfuProgressListenerAdapter;
import no.nordicsemi.android.dfu.DfuServiceInitiator;
import no.nordicsemi.android.dfu.DfuServiceListenerHelper;

/**
 * @author hanson
 */
public class MainActivity extends BaseActivity implements BleDeviceConsumer, ScanResultCallback, View.OnClickListener, OnItemClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    public static final int REQUEST_ACCESS_COARSE_LOCATION = 1000;

    RecyclerView mRecyclerView;

    BleDeviceAdapter mAdapter;

    BleDeviceManager mManager;

    ArrayList<BleDevice> mBleDeviceList = new ArrayList<>();

    Button mBtnScan;

    Button mBtnScheduleScan;

    boolean mIsPermissionGranted;

    int latestPercent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        mManager = BleDeviceManager.getInstance(this);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        mAdapter = new BleDeviceAdapter(mBleDeviceList, this);
        mRecyclerView.setAdapter(mAdapter);

        mBtnScan = (Button) findViewById(R.id.btn_scan);
        mBtnScan.setOnClickListener(this);
        mBtnScheduleScan = (Button) findViewById(R.id.btn_scheduled_scan);
        mBtnScheduleScan.setOnClickListener(this);

        checkPermission();
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED  &&
                    checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                mIsPermissionGranted = false;
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                } else {
                    // No explanation needed, we can request the permission.
                    String[] permissionArray = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION};
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        permissionArray = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION};
                    }
                    ActivityCompat.requestPermissions(this, permissionArray, REQUEST_ACCESS_COARSE_LOCATION);
                }
            } else {
                mIsPermissionGranted = true;
            }
        } else {
            mIsPermissionGranted = true;
        }
    }

    @Override
    public void onServiceBound() {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_ACCESS_COARSE_LOCATION:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mIsPermissionGranted = true;
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onBleDeviceScanned(BleDevice device) {
        int index = mBleDeviceList.indexOf(device);
        if (index >= 0) {
            BleDevice oldDevice = mBleDeviceList.get(index);
            oldDevice.rssi = device.rssi;
            mAdapter.notifyItemChanged(index);
        } else {
            if (device.rssi >= -65) {
                mBleDeviceList.add(device);
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mManager.addScanResultCallback(this);
        mManager.bind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        DfuServiceListenerHelper.registerProgressListener(this, mDfuProgressListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        DfuServiceListenerHelper.unregisterProgressListener(this, mDfuProgressListener);
    }

    @Override
    protected void onStop() {
        mManager.removeScanResultCallback(this);
        mManager.unbind(this);
        super.onStop();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_scan:
                if (mBtnScan.getText().toString().equals("start scan")) {
                    if (mIsPermissionGranted) {
                        mBleDeviceList.clear();
                        mAdapter.notifyDataSetChanged();
                        mBtnScan.setText("stop scan");
                        startScan();
                    } else {
                        Toast.makeText(this, "permission not granted", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    mManager.stopScan();
                    mBtnScan.setText("start scan");
                }
                break;
            case R.id.btn_scheduled_scan:
                if (mBtnScheduleScan.getText().toString().equals("scheduled scan")) {
                    mBtnScheduleScan.setText("stop schedule");
                    mBleDeviceList.clear();
                    mAdapter.notifyDataSetChanged();
                    if (mIsPermissionGranted) {
                        startScheduleScan();
                    } else {
                        Toast.makeText(this, "permission not granted", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    mManager.stopScheduledScan();
                    mBtnScheduleScan.setText("scheduled scan");
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemClick(int position) {
        BleDevice device = mBleDeviceList.get(position);
        if (device != null) {
            if (device.name.startsWith("DfuTarg")) {
                performDFUUpload(device.address);
            } else {
                Intent intent = new Intent(this, ConnectActivity.class);
                intent.putExtra("device", device);
                startActivityForResult(intent, 1);
            }
        }
    }

    private void startScan() {
        try {
            if (mManager.checkBluetoothAvailability(this)) {
                mManager.startScan();
                if (mBtnScheduleScan != null) {
                    mBtnScheduleScan.setText("scheduled scan");
                }
            } else {
                Toast.makeText(this, "bluetooth not open", Toast.LENGTH_SHORT).show();
            }
        } catch (BleNotAvailableException e) {
            Toast.makeText(this, "bluetooth le not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void startScheduleScan() {
        try {
            if (mManager.checkBluetoothAvailability(this)) {
                mManager.startScheduledScan(12 * 1000, 4 * 1000);
                if (mBtnScan != null) {
                    mBtnScan.setText("start scan");
                }
            } else {
                Toast.makeText(this, "bluetooth not open", Toast.LENGTH_SHORT).show();
            }
        } catch (BleNotAvailableException e) {
            Toast.makeText(this, "bluetooth le not available", Toast.LENGTH_SHORT).show();
        }
    }

    public static class BleDeviceAdapter extends RecyclerView.Adapter<BleDeviceAdapter.ViewHolder> {

        static class ViewHolder extends RecyclerView.ViewHolder {

            TextView mTvName;
            TextView mTvAddress;
            TextView mTvRssi;

            ViewHolder(View view) {
                super(view);
                mTvName = view.findViewById(R.id.tv_name);
                mTvAddress = view.findViewById(R.id.tv_address);
                mTvRssi = view.findViewById(R.id.tv_rssi);
            }
        }

        OnItemClickListener itemClickListener;

        ArrayList<BleDevice> mBleDeviceList;

        BleDeviceAdapter(ArrayList<BleDevice> deviceArrayList, OnItemClickListener listener) {
            mBleDeviceList = deviceArrayList;
            itemClickListener = listener;
        }

        @Override
        public void onBindViewHolder(final BleDeviceAdapter.ViewHolder holder, int position) {
            BleDevice device = mBleDeviceList.get(position);
            holder.mTvName.setText(device.name);
            holder.mTvAddress.setText(device.address);
            holder.mTvRssi.setText(device.rssi + "");
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemClickListener.onItemClick(holder.getAdapterPosition());
                }
            });
        }

        @Override
        public BleDeviceAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_device_item, parent, false));
        }

        @Override
        public int getItemCount() {
            return mBleDeviceList == null ? 0 : mBleDeviceList.size();
        }
    }

    @Override
    public void onBackPressed() {
        mManager.stopScan();
        super.onBackPressed();
    }

    /**************************************************************************
     * Device DFU Handle functions start
     *************************************************************************/

    public void performDFUUpload(String macAddress) {
        //Nordic's DFU firmware code, for DFU mode, it will +1 MacAddress, so add +1 to the existing Mac address
        final DfuServiceInitiator starter = new DfuServiceInitiator(macAddress)
                .setDisableNotification(true)
                .setForeground(false)
                .setKeepBond(true);

        starter.setZip(R.raw.dfu_fw_114);
        starter.start(this, DfuService.class);
        LoadingDialogFragment.show(this);
    }

    private void toastOnMainLooper(String text, long delayMillis) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();
            }
        }, delayMillis);
    }

    private final DfuProgressListener mDfuProgressListener = new DfuProgressListenerAdapter() {
        @Override
        public void onDeviceConnecting(@NonNull String deviceAddress) {
            Log.i(TAG, "DFU Device Connecting!");
            toastOnMainLooper("DFU Device Connecting!", 200);
        }
        @Override
        public void onDfuProcessStarting(@NonNull String deviceAddress) {
            runOnUiThread(() -> LoadingDialogFragment.hide(MainActivity.this));
            Log.i(TAG, "DFU Upload starting!");
            toastOnMainLooper("DFU Upload starting!", 200);
        }
        @Override
        public void onEnablingDfuMode(@NonNull String deviceAddress) { }
        @Override
        public void onFirmwareValidating(@NonNull String deviceAddress) { }
        @Override
        public void onDeviceDisconnecting(@NonNull String deviceAddress) { }
        @Override
        public void onDfuCompleted(@NonNull String deviceAddress) {
            toastOnMainLooper("DFU Upload completed!", 200);
        }
        @Override
        public void onDfuAborted(@NonNull String deviceAddress) { }
        @Override
        public void onProgressChanged(@NonNull String deviceAddress, final int percent, final float speed, final float avgSpeed, final int currentPart, final int partsTotal) {
            Log.i(TAG, "upload progress changed " + percent + "%");
            int curPercent = percent / 10;
            if (curPercent > latestPercent) {
                latestPercent = curPercent;
                toastOnMainLooper("upload progress changed " + percent + "%", 200);
            }

        }
        @Override
        public void onError(@NonNull String deviceAddress, final int error, final int errorType, final String message) {
            Log.i(TAG, "upload error error code " + error + " error type " + errorType + " message " + message);
        }
    };
    /**************************************************************************
     * Device DFU Handle functions end
     *************************************************************************/
}