package inshow.carl.com.csd.csd.basic;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import inshow.carl.com.csd.AppController;
import inshow.carl.com.csd.BasicAct;
import inshow.carl.com.csd.R;
import inshow.carl.com.csd.csd.core.BleManager;
import inshow.carl.com.csd.tools.L;
import inshow.carl.com.csd.tools.SPManager;

/**
 * Comment:
 * Author: ftc300
 * Date: 2018/11/5
 * Blog: www.ftc300.pub
 * GitHub: https://github.com/ftc300
 */

public class AdjustBasicAct extends BasicAct {
    protected View mTitleView, mContentView;
    protected FrameLayout flTitle, flContent, flSelectAll;
    protected LinearLayout llBasic;
    protected Context mContext;
    public static final String EXTRAS_EVENT_BUS = "EVENTBUS";
    public static final String EXTRAS_EVENT_MAC = "EXTRAS_EVENT_MAC";
    public static final String EXTRAS_EVENT_VERSION = "EXTRAS_EVENT_VERSION";
    protected String MAC ;
    protected String VERSION ;
    protected BleManager bleInstance = BleManager.getInstance();
    //直接跳转
    protected void switchTo(Class to) {
        Intent i = new Intent(AdjustBasicAct.this,to);
        startActivity(i);
    }

    //带任意参数的跳转
    protected void switchTo(Class<?> to, Map<String, Object> extras) {
        Intent i = new Intent();
        i.setClass(AdjustBasicAct.this,to.getClass());
        Map<String, Object> map = new HashMap<>();
        putExtras(extras, i);
        startActivity(i);
    }


    //EvebtBus的跳转
    protected void switchToWithEventBus(Class to) {
        Intent i = new Intent();
        i.setClass(AdjustBasicAct.this,to.getClass());
        Map<String, Object> map = new HashMap<>();
        map.put(EXTRAS_EVENT_BUS, true);
        putExtras(map, i);
        startActivity(i);
    }
    /**
     * intent 中 传递数据
     *
     * @param extras
     * @param i
     */
    protected static void putExtras(Map<String, Object> extras, Intent i) {
        if (extras != null) {
            for (String name : extras.keySet()) {
                Object obj = extras.get(name);
                if (obj instanceof String) {
                    i.putExtra(name, (String) obj);
                }
                if (obj instanceof Integer) {
                    i.putExtra(name, (Integer) obj);
                }
                if (obj instanceof String[]) {
                    i.putExtra(name, (String[]) obj);
                }
                if (obj instanceof Boolean) {
                    i.putExtra(name, (Boolean) obj);
                }
            }
        }
    }

    protected void setBtnOnBackPress() {
        findViewById(R.id.title_bar_return).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.watch_activity_base);
        mContext = this;
        MAC  = (String )SPManager.get(context,EXTRAS_EVENT_MAC,"");
        VERSION  = (String )SPManager.get(context,EXTRAS_EVENT_VERSION,"");
        flTitle = (FrameLayout) findViewById(R.id.act_base_title);
        flContent = (FrameLayout) findViewById(R.id.act_base_content);
        flSelectAll = (FrameLayout) findViewById(R.id.select_all_title_bar);
        llBasic = (LinearLayout) findViewById(R.id.act_base);
        //注册EventBus
        if (getIntent().getBooleanExtra(EXTRAS_EVENT_BUS, false))
            EventBus.getDefault().register(this);
        //渲染页面
        if (!isNeedTitle()) {
            flTitle.setVisibility(View.GONE);
        } else if (mTitleView == null) {
            final int contentRes = getTitleRes();
            if (contentRes > 0) {
                mTitleView = View.inflate(AdjustBasicAct.this, contentRes, null);
                if (mTitleView != null)
                    flTitle.addView(mTitleView);
            }
        }
        if (mContentView == null) {
            final int contentRes = getContentRes();
            if (contentRes > 0) {
                mContentView = View.inflate(AdjustBasicAct.this, contentRes, null);
                if (mContentView != null)
                    flContent.addView(mContentView);
            }
        }
        preInitViewData();
        initViewOrData();
    }

    /**
     * 获取Content需要显示的View的资源文件
     *
     * @return
     */
    protected int getContentRes() {
        return 0;
    }

    /**
     * 获取Title需要显示的View的资源文件
     *
     * @return
     */
    protected int getTitleRes() {
        return R.layout.watch_title_bar_transparent_white;
    }
    protected boolean isNeedTitle() {
        return true;
    }

    protected void initViewOrData() {

    }

    protected void preInitViewData() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {//防止异常导致unregister抛异常
            if (getIntent().getBooleanExtra(EXTRAS_EVENT_BUS, false))
                EventBus.getDefault().unregister(this);//取消注册
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
