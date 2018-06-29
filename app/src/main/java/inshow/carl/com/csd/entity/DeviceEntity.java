package inshow.carl.com.csd.entity;

import android.text.TextUtils;

import com.orm.SugarRecord;

import inshow.carl.com.csd.tools.Utils;

/**
 * @ 创建者:   CoderChen
 * @ 时间:     2018/4/4
 * @ 描述:
 */

public class DeviceEntity extends SugarRecord {
    public String mac;
    public int process;
    public int state;
    public String filePath;

    public String getTrueMac() {
        if (TextUtils.isEmpty(mac))
            return "";
        return Utils.formatMac(mac);
    }

    public DeviceEntity() {
    }

    public DeviceEntity(String mac, int process, int state ,String filePath) {
        this.mac = mac;
        this.process = process;
        this.state = state;
        this.filePath = filePath;
    }
}
