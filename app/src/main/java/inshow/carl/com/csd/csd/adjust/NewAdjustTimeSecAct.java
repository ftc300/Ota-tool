package inshow.carl.com.csd.csd.adjust;


import org.greenrobot.eventbus.EventBus;

import inshow.carl.com.csd.R;
import inshow.carl.com.csd.csd.basic.BasicSingleButtonAct;

/**
 * Created by chendong on 2018/8/1.
 */

public class NewAdjustTimeSecAct extends BasicSingleButtonAct {

    @Override
    protected void btnOnClick() {
        EventBus.getDefault().post(new AdjustTimeBus(true));
        finish();
    }

    @Override
    protected String getTipText() {
        return getString(R.string.tap_calibrate);
    }

    @Override
    protected String getBtnText() {
        return getString(R.string.calibrate);
    }



}
