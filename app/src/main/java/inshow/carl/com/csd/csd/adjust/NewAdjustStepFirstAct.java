package inshow.carl.com.csd.csd.adjust;

import org.greenrobot.eventbus.Subscribe;
import java.util.UUID;
import inshow.carl.com.csd.R;
import inshow.carl.com.csd.csd.basic.BasicMultiButtonAct;
import inshow.carl.com.csd.csd.core.BleManager;
import inshow.carl.com.csd.csd.core.ConvertDataMgr;

import static inshow.carl.com.csd.csd.core.CsdConstant.CHARACTERISTIC_3106;
import static inshow.carl.com.csd.csd.core.CsdConstant.CHARACTERISTIC_3108;
import static inshow.carl.com.csd.csd.core.CsdConstant.SERVICE_INSO;


/**
 * Created by chendong on 2018/8/1.
 */

public class NewAdjustStepFirstAct extends BasicMultiButtonAct {

    boolean hasChanged = false;
    @Subscribe
    public void onEventMainThread(AdjustStepBus event) {
        if(event.finish) {
            finish();
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
