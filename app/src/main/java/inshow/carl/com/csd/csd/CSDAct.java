package inshow.carl.com.csd.csd;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.allenliu.badgeview.BadgeFactory;
import com.allenliu.badgeview.BadgeView;
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import butterknife.ButterKnife;
import butterknife.InjectView;
import inshow.carl.com.csd.BasicAct;
import inshow.carl.com.csd.R;
import inshow.carl.com.csd.csd.core.BleManager;
import inshow.carl.com.csd.csd.core.CsdMgr;
import inshow.carl.com.csd.csd.iface.ICheckDeviceComplete;
import inshow.carl.com.csd.csd.iface.ICheckDevicePressed;
import inshow.carl.com.csd.entity.MiWatch;
import inshow.carl.com.csd.tools.L;
import inshow.carl.com.csd.view.radar.RadarScanView;
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanResult;

import static com.inuker.bluetooth.library.Constants.STATUS_CONNECTED;
import static com.inuker.bluetooth.library.Constants.STATUS_DISCONNECTED;
import static inshow.carl.com.csd.csd.core.CsdConstant.CHARACTERISTIC_CONTROL;
import static inshow.carl.com.csd.csd.core.CsdConstant.IN_SHOW_SERVICE;
import static inshow.carl.com.csd.csd.core.CsdMgr.startScan;

/**
 * Created by chendong on 2018/6/25.
 */

public class CSDAct extends BasicAct {
    @InjectView(R.id.img)
    TextView img;
    @InjectView(R.id.tip)
    TextView tip;
    BadgeView badgeView;
    @InjectView(R.id.radarScanView)
    RadarScanView radarScanView;
    final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
    private BleManager bleInstance = BleManager.getInstance();
    long lastTs;
    final ScanCallback callback = new ScanCallback() {
        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            L.d("onScanResult" + result.toString());
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            CsdMgr.getInstance().checkDevice(results);
        }
    };
    private final BleConnectStatusListener mBleConnectStatusListener = new BleConnectStatusListener() {

        @Override
        public void onConnectStatusChanged(String mac, int status) {
            if (status == STATUS_CONNECTED) {
                bleInstance.writeCharacteristic(mac, UUID.fromString(IN_SHOW_SERVICE), UUID.fromString(CHARACTERISTIC_CONTROL), new byte[]{3, 1, 0, 0});
                Intent i = new Intent(context, TestWatchAct.class);
                i.putExtra("MAC", mac);
                startActivity(i);
            } else if (status == STATUS_DISCONNECTED) {
                bleInstance.unRegister(mac, mBleConnectStatusListener);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_csd);
        ButterKnife.inject(this);

        CsdMgr.getInstance().setCheckFinished(new ICheckDeviceComplete() {
            @Override
            public void checkFinished(HashSet<MiWatch> set) {
                int size = set.size();
                if (size > 0) {
                    if (badgeView == null) {
                        img.setVisibility(View.VISIBLE);
                        tip.setVisibility(View.VISIBLE);
                        badgeView = BadgeFactory.createOval(context).setBadgeCount(size).bind(img);
                    } else {
                        badgeView.setBadgeCount(size);
                    }
                }
            }
        });
//       CsdMgr.getInstance().setCheckDevicePressed(new ICheckDevicePressed() {
//           @Override
//           public void miWatchPressed(String mac) {
//               switchTo(PressedMiWatchAct.class);
//           }
//       });

        CsdMgr.getInstance().setCheckDevicePressed(new ICheckDevicePressed() {
            @Override
            public void miWatchPressed(String mac) {
                L.d("PressedMiWatchAct miWatchPressed mac:" + mac);
                if (System.currentTimeMillis() - lastTs > 20 * 1000) {
                    showToast("检测到按压表冠，连接中...");
                    lastTs = System.currentTimeMillis();
                    bleInstance.connect(mac);
                    bleInstance.register(mac, mBleConnectStatusListener);
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        radarScanView.restartTask();
        startScan(scanner, callback);
    }

    @Override
    protected void onPause() {
        super.onPause();
        radarScanView.pauseTask();
        scanner.stopScan(callback);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        scanner.stopScan(callback);
    }

}
