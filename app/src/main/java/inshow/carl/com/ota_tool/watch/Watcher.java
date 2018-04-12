package inshow.carl.com.ota_tool.watch;

/**
 * @ 创建者:   CoderChen
 * @ 时间:     2018/4/12
 * @ 描述:观察者接口
 */

public interface Watcher<T> {
    public void updata(T t);
}
