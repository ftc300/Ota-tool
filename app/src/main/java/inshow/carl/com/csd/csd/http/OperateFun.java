package inshow.carl.com.csd.csd.http;

/**
 * Comment:
 * Author: ftc300
 * Date: 2018/11/12
 * Blog: www.ftc300.pub
 * GitHub: https://github.com/ftc300
 */
//{"mac":"XXX","time":1490900914,"step_hand":"xxx",version:"xx","Value":"{}"}
//{"mac":"XXX","time":1490900914,"time_hand":"xxx",version:"xx","Value":"{}"}
//{"mac":"XXX","time":1490900914,"vibrate":"xxx",version:"xx","Value":"{}"}
//{"mac":"XXX","time":1490900914,"presskey":"xxx",version:"xx","Value":"{count:0}"}按了测试按钮
//{"mac":"XXX","time":1490900914,"presskey":"xxx",version:"xx","Value":"{count:1}"}收到了按了表冠的消息
//{"mac":"XXX","time":1490900914,"adjust":"xxx",version:"xx","Value":"{input_time:1490900914}"}
public class OperateFun {
    long time;
    String mac;
    String key;
    String version;
    Value value;

    public OperateFun(long arg_time, String arg_mac, String arg_key, String arg_version) {
        time = arg_time;
        mac = arg_mac;
        key = arg_key;
        version = arg_version;
    }

    public OperateFun(long arg_time, String arg_mac, String arg_key, String arg_version, Value arg_value) {
        time = arg_time;
        mac = arg_mac;
        key = arg_key;
        version = arg_version;
        value = arg_value;
    }

    public static class Value{
        int pressCount;
        long input_time;

        public Value() {
        }

        public Value(int arg_pressCount) {
            pressCount = arg_pressCount;
        }

        public Value(long arg_input_time) {
            input_time = arg_input_time;
        }
    }

}
