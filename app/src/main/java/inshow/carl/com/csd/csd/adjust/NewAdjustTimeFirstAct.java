package inshow.carl.com.csd.csd.adjust;

import android.content.DialogInterface;
import android.view.View;
import android.widget.TextView;
import com.xiaomi.smarthome.common.ui.dialog.MLAlertDialog;
import org.greenrobot.eventbus.Subscribe;
import java.util.UUID;
import inshow.carl.com.csd.R;
import inshow.carl.com.csd.csd.basic.BasicSingleButtonAct;
import inshow.carl.com.csd.tools.L;
import inshow.carl.com.csd.view.LabelTextRow;
import inshow.carl.com.csd.view.WatchNumberPicker;

import static inshow.carl.com.csd.csd.core.ConvertDataMgr.I2B_Control;
import static inshow.carl.com.csd.csd.core.ConvertDataMgr.I2B_WatchTime;
import static inshow.carl.com.csd.csd.core.CsdConstant.CHARACTERISTIC_3102;
import static inshow.carl.com.csd.csd.core.CsdConstant.CHARACTERISTIC_3106;
import static inshow.carl.com.csd.csd.core.CsdConstant.CHARACTERISTIC_3107;
import static inshow.carl.com.csd.csd.core.CsdConstant.SERVICE_INSO;

/**
 * Created by chendong on 2018/7/27.
 */

public class NewAdjustTimeFirstAct extends BasicSingleButtonAct {
    private LabelTextRow minLabelTextRow;
    private LabelTextRow hourLabelTextRow;
    private TextView tvAccurate;
    private int selectH = -1;
    private int selectM = -1;
    private int settingTime;

   @Subscribe
 public void onEventMainThread(AdjustTimeBus event) {
     if (event.finish) {
         if (hasSelected()) {
             generateSettingTime();
               bleInstance.writeCharacteristic(MAC, UUID.fromString(SERVICE_INSO), UUID.fromString(CHARACTERISTIC_3107), I2B_WatchTime(settingTime));
           }
           finish();
       }
   }

    @Override
    protected String getTipText() {
        return getString(R.string.pls_set_pos);
    }

    @Override
    protected String getBtnText() {
        return getString(R.string.next_step);
    }

    protected int getContentViewLayout() {
        return R.layout.watch_content_time_first;
    }

    protected void initViewOrData() {
        super.initViewOrData();
        btn.setEnabled(false);
        minLabelTextRow = (LabelTextRow) contentView.findViewById(R.id.minLocation);
        hourLabelTextRow = (LabelTextRow) contentView.findViewById(R.id.hourLocation);
        tvAccurate = (TextView) contentView.findViewById(R.id.tv_accurate);
        minLabelTextRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMinutePositionDialog();
            }
        });
        hourLabelTextRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHourPositionDialog();
            }
        });
        tvAccurate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchTo(NewAdjustTimeThirdAct.class);
            }
        });
        //停表
        bleInstance.writeCharacteristic(MAC, UUID.fromString(SERVICE_INSO), UUID.fromString(CHARACTERISTIC_3102), I2B_Control(new int[]{1, 0, 0, 0}));
    }

    @Override
    protected void btnOnClick() {
        switchTo(NewAdjustTimeSecAct.class);
    }

    public void showHourPositionDialog() {
        View v = View.inflate(mContext, R.layout.watch_dialog_line, null);
        final WatchNumberPicker lp = (WatchNumberPicker) v.findViewById(R.id.lp);
        lp.setMinValue(0);
        lp.setMaxValue(23);
        lp.setDisplayedValues(getHourDisplayStrings());
        lp.setLabel("");
        lp.setValue(selectH >-1?selectH:0);
        new MLAlertDialog.Builder(mContext).setTitle(getString(R.string.h_hand_position))
                .setView(v).
                setPositiveButton(mContext.getResources().getString(R.string.button_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
//                        selectH = lp.getValue() / 2 + 1;
                        selectH = lp.getValue();
                        hourLabelTextRow.setText(getHourDisplayStrings()[selectH]);
                        if (selectH % 2 == 0) {
                            minLabelTextRow.setText("0");
                            selectM = 0;
                        }
                        if (hasSelected()) {
                            btn.setEnabled(true);
                        }
                    }
                }).setNegativeButton(mContext.getResources().getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        }).setCancelable(false).show();
    }

    public void showMinutePositionDialog() {
        View v = View.inflate(mContext, R.layout.watch_dialog_line, null);
        final WatchNumberPicker lp = (WatchNumberPicker) v.findViewById(R.id.lp);
        lp.setMinValue(0);
        lp.setMaxValue(59);
        lp.setLabel("");
        lp.setValue(selectM >-1?selectM:0);
        lp.setDisplayedValues(getMinDisplayStrings());
        new MLAlertDialog.Builder(mContext).setTitle(getString(R.string.m_hand_position))
                .setView(v).
                setPositiveButton(mContext.getResources().getString(R.string.button_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        selectM = lp.getValue();
                        minLabelTextRow.setText(getMinDisplayStrings()[selectM]);
                        if (selectH > -1) {
                            if (selectM == 0 && selectH % 2 == 1) {
                                L.d("selectM == 0 && selectH % 2 == 1");
                                selectH = selectH - 1;
                                hourLabelTextRow.setText(getHourDisplayStrings()[selectH]);
                            } else if (selectM != 0 && selectH % 2 == 0) {
                                L.d("selectM != 0 && selectH % 2 == 0");
                                selectH = selectH + 1;
                                hourLabelTextRow.setText(getHourDisplayStrings()[selectH]);
                            }
                            if (hasSelected()) {
                                btn.setEnabled(true);
                            }
                        }
                    }
                }).setNegativeButton(mContext.getResources().getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        }).setCancelable(false).show();
    }

    private String[] getHourDisplayStrings() {
        return getResources().getStringArray(R.array.hour_positon);
    }

    private String[] getMinDisplayStrings() {
        String[] ret = new String[60];
        for (int i = 0; i < 60; i++) {
            ret[i] = String.valueOf(i);
        }
        return ret;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //恢复时针走针
        bleInstance.writeCharacteristic(MAC, UUID.fromString(SERVICE_INSO), UUID.fromString(CHARACTERISTIC_3102), I2B_Control(new int[]{2, 0, 0, 0}));
    }

    private void generateSettingTime() {
        settingTime = (selectH / 2) * 3600 + selectM * 60;
    }

    private boolean hasSelected() {
        return selectH >= 0 && selectM >= 0;
    }
}
