package inshow.carl.com.csd.csd.adjust;

import com.google.gson.Gson;

import org.greenrobot.eventbus.Subscribe;
import java.util.UUID;
import inshow.carl.com.csd.R;
import inshow.carl.com.csd.csd.AesEncryptionUtil;
import inshow.carl.com.csd.csd.core.HttpUtils;
import inshow.carl.com.csd.csd.basic.BasicMultiButtonAct;
import inshow.carl.com.csd.csd.core.ConvertDataMgr;
import inshow.carl.com.csd.csd.http.FunConsts;
import inshow.carl.com.csd.csd.http.OperateFun;
import inshow.carl.com.csd.tools.L;

import static inshow.carl.com.csd.csd.core.CsdConstant.CHARACTERISTIC_3106;
import static inshow.carl.com.csd.csd.core.CsdConstant.CHARACTERISTIC_3108;
import static inshow.carl.com.csd.csd.core.CsdConstant.SERVICE_INSO;


/**
 * Created by chendong on 2018/8/1.
 */

public class NewAdjustStepFirstAct extends BasicMultiButtonAct {

    boolean hasChanged = false;
    private void uploadTestFun(String key) {
        try {
            Gson gson = new Gson();
            OperateFun info = new OperateFun(System.currentTimeMillis() / 1000L, MAC, key, VERSION, new OperateFun.Value());
            String content = gson.toJson(info);
            L.d(content);
            HttpUtils.getRequestQueue(this).add(HttpUtils.postInfo(AesEncryptionUtil.encrypt(content)));
        } catch (Exception e) {
            e.printStackTrace();
            L.d(e.getMessage());
        }
    }
    @Subscribe
    public void onEventMainThread(AdjustStepBus event) {
        if(event.finish) {
            finish();
            uploadTestFun(FunConsts.ADJUST_STEP);
        }
    }

    @Override
    protected String getTipText() {
        return getString(R.string.xx_01);
    }

    @Override
    protected String getLeftBtnText() {
        return getString(R.string.move_hand);
    }

    @Override
    protected String getRightBtnText() {
        return getString(R.string.next_step);
    }

    protected int getContentViewLayout() {
        return R.layout.watch_content_step_first;
    }

    @Override
    protected void onRightClick() {
        switchTo(NewAdjustStepSecAct.class);
    }

    @Override
    protected void onLeftClick() {
        onStartDriver();
    }

    @Override
    public void onStartDriver() {
        if(!hasChanged) hasChanged = true;
        bleInstance.writeCharacteristic(MAC, UUID.fromString(SERVICE_INSO), UUID.fromString(CHARACTERISTIC_3106), ConvertDataMgr.I2B_OneBit(1));
    }

    @Override
    public boolean hasChangedDriver() {
        return hasChanged;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(hasChanged) {
            bleInstance.writeCharacteristic(MAC, UUID.fromString(SERVICE_INSO), UUID.fromString(CHARACTERISTIC_3108), new byte[]{0, 0, 0, 0});
//            EventBus.getDefault().post(new AdjustStepBus(true));
        }
    }
}
