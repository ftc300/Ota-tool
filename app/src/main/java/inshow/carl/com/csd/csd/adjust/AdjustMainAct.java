package inshow.carl.com.csd.csd.adjust;

import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import inshow.carl.com.csd.BasicAct;
import inshow.carl.com.csd.R;
import inshow.carl.com.csd.csd.TestWatchAct;
import inshow.carl.com.csd.csd.basic.AdjustBasicAct;

/**
 * @ 创建者:   CoderChen
 * @ 时间:     2017/8/25
 * @ 描述:
 */

public class AdjustMainAct extends AdjustBasicAct {

    private TextView tvTime,tvTimeStatus,tvStep,tvStepStatus;

    @Subscribe
    public void onEventMainThread(AdjustTimeBus event) {
        if(event.finish) {
            tvTimeStatus.setVisibility(View.VISIBLE);
            tvTimeStatus.postDelayed(new Runnable() {
                @Override
                public void run() {
                    tvTimeStatus.setVisibility(View.INVISIBLE);
                }
            },2000);
        }
    }

    @Subscribe
    public void onEventMainThread(AdjustStepBus event) {
        if(event.finish) {
            tvStepStatus.setVisibility(View.VISIBLE);
            tvStepStatus.postDelayed(new Runnable() {
                @Override
                public void run() {
                    tvStepStatus.setVisibility(View.INVISIBLE);
                }
            }, 2000);
        }
    }

    @Override
    protected int getContentRes() {
        return R.layout.watch_act_adjust_main;
    }

    @Override
    protected void initViewOrData() {
        ((ImageView) findViewById(R.id.title_bar_return)).setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.std_tittlebar_main_device_back));
        ((TextView) findViewById(R.id.title_bar_title)).setTextColor(ContextCompat.getColor(mContext, R.color.std_word_001));
        ((TextView) findViewById(R.id.title_bar_title)).setText("指针校准");
        findViewById(R.id.divider_line).setVisibility(View.VISIBLE);
        setBtnOnBackPress();
        tvTime = (TextView) findViewById(R.id.tv_adjust_time);
        tvTimeStatus = (TextView) findViewById(R.id.tv_adjust_time_status);
        tvStep= (TextView) findViewById(R.id.tv_adjust_step);
        tvStepStatus = (TextView) findViewById(R.id.tv_adjust_step_status);
        tvTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AdjustMainAct.this, NewAdjustTimeFirstAct.class);
                intent.putExtra(EXTRAS_EVENT_BUS,true);
                startActivity(intent);
            }
        });

        tvStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AdjustMainAct.this, NewAdjustStepFirstAct.class);
                intent.putExtra(EXTRAS_EVENT_BUS,true);
                startActivity(intent);
            }
        });
    }
}
