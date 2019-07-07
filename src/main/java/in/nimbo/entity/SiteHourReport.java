package in.nimbo.entity;

import java.util.Objects;

public class SiteHourReport extends Report {
    private int hour;

    public SiteHourReport(String channel, int count, int hour) {
        super(channel, count);
        this.hour = hour;
    }

    public int getHour() {
        return hour;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SiteHourReport that = (SiteHourReport) o;
        if (!Objects.equals(getChannel(), that.getChannel())) return false;
        if (!Objects.equals(getCount(), that.getCount())) return false;
        return hour == that.hour;
    }

    @Override
    public int hashCode() {
        return hour;
    }

    @Override
    public String toString() {
        return "SiteHourReport{" +
                "hour=" + hour +
                ", channel=" + getChannel() +
                ", count=" + getCount() +
                '}';
    }
}
