package inshow.carl.com.ota_tool;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.yanzhenjie.recyclerview.swipe.SwipeMenu;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuBridge;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuCreator;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuItem;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuItemClickListener;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuRecyclerView;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import inshow.carl.com.ota_tool.adapter.MainAdapter;
import inshow.carl.com.ota_tool.entity.FileEntity;
import inshow.carl.com.ota_tool.tools.L;
import inshow.carl.com.ota_tool.tools.Utils;
import inshow.carl.com.ota_tool.upgrade.BluetoothLeService;
import inshow.carl.com.ota_tool.upgrade.DfuService;
import no.nordicsemi.android.dfu.DfuServiceInitiator;
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

import static inshow.carl.com.ota_tool.tools.Const.FILE_SELECT_CODE;
import static inshow.carl.com.ota_tool.tools.Const.PROCESS_INDETERMINATE_TRUE;
import static inshow.carl.com.ota_tool.tools.Const.STATE_FAIL;
import static inshow.carl.com.ota_tool.tools.Const.STATE_INIT;
import static inshow.carl.com.ota_tool.tools.Const.STATE_PROCESSING;
import static inshow.carl.com.ota_tool.tools.Const.VIEW_TYPE_AGAIN;
import static inshow.carl.com.ota_tool.tools.Const.VIEW_TYPE_DELETE;
import static inshow.carl.com.ota_tool.tools.Utils.getIncrementedAddress;

/**
 * @ 创建者:   CoderChen
 * @ 时间:     2018/4/11
 * @ 描述:
 */


public class MainPagerHelper {

    private static final String TAG = "MainActivity";
    private static boolean hasFound = false;

