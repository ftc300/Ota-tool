package inshow.carl.com.ota_tool.csd;

import android.os.ParcelUuid;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import inshow.carl.com.ota_tool.entity.MiWatch;
import inshow.carl.com.ota_tool.tools.L;
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanRecord;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

/**
 * Created by chendong on 2018/6/25.
 * 客服
 * customer service department
 */

public class CsdMgr {

    public static final String MI_SERVICE_UUID = "0000fe95-0000-1000-8000-00805f9b34fb";
    private static volatile CsdMgr mInstance;
    private ICheckDevicePressed checkDevicePressed;
    private ICheckDeviceComplete checkFinished;

    public void setCheckFinished(ICheckDeviceComplete checkFinished) {
        this.checkFinished = checkFinished;
    }

    private HashSet<MiWatch> set = new HashSet<>();

    public void setCheckDevicePressed(ICheckDevicePressed checkDevicePressed) {
        this.checkDevicePressed = checkDevicePressed;
    }

    private CsdMgr() {
    }

    public static CsdMgr getInstance() {
        if (mInstance == null) {
            mInstance = new CsdMgr();
        }
        return mInstance;
    }


    public HashSet<MiWatch> getMiWatchSet() {
        return set;
    }

    public void checkDevice(List<ScanResult> results) {
        for (ScanResult result : results) {
            try {
                ScanRecord record = result.getScanRecord();
                Map<ParcelUuid, byte[]> map = record.getServiceData();
                Iterator<Map.Entry<ParcelUuid, byte[]>> it = map.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<ParcelUuid, byte[]> entry = it.next();
                    ParcelUuid key = entry.getKey();
                    byte[] b = map.get(key);
                    //有米家的service uuid 并且有手表的产品id : AC01
                    if (isMiWatch(key, b)) {
                        if (isMiWatchNormal(b)) {
                            L.d("MiWatchNormal:" + result.getDevice().getAddress());
                            set.add(new MiWatch(result.getDevice().getAddress(), false));
                        } else if (isMiWatchPressed(b)) {
                            boolean flag = set.add(new MiWatch(result.getDevice().getAddress(), true));
                            if (!flag) {
                                for (MiWatch watch : set) {
                                    if(TextUtils.equals(watch.mac,result.getDevice().getAddress())){
                                        watch.pressed = true;
                                    }
                                }
                            }
                            L.d("MiWatchPressed:" + result.getDevice().getAddress());
                            if (checkDevicePressed != null) {
                                checkDevicePressed.miWatchPressed(result.getDevice().getAddress());
                            }
                        }
                    }
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
        if (checkFinished != null) {
            checkFinished.checkFinished(set);
        }
    }

    private boolean isMiWatch(ParcelUuid key, byte[] b) {
        return TextUtils.equals(key.toString(), MI_SERVICE_UUID) && (b[2] == -84 && b[3] == 1);
    }

    private boolean isMiWatchNormal(byte[] b) {
        return (b[0] == 48 && b[1] == 48);
    }

    private boolean isMiWatchPressed(byte[] b) {
        return (b[0] == 48 && b[1] == 50);
    }

    public static void startScan(BluetoothLeScannerCompat scanner, ScanCallback callback) {
        final ScanSettings settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).setReportDelay(1000).setUseHardwareBatchingIfSupported(false).build();
        final List<ScanFilter> filters = new ArrayList<>();
        filters.add(new ScanFilter.Builder().setServiceUuid(null).build());
        scanner.startScan(filters, settings, callback);

    }

}
