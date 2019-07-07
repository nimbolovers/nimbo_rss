package in.nimbo.entity.report;

import java.util.Objects;

/**
 * represents count of news for each hour for each site
 */
public class HourReport extends Report {
    private int hour;

    public HourReport(String channel, int count, int hour) {
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

        HourReport that = (HourReport) o;
        return Objects.equals(getChannel(), that.getChannel())
                && Objects.equals(getCount(), that.getCount())
                && hour == that.hour;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getChannel(), hour, getCount());
    }

}