    public static void handleChooseFile(Context c, Intent data, TextView filePath) {
        Uri uri = data.getData();
        L.d( "File Uri: " + uri.toString());
        String path = null;
        try {
            path = Utils.getPath(c, uri);
            L.d( "File Path: " + path);
            if (null != path) {
                final File file = new File(path);
                filePath.setText(path);
                saveSelectFileInfo(path, file.getName());
            }

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public static void showFileChooser(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/zip");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            activity.startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Upload"),
                    FILE_SELECT_CODE);
        } catch (ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(activity, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    public static void saveSelectFileInfo(String path, String name) {
        FileEntity fileEntity = new FileEntity(path, name);
        fileEntity.save();
    }

    public static void loadFileInfo(TextView filePath) {
        FileEntity f = FileEntity.last(FileEntity.class);
        if (null != f) {
            filePath.setText(f.filePath);
        }
    }

    public static SwipeMenuCreator getSwipeMenuCreator(final Context context) {
        return new SwipeMenuCreator() {
            @Override
            public void onCreateMenu(SwipeMenu swipeLeftMenu, SwipeMenu swipeRightMenu, int viewType) {
                int width = context.getResources().getDimensionPixelSize(R.dimen.dp_70);
                // 1. MATCH_PARENT 自适应高度，保持和Item一样高;
                // 2. 指定具体的高，比如80;
                // 3. WRAP_CONTENT，自身高度，不推荐;
                int height = ViewGroup.LayoutParams.MATCH_PARENT;
                // 添加右侧的，如果不添加，则右侧不会出现菜单。
                if (viewType == VIEW_TYPE_DELETE) {
                    SwipeMenuItem deleteItem = new SwipeMenuItem(context)
                            .setBackground(R.drawable.selector_red)
                            .setText("删除")
                            .setTextColor(Color.WHITE)
                            .setWidth(width)
                            .setHeight(height);
                    swipeRightMenu.addMenuItem(deleteItem);// 添加菜单到右侧。
                } else if (viewType == VIEW_TYPE_AGAIN) {
                    SwipeMenuItem deleteItem = new SwipeMenuItem(context)
                            .setBackground(R.drawable.selector_blue)
                            .setText("重试")
                            .setTextColor(Color.WHITE)
                            .setWidth(width)
                            .setHeight(height);
                    swipeRightMenu.addMenuItem(deleteItem);// 添加菜单到右侧。
                } else {

                }
            }
        };
    }

    public static void startScan(DaemonManager manager,MainAdapter mAdapter, final String mac, final BluetoothLeService mBluetoothLeService) {
        manager.start();
        int currentPos = mAdapter.getCurrentPos();
        mAdapter.getItem(currentPos).state = STATE_PROCESSING;
        mAdapter.getItem(currentPos).process = PROCESS_INDETERMINATE_TRUE;
        mAdapter.notifyItemChanged(currentPos);
        L.d( "startScan,current position:" + currentPos);
        scanFoundHandle(mac, new IScanHelper() {
            @Override
            public void deviceFound() {
                if (null != mBluetoothLeService) {
                    L.d("mBluetoothLeService connect");
                    mBluetoothLeService.connect(mac);
                }
            }
        });
    }


    public static SwipeMenuItemClickListener getSwipeMenuItemClickListener(final Context c,  final MainAdapter mAdapter) {
        return new SwipeMenuItemClickListener() {
            @Override
            public void onItemClick(SwipeMenuBridge menuBridge) {
                menuBridge.closeMenu();
                int direction = menuBridge.getDirection(); // 左侧还是右侧菜单。
                int adapterPosition = menuBridge.getAdapterPosition(); // RecyclerView的Item的position。
                int menuPosition = menuBridge.getPosition(); // 菜单在RecyclerView的Item中的Position。
                if (direction == SwipeMenuRecyclerView.RIGHT_DIRECTION) {
                    if (menuPosition == 0) {
                        if (mAdapter.getItem(adapterPosition).state == STATE_INIT) {
                            mAdapter.removeAtNotify(adapterPosition);
                            Toast.makeText(c, "删除成功 (●’◡’●)", Toast.LENGTH_LONG).show();
                        }else {
                            if(canUpgradeAgain(mAdapter)) {
                                if(mAdapter.getItem(adapterPosition).state == STATE_FAIL) {
                                    Toast.makeText(c, "功能开发中(●’◡’●)", Toast.LENGTH_LONG).show();
                                }
                            }else{
                                Toast.makeText(c, "任务还未执行完成，不能重新升级(●’◡’●)", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }
            }
        };
    }

    public static  boolean canUpgradeAgain( final MainAdapter mAdapter){
        return mAdapter.getCurrentPos() == mAdapter.getItemCount() -1 ;
    }


    public static void startOTA(final Context context, final String mac) {
        L.d( "starOTA");
        scanFoundHandle(mac, new IScanHelper() {
            @Override
            public void deviceFound() {
                final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
                final FileEntity f = FileEntity.last(FileEntity.class);
                final DfuServiceInitiator starter = new DfuServiceInitiator(getIncrementedAddress(mac))
                        .setDisableNotification(true)//need't Notification Act
                        .setKeepBond(false)
                        .setDeviceName("DFU")
                        .setZip(f.filePath);
                starter.start(context, DfuService.class);
            }
        });
    }


    public static void scanFoundHandle(final String mac, final IScanHelper helper) {
        final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
        final ScanCallback callback = new ScanCallback() {
            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                L.d( "  onScanFailed :" + errorCode);
            }

            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                L.d( "  onScanResult");
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                L.d( "  onBatchScanResults");
                for (ScanResult item : results) {
                    if (item.getDevice().getAddress().toUpperCase().equals(mac.toUpperCase())) {
                        L.d("  onBatchScanResults equals ");
                        hasFound = true;
                        helper.deviceFound();
                        break;
                    }
                }
            }
        };
        final ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).setReportDelay(1000).setUseHardwareBatchingIfSupported(false).build();
        final List<ScanFilter> filters = new ArrayList<>();
        filters.add(new ScanFilter.Builder().setServiceUuid(null).build());
        scanner.startScan(filters, settings, callback);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                scanner.stopScan(callback);
                if (!hasFound) {
                    helper.deviceFound();
                }
                hasFound = false;
            }
        }, 5000);

    }

    public interface IScanHelper {
        void deviceFound();
    }

}

