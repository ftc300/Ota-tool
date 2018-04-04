package inshow.carl.com.ota_tool.tools;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import java.net.URISyntaxException;

import no.nordicsemi.android.dfu.internal.scanner.BootloaderScanner;

/**
 * @ 创建者:   CoderChen
 * @ 时间:     2018/4/4
 * @ 描述:
 */


public class Utils {
    /**
     * dfu 升级 mac地址加1
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
            String[] projection = { "_data" };
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
        }
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    //    16023/213G0460-705896002EC2
    public static String getScanMac(String content) {
        StringBuffer  result = new StringBuffer();
        try {
            String[] temp = content.split("-");
            result = new StringBuffer(temp[1]);
            for (int index = 2; index < result.length(); index += 3) {
                result.insert(index, ':');
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString();
    }
}
