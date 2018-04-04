package inshow.carl.com.ota_tool;

import android.app.Application;

import com.orm.SugarContext;

/**
 * @ 创建者:   CoderChen
 * @ 时间:     2018/4/4
 * @ 描述:
 */


public class AppController extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SugarContext.init(this);
    }
    @Override
    public void onTerminate() {
        super.onTerminate();
        SugarContext.terminate();
    }
}
