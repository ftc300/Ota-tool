package inshow.carl.com.csd.csd.adjust;


import java.util.UUID;

import inshow.carl.com.csd.R;
import inshow.carl.com.csd.csd.basic.BasicMultiButtonAct;

import static inshow.carl.com.csd.csd.core.ConvertDataMgr.I2B_OneBit;
import static inshow.carl.com.csd.csd.core.CsdConstant.CHARACTERISTIC_3105;
import static inshow.carl.com.csd.csd.core.CsdConstant.SERVICE_INSO;


/**
 * Created by chendong on 2018/8/1.
 */

public class NewAdjustTimeThirdAct extends BasicMultiButtonAct {

    protected String getTipText() {
        return getString(R.string.tap_to_exact_mark);
    }

    @Override
    protected String getLeftBtnText() {
        return getString(R.string.move_hand);
    }

    @Override
    protected String getRightBtnText() {
        return getString(R.string.button_ok);
    }

    protected int getContentViewLayout() {
        return R.layout.watch_content_time_third;
    }

    @Override
    protected void onRightClick() {
        finish();
    }

    @Override
    protected void onLeftClick() {
        onStartDriver();
    }

    @Override
    public void onStartDriver() {
        bleInstance.writeCharacteristic(MAC, UUID.fromString(SERVICE_INSO), UUID.fromString(CHARACTERISTIC_3105), I2B_OneBit(1));
    }

    @Override
    public boolean hasChangedDriver() {
        return false;
    }

}
