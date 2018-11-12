package inshow.carl.com.csd.csd.core;

/**
 * Comment:
 * Author: ftc300
 * Date: 2018/11/12
 * Blog: www.ftc300.pub
 * GitHub: https://github.com/ftc300
 */

public class HttpMgr {

    private HttpMgr() {
    }

    public static HttpMgr getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private static class SingletonHolder {
        private static final HttpMgr INSTANCE = new HttpMgr();
    }



}
