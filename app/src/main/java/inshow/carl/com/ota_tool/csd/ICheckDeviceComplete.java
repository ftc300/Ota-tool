package inshow.carl.com.ota_tool.csd;

import java.util.HashSet;

import inshow.carl.com.ota_tool.entity.MiWatch;

/**
 * Created by chendong on 2018/6/28.
 */

public interface ICheckDeviceComplete {
    void checkFinished(HashSet<MiWatch> set);
}
