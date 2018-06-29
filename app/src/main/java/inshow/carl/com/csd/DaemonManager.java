package inshow.carl.com.csd;

import android.os.Handler;
import android.util.SparseArray;

import inshow.carl.com.csd.tools.L;

import static inshow.carl.com.csd.DaemonManager.ProcessEnums.DFU_PROCESSING;
import static inshow.carl.com.csd.DaemonManager.ProcessEnums.START_DFU;

/**
 * @ 创建者:   CoderChen
 * @ 时间:     2018/6/1
 * @ 描述:
 */


public class DaemonManager {
    private static final int INTERVAL = 3 *60 * 1000 ;//3分钟后检查
    private int currentKey;
    private String TAG = "DaemonManager";
    SparseArray<Integer> array = new SparseArray<>();
    IDaemonProcess listener;

    public DaemonManager(IDaemonProcess listener) {
        this.listener = listener;
    }

    public static class ProcessEnums {
        public static final int START_DFU = 0;
        public static final int DFU_PROCESSING = 1;
    }

    public void setCurrentKey(int currentKey) {
        this.currentKey = currentKey;
        array.put(currentKey,START_DFU);
    }

    public void notifyStateChange(SparseArray array) {
        this.array = array;
    }

    public void start() {
        new Handler().postDelayed(new DaemonRunnable(currentKey), INTERVAL);
    }

    private class DaemonRunnable implements Runnable {
        int currentPos;

        public DaemonRunnable(int currentPos) {
            this.currentPos = currentPos;
        }

        @Override
        public void run() {
            try {
                L.d( "array.get(currentKey):" + array.get(currentKey) + ",STARTDFU:" + DFU_PROCESSING);
                if (array.get(currentPos) != DFU_PROCESSING) {
                   L.d( "run: DaemonRunnable onFail");
                    listener.onFail(currentPos);
                } else {
                   L.d( "not run: DaemonRunnable onFail");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    interface IDaemonProcess {
        void onFail(int pos);
    }
}
