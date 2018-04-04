package inshow.carl.com.ota_tool;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;
import android.view.Window;
import android.widget.Toast;

import com.google.zxing.Result;
import com.google.zxing.client.android.AutoScannerView;
import com.google.zxing.client.android.BaseCaptureActivity;

import butterknife.ButterKnife;
import inshow.carl.com.ota_tool.entity.DeviceEntity;
import inshow.carl.com.ota_tool.tools.Utils;

/**
 * 模仿微信的扫描界面
 */
public class WeChatCaptureActivity extends BaseCaptureActivity {

    private static final String TAG = WeChatCaptureActivity.class.getSimpleName();
//    @InjectView(R.id.scan_complete)
//    Button scanComplete;
    private SurfaceView surfaceView;
    private AutoScannerView autoScannerView;
    private long timestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        timestamp = System.currentTimeMillis();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_wechat_capture);
        ButterKnife.inject(this);
        surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        autoScannerView = (AutoScannerView) findViewById(R.id.autoscanner_view);
//        scanComplete.setVisibility(View.GONE);
//        scanComplete.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent i = new Intent(WeChatCaptureActivity.this, DfuAct.class);
//                startActivity(i);
//            }
//        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        autoScannerView.setCameraManager(cameraManager);
    }

    @Override
    public SurfaceView getSurfaceView() {
        return (surfaceView == null) ? (SurfaceView) findViewById(R.id.preview_view) : surfaceView;
    }

    @Override
    public void dealDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
        String MAC =  Utils.getScanMac(rawResult.getText());
        Log.i(TAG, "dealDecode  " + rawResult.getText() + " " + barcode + " " + scaleFactor + " " + MAC);
        playBeepSoundAndVibrate(true, true);
        if(!TextUtils.isEmpty( MAC)&& MAC.length() == 17 && MAC.contains(":")){
            DeviceEntity entity = new DeviceEntity(MAC, 0, timestamp);
            entity.save();
            Intent i = new Intent(WeChatCaptureActivity.this, DfuAct.class);
            startActivity(i);
        }else{
            Toast.makeText(WeChatCaptureActivity.this,"手表二维码识别解析有误！",Toast.LENGTH_LONG).show();
            reScan();
        }
    }

}
