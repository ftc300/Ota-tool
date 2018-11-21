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
import com.google.gson.Gson;
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;

import java.util.Calendar;
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
import inshow.carl.com.csd.csd.core.HttpUtils;
import inshow.carl.com.csd.csd.core.SPManager;
import inshow.carl.com.csd.csd.http.FunConsts;
import inshow.carl.com.csd.csd.http.OperateFun;
import inshow.carl.com.csd.csd.http.WatchInfo;
import inshow.carl.com.csd.tools.L;

import static com.inuker.bluetooth.library.Constants.STATUS_CONNECTED;
import static com.inuker.bluetooth.library.Constants.STATUS_DEVICE_CONNECTED;
import static com.inuker.bluetooth.library.Constants.STATUS_DISCONNECTED;
import static inshow.carl.com.csd.csd.basic.AdjustBasicAct.EXTRAS_EVENT_BUS;
import static inshow.carl.com.csd.csd.basic.AdjustBasicAct.EXTRAS_EVENT_MAC;
import static inshow.carl.com.csd.csd.basic.AdjustBasicAct.EXTRAS_EVENT_VERSION;
import static inshow.carl.com.csd.csd.core.ConvertDataMgr.B2I_getBatteryLevel2;
import static inshow.carl.com.csd.csd.core.ConvertDataMgr.B2I_getStep;
import static inshow.carl.com.csd.csd.core.ConvertDataMgr.bytes2Char;
import static inshow.carl.com.csd.csd.core.ConvertDataMgr.getCurrentStep;
import static inshow.carl.com.csd.csd.core.ConvertDataMgr.getCurrentTime;
import static inshow.carl.com.csd.csd.core.ConvertDataMgr.getPowerConsumption;
import static inshow.carl.com.csd.csd.core.ConvertDataMgr.getSumPress;
import static inshow.carl.com.csd.csd.core.ConvertDataMgr.set0E;
import static inshow.carl.com.csd.csd.core.ConvertDataMgr.setControlData;
import static inshow.carl.com.csd.csd.core.ConvertDataMgr.setCurrentTime;
import static inshow.carl.com.csd.csd.core.ConvertDataMgr.setVibrateData;
import static inshow.carl.com.csd.csd.core.CsdConstant.CHARACTERISTIC_3101;
import static inshow.carl.com.csd.csd.core.CsdConstant.CHARACTERISTIC_3102;
import static inshow.carl.com.csd.csd.core.CsdConstant.CHARACTERISTIC_3103;
import static inshow.carl.com.csd.csd.core.CsdConstant.CHARACTERISTIC_3106;
import static inshow.carl.com.csd.csd.core.CsdConstant.CHARACTERISTIC_3108;
import static inshow.carl.com.csd.csd.core.CsdConstant.CHARACTERISTIC_3109;
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
    String currentVersion;
    boolean batteryState;
    int rtcTime;
    boolean b = true;
    boolean bReconnect = false;
    LoadingDailog dialog;
    int peak;
    int valley;
    private final BleConnectStatusListener mBleConnectStatusListener = new BleConnectStatusListener() {

        @Override
        public void onConnectStatusChanged(String mac, int status) {
            if (status == STATUS_CONNECTED) {
                L.d("TestWatchAct ble on");
                tvState.setText("on");
                btnDiscon.setEnabled(true);
                btnRecon.setEnabled(false);
                if (bReconnect) {
                    bReconnect = false;
                    refreshPage();
                }
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


    public void showLoading() {
        LoadingDailog.Builder loadBuilder = new LoadingDailog.Builder(this)
                .setMessage("Loading")
                .setCancelable(false)
                .setCancelOutside(false);
        dialog = loadBuilder.create();
        dialog.show();
    }

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
                refreshPage();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        uploadWatchInfo();
                    }
                }, 5000);
            }

        }
        btnDiscon.setEnabled(true);
        btnRecon.setEnabled(false);
    }

    private void refreshPage() {
        showLoading();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dialog.dismiss();
            }
        },5000);
        BleManager.getInstance().readCharacteristic(MAC, UUID.fromString(SERVICE_INSO), UUID.fromString(CHARACTERISTIC_3101), new BleManager.IReadOnResponse() {
            @Override
            public void onSuccess(byte[] data) {
                currentStep = B2I_getStep(data)[0];
                currentStep = getCurrentStep(data);
                tvCurrentStep.setText(currentStep + "");
            }

            @Override
            public void onFail() {
                showToast(getString(R.string.operate_fail));
            }
        });
        BleManager.getInstance().readCharacteristic(MAC, UUID.fromString(SERVICE_DEVICE_INFO), UUID.fromString(CHARACTERISTIC_DEVICE_INFO), new BleManager.IReadOnResponse() {
            @Override
            public void onSuccess(byte[] data) {
                currentVersion = bytes2Char(data);
                tvVersion.setText(bytes2Char(data));
                SPManager.put(context, EXTRAS_EVENT_VERSION, currentVersion);
            }

            @Override
            public void onFail() {
                showToast(getString(R.string.operate_fail));
            }
        });

        BleManager.getInstance().readCharacteristic(MAC, UUID.fromString(SERVICE_INSO), UUID.fromString(CHARACTERISTIC_3300), new BleManager.IReadOnResponse() {
            @Override
            public void onSuccess(byte[] data) {
                batteryLevel = getPowerConsumption(data)[0];
                batteryState = batteryLevel > 30;
                L.d("batteryLevel:"+ batteryLevel);
                if (batteryState) {
                    BleManager.getInstance().writeCharacteristic(MAC, UUID.fromString(SERVICE_INSO), UUID.fromString(CHARACTERISTIC_3102), new byte[]{9, 0, 0, 0});
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            BleManager.getInstance().readCharacteristic(MAC, UUID.fromString(SERVICE_INSO), UUID.fromString(CHARACTERISTIC_3103), new BleManager.IReadOnResponse() {
                                @Override
                                public void onSuccess(byte[] data) {
                                    peak = B2I_getBatteryLevel2(data)[0];
                                    valley = B2I_getBatteryLevel2(data)[1];
                                    L.d("peak:" + peak + ",valley:" + valley);
                                    int delta = Math.abs(peak - valley);
                                    batteryState = delta <= 350 && valley >= 2650;
                                    tvBattery.setText(batteryState ? getString(R.string.battery_enough) : getString(R.string.battery_not_enough));
                                    tvBattery.setBackgroundResource(batteryState ? R.color.green : R.color.red);
                                }

                                @Override
                                public void onFail() {
                                }
                            });
                        }
                    }, 4000);
                } else {
                    tvBattery.setText(getString(R.string.battery_not_enough));
                    tvBattery.setBackgroundResource(R.color.red);
                }

            }

            @Override
            public void onFail() {
                showToast(getString(R.string.operate_fail));
            }
        });
        BleManager.getInstance().readCharacteristic(MAC, UUID.fromString(SERVICE_INSO), UUID.fromString(CHARACTERISTIC_3109), new BleManager.IReadOnResponse() {
            @Override
            public void onSuccess(byte[] data) {
                rtcTime = getCurrentTime(data) + 951840000;
                int currentTime = getCurrentTime(data);
                long delta = Math.abs(System.currentTimeMillis() / 1000 - 951840000 - currentTime);
                int mod = (int) delta % 3600;
                L.d("currentTimeMillis:" + System.currentTimeMillis() / 1000 + ",currentTime:" + (currentTime + 951840000) + ",delta = " + delta);
                boolean b = delta < 2 * 60 || mod <= 2 * 60 || mod >= 3480;
                mTvRtcTime.setText(b ? getString(R.string.good) : getString(R.string.bad));
                mTvRtcTime.setBackgroundResource(b ? R.color.green : R.color.red);
                int a = getH(System.currentTimeMillis());
                int c = getH((951840000 + currentTime) * 1000L);
                L.d("a：" + a + ",c:" + c);
                if (Math.abs(a - c) > 0 && delta > 2 * 60 && b) {
                    showAlertDialog(getString(R.string.tip), getString(R.string.pls_adjust_to_bj), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            bleInstance.writeCharacteristic(MAC, UUID.fromString(SERVICE_INSO), UUID.fromString(CHARACTERISTIC_3109), setCurrentTime((int) (System.currentTimeMillis() / 1000 - 951840000)));
                        }
                    });
                }
            }

            @Override
            public void onFail() {
                showToast(getString(R.string.operate_fail));
            }
        });
    }

    private int getH(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        return calendar.get(Calendar.HOUR_OF_DAY);
    }


    private void uploadWatchInfo() {
        try {
            Gson gson = new Gson();
            WatchInfo info = new WatchInfo(System.currentTimeMillis() / 1000L, MAC, "watch_info", currentVersion, new WatchInfo.Value(currentStep, rtcTime, batteryState,peak,valley));
            String content = gson.toJson(info);
            L.d(content);
            HttpUtils.getRequestQueue(this).add(HttpUtils.postInfo(AesEncryptionUtil.encrypt(content)));
        } catch (Exception e) {
            e.printStackTrace();
            L.d(e.getMessage());
        }
    }

    private void uploadTestFun(String key, int count) {
        try {
            Gson gson = new Gson();
            OperateFun info = new OperateFun(System.currentTimeMillis() / 1000L, MAC, key, currentVersion, new OperateFun.Value(count));
            String content = gson.toJson(info);
            L.d(content);
            HttpUtils.getRequestQueue(this).add(HttpUtils.postInfo(AesEncryptionUtil.encrypt(content)));
        } catch (Exception e) {
            e.printStackTrace();
            L.d(e.getMessage());
        }
    }

    private void uploadTestFun(String key) {
        try {
            Gson gson = new Gson();
            OperateFun info = new OperateFun(System.currentTimeMillis() / 1000L, MAC, key, currentVersion, new OperateFun.Value());
            String content = gson.toJson(info);
            L.d(content);
            HttpUtils.getRequestQueue(this).add(HttpUtils.postInfo(AesEncryptionUtil.encrypt(content)));
        } catch (Exception e) {
            e.printStackTrace();
            L.d(e.getMessage());
        }
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
                    showToast(getString(R.string.into_debug_mode));
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
                showToast(getString(R.string.closing));
                isPassive = false;
                bleInstance.disConnect(MAC);
                break;
            case R.id.reconnect:
                showToast(getString(R.string.reconning));
                bReconnect = true;
                bleInstance.connect(MAC);
                break;
            case R.id.btnStep:
                if (currentStep < 0) {
                    showToast(getString(R.string.read_current_step_fail));
                }
                if (!TextUtils.isEmpty(MAC) && bleInstance.getBleState(MAC) == STATUS_DEVICE_CONNECTED) {
                    showToast(getString(R.string.start_step_hand_test));
                    uploadTestFun(FunConsts.STEP_HAND);
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
                    showToast(getString(R.string.start_time_test));
                    uploadTestFun(FunConsts.TIME_HAND);
                    BleManager.getInstance().readCharacteristic(MAC, UUID.fromString(SERVICE_INSO), UUID.fromString(CHARACTERISTIC_3109), new BleManager.IReadOnResponse() {
                        @Override
                        public void onSuccess(byte[] data) {
                            rtcTime = getCurrentTime(data);
                            int currentTime = getCurrentTime(data);
                            L.d("currentTime:" + currentTime);
                            if (currentTime > 0) {
                                BleManager.getInstance().writeCharacteristic(MAC, UUID.fromString(SERVICE_INSO), UUID.fromString(CHARACTERISTIC_3109), setCurrentTime(currentTime + 3600));
                            }
                        }

                        @Override
                        public void onFail() {
                            showToast(getString(R.string.operate_fail));
                        }
                    });

                }
                break;
            case R.id.btnVibrate:
                if (batteryLevel == -1) {
                    showToast(getString(R.string.reading_battery));
                    return;
                }
                if (!batteryState) {
                    showToast(getString(R.string.low_power));
                    return;
                }

                if (!TextUtils.isEmpty(MAC) && bleInstance.getBleState(MAC) == STATUS_DEVICE_CONNECTED) {
                    showToast(getString(R.string.start_vibrate_test));
                    uploadTestFun(FunConsts.VIBRATE);
                    BleManager.getInstance().writeCharacteristic(MAC, UUID.fromString(SERVICE_IMMEDIATE_ALERT), UUID.fromString(CHARACTERISTIC_IMMEDIATE_ALERT), setVibrateData());
                }
                break;
            case R.id.btnRecovery:
                if (!TextUtils.isEmpty(MAC) && bleInstance.getBleState(MAC) == STATUS_DEVICE_CONNECTED) {
                    showAlertDialog(getString(R.string.recovery), getString(R.string.recovery_tip), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            showToast(getString(R.string.recoverying));
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
                    showAlertDialog(getString(R.string.press_test), i == 0 ? getString(R.string.press_test_tip1) : getString(R.string.press_test_tip2), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            i = 0;
                            uploadTestFun(FunConsts.PRESSKEY, 0);
                            b = true;
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
                                            if (getSumPress(data) > 0 && b) {
                                                b = false;
                                                uploadTestFun(FunConsts.PRESSKEY, 1);
                                            }
                                        }

                                        @Override
                                        public void onFail() {
                                            showToast(getString(R.string.operate_fail));
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
                .setPositiveButton(getString(R.string.ok), positive)
                .setNegativeButton(getString(R.string.cancel), null)
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
                .setMessage(getString(R.string.pls_dont_press))
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
                        Intent i = new Intent(context, CSDAct.class);
                        startActivity(i);
                        finish();
                    }
                });

            }
        }, 5000);
    }
}


