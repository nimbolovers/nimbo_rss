package in.nimbo.entity.report;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * represents count of news for each day for each site
 */
public class DateReport extends Report {
    private LocalDateTime date;

    public DateReport(String channel, int count, LocalDateTime date) {
        super(channel, count);
        this.date = date;
    }

    public LocalDateTime getDate() {
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
