package model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hisrc.w3c.atom.v_1_0.Link;

public class LinkHourlyCount {
    public static final int maxHour = 30;

    private int[] hour = new int[maxHour];
    @Getter
    @Setter
    private String link;
    @Getter
    @Setter
    private String stationNo;

    public LinkHourlyCount inc(int hour) {
        this.hour[hour]++;
        return this;
    }

    public LinkHourlyCount set(int hour, int cnt) {
        this.hour[hour] = cnt;
        return this;
    }

    public int[] getCounts() {
        return hour;
    }

    public static LinkHourlyCount linkInit(int hour) {
        LinkHourlyCount lhc = new LinkHourlyCount();
        lhc.inc(hour);
        return lhc;
    }

    public static LinkHourlyCount linkInit(String pointId, int hour, int cnt) {
        LinkHourlyCount lhc = new LinkHourlyCount();
        lhc.set(hour, cnt);
        lhc.setStationNo(pointId);
        return lhc;
    }
}
