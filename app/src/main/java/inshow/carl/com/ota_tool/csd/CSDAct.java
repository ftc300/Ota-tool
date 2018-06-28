package inshow.carl.com.ota_tool.csd;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.allenliu.badgeview.BadgeFactory;
import com.allenliu.badgeview.BadgeView;

import java.util.HashSet;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import inshow.carl.com.ota_tool.BasicAct;
import inshow.carl.com.ota_tool.R;
import inshow.carl.com.ota_tool.entity.MiWatch;
import inshow.carl.com.ota_tool.tools.L;
import inshow.carl.com.ota_tool.view.radar.RadarScanView;
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanResult;

import static inshow.carl.com.ota_tool.csd.CsdMgr.startScan;

/**
 * Created by chendong on 2018/6/25.
 */

public class CSDAct extends BasicAct {
    @InjectView(R.id.img)
    TextView img;
    BadgeView badgeView;
    @InjectView(R.id.radarScanView)
    RadarScanView radarScanView;
    final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
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
            L.d("");
            CsdMgr.getInstance().checkDevice(results);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_service_agent);
        ButterKnife.inject(this);
        startScan(scanner, callback);
        CsdMgr.getInstance().setCheckFinished(new ICheckDeviceComplete() {
            @Override
            public void checkFinished(HashSet<MiWatch> set) {
                int size = set.size();
                if (size > 0) {
                    if (badgeView == null) {
                        badgeView = BadgeFactory.createOval(context).setBadgeCount(size).bind(img);
                    } else {
                        badgeView.setBadgeCount(size);
                    }
                }
            }
        });
       CsdMgr.getInstance().setCheckDevicePressed(new ICheckDevicePressed() {
           @Override
           public void miWatchPressed(String mac) {
               scanner.stopScan(callback);
           }
       });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        scanner.stopScan(callback);
    }

    @OnClick(R.id.img)
    public void onViewClicked() {
        switchTo(PressedMiWatchAct.class);
    }
}
