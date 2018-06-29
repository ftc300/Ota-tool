package inshow.carl.com.csd.csd.iface;

import java.util.HashSet;

import inshow.carl.com.csd.entity.MiWatch;

/**
 * Created by chendong on 2018/6/28.
 */

public interface ICheckDeviceComplete {
    void checkFinished(HashSet<MiWatch> set);
}
