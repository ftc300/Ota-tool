package inshow.carl.com.csd.csd.basic;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import inshow.carl.com.csd.R;


/**
 * Created by chendong on 2018/7/27.
 */

public abstract class BasicSingleButtonAct extends AdjustBasicAct {
    protected TextView tip;
    protected Button btn;
    protected FrameLayout frameLayout;
    protected View contentView;
    protected LayoutInflater inflater;

    @Override
    protected int getContentRes() {
        return R.layout.watch_singlebutton_act;
    }

    @Override
    protected int getTitleRes() {
        return R.layout.title_bar_transparent_black;
    }

    @Override
    protected void initViewOrData() {
        super.initViewOrData();
        findViewById(R.id.title_bar_more).setVisibility(View.GONE);
        inflater = LayoutInflater.from(mContext);
        setBtnOnBackPress();
        tip = (TextView) findViewById(R.id.tv_tip);
        btn = (Button) findViewById(R.id.ok);
        frameLayout = (FrameLayout) findViewById(R.id.contentPanel);
        if (!TextUtils.isEmpty(getTipText())) {
            tip.setText(getTipText());
        }

        if (!TextUtils.isEmpty(getBtnText())) {
            btn.setText(getBtnText());
        }

        if(getContentView() != null) {
            frameLayout.addView(getContentView());
        }

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnOnClick();
            }
        });
    }

    protected abstract void btnOnClick();

    protected abstract String getTipText();

    protected abstract String getBtnText();

    private View getContentView(){
        if(getContentViewLayout() > 0) {
            contentView = inflater.inflate(getContentViewLayout(), null);
            return contentView;
        }
        return null;
    }

    protected  int getContentViewLayout(){
        return -1;
    }



}
