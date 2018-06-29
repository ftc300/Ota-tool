package inshow.carl.com.csd.entity;

/**
 * Created by chendong on 2018/6/27.
 * 米家手表
 */

public class MiWatch {
   public String mac;
   public boolean pressed;

    public MiWatch(String mac, boolean pressed) {
        this.mac = mac;
        this.pressed = pressed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MiWatch miWatch = (MiWatch) o;

        return mac.equals(miWatch.mac);
    }

    @Override
    public int hashCode() {
        return mac.hashCode();
    }
}
