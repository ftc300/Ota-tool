package inshow.carl.com.csd.csd.http;

/**
 * Comment:
 * Author: ftc300
 * Date: 2018/11/9
 * Blog: www.ftc300.pub
 * GitHub: https://github.com/ftc300
 */
//{"mac":"XXX","time":1490900914,"watch_info":"xxx",version:"xx","value":"{battery_enough:true,rtc_time=1490900914 }"}
//{"mac":"XXX","time":1490900914,"step_hand":"xxx",version:"xx","value":"{}"}
//{"mac":"XXX","time":1490900914,"time_hand":"xxx",version:"xx","value":"{}"}
//{"mac":"XXX","time":1490900914,"vibrate":"xxx",version:"xx","value":"{}"}
//{"mac":"XXX","time":1490900914,"presskey":"xxx",version:"xx","value":"{count=0}"}按了测试按钮
//{"mac":"XXX","time":1490900914,"presskey":"xxx",version:"xx","value":"{count=1}"}收到了按了表冠的消息
//{"mac":"XXX","time":1490900914,"adjust":"xxx",version:"xx","value":"{input_time:1490900914}"}
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
    value mValue;
    static class value{
        int stepCount;
        boolean isEnough;
    }
}
