package inshow.carl.com.ota_tool.entity;

import com.orm.SugarRecord;

/**
 * @ 创建者:   CoderChen
 * @ 时间:     2018/4/4
 * @ 描述:
 */

public class DeviceEntity extends SugarRecord {
    public String mac ;
    public int process;
    public int state ; //0: 准备升级;1:升级成功;2s失败
    public long timestamp;

    public DeviceEntity() {
    }

    public DeviceEntity(String mac, int process, int state, long timestamp) {
        this.mac = mac;
        this.process = process;
        this.state = state;
        this.timestamp = timestamp;
    }
}
