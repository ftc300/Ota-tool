package inshow.carl.com.ota_tool.csd;
import android.os.Bundle;
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import inshow.carl.com.ota_tool.BasicAct;
import inshow.carl.com.ota_tool.R;
import inshow.carl.com.ota_tool.tools.L;
import static com.inuker.bluetooth.library.Constants.STATUS_CONNECTED;
import static com.inuker.bluetooth.library.Constants.STATUS_DISCONNECTED;

/**
 * Created by chendong on 2018/6/25.
 */

public class PressedMiWatchAct extends BasicAct {
    ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private BleManager bleInstance = BleManager.getInstance();
    long lastTs;

    //TODO : 延时几秒看看是否断开
    private final BleConnectStatusListener mBleConnectStatusListener = new BleConnectStatusListener() {

        @Override
        public void onConnectStatusChanged(String mac, int status) {
            if (status == STATUS_CONNECTED) {
                bleInstance.writeCharacteristic(mac, UUID.fromString("c99a3001-7f3c-4e85-bde2-92f2037bfd42"),UUID.fromString("c99a3102-7f3c-4e85-bde2-92f2037bfd42") ,new byte[]{3,1,0,0});
                switchTo(TestWatchAct.class);
                finish();
            } else if (status == STATUS_DISCONNECTED) {
                bleInstance.unRegister(mac,mBleConnectStatusListener);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_pressed);
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                CsdMgr.getInstance().setCheckDevicePressed(new ICheckDevicePressed() {
                    @Override
                    public void miWatchPressed(String mac) {
                        if(System.currentTimeMillis() - lastTs > 20 * 1000  ) {
                            lastTs = System.currentTimeMillis();
                            bleInstance.connect(mac);
                            bleInstance.register(mac,mBleConnectStatusListener);
                            L.d("PressedMiWatchAct miWatchPressed mac:" + mac);
                        }
                    }
                });
            }
        },2L,1, TimeUnit.SECONDS);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}
