package inshow.carl.com.csd.csd;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.tu.loadingdialog.LoadingDailog;
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import inshow.carl.com.csd.BasicAct;
import inshow.carl.com.csd.R;
import inshow.carl.com.csd.csd.adjust.AdjustMainAct;
import inshow.carl.com.csd.csd.core.BleManager;
import inshow.carl.com.csd.csd.core.ConvertDataMgr;
import inshow.carl.com.csd.csd.core.SPManager;
import inshow.carl.com.csd.csd.http.WatchInfo;
import inshow.carl.com.csd.tools.L;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.POST;

import static com.inuker.bluetooth.library.Constants.STATUS_CONNECTED;
import static com.inuker.bluetooth.library.Constants.STATUS_DEVICE_CONNECTED;
import static com.inuker.bluetooth.library.Constants.STATUS_DISCONNECTED;
import static inshow.carl.com.csd.csd.basic.AdjustBasicAct.EXTRAS_EVENT_BUS;
import static inshow.carl.com.csd.csd.basic.AdjustBasicAct.EXTRAS_EVENT_MAC;
import static inshow.carl.com.csd.csd.core.ConvertDataMgr.bytes2Char;
import static inshow.carl.com.csd.csd.core.ConvertDataMgr.getCurrentStep;
import static inshow.carl.com.csd.csd.core.ConvertDataMgr.getCurrentTime;
import static inshow.carl.com.csd.csd.core.ConvertDataMgr.getPowerConsumption;
import static inshow.carl.com.csd.csd.core.ConvertDataMgr.getSumPress;
import static inshow.carl.com.csd.csd.core.ConvertDataMgr.set0E;
import static inshow.carl.com.csd.csd.core.ConvertDataMgr.setControlData;
import static inshow.carl.com.csd.csd.core.ConvertDataMgr.setCurrentTime;
import static inshow.carl.com.csd.csd.core.ConvertDataMgr.setVibrateData;
import static inshow.carl.com.csd.csd.core.CsdConstant.CHARACTERISTIC_3102;
import static inshow.carl.com.csd.csd.core.CsdConstant.CHARACTERISTIC_3106;
import static inshow.carl.com.csd.csd.core.CsdConstant.CHARACTERISTIC_3108;
import static inshow.carl.com.csd.csd.core.CsdConstant.CHARACTERISTIC_3109;
import static inshow.carl.com.csd.csd.core.CsdConstant.CHARACTERISTIC_310A;
import static inshow.carl.com.csd.csd.core.CsdConstant.CHARACTERISTIC_3300;
import static inshow.carl.com.csd.csd.core.CsdConstant.CHARACTERISTIC_DEVICE_INFO;
import static inshow.carl.com.csd.csd.core.CsdConstant.CHARACTERISTIC_IMMEDIATE_ALERT;
import static inshow.carl.com.csd.csd.core.CsdConstant.SERVICE_DEVICE_INFO;
import static inshow.carl.com.csd.csd.core.CsdConstant.SERVICE_IMMEDIATE_ALERT;
import static inshow.carl.com.csd.csd.core.CsdConstant.SERVICE_INSO;

/**
 * Created by chendong on 2018/6/28.
 */

public class TestWatchAct extends BasicAct {

