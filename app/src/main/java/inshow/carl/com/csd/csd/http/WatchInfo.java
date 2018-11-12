package inshow.carl.com.csd.csd.http;

/**
 * Comment:
 * Author: ftc300
 * Date: 2018/11/9
 * Blog: www.ftc300.pub
 * GitHub: https://github.com/ftc300
 */
//{"mac":"XXX","time":1490900914,"watch_info":"xxx",version:"xx","Value":"{battery_enough:true,rtc_time=1490900914 }"}
//{"mac":"XXX","time":1490900914,"step_hand":"xxx",version:"xx","Value":"{}"}
//{"mac":"XXX","time":1490900914,"time_hand":"xxx",version:"xx","Value":"{}"}
//{"mac":"XXX","time":1490900914,"vibrate":"xxx",version:"xx","Value":"{}"}
//{"mac":"XXX","time":1490900914,"presskey":"xxx",version:"xx","Value":"{count=0}"}按了测试按钮
//{"mac":"XXX","time":1490900914,"presskey":"xxx",version:"xx","Value":"{count=1}"}收到了按了表冠的消息
//{"mac":"XXX","time":1490900914,"adjust":"xxx",version:"xx","Value":"{input_time:1490900914}"}
//    连接后上传第一条记录
//            mac，固件版本，步数，电量，手表rtc时间，手机时间
//
//            每点击按键时上传一条记录
//            mac，步针测试，手机时间
//            mac，分针测试，手机时间
//            mac，振动测试，手机时间
//            mac，按键测试，手机时间
//            mac，校准，输入时间，手机时间 （确定后上传）

public class WatchInfo {
    long time;
    String mac;
    String key;
    String version;
    Value value;

    public static class Value {
        int stepCount;
        int rtc_time;
        boolean battery_enough;

        public Value(int arg_stepCount, int arg_rtc_time, boolean arg_battery_enough) {
            stepCount = arg_stepCount;
            rtc_time = arg_rtc_time;
            battery_enough = arg_battery_enough;
        }
    }

    public WatchInfo(long arg_time, String arg_mac, String arg_key, String arg_version, Value arg_value) {
        time = arg_time;
        mac = arg_mac;
        key = arg_key;
        version = arg_version;
        value = arg_value;
    }
}
