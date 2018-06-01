package inshow.carl.com.ota_tool;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.yanzhenjie.recyclerview.swipe.SwipeMenuRecyclerView;
import com.yanzhenjie.recyclerview.swipe.widget.DefaultItemDecoration;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import inshow.carl.com.ota_tool.adapter.MainAdapter;
import inshow.carl.com.ota_tool.entity.DeviceEntity;
import inshow.carl.com.ota_tool.upgrade.BluetoothLeService;
import no.nordicsemi.android.dfu.DfuProgressListener;
import no.nordicsemi.android.dfu.DfuProgressListenerAdapter;
import no.nordicsemi.android.dfu.DfuServiceListenerHelper;

import static inshow.carl.com.ota_tool.DaemonManager.ProcessEnums.DFU_PROCESSING;
import static inshow.carl.com.ota_tool.MainPagerHelper.getSwipeMenuCreator;
import static inshow.carl.com.ota_tool.MainPagerHelper.getSwipeMenuItemClickListener;
import static inshow.carl.com.ota_tool.MainPagerHelper.handleChooseFile;
import static inshow.carl.com.ota_tool.MainPagerHelper.loadFileInfo;
import static inshow.carl.com.ota_tool.MainPagerHelper.makeGattUpdateIntentFilter;
import static inshow.carl.com.ota_tool.MainPagerHelper.showFileChooser;
import static inshow.carl.com.ota_tool.MainPagerHelper.startOTA;
import static inshow.carl.com.ota_tool.MainPagerHelper.startScan;
import static inshow.carl.com.ota_tool.tools.Const.ACT_REQ_CODE;
import static inshow.carl.com.ota_tool.tools.Const.ARG_MAC;
import static inshow.carl.com.ota_tool.tools.Const.FILE_SELECT_CODE;
import static inshow.carl.com.ota_tool.tools.Const.PERMISSION_REQ;
import static inshow.carl.com.ota_tool.tools.Const.PROCESS_INDETERMINATE_FALSE;
import static inshow.carl.com.ota_tool.tools.Const.STATE_FAIL;
import static inshow.carl.com.ota_tool.tools.Const.STATE_INIT;
import static inshow.carl.com.ota_tool.tools.Const.STATE_PROCESSING;
import static inshow.carl.com.ota_tool.tools.Const.STATE_SUCCESS;
import static inshow.carl.com.ota_tool.tools.Const.VIEW_TYPE_AGAIN;
import static inshow.carl.com.ota_tool.tools.Const.VIEW_TYPE_DELETE;
import static inshow.carl.com.ota_tool.tools.Const.VIEW_TYPE_NONE;
import static inshow.carl.com.ota_tool.tools.Utils.checkBleAdapter;
import static inshow.carl.com.ota_tool.tools.Utils.getLocalVersionName;
import static inshow.carl.com.ota_tool.tools.Utils.showExitD;
import static inshow.carl.com.ota_tool.tools.Utils.writeData2SD;

public class MainActivity extends BasicAct implements TextWatcher {
    @InjectView(R.id.select_file)
    Button selectFile;
    @InjectView(R.id.btn_input_sure)
    Button btnInputSure;
    private static final String TAG = "MainActivity";
    @InjectView(R.id.activity_main)
    LinearLayout activityMain;
    @InjectView(R.id.file_path)
    TextView filePath;
    @InjectView(R.id.more)
    TextView more;
    @InjectView(R.id.bar_version)
    TextView version;
    @InjectView(R.id.recycler_view)
    SwipeMenuRecyclerView mRecyclerView;
    protected MainAdapter mAdapter;
    @InjectView(R.id.ll_input_type)
    LinearLayout llInputType;
    @InjectView(R.id.ll_scan_type)
    LinearLayout llScanType;
    @InjectView(R.id.ll_gun_type)
    LinearLayout llGunType;
    @InjectView(R.id.et_input_mac)
    EditText et;
    @InjectView(R.id.et_gun_mac)
    EditText etGun;
    LinearLayoutManager linearLayoutManager;
    private BluetoothLeService mBluetoothLeService;
    private int intoDfuFlag = 0;
    private SparseArray<Integer> processArray = new SparseArray();
    private Handler handler = new Handler();

