package inshow.carl.com.ota_tool;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.allenliu.badgeview.BadgeFactory;
import com.allenliu.badgeview.BadgeView;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import inshow.carl.com.ota_tool.csd.CsdMgr;
import inshow.carl.com.ota_tool.tools.L;
import inshow.carl.com.ota_tool.upgrade.BluetoothLeService;
import inshow.carl.com.ota_tool.view.radar.RadarScanView;
import inshow.carl.com.ota_tool.view.radar.RandomTextView;
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

import static inshow.carl.com.ota_tool.MainPagerHelper.makeGattUpdateIntentFilter;
import static inshow.carl.com.ota_tool.MainPagerHelper.startOTA;
import static inshow.carl.com.ota_tool.csd.CsdMgr.startScan;

/**
 * Created by chendong on 2018/6/25.
 */

public class CustomerServiceAgentAct extends BasicAct {
    @InjectView(R.id.img)
    TextView img;
    @InjectView(R.id.randomTextView)
    RandomTextView randomTextView;
    BadgeView badgeView;
    int watchCount;
    List<ScanResult> scanResults;
    @InjectView(R.id.radarScanView)
    RadarScanView radarScanView;
    private BluetoothLeService mBluetoothLeService;
    private String scanPressedMac;
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                finish();
            }
            if (!TextUtils.isEmpty(scanPressedMac)) {
                L.d("onServiceConnected : " + scanPressedMac);
                mBluetoothLeService.connect(scanPressedMac);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                L.d(scanPressedMac + " Connected");
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                L.d(scanPressedMac + " Disconnected");
                mBluetoothLeService.disconnect();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                L.d(scanPressedMac + " Services Discovered ");
                mBluetoothLeService.writeSecureCharacteristic();
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                L.d("Data Available");
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_service_agent);
        ButterKnife.inject(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        radarScanView.restartRun();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    @Override
    protected void onPause() {
        super.onPause();
        radarScanView.pauseRun();
    }

    final ScanCallback callback = new ScanCallback() {
        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            scanResults = results;
            watchCount = 0;
            scanPressedMac = CsdMgr.getInstance().getMiWatchPressedMac(results);
            if(!TextUtils.isEmpty(scanPressedMac)){
                L.d("getMiWatchPressedMac:" + scanPressedMac);
                radarScanView.pauseRun();
                if (null != mBluetoothLeService) {
                    L.d("mBluetoothLeService connect");
                    mBluetoothLeService.connect(scanPressedMac);
                    radarScanView.pauseRun();
                }
            }

            if (CsdMgr.getInstance().getMiWatchCount(results) > 0) {
                watchCount = CsdMgr.getInstance().getMiWatchCount(results);
                if (null == badgeView) {
                    badgeView = BadgeFactory.createCircle(context).setBadgeCount(watchCount).bind(img);
                }else {
                    badgeView.setBadgeCount(watchCount);
                }
            }
        }
    };

    @OnClick(R.id.more)
    public void onViewClicked() {
        startScan(callback);
    }



}
