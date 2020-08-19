package com.alan.bledemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.nutspace.nut.api.BleDeviceConsumer;
import com.nutspace.nut.api.BleDeviceManager;
import com.nutspace.nut.api.ble.exception.BleNotAvailableException;
import com.nutspace.nut.api.callback.ScanResultCallback;
import com.nutspace.nut.api.model.BleDevice;

import java.util.ArrayList;

/**
 * @author hanson
 */
public class MainActivity extends BaseActivity implements BleDeviceConsumer, ScanResultCallback, View.OnClickListener, OnItemClickListener {

    public static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1000;

    RecyclerView mRecyclerView;

    BleDeviceAdapter mAdapter;

    BleDeviceManager mManager;

    ArrayList<BleDevice> mBleDeviceList = new ArrayList<>();

    Button mBtnScan;

    boolean mIsPermissionGranted;

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

        checkPermission();
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                mIsPermissionGranted = false;
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION)) {

                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                            MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
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
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION:
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
        //Filter the nearest device
        if(device.rssi < -60) {
            return;
        }
        int index = mBleDeviceList.indexOf(device);
        if (index >= 0) {
            BleDevice oldDevice = mBleDeviceList.get(index);
            oldDevice.rssi = device.rssi;
            mAdapter.notifyItemChanged(index);
        } else {
            mBleDeviceList.add(device);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mManager.addScanResultCallback(this);
        mManager.bind(this);
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
                    mBtnScan.setText("stop scan");
                    if (mIsPermissionGranted) {
                        startScan();
                    } else {
                        Toast.makeText(this, "permission not granted", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    mManager.stopScan();
                    mBtnScan.setText("start scan");
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemClick(int position) {
        BleDevice device = mBleDeviceList.get(position);
        Intent intent = new Intent(this, ConnectActivity.class);
        intent.putExtra("device", device);
        startActivityForResult(intent, 1);
    }

    private void startScan() {
        try {
            if (mManager.checkBluetoothAvailability(this)) {
                mManager.startScan();
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
                mTvName = (TextView) view.findViewById(R.id.tv_name);
                mTvAddress = (TextView) view.findViewById(R.id.tv_address);
                mTvRssi = (TextView) view.findViewById(R.id.tv_rssi);
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
}