    DaemonManager manager = new DaemonManager(new DaemonManager.IDaemonProcess() {
        @Override
        public void onFail(int currentPos) {
            Log.d(TAG,"onFail");
            //取消升级过程
            if(currentPos == mAdapter.getCurrentPos()) {
                mBluetoothLeService.disconnect();
                upgradeFail();
            }
        }
    });

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            if (!TextUtils.isEmpty(getCurrentMac())) {
                Log.d(TAG, "onServiceConnected  " + getCurrentMac());
                mBluetoothLeService.connect(getCurrentMac());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                Log.d(TAG, getCurrentMac() + " Connected");
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Log.d(TAG, getCurrentMac() + " Disconnected");
                mBluetoothLeService.disconnect();
                if (intoDfuFlag > 0) {
                    startOTA(context, getCurrentMac());
                    intoDfuFlag = 0;
                }else {
                    //connec fail
                    upgradeFail();
                    Toast.makeText(context, "未发现该设备,请确认该设备可用(￢_￢)", Toast.LENGTH_LONG).show();
                }
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                Log.d(TAG, getCurrentMac() + " Services Discovered");
                mBluetoothLeService.writeCharacteristic();
                intoDfuFlag++;
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                Log.d(TAG, "Data Available");
            }
        }
    };


    private void upgradeFail() {
        int pos = mAdapter.getCurrentPos();
        mAdapter.getItem(pos).state = STATE_FAIL;
        mAdapter.notifyItemChanged(pos);
        writeData2SD(mAdapter.getItem(pos));
        if (pos < mAdapter.getItemCount() - 1 ) {
            mAdapter.setCurrentPos(pos + 1);
            startScan(manager,mAdapter, getCurrentMac(), mBluetoothLeService);
        }
    }


    private final DfuProgressListener mDfuProgressListener = new DfuProgressListenerAdapter() {
        private int mPercent;

        @Override
        public void onDfuProcessStarting(String deviceAddress) {
            super.onDfuProcessStarting(deviceAddress);
            Log.d(TAG,"onDfuProcessStarting");
        }

        @Override
        public void onDeviceConnected(String deviceAddress) {
            super.onDeviceConnected(deviceAddress);
            Log.d(TAG,"onDeviceConnected;");
            processArray.put(mAdapter.getCurrentPos(), DFU_PROCESSING);
            manager.notifyStateChange(processArray);
        }

        @Override
        public void onDeviceDisconnecting(final String deviceAddress) {
            Log.e(TAG, "onDeviceDisconnecting");
            if (mPercent < 100) {
                upgradeFail();
            }
        }

        @Override
        public void onDfuCompleted(final String deviceAddress) {
            Log.e(TAG, "onDfuCompleted");
            int pos = mAdapter.getCurrentPos();
            mAdapter.getItem(mAdapter.getCurrentPos()).state = STATE_SUCCESS;
            mAdapter.notifyItemChanged(pos);
            writeData2SD(mAdapter.getItem(mAdapter.getCurrentPos()));
            if (pos < mAdapter.getItemCount() - 1) {
                Log.e(TAG, "onDfuCompleted next start");
                mAdapter.setCurrentPos(pos + 1);
                startScan(manager,mAdapter, getCurrentMac(), mBluetoothLeService);
            }
        }

        @Override
        public void onProgressChanged(final String deviceAddress, final int percent, final float speed, final float avgSpeed, final int currentPart, final int partsTotal) {
            Log.d(TAG, "onProgressChanged" + percent);
            mPercent = percent;
            mAdapter.getItem(mAdapter.getCurrentPos()).state = STATE_PROCESSING;
            mAdapter.getItem(mAdapter.getCurrentPos()).process = mPercent;
            mAdapter.notifyItemChanged(mAdapter.getCurrentPos());
        }

        @Override
        public void onError(final String deviceAddress, final int error, final int errorType, final String message) {
//            mAdapter.getItem(mAdapter.getCurrentPos()).state = STATE_FAIL;
//            mAdapter.notifyItemChanged(mAdapter.getCurrentPos());
            if (mPercent < 100) {
                upgradeFail();
            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        //test
//        manager.start(handler);
//        manager.setCurrentKey(0);
//        processArray.put(0,0);
//        manager.notifyStateChange(processArray);


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                ) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE,}, PERMISSION_REQ);
        }
        selectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFileChooser((Activity) context);
            }
        });
        loadFileInfo(filePath);
        et.addTextChangedListener(this);
        et.setImeOptions(EditorInfo.IME_ACTION_DONE);
        etGun.setImeOptions(EditorInfo.IME_ACTION_DONE);
        etGun.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String scanText = etGun.getText().toString();
                if(TextUtils.isEmpty(filePath.getText().toString())){
                    showToast("请先选择固件(￢_￢)");
                    return;
                }

                if(!TextUtils.isEmpty(scanText)&&scanText.length() == 27){
                    try {
                        String mac = etGun.getText().toString().toUpperCase().split("-")[1];
                        addDevice2List(mac);
                        etGun.requestFocus();
                        etGun.setText("");
                        etGun.setSelection(0);
                    }catch (Exception e){
                        showToast("解析有误，添加失败(￢_￢)");
                        e.printStackTrace();
                    }
                }

            }
        });
        version.setText("V"+getLocalVersionName(context));
        btnInputSure.setEnabled(getBtnInputSureUsed());
        checkBleAdapter(this);
        initSwipe();
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        DfuServiceListenerHelper.registerProgressListener(this, mDfuProgressListener);
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, final String[] permissions, final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    handleChooseFile(context, data, filePath);
                }
            case ACT_REQ_CODE:
                try {
                    if (null != data) {
                        String mac = data.getExtras().getString(ARG_MAC);//得到新Activity 关闭后返回的数据
                        addDevice2List(mac);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private String getCurrentMac() {
        if (null != mAdapter.getItem(mAdapter.getCurrentPos())) {
            int pos = mAdapter.getCurrentPos();
            Log.d("current position:",pos + "");
            return mAdapter.getItem(pos).getTrueMac();
        }
        return "";
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mGattUpdateReceiver);
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }


    @OnClick(R.id.more)
    public void onViewClicked() {
        PopupMenu popupMenu = new PopupMenu(MainActivity.this, more);
        popupMenu.getMenuInflater().inflate(R.menu.pop_menu, popupMenu.getMenu());
        popupMenu.show();
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_camera:
                        llGunType.setVisibility(View.GONE);
                        llInputType.setVisibility(View.GONE);
                        llScanType.setVisibility(View.VISIBLE);
                        break;
                    case R.id.menu_man:
                        llGunType.setVisibility(View.GONE);
                        llScanType.setVisibility(View.GONE);
                        llInputType.setVisibility(View.VISIBLE);
                        et.requestFocus();
                        et.setText("");
                        et.setSelection(0);
                        break;
                    case R.id.menu_log:
                        llGunType.setVisibility(View.GONE);
                        llScanType.setVisibility(View.GONE);
                        llInputType.setVisibility(View.GONE);
//                        openLogFolder((Activity) context);
                        testAdd();
                        break;
                    case R.id.menu_gun:
                        llScanType.setVisibility(View.GONE);
                        llInputType.setVisibility(View.GONE);
                        llGunType.setVisibility(View.VISIBLE);
                        etGun.requestFocus();
                        etGun.setText("");
                        etGun.setSelection(0);
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
    }

    @OnClick(R.id.btn_scan)
    public void Scan() {
        if (filePath.getText().length() == 0) {
            showToast("请先选择固件(￢_￢)");
            return;
        }
        startActivityForResult(new Intent(MainActivity.this, WeChatCaptureActivity.class), ACT_REQ_CODE);
    }

    @OnClick(R.id.btn_input_sure)
    public void InputSure() {
        addDevice2List(et.getText().toString().toUpperCase());
    }

    @OnClick(R.id.btn_gun_sure)
    public void GunSure() {
        try {
            String mac = etGun.getText().toString().toUpperCase().split("-")[1];
            addDevice2List(mac);
            etGun.setText("");
        }catch (Exception e){
            showToast("解析有误，添加失败(￢_￢)");
            e.printStackTrace();
        }
    }

    void testAdd(){
        addDevice2List("7058960002C4");
        addDevice2List("705896003CA0");
        addDevice2List("7058960002C9");
        addDevice2List("705896000864");
        addDevice2List("7058960094C8");
    }

    private void addDevice2List(String mac) {
        if (TextUtils.isEmpty(mac)) return;
        //        String mac, int process, int state, long timestamp
        DeviceEntity entity = new DeviceEntity(mac, PROCESS_INDETERMINATE_FALSE, STATE_INIT, filePath.getText().toString());
        if (!hasAddDevice(entity)) {
            mAdapter.addNotify(entity);
            if (!isProcessing()) {
                if (isFirstItem()) {
                    findViewById(R.id.header).setVisibility(View.VISIBLE);
                } else {
                    mAdapter.setCurrentPos(mAdapter.getCurrentPos() + 1);
                }
                startScan(manager,mAdapter, getCurrentMac(), mBluetoothLeService);
            }
            showToast("添加成功 (●’◡’●)");
        } else {
            Toast.makeText(context, "已经在列表中，请勿重复添加，已经为您忽略该操作(￢_￢)", Toast.LENGTH_LONG).show();
        }
    }


    private boolean hasAddDevice(DeviceEntity entity) {
        for (DeviceEntity item : mAdapter.getDataList()) {
            if (item.mac.toUpperCase().equals(entity.mac.toUpperCase())) {
                return true;
            }
        }
        return false;
    }

    private boolean isFirstItem() {
        return mAdapter.getDataList().size() == 1;
    }

    private void initSwipe() {
        mAdapter = new MainAdapter(this,manager) {
            @Override
            public int getItemViewType(int position) {
                if (null != mAdapter.getItem(position)) {
                    if (mAdapter.getItem(position).state == STATE_SUCCESS || mAdapter.getItem(position).state == STATE_PROCESSING)
                        return VIEW_TYPE_NONE;
                    else if (mAdapter.getItem(position).state == STATE_FAIL)
                        return VIEW_TYPE_AGAIN;
                    else if (mAdapter.getItem(position).state == STATE_INIT)
                        return VIEW_TYPE_DELETE;
                }
                return VIEW_TYPE_NONE;
            }
        };
        mAdapter.setCurrentPos(0);
        linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.addItemDecoration(new DefaultItemDecoration(ContextCompat.getColor(this, R.color.divider_color)));
        mRecyclerView.setSwipeMenuCreator(getSwipeMenuCreator(context));
        mRecyclerView.setSwipeMenuItemClickListener(getSwipeMenuItemClickListener(context, mAdapter));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.getItemAnimator().setChangeDuration(0);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        btnInputSure.setEnabled(getBtnInputSureUsed());
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        btnInputSure.setEnabled(getBtnInputSureUsed());
    }

    private boolean getBtnInputSureUsed() {
        return et.getText().toString().length() == 12 && filePath.getText().length() > 0;
    }

    @Override
    public void onBackPressed() {
        showExitD(context);
    }


    private boolean isProcessing() {
        if (null != mAdapter.getItem(mAdapter.getCurrentPos()))
            return mAdapter.getItem(mAdapter.getCurrentPos()).state == STATE_PROCESSING;
        return false;
    }




}
