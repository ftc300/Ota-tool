package inshow.carl.com.csd.tools;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @ 创建者:   CoderChen
 * @ 时间:     2018/6/4
 * @ 描述:
 */


public class L {
    public static void d(String msg){
        Log.d("inshow-ota-tag",msg);
        try {
            writeLogDataToFile(msg,Utils.getLogFilePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public  static void writeLogDataToFile(String content,String file) throws IOException {
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true)));
            out.write(getNowTimeString()+"\t\t\t\t"+content+"\r\n");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getNowTimeString() {
        Date date = new Date();
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return format.format(date);
    }
}