    @InjectView(R.id.reconnect)
    Button btnRecon;
    @InjectView(R.id.disconnect)
    Button btnDiscon;
    @InjectView(R.id.btnRecovery)
    Button btnRecovery;
    @InjectView(R.id.tvVersion)
    TextView tvVersion;
    @InjectView(R.id.tvCurrentStep)
    TextView tvCurrentStep;
    @InjectView(R.id.tvState)
    TextView tvState;
    @InjectView(R.id.tvPress)
    TextView tvPress;
    @InjectView(R.id.tvBattery)
    TextView tvBattery;
    @InjectView(R.id.tvRtcTime)
    TextView mTvRtcTime;
    private BleManager bleInstance = BleManager.getInstance();
    String MAC;
    @InjectView(R.id.tvMac)
    TextView tvMac;
    ScheduledExecutorService scheduledExecutorService;
    String TAG = "TestWatchAct";
    private int i;
    private int currentStep;
    private boolean isPassive;// 蓝牙被动断开
    int GE10_MAX_RANGE = 20000;
    int GE10_HIGH_RANGE = 15200;
    int GE10_LOW_STEPS_PER_MOVE = 400;
    int GE10_HIGH_STEPS_PER_MOVE = 600;
    int GE10_STEPS_360 = 60;
    private int driveStep;
    private int clickCount;
    int batteryLevel = -1;
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
                if (isPassive) {
                    L.d("TestWatchAct ble off ==>被动断开了，主动重连");
                    bleInstance.connect(MAC);
                } else {
                    L.d("TestWatchAct ble off ==>断开连接主动断开");
                    tvState.setText("off");
                    btnDiscon.setEnabled(false);
                    btnRecon.setEnabled(true);
                    isPassive = false;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_test_watch);
        ButterKnife.inject(this);
        MAC = getIntent().getStringExtra("MAC");
        SPManager.put(context, EXTRAS_EVENT_MAC, MAC);
        currentStep = -1;
        if (!TextUtils.isEmpty(MAC)) {
            isPassive = true;
            tvMac.setText(MAC);
            bleInstance.register(MAC, mBleConnectStatusListener);
            tvState.setText(bleInstance.getBleState(MAC) == STATUS_DEVICE_CONNECTED ? "on" : "off");
            if (bleInstance.getBleState(MAC) == STATUS_DEVICE_CONNECTED) {
                BleManager.getInstance().readCharacteristic(MAC, UUID.fromString(SERVICE_INSO), UUID.fromString(CHARACTERISTIC_310A), new BleManager.IReadOnResponse() {
                    @Override
                    public void onSuccess(byte[] data) {
                        currentStep = getCurrentStep(data);
                        tvCurrentStep.setText(currentStep + "");
                    }

                    @Override
                    public void onFail() {
                        showToast("操作失败");
                    }
                });
                BleManager.getInstance().readCharacteristic(MAC, UUID.fromString(SERVICE_DEVICE_INFO), UUID.fromString(CHARACTERISTIC_DEVICE_INFO), new BleManager.IReadOnResponse() {
                    @Override
                    public void onSuccess(byte[] data) {
                        tvVersion.setText(bytes2Char(data));
                    }

                    @Override
                    public void onFail() {
                        showToast("操作失败");
                    }
                });

                BleManager.getInstance().readCharacteristic(MAC, UUID.fromString(SERVICE_INSO), UUID.fromString(CHARACTERISTIC_3300), new BleManager.IReadOnResponse() {
                    @Override
                    public void onSuccess(byte[] data) {
                        batteryLevel = getPowerConsumption(data)[0];
                        tvBattery.setText(batteryLevel > 30 ? "电量充足" : "电量不足");
                    }

                    @Override
                    public void onFail() {
                        showToast("读取电量操作失败");
                    }
                });
                BleManager.getInstance().readCharacteristic(MAC, UUID.fromString(SERVICE_INSO), UUID.fromString(CHARACTERISTIC_3109), new BleManager.IReadOnResponse() {
                    @Override
                    public void onSuccess(byte[] data) {
                        int currentTime = getCurrentTime(data);
                        long delta = new Date().getTime() / 1000 - 951840000 - currentTime;
                        L.d("currentTime:" + currentTime + ",delta = " + delta);
                        mTvRtcTime.setText(Math.abs(delta) < 2 * 60 ? "正常" : "超标");
                        mTvRtcTime.setBackgroundResource(Math.abs(delta) < 2 * 60 ? R.color.green : R.color.red);
                    }

                    @Override
                    public void onFail() {
                        showToast("操作失败");
                    }
                });

            }

        }
        btnDiscon.setEnabled(true);
        btnRecon.setEnabled(false);
    }

    @Override
    protected void onDestroy() {
        if (null != scheduledExecutorService) {
            scheduledExecutorService.shutdownNow();
        }
        super.onDestroy();
    }

    private boolean isConnected() {
        return bleInstance.getBleState(MAC) == STATUS_CONNECTED;
    }

    @OnClick({R.id.adjust, R.id.back, R.id.tvState, R.id.disconnect, R.id.reconnect, R.id.btnHour, R.id.btnPress, R.id.btnStep, R.id.btnVibrate, R.id.btnRecovery, R.id.tvVersion, R.id.tvBattery})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.adjust:
                Intent intent = new Intent(TestWatchAct.this, AdjustMainAct.class);
                intent.putExtra(EXTRAS_EVENT_BUS, true);
                startActivity(intent);
                break;
            case R.id.tvVersion:
                if (clickCount > 7) {
                    showToast("进入调试模式...");
                    btnRecovery.setVisibility(View.VISIBLE);
                } else {
                    clickCount++;
                }
                break;
            case R.id.back:
                L.d("press back button");
                delayfinish();
                break;
            case R.id.disconnect:
                showToast("断开连接中...");
                isPassive = false;
                bleInstance.disConnect(MAC);
                break;
            case R.id.reconnect:
                showToast("重新连接中...");
                bleInstance.connect(MAC);
                break;
            case R.id.btnStep:
                if (currentStep < 0) {
                    showToast("读取当前步数失败...");
                }
                if (!TextUtils.isEmpty(MAC) && bleInstance.getBleState(MAC) == STATUS_DEVICE_CONNECTED) {
                    showToast("开始步针测试...");
                    if (currentStep == 0) {
                        driveStep = 60;
                    } else if (currentStep < GE10_HIGH_RANGE) {
                        driveStep = GE10_STEPS_360 - currentStep / GE10_LOW_STEPS_PER_MOVE;
                    } else if (currentStep >= GE10_HIGH_RANGE && currentStep <= GE10_MAX_RANGE) {
                        driveStep = GE10_STEPS_360 - GE10_HIGH_RANGE / GE10_LOW_STEPS_PER_MOVE - (currentStep - GE10_HIGH_RANGE) / GE10_HIGH_STEPS_PER_MOVE;
                    } else if (currentStep > GE10_MAX_RANGE) {
                        driveStep = 14;
                    }
                    L.d("driveStep:" + driveStep);
                    BleManager.getInstance().writeCharacteristic(MAC, UUID.fromString(SERVICE_INSO), UUID.fromString(CHARACTERISTIC_3106), ConvertDataMgr.setStepData(driveStep));
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            BleManager.getInstance().writeCharacteristic(MAC, UUID.fromString(SERVICE_INSO), UUID.fromString(CHARACTERISTIC_3108), new byte[]{0, 0, 0, 0});
                        }
                    }, 2000);
                }
                break;
            case R.id.btnHour:
                if (!TextUtils.isEmpty(MAC) && bleInstance.getBleState(MAC) == STATUS_DEVICE_CONNECTED) {
                    showToast("开始分针测试...");
                    BleManager.getInstance().readCharacteristic(MAC, UUID.fromString(SERVICE_INSO), UUID.fromString(CHARACTERISTIC_3109), new BleManager.IReadOnResponse() {
                        @Override
                        public void onSuccess(byte[] data) {
                            int currentTime = getCurrentTime(data);
                            L.d("currentTime:" + currentTime);
                            if (currentTime > 0) {
                                BleManager.getInstance().writeCharacteristic(MAC, UUID.fromString(SERVICE_INSO), UUID.fromString(CHARACTERISTIC_3109), setCurrentTime(currentTime + 3600));
                            }
                        }

                        @Override
                        public void onFail() {
                            showToast("操作失败");
                        }
                    });

                }
                break;
            case R.id.btnVibrate:
                if (batteryLevel == -1) {
                    showToast("稍等正在读取电量...");
                    return;
                }
                if (batteryLevel < 30) {
                    showToast("电量低，无法开始振动测试...");
                    return;
                }

                if (!TextUtils.isEmpty(MAC) && bleInstance.getBleState(MAC) == STATUS_DEVICE_CONNECTED) {
                    showToast("开始振动测试...");
                    BleManager.getInstance().writeCharacteristic(MAC, UUID.fromString(SERVICE_IMMEDIATE_ALERT), UUID.fromString(CHARACTERISTIC_IMMEDIATE_ALERT), setVibrateData());
                }
                break;
            case R.id.btnRecovery:
                if (!TextUtils.isEmpty(MAC) && bleInstance.getBleState(MAC) == STATUS_DEVICE_CONNECTED) {
                    showAlertDialog("恢复出厂设置", "该项测试会使手表进入待机状态，是否开始测试？", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            showToast("恢复出厂设置中...");
                            isPassive = false;
                            BleManager.getInstance().writeCharacteristic(MAC, UUID.fromString(SERVICE_INSO), UUID.fromString(CHARACTERISTIC_3102), setControlData(new int[]{1, 0, 0, 0}));
                            long time = System.currentTimeMillis() / 1000 - 951840000;
                            BleManager.getInstance().writeCharacteristic(MAC, UUID.fromString(SERVICE_INSO), UUID.fromString(CHARACTERISTIC_3109), setCurrentTime((int) time));
                            BleManager.getInstance().writeCharacteristic(MAC, UUID.fromString(SERVICE_INSO), UUID.fromString(CHARACTERISTIC_3102), setControlData(new int[]{4, 0, 0, 0}));
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    finish();
                                }
                            }, 3000);
                        }
                    });
                }
                break;
            case R.id.btnPress:

                if (!TextUtils.isEmpty(MAC) && bleInstance.getBleState(MAC) == STATUS_DEVICE_CONNECTED) {
                    showAlertDialog("按键测试", i == 0 ? "请在10s内连续按压表冠，并观察基本信息栏中按键次数变化。" : "基本信息栏中按键次数会被清零，并重新开启10s监测按压表冠任务。", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            i = 0;
                            BleManager.getInstance().writeCharacteristic(MAC, UUID.fromString(SERVICE_INSO), UUID.fromString(CHARACTERISTIC_3102), set0E());
                        }
                    });
                    if (scheduledExecutorService == null) {
                        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
                        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
                            @Override
                            public void run() {
                                if (i <= 10) {
                                    BleManager.getInstance().readCharacteristic(MAC, UUID.fromString(SERVICE_INSO), UUID.fromString(CHARACTERISTIC_3102), new BleManager.IReadOnResponse() {
                                        @Override
                                        public void onSuccess(byte[] data) {
                                            tvPress.setText(getSumPress(data) + "");
                                        }

                                        @Override
                                        public void onFail() {
                                            showToast("操作失败");
                                        }
                                    });
                                    i++;
                                }
                            }
                        }, 0, 1, TimeUnit.SECONDS);
                    }
                }
                break;

        }

    }


    public void showAlertDialog(String t, String m, DialogInterface.OnClickListener positive) {
        new AlertDialog.Builder(context)
                .setTitle(t)
                .setMessage(m)
                .setCancelable(false)
                .setPositiveButton("确定", positive)
                .setNegativeButton("取消", null)
                .show();
    }

    @Override
    public void onBackPressed() {
        delayfinish();
    }

    private void delayfinish() {
        if (!TextUtils.isEmpty(MAC)) {
            bleInstance.unRegister(MAC, mBleConnectStatusListener);
        }
        bleInstance.disConnect(MAC);
        LoadingDailog.Builder loadBuilder = new LoadingDailog.Builder(this)
                .setMessage("请不要按压表冠退出中")
                .setCancelable(false)
                .setCancelOutside(false);
        final LoadingDailog dialog = loadBuilder.create();
        dialog.show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        finish();
                    }
                });

            }
        }, 5000);
    }

    //  使用Retrofit封装的方法
    private void request() { //步骤4:创建Retrofit对象
        Retrofit retrofit = new Retrofit.Builder().baseUrl("http://fy.iciba.com/").addConverterFactory(GsonConverterFactory.create()).build();
    }

    //采用 注解 描述 网络请求参数
    public interface GetRequestInterface {
        // 注解里传入 网络请求 的部分URL地址 // Retrofit把网络请求的URL分成了两部分：一部分放在Retrofit对象里，
        // 另一部分放在网络请求接口里 // 如果接口里的url是一个完整的网址，那么放在Retrofit对象里的URL可以忽略 // getCall()是接受网络请求数据的方法
        @POST("ajax.php?a=fy&f=auto&t=auto&w=你好")
        Call<WatchInfo> getCall();
    }
}


