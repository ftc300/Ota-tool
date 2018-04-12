package inshow.carl.com.ota_tool.tools;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;

import inshow.carl.com.ota_tool.entity.DeviceEntity;
import no.nordicsemi.android.dfu.internal.scanner.BootloaderScanner;

import static inshow.carl.com.ota_tool.tools.Const.STATE_SUCCESS;

/**
 * @ 创建者:   CoderChen
 * @ 时间:     2018/4/4
 * @ 描述:
 */


public class Utils {
    /**
     * dfu 升级 mac地址加1
     *
     * @param deviceAddress
     * @return
     */
    public static String getIncrementedAddress(final String deviceAddress) {
        final String firstBytes = deviceAddress.substring(0, 15);
        final String lastByte = deviceAddress.substring(15); // assuming that the device address is correct
        final String lastByteIncremented = String.format("%02X", (Integer.valueOf(lastByte, 16) + BootloaderScanner.ADDRESS_DIFF) & 0xFF);
        return firstBytes + lastByteIncremented;
    }

    public static String getPath(Context context, Uri uri) throws URISyntaxException {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {"_data"};
            Cursor cursor = null;

            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                // Eat it
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    // 705896002EC2
    public static String formateMac(String content) {
        StringBuffer result = new StringBuffer(content);
        try {
            for (int index = 2; index < result.length(); index += 3) {
                result.insert(index, ':');
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    //    16023/213G0460-705896002EC2
    public static String getDisplayMac(String content) {
        StringBuffer result = new StringBuffer();
        try {
            String[] temp = content.split("-");
            result = new StringBuffer(temp[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    public static void checkBleAdapter(final Context context) {
        BluetoothAdapter blueadapter = BluetoothAdapter.getDefaultAdapter();
        //支持蓝牙模块
        if (blueadapter != null) {
            if (blueadapter.isEnabled()) {
            } else {
                new AlertDialog.Builder(context).setTitle("蓝牙功能尚未打开，是否打开蓝牙")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (turnOnBluetooth()) {
                                    Toast tst = Toast.makeText(context, "打开蓝牙成功", Toast.LENGTH_SHORT);
                                    tst.show();
                                } else {
                                    Toast tst = Toast.makeText(context, "打开蓝牙失败！！", Toast.LENGTH_SHORT);
                                    tst.show();
                                }
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 点击“返回”后的操作,这里不设置没有任何操作
                            }
                        }).show();
            }
        } else {//不支持蓝牙模块
            Toast tst = Toast.makeText(context, "该设备不支持蓝牙或没有蓝牙模块", Toast.LENGTH_SHORT);
            tst.show();
        }
    }

    /**
     * 强制开启当前 Android 设备的 Bluetooth
     *
     * @return true：强制打开 Bluetooth　成功　false：强制打开 Bluetooth 失败
     */
    public static boolean turnOnBluetooth() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter
                .getDefaultAdapter();

        if (bluetoothAdapter != null) {
            return bluetoothAdapter.enable();
        }

        return false;
    }

    public static void writeData2SD(DeviceEntity de) {
        File sdcard = Environment.getExternalStorageDirectory();
        File dir = new File(sdcard.getAbsolutePath() + "/0-inshow-ota/");
        if (!dir.exists()) {
            dir.mkdir();
        }
        File file = new File(dir, "upgrade-log.txt");
        try {
            FileOutputStream os = new FileOutputStream(file,true);
            os.write(getFormatLog(de).getBytes());
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static String getFormatLog(DeviceEntity e) {
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return  sdf.format(d)  + "  " +
                e.getTrueMac() + "  " +
                (e.state == STATE_SUCCESS ? "Success  " : "Fail  ") +
                (e.state == STATE_SUCCESS ?  e.filePath : "") +
                " \n";
    }

    public static void showExitD(final Context c) {
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(c)
                        .setMessage("确定退出吗？").setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ((Activity)c).finish();
                            }
                        })
                        .setNegativeButton("取消",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                });
        normalDialog.show();
    }


}
