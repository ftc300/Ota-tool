package inshow.carl.com.csd.ble.callback;

/**
 * 这一个类用于表示什么也不是, 什么也没有
 * <p/>
 */
public final class NONE {

    public static final NONE NONE = new NONE();

    private NONE() {
    }

    @Override
    public String toString() {
        return "NONE";
    }
}
