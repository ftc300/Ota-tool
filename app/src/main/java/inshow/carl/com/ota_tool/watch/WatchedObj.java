package inshow.carl.com.ota_tool.watch;

import java.util.ArrayList;
import java.util.List;

import inshow.carl.com.ota_tool.MainPagerHelper;

/**
 * @ 创建者:   CoderChen
 * @ 时间:     2018/4/12
 * @ 描述:
 */

public class WatchedObj implements Watched<MainPagerHelper.IScanHelper> {
    List<Watcher> list = new ArrayList<>();

    @Override
    public void addWatcher(Watcher watcher) {
        list.add(watcher);
    }

    @Override
    public void removeWatcher(Watcher watcher) {
        list.remove(watcher);
    }

    @Override
    public void notifyWatcher(MainPagerHelper.IScanHelper helper) {
         for (Watcher i : list) {
            i.updata(helper);
        }
    }
}
