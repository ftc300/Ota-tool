package inshow.carl.com.csd.csd.http;

/**
 * Comment:
 * Author: ftc300
 * Date: 2018/11/9
 * Blog: www.ftc300.pub
 * GitHub: https://github.com/ftc300
 */
//{"mac":"XXX","time":1490900914,"watch-info":"xxx",version:"","value":"{}"}
//{"mac":"XXX","time":1490900914,"stepHand":"xxx",version:"","value":"{}"}
//{"mac":"XXX","time":1490900914,"timeHand":"xxx",version:"","value":"{}"}
//{"mac":"XXX","time":1490900914,"vibrate":"xxx",version:"","value":"{}"}
//{"mac":"XXX","time":1490900914,"vibrate":"xxx",version:"","value":"{}"}
//    连接后上传第一条记录
//            mac，固件版本，步数，电量，手表rtc时间，手机时间
//
//            每点击按键时上传一条记录
//            mac，步针测试，手机时间
//            mac，分针测试，手机时间
//            mac，振动测试，手机时间
//            mac，按键测试，手机时间
//            mac，校准，输入时间，手机时间 （确定后上传）

public class WatchInfo extends BaseReqParam{
    value mValue;
    static class value{
        int stepCount;
        boolean isEnough;
    }
}
