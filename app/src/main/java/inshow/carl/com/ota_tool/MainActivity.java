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
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.yanzhenjie.recyclerview.swipe.SwipeMenuRecyclerView;
import com.yanzhenjie.recyclerview.swipe.widget.DefaultItemDecoration;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import inshow.carl.com.ota_tool.adapter.MainAdapter;
import inshow.carl.com.ota_tool.entity.DeviceEntity;
import inshow.carl.com.ota_tool.upgrade.BluetoothLeService;
import no.nordicsemi.android.dfu.DfuProgressListener;
import no.nordicsemi.android.dfu.DfuProgressListenerAdapter;
import no.nordicsemi.android.dfu.DfuServiceListenerHelper;

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
import static inshow.carl.com.ota_tool.tools.Const.PROCESS_INDETERMINATE_TRUE;
import static inshow.carl.com.ota_tool.tools.Const.STATE_FAIL;
import static inshow.carl.com.ota_tool.tools.Const.STATE_INIT;
import static inshow.carl.com.ota_tool.tools.Const.STATE_PROCESSING;
import static inshow.carl.com.ota_tool.tools.Const.STATE_SUCCESS;
import static inshow.carl.com.ota_tool.tools.Const.VIEW_TYPE_AGAIN;
import static inshow.carl.com.ota_tool.tools.Const.VIEW_TYPE_NONE;
import static inshow.carl.com.ota_tool.tools.Utils.checkBleAdapter;
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
    @InjectView(R.id.recycler_view)
    SwipeMenuRecyclerView mRecyclerView;
    protected MainAdapter mAdapter;
    protected List<DeviceEntity> mDataList = new ArrayList<>();
    @InjectView(R.id.ll_input_type)
    LinearLayout llInputType;
    @InjectView(R.id.ll_scan_type)
    LinearLayout llScanType;
    @InjectView(R.id.et_input_mac)
    EditText et;
    LinearLayoutManager linearLayoutManager;
    private BluetoothLeService mBluetoothLeService;
    private int currentPos = 0;
    private DeviceEntity currentDeviceEntity;
    private int taskState = STATE_INIT;
    private int intoDfuFlag = 0;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            if(!TextUtils.isEmpty(getCurrentMac())) {
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
                if(intoDfuFlag >0 ) {
                    startOTA(context, getCurrentMac());
                    intoDfuFlag = 0;
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


    private final DfuProgressListener mDfuProgressListener = new DfuProgressListenerAdapter() {
        private int mPercent;

        @Override
        public void onDeviceDisconnecting(final String deviceAddress) {
            Log.e(TAG,"onDeviceDisconnecting");
            if (mPercent < 100) {
                currentDeviceEntity = mAdapter.getItem(currentPos);
                currentDeviceEntity.state = STATE_FAIL;
                mAdapter.notifyItemChanged(currentPos);
                writeData2SD(currentDeviceEntity);
                currentPos++;
                if (currentPos < mAdapter.getItemCount()) {
                    startScan(getCurrentMac(), mBluetoothLeService);
                }
            }
        }

        @Override
        public void onDfuCompleted(final String deviceAddress) {
            Log.e(TAG,"onDfuCompleted");
            currentDeviceEntity = mAdapter.getItem(currentPos);
            currentDeviceEntity.state = STATE_SUCCESS;
            mAdapter.notifyItemChanged(currentPos);
            writeData2SD(currentDeviceEntity);
            if (currentPos < mAdapter.getItemCount() - 2) {
                currentPos++;
                startScan(getCurrentMac(), mBluetoothLeService);
            }
        }

        @Override
        public void onProgressChanged(final String deviceAddress, final int percent, final float speed, final float avgSpeed, final int currentPart, final int partsTotal){
            Log.d(TAG,"onProgressChanged" + percent);
            mPercent = percent;
            currentDeviceEntity = mAdapter.getItem(currentPos);
            currentDeviceEntity.state = STATE_PROCESSING;
            currentDeviceEntity.process = mPercent;
            mAdapter.notifyItemChanged(currentPos);
        }

        @Override
        public void onError(final String deviceAddress, final int error, final int errorType, final String message) {
            currentDeviceEntity = mAdapter.getItem(currentPos);
            currentDeviceEntity.state = STATE_FAIL;
            mAdapter.notifyItemChanged(currentPos);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
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
        currentDeviceEntity = mAdapter.getItem(currentPos);
        if (null != currentDeviceEntity)
            return currentDeviceEntity.getTrueMac();
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
                        llInputType.setVisibility(View.GONE);
                        llScanType.setVisibility(View.VISIBLE);
                        break;
                    case R.id.menu_gun:
                        llScanType.setVisibility(View.GONE);
                        llInputType.setVisibility(View.VISIBLE);
                        break;
                    case R.id.menu_log:
                        llScanType.setVisibility(View.GONE);
                        llInputType.setVisibility(View.GONE);
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

    private void addDevice2List(String mac) {
        if (TextUtils.isEmpty(mac)) return;
        //        String mac, int process, int state, long timestamp
        DeviceEntity entity = new DeviceEntity(mac, PROCESS_INDETERMINATE_FALSE, STATE_INIT, filePath.getText().toString());
        if (!hasAddDevice(entity)) {
            mDataList.add(entity);
            mAdapter.notifyDataSetChanged(mDataList);
            if (isFirstItem()) {
                findViewById(R.id.header).setVisibility(View.VISIBLE);
            }
            if(!isProcessing()){
                currentDeviceEntity = mAdapter.getItem(currentPos);
                currentDeviceEntity.state = STATE_PROCESSING;
                currentDeviceEntity.process = PROCESS_INDETERMINATE_TRUE;
                mAdapter.notifyItemChanged(currentPos);
                startScan(getCurrentMac(), mBluetoothLeService);
            }
            showToast("添加成功 (●’◡’●)");
        } else {
            Toast.makeText(context, "已经在列表中，请勿重复添加，已经为您忽略该操作(￢_￢)", Toast.LENGTH_LONG).show();
        }
    }

    private boolean hasAddDevice(DeviceEntity entity) {
        for (DeviceEntity item : mDataList) {
            if (item.mac.toUpperCase().equals(entity.mac.toUpperCase())) {
                return true;
            }
        }
        return false;
    }

    private boolean isFirstItem() {
        return mDataList.size() == 1;
    }

    private void initSwipe() {
        mAdapter = new MainAdapter(this){
            @Override
            public int getItemViewType(int position) {
                currentDeviceEntity = getItem(position);
                if(null!=currentDeviceEntity) {
                    if (currentDeviceEntity.state == STATE_SUCCESS || currentDeviceEntity.state == STATE_PROCESSING)  return VIEW_TYPE_NONE;
                    else if (currentDeviceEntity.state == STATE_FAIL) return VIEW_TYPE_AGAIN;
                    else if (currentDeviceEntity.state == STATE_INIT ) return VIEW_TYPE_NONE;
                }
                return VIEW_TYPE_NONE;
            }
        };
        linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.addItemDecoration(new DefaultItemDecoration(ContextCompat.getColor(this, R.color.divider_color)));
        mRecyclerView.setSwipeMenuCreator(getSwipeMenuCreator(context));
        mRecyclerView.setSwipeMenuItemClickListener(getSwipeMenuItemClickListener(context, currentPos, mAdapter));
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged(mDataList);
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
        super.onBackPressed();
        showExitD(context);
    }


    private boolean isProcessing(){
        currentDeviceEntity = mAdapter.getItem(currentPos);
        if (null != currentDeviceEntity)
            return currentDeviceEntity.state == STATE_PROCESSING;
        return false;
    }

}
