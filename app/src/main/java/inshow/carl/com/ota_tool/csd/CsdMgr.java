package inshow.carl.com.ota_tool.csd;

import android.content.IntentFilter;
import android.os.ParcelUuid;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import inshow.carl.com.ota_tool.tools.L;
import inshow.carl.com.ota_tool.upgrade.BluetoothLeService;
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
    private List<ScanResult> olderResults;
    private HashMap<ScanResult, Boolean> map = new HashMap<>();

    private CsdMgr() {
    }

    public static CsdMgr getInstance() {
        if (mInstance == null) {
            mInstance = new CsdMgr();
        }
        return mInstance;
    }

    //
//[48, 50, -84, 1, -5, -96, 60, 0, -106, 88, 112, 13]
// [30, 32, AC, 01, FB, 60, 3C,,00, 96, 58, 70 ,0D ]
    public boolean isMiWatch(ScanResult result) {
        try {
            ScanRecord record = result.getScanRecord();
            Map<ParcelUuid, byte[]> map = record.getServiceData();
            if (map == null) throw new NullPointerException();
            Iterator<Map.Entry<ParcelUuid, byte[]>> it = map.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<ParcelUuid, byte[]> entry = it.next();
                ParcelUuid key = entry.getKey();
                byte[] b = map.get(key);
                //有米家的service uuid 并且有手表的产品id : AC01
//                L.d("TextUtils.equals(key.toString(),MI_SERVICE_UUID ):" + TextUtils.equals(key.toString(), MI_SERVICE_UUID));
//                L.d("(b[2] == -84 && b[3] == 1):" + (b[2] == -84 && b[3] == 1));
                if (TextUtils.equals(key.toString(), MI_SERVICE_UUID) && (b[2] == -84 && b[3] == 1))
                    return true;
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public String getMiWatchPressedMac(List<ScanResult> results) {
        for (ScanResult result: results) {
            try {
                ScanRecord record = result.getScanRecord();
                Map<ParcelUuid, byte[]> map = record.getServiceData();
                Iterator<Map.Entry<ParcelUuid, byte[]>> it = map.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<ParcelUuid, byte[]> entry = it.next();
                    ParcelUuid key = entry.getKey();
                    byte[] b = map.get(key);
                    //有米家的service uuid 并且有手表的产品id : AC01
                    if (TextUtils.equals(key.toString(), MI_SERVICE_UUID) && (b[2] == -84 && b[3] == 1) && (b[0] == 48 && b[1] == 50))
                        return result.getDevice().getAddress();
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
                return "";
            }
        }
        return "";
    }


    public static void startScan(ScanCallback callback) {
        final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
        final ScanSettings settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).setReportDelay(1000).setUseHardwareBatchingIfSupported(false).build();
        final List<ScanFilter> filters = new ArrayList<>();
        filters.add(new ScanFilter.Builder().setServiceUuid(null).build());
        scanner.startScan(filters, settings, callback);
    }



    public int getMiWatchCount(List<ScanResult> results) {
        int ret = 0;
        for (ScanResult item : results) {
            if (isMiWatch(item)) {
                ret += 1;
            }
        }
        return ret;
    }



}
