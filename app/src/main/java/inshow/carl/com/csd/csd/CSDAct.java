package inshow.carl.com.csd.csd;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.allenliu.badgeview.BadgeFactory;
import com.allenliu.badgeview.BadgeView;
import com.android.tu.loadingdialog.LoadingDailog;
import com.inso.watch.update_lib.update.UpdateChecker;
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import butterknife.ButterKnife;
import butterknife.InjectView;
import inshow.carl.com.csd.BasicAct;
import inshow.carl.com.csd.R;
import inshow.carl.com.csd.csd.core.BleManager;
import inshow.carl.com.csd.csd.core.CsdMgr;
import inshow.carl.com.csd.csd.core.HttpUtils;
import inshow.carl.com.csd.csd.iface.ICheckDeviceComplete;
import inshow.carl.com.csd.csd.iface.ICheckDevicePressed;
import inshow.carl.com.csd.csd.silentcamera.CapPhotoService;
import inshow.carl.com.csd.entity.MiWatch;
import inshow.carl.com.csd.tools.L;
import inshow.carl.com.csd.view.radar.RadarScanView;
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanResult;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.WRITE_SECURE_SETTINGS;
import static com.inuker.bluetooth.library.Constants.STATUS_CONNECTED;
import static com.inuker.bluetooth.library.Constants.STATUS_DISCONNECTED;
import static inshow.carl.com.csd.AppController.hasCheckVersion;
import static inshow.carl.com.csd.csd.core.CsdConstant.CHARACTERISTIC_3102;
import static inshow.carl.com.csd.csd.core.CsdConstant.SERVICE_INSO;
import static inshow.carl.com.csd.csd.core.CsdMgr.startScan;
import static inshow.carl.com.csd.tools.Const.PERMISSION_REQ;

/**
 * Created by chendong on 2018/6/25.
 */

public class CSDAct extends BasicAct {
    @InjectView(R.id.img)
    ImageView img;
    @InjectView(R.id.tip)
    TextView tip;
    BadgeView badgeView;
    @InjectView(R.id.radarScanView)
    RadarScanView radarScanView;
    final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
    private BleManager bleInstance = BleManager.getInstance();
    long lastTs;
    LoadingDailog dialog;
    Intent service;
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
                if(null!=dialog){
                    dialog.dismiss();
                }
                bleInstance.writeCharacteristic(mac, UUID.fromString(SERVICE_INSO), UUID.fromString(CHARACTERISTIC_3102), new byte[]{3, 1, 0, 0});
                Intent i = new Intent(context, TestWatchAct.class);
                i.putExtra("MAC", mac);
                startActivity(i);
                finish();
            } else if (status == STATUS_DISCONNECTED) {
                if(null!=dialog){
                    dialog.dismiss();
                }
                bleInstance.unRegister(mac, mBleConnectStatusListener);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_csd);
        ButterKnife.inject(this);
        if(isNetworkAvailable()) {
            if(!hasCheckVersion) {
                UpdateChecker.checkForDialog(this);
                hasCheckVersion = true;
            }
            AndPermission.with(this)
                    .runtime()
                    .permission(Permission.ACCESS_COARSE_LOCATION, Permission.WRITE_EXTERNAL_STORAGE)
                    .onGranted(new Action<List<String>>() {
                        @Override
                        public void onAction(List<String> data) {
                            start();
                        }
                    })
                    .onDenied(new Action<List<String>>() {
                        @Override
                        public void onAction(List<String> data) {
                            finish();
                        }
                    })
                    .start();
        } else {
            showToast(getString(R.string.net_error));
            finish();
        }

    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void start() {
        /////////
        Calendar cal = Calendar.getInstance();
        service = new Intent(getBaseContext(), CapPhotoService.class);
        cal.add(Calendar.SECOND, 15);
        //TAKE PHOTO EVERY 15 SECONDS
        PendingIntent pintent = PendingIntent.getService(this, 0, service, 0);
        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
                2000, pintent);
        startService(service);
        /////////
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

        CsdMgr.getInstance().setCheckDevicePressed(new ICheckDevicePressed() {
            @Override
            public void miWatchPressed(String mac) {
//                L.d("PressedMiWatchAct miWatchPressed mac:" + mac);
                try {
                    if (System.currentTimeMillis() - lastTs > 20 * 1000) {
                        showLoading(true);
                        lastTs = System.currentTimeMillis();
//                        bleInstance.disConnect(mac);
                        bleInstance.connect(mac);
                        bleInstance.register(mac, mBleConnectStatusListener);
                    }
                } catch (Exception arg_e) {
                    arg_e.printStackTrace();
                    showToast("connect error");
                }
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        radarScanView.restartTask();
        if (BleManager.getInstance().isBluetoothOpened()) {
            startScan(scanner, callback);
        } else {
            showAlertDialog(getString(R.string.tip), getString(R.string.pls_enable_ble), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    BleManager.getInstance().openBle();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (BleManager.getInstance().isBluetoothOpened()) {
                                startScan(scanner, callback);
                            }
                        }
                    }, 2000);
                }
            });
        }
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

    public void showLoading(boolean arg_b) {
        LoadingDailog.Builder loadBuilder = new LoadingDailog.Builder(this)
                .setMessage(getString(R.string.receive_press) )
                .setShowMessage(arg_b)
                .setCancelable(false)
                .setCancelOutside(false);
        dialog = loadBuilder.create();
        dialog.show();
    }

    public void showAlertDialog(String t, String m, DialogInterface.OnClickListener positive) {
        new AlertDialog.Builder(context)
                .setTitle(t)
                .setMessage(m)
                .setCancelable(false)
                .setPositiveButton(R.string.ok, positive)
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
