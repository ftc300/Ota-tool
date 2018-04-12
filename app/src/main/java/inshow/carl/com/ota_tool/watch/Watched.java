package inshow.carl.com.ota_tool.watch;

/**
 * @ 创建者:   CoderChen
 * @ 时间:     2018/4/12
 * @ 描述:被观察者接口
 */


public interface Watched<T> {
    public void addWatcher(Watcher watcher);
    public void removeWatcher(Watcher watcher);
    public void notifyWatcher(T t);
}
