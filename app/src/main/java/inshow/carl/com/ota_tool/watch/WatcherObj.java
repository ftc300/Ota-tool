package inshow.carl.com.ota_tool.watch;

import inshow.carl.com.ota_tool.MainPagerHelper;

/**
 * @ 创建者:   CoderChen
 * @ 时间:     2018/4/12
 * @ 描述:
 */


public class WatcherObj implements Watcher<MainPagerHelper.IScanHelper> {

    @Override
    public void updata(MainPagerHelper.IScanHelper helper) {
        helper.deviceFound();
    }
}
