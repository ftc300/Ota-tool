package inshow.carl.com.ota_tool;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import inshow.carl.com.ota_tool.entity.DeviceEntity;
import inshow.carl.com.ota_tool.entity.FileEntity;
import inshow.carl.com.ota_tool.upgrade.BluetoothLeService;
import inshow.carl.com.ota_tool.upgrade.DfuService;
import no.nordicsemi.android.dfu.DfuProgressListener;
import no.nordicsemi.android.dfu.DfuProgressListenerAdapter;
import no.nordicsemi.android.dfu.DfuServiceInitiator;
import no.nordicsemi.android.dfu.DfuServiceListenerHelper;
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

import static inshow.carl.com.ota_tool.tools.Utils.getIncrementedAddress;

/**
 * @ 创建者:   CoderChen
 * @ 时间:     2018/4/4
 * @ 描述:
 */


public class DfuAct extends BasicAct {

    @InjectView(R.id.textviewUploading)
    TextView mTextUploading;
    @InjectView(R.id.progressbar_file)
    ProgressBar mProgressBar;
    @InjectView(R.id.textviewProgress)
    TextView mTextPercentage;
    @InjectView(R.id.current_device)
    TextView currentDevice;
    @InjectView(R.id.dfu_pane)
    LinearLayout dfuPane;
    @InjectView(R.id.img_back)
    ImageView imgBack;
    private BluetoothLeService mBluetoothLeService;
    private static final String TAG = "DfuAct";
    //    private String selectMac =  "70:58:96:00:3C:A0";
    private String selectMac;
    private final int S = 1;
    private final int F = 2;


    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            if (!TextUtils.isEmpty(selectMac))
                mBluetoothLeService.connect(selectMac);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mTextPercentage.setText("Connected");
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mTextPercentage.setText("Disconnected");
                invalidateOptionsMenu();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startOTA();
                    }
                }, 3000);
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                mTextPercentage.setText("Discover Dfu Service");
                mBluetoothLeService.writeCharacteristic();
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
            }
        }
    };

    private final DfuProgressListener mDfuProgressListener = new DfuProgressListenerAdapter() {
        private int mPercent;
        @Override
        public void onDeviceConnecting(final String deviceAddress) {
            mProgressBar.setIndeterminate(true);
            mTextPercentage.setText(R.string.dfu_status_connecting);
        }

        @Override
        public void onDfuProcessStarting(final String deviceAddress) {
            mProgressBar.setIndeterminate(true);
            mTextPercentage.setText(R.string.dfu_status_starting);
        }

        @Override
        public void onEnablingDfuMode(final String deviceAddress) {
            mProgressBar.setIndeterminate(true);
            mTextPercentage.setText(R.string.dfu_status_switching_to_dfu);
        }

        @Override
        public void onFirmwareValidating(final String deviceAddress) {
            mProgressBar.setIndeterminate(true);
            mTextPercentage.setText(R.string.dfu_status_validating);
        }

        @Override
        public void onDeviceDisconnecting(final String deviceAddress) {
            if(mPercent < 100){
                updateDeviceState(F);
            }
            mProgressBar.setIndeterminate(true);
            mTextPercentage.setText(R.string.dfu_status_disconnecting);
        }

        @Override
        public void onDfuCompleted(final String deviceAddress) {
            mTextPercentage.setText(R.string.dfu_status_completed);
            mProgressBar.setIndeterminate(false);
            updateDeviceState(S);
            // let's wait a bit until we cancel the notification. When canceled immediately it will be recreated by service again.
            showToast("升级成功，请返回重新扫描升级下一个设备！");
        }

        @Override
        public void onDfuAborted(final String deviceAddress) {
            mTextPercentage.setText(R.string.dfu_status_aborted);
        }

        @Override
        public void onProgressChanged(final String deviceAddress, final int percent, final float speed, final float avgSpeed, final int currentPart, final int partsTotal) {
            mPercent = percent;
            mProgressBar.setIndeterminate(false);
            mProgressBar.setProgress(percent);
            mTextPercentage.setText(getString(R.string.dfu_uploading_percentage, percent));
            if (partsTotal > 1)
                mTextUploading.setText(getString(R.string.dfu_status_uploading_part, currentPart, partsTotal));
            else
                mTextUploading.setText(R.string.dfu_status_uploading);
        }

        @Override
        public void onError(final String deviceAddress, final int error, final int errorType, final String message) {
            updateDeviceState(F);
            showErrorMessage(message);
        }

    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feature_dfu);
        ButterKnife.inject(this);
        loadDevice();
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        DfuServiceListenerHelper.registerProgressListener(this, mDfuProgressListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
        DfuServiceListenerHelper.unregisterProgressListener(this, mDfuProgressListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }


    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    private void showErrorMessage(String message) {
        showToast("Upload failed: " + message);
    }


    //    /storage/emulated/0/000/Mi_Watch_APP_OTA_V1.0.4_20c.zip
    private void startOTA() {
        FileEntity f = FileEntity.last(FileEntity.class);
        if (null != f) {
            final DfuServiceInitiator starter = new DfuServiceInitiator(getIncrementedAddress(selectMac))
                    .setDisableNotification(true)//need't Notification Act
                    .setKeepBond(false)
                    .setDeviceName("DFU")
                    .setZip(f.filePath);
            starter.start(this, DfuService.class);
        }
    }

    private void startScan() {
        final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
        final ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).setReportDelay(1000).setUseHardwareBatchingIfSupported(false).build();
        final List<ScanFilter> filters = new ArrayList<>();
        filters.add(new ScanFilter.Builder().setServiceUuid(null).build());
        scanner.startScan(filters, settings, scanCallback);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //stop
                final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
                scanner.stopScan(scanCallback);
            }
        }, 5000);
    }


    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(final int callbackType, final ScanResult result) {
            // do nothing
        }

        @Override
        public void onBatchScanResults(final List<ScanResult> results) {
            for (ScanResult item : results) {
                if (item.getDevice().getAddress().toUpperCase().equals(selectMac)) {
                    Log.i(TAG, "item.getDevice().getAddress().toUpperCase().equals(selectMac)");
                }
            }
            if (null != mBluetoothLeService)
                mBluetoothLeService.connect(selectMac);
        }

        @Override
        public void onScanFailed(final int errorCode) {
            // should never be called
        }
    };

    private void loadDevice() {
        DeviceEntity entity = DeviceEntity.last(DeviceEntity.class);
        selectMac = entity.mac;
        currentDevice.setText(selectMac);
        startScan();
    }

    private void updateDeviceState(int state){
        DeviceEntity entity = DeviceEntity.last(DeviceEntity.class);
        entity.state = state;
        entity.update();
    }

}
