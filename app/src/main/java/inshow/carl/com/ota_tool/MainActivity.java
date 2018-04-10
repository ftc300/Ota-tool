package inshow.carl.com.ota_tool;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.yanzhenjie.recyclerview.swipe.SwipeMenu;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuBridge;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuCreator;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuItem;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuItemClickListener;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuRecyclerView;
import com.yanzhenjie.recyclerview.swipe.widget.DefaultItemDecoration;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import inshow.carl.com.ota_tool.adapter.MainAdapter;
import inshow.carl.com.ota_tool.entity.DeviceEntity;
import inshow.carl.com.ota_tool.entity.FileEntity;
import inshow.carl.com.ota_tool.tools.Utils;

import static inshow.carl.com.ota_tool.tools.Const.ACT_REQ_CODE;
import static inshow.carl.com.ota_tool.tools.Const.ARG_MAC;
import static inshow.carl.com.ota_tool.tools.Const.FILE_SELECT_CODE;
import static inshow.carl.com.ota_tool.tools.Const.PERMISSION_REQ;
import static inshow.carl.com.ota_tool.tools.Const.PROCESS_INDETERMINATE_TRUE;
import static inshow.carl.com.ota_tool.tools.Const.STATE_FAIL;
import static inshow.carl.com.ota_tool.tools.Utils.checkBleAdapter;

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
                showFileChooser();
            }
        });
        loadFileInfo();
        et.addTextChangedListener(this);
        btnInputSure.setEnabled(et.getText().toString().length() == 12);
        checkBleAdapter(this);
        initSwipe();
    }


    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/zip");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Upload"),
                    FILE_SELECT_CODE);
        } catch (ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
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
                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    Log.d(TAG, "File Uri: " + uri.toString());
                    // Get the path
                    String path = null;
                    try {
                        path = Utils.getPath(this, uri);
                        Log.d(TAG, "File Path: " + path);
                        if (null != path) {
                            final File file = new File(path);
                            filePath.setText(path);
                            saveSelectFileInfo(path, file.getName());
                        }

                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                }
            case ACT_REQ_CODE:
                try {
                    if(null!=data) {
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

    private void saveSelectFileInfo(String path, String name) {
        FileEntity fileEntity = new FileEntity(path, name);
        fileEntity.save();
    }

    private void loadFileInfo() {
        FileEntity f = FileEntity.last(FileEntity.class);
        if (null != f) {
            filePath.setText(f.filePath);
        }
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
        startActivityForResult(new Intent(MainActivity.this, WeChatCaptureActivity.class), ACT_REQ_CODE);
    }

    @OnClick(R.id.btn_input_sure)
    public void InputSure() {
        addDevice2List(et.getText().toString().toUpperCase());
    }

    private void addDevice2List(String mac) {
        if(TextUtils.isEmpty(mac)) return;
        //        String mac, int process, int state, long timestamp
        DeviceEntity entity = new DeviceEntity(mac, PROCESS_INDETERMINATE_TRUE, STATE_FAIL, System.currentTimeMillis());
        if (!hasAddDevice(entity)) {
            mDataList.add(entity);
            mAdapter.notifyDataSetChanged(mDataList);
            showToast("添加成功 (●’◡’●)");
        } else {
            Toast.makeText(context, "已经在列表中，请勿重复添加，已经为您忽略该操作(￢_￢)", Toast.LENGTH_LONG).show();
        }
    }

    private boolean hasAddDevice(DeviceEntity entity) {
        if (mDataList.size() == 0) {
            findViewById(R.id.header).setVisibility(View.VISIBLE);
            return false;
        }
        for (DeviceEntity item : mDataList) {
            if (item.mac.toUpperCase().equals(entity.mac.toUpperCase())) {
                return true;
            }
        }
        return false;
    }

    private void initSwipe() {
        mAdapter = new MainAdapter(this);
        linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.addItemDecoration(new DefaultItemDecoration(ContextCompat.getColor(this, R.color.divider_color)));
        mRecyclerView.setSwipeMenuCreator(swipeMenuCreator);
        mRecyclerView.setSwipeMenuItemClickListener(mMenuItemClickListener);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged(mDataList);
    }

    private SwipeMenuCreator swipeMenuCreator = new SwipeMenuCreator() {
        @Override
        public void onCreateMenu(SwipeMenu swipeLeftMenu, SwipeMenu swipeRightMenu, int viewType) {
            int width = getResources().getDimensionPixelSize(R.dimen.dp_70);
            // 1. MATCH_PARENT 自适应高度，保持和Item一样高;
            // 2. 指定具体的高，比如80;
            // 3. WRAP_CONTENT，自身高度，不推荐;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            // 添加右侧的，如果不添加，则右侧不会出现菜单。
            {
                SwipeMenuItem deleteItem = new SwipeMenuItem(context)
                        .setBackground(R.drawable.selector_red)
                        .setImage(R.drawable.ic_action_delete)
                        .setText("删除")
                        .setTextColor(Color.WHITE)
                        .setWidth(width)
                        .setHeight(height);
                swipeRightMenu.addMenuItem(deleteItem);// 添加菜单到右侧。
            }
        }
    };

    /**
     * RecyclerView的Item的Menu点击监听。
     */
    private SwipeMenuItemClickListener mMenuItemClickListener = new SwipeMenuItemClickListener() {
        @Override
        public void onItemClick(SwipeMenuBridge menuBridge) {
            menuBridge.closeMenu();
            int direction = menuBridge.getDirection(); // 左侧还是右侧菜单。
            int adapterPosition = menuBridge.getAdapterPosition(); // RecyclerView的Item的position。
            int menuPosition = menuBridge.getPosition(); // 菜单在RecyclerView的Item中的Position。
            if (direction == SwipeMenuRecyclerView.RIGHT_DIRECTION) {
                if (menuPosition == 0) {
                    mAdapter.removeAtNotify(adapterPosition);
                }
            }
        }
    };

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        btnInputSure.setEnabled(et.getText().toString().length() == 12);
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        btnInputSure.setEnabled(et.getText().toString().length() == 12);
    }
}
