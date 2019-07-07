package in.nimbo.entity.report;

import java.util.Date;
import java.util.Objects;

/**
 * represents count of news for each day for each site
 */
public class DateReport extends Report {
    private Date date;

    public DateReport(String channel, int count, Date date) {
        super(channel, count);
        this.date = date;
    }

    public Date getDate() {
        return date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DateReport report = (DateReport) o;

        return getCount() == report.getCount()
                && Objects.equals(date, report.date)
                && Objects.equals(getChannel(), this.getChannel());
    }
}
