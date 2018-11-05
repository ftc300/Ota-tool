package inshow.carl.com.csd.csd.basic;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import inshow.carl.com.csd.R;
import inshow.carl.com.csd.tools.L;

/**
 * Created by chendong on 2018/7/27.
 */

public abstract class BasicMultiButtonAct extends AdjustBasicAct implements IDriver {
    protected TextView tip;
    protected Button btnLeft;
    protected Button btnRight;
    protected FrameLayout frameLayout;
    protected View contentView;
    protected LayoutInflater inflater;
    protected boolean isLongOnClick;

    @Override
    protected int getContentRes() {
        return R.layout.watch_multibutton_act;
    }

    @Override
    protected int getTitleRes() {
        return R.layout.title_bar_transparent_black;
    }

    @Override
    protected void initViewOrData() {
        super.initViewOrData();
        findViewById(R.id.title_bar_more).setVisibility(View.GONE);
        setBtnOnBackPress();
        inflater = LayoutInflater.from(mContext);
        tip = (TextView) findViewById(R.id.tv_tip);
        btnLeft = (Button) findViewById(R.id.left);
        btnRight = (Button) findViewById(R.id.right);
        frameLayout = (FrameLayout) findViewById(R.id.contentPanel);
        if (!TextUtils.isEmpty(getTipText())) {
            tip.setText(getTipText());
        }

        if (!TextUtils.isEmpty(getLeftBtnText())) {
            btnLeft.setText(getLeftBtnText());
        }

        if (!TextUtils.isEmpty(getRightBtnText())) {
            btnRight.setText(getRightBtnText());
        }

        if(getContentView() != null) {
            frameLayout.addView(getContentView());
        }

        btnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLeftClick();
            }
        });

        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRightClick();
            }
        });

        btnLeft.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                isLongOnClick = true;
                new Thread(){
                    public void run() {
                        while (isLongOnClick){
                            try {
                                onStartDriver();
                                Thread.sleep(100);
                            }catch(InterruptedException e){
                                e.printStackTrace();
                            }
                        }
                    }
                }.start();
                return true;
            }
        });

        btnLeft.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if( motionEvent.getAction() == MotionEvent.ACTION_UP ){
                    isLongOnClick = false;
                }
                if(motionEvent.getAction() == MotionEvent.ACTION_CANCEL){
                    isLongOnClick = false;
                }
                return false;
            }

        });

    }

    protected abstract void onRightClick();

    protected abstract void onLeftClick();

    protected abstract String getTipText();

    protected abstract String getLeftBtnText();

    protected abstract String getRightBtnText();

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
