package inshow.carl.com.csd.csd;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;

import java.util.UUID;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import inshow.carl.com.csd.BasicAct;
import inshow.carl.com.csd.R;
import inshow.carl.com.csd.csd.core.BleManager;
import inshow.carl.com.csd.tools.L;

import static com.inuker.bluetooth.library.Constants.STATUS_CONNECTED;
import static com.inuker.bluetooth.library.Constants.STATUS_DEVICE_CONNECTED;
import static com.inuker.bluetooth.library.Constants.STATUS_DISCONNECTED;
import static inshow.carl.com.csd.csd.core.CsdConstant.CHARACTERISTIC_STEP_DRIVER;
import static inshow.carl.com.csd.csd.core.CsdConstant.IN_SHOW_SERVICE;

/**
 * Created by chendong on 2018/6/28.
 */

public class TestWatchAct extends BasicAct {

    @InjectView(R.id.tvState)
    TextView tvState;
    @InjectView(R.id.reconnect)
    Button btnRecon;
    @InjectView(R.id.disconnect)
    Button btnDiscon;
    private BleManager bleInstance = BleManager.getInstance();
    String MAC;
    @InjectView(R.id.tvMac)
    TextView tvMac;

    private final BleConnectStatusListener mBleConnectStatusListener = new BleConnectStatusListener() {

        @Override
        public void onConnectStatusChanged(String mac, int status) {
            if (status == STATUS_CONNECTED) {
                L.d("TestWatchAct ble on");
                tvState.setText("on");
                btnDiscon.setEnabled(true);
                btnRecon.setEnabled(false);
            } else if (status == STATUS_DISCONNECTED) {
                L.d("TestWatchAct ble off");
                tvState.setText("off");
                btnDiscon.setEnabled(false);
                btnRecon.setEnabled(true);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_test_watch);
        ButterKnife.inject(this);
        MAC = getIntent().getStringExtra("MAC");
        if (!TextUtils.isEmpty(MAC)) {
            tvMac.setText(MAC);
            bleInstance.register(MAC, mBleConnectStatusListener);
            tvState.setText(bleInstance.getBleState(MAC) == STATUS_DEVICE_CONNECTED ? "on" : "off");
        }
        btnDiscon.setEnabled(true);
        btnRecon.setEnabled(false);
    }

    @Override
    protected void onDestroy() {
        bleInstance.disConnect(MAC);
        if (!TextUtils.isEmpty(MAC)) {
            bleInstance.unRegister(MAC, mBleConnectStatusListener);
        }
        super.onDestroy();
    }

    private boolean isConnected() {
        return bleInstance.getBleState(MAC) == STATUS_CONNECTED;
    }

    @OnClick({R.id.back, R.id.step, R.id.tvState, R.id.disconnect, R.id.reconnect})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.disconnect:
                    showToast("断开连接中...");
                    bleInstance.disConnect(MAC);
                break;
            case R.id.reconnect:
                    showToast("重新连接中...");
                    bleInstance.connect(MAC);
                break;
            case R.id.step:
                if (!TextUtils.isEmpty(MAC)) {
                    BleManager.getInstance().writeCharacteristic(MAC, UUID.fromString(IN_SHOW_SERVICE), UUID.fromString(CHARACTERISTIC_STEP_DRIVER), new byte[]{1});
                }
                break;
        }

    }
}
