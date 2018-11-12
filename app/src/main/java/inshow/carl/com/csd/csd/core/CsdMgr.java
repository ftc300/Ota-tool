package inshow.carl.com.csd.csd.core;

import android.os.ParcelUuid;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import inshow.carl.com.csd.csd.iface.ICheckDeviceComplete;
import inshow.carl.com.csd.csd.iface.ICheckDevicePressed;
import inshow.carl.com.csd.entity.MiWatch;
import inshow.carl.com.csd.tools.L;
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanRecord;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

import static inshow.carl.com.csd.csd.core.ConvertDataMgr.bytes2HexString;

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
    ConcurrentHashMap<String ,Integer> hashMap = new ConcurrentHashMap<>();
    int count = -1;
    String cMac;
    private CsdMgr() {
    }

    public static CsdMgr getInstance() {
        if (mInstance == null) {
            synchronized (CsdMgr.class) {
                if (mInstance == null) {
                    mInstance = new CsdMgr();
                }
            }
        }
        return mInstance;
    }

    public void setCheckFinished(ICheckDeviceComplete checkFinished) {
        this.checkFinished = checkFinished;
    }

    private HashSet<MiWatch> set = new HashSet<>();

    public void setCheckDevicePressed(ICheckDevicePressed checkDevicePressed) {
        this.checkDevicePressed = checkDevicePressed;
    }

    public synchronized void checkDevice(List<ScanResult> results) {
        for (ScanResult result : results) {
            try {
                cMac = result.getDevice().getAddress();
                ScanRecord record = result.getScanRecord();
                Map<ParcelUuid, byte[]> map = record.getServiceData();
                Iterator<Map.Entry<ParcelUuid, byte[]>> it = map.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<ParcelUuid, byte[]> entry = it.next();
                    ParcelUuid key = entry.getKey();
                    byte[] b = map.get(key);
                    //有米家的service uuid 并且有手表的产品id : AC01
                    if (isMiWatch(key, b)) {
//                        L.d("isMiWatch:"+ bytes2HexString(b));
                        if (isMiWatchNormal(b)) {
//                            L.d("MiWatchNormal:" + result.getDevice().getAddress() + ",isMiWatchNormal"+ bytes2HexString(b));
                            set.add(new MiWatch(cMac, false));
                        } else if (isMiWatchPressed(b)) {
                            L.d("isMiWatchPressed:" +cMac+","+key.toString() + ",b:"+ bytes2HexString(b));
                            try {
                                count = hashMap.get(cMac);
                            }catch (Exception e){
                                count = -1;
                            }
                            hashMap.put(cMac,count+1);
                            boolean flag = set.add(new MiWatch(cMac, true));
                            if (!flag) {
                                for (MiWatch watch : set) {
                                    if(TextUtils.equals(watch.mac,cMac)){
                                        watch.pressed = true;
                                    }
                                }
                            }
//                            L.d("MiWatchPressed:" + cMac);
                            if (checkDevicePressed != null&& hashMap.get(cMac)>1) {
                                hashMap.put(cMac,-1);
                                count = -1;
                                checkDevicePressed.miWatchPressed(cMac);
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
