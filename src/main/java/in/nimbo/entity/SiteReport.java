package in.nimbo.entity;

import java.util.Date;

public class SiteReport extends Report {
    private Date date;

    public SiteReport(String channel, int count, Date date) {
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

        SiteReport report = (SiteReport) o;

        if (getCount() != report.getCount()) return false;
        if (date != null ? !date.equals(report.date) : report.date != null) return false;
        return getChannel() != null ? getChannel().equals(report.getChannel()) : report.getChannel() == null;
    }

    @Override
    public int hashCode() {
        int result = date != null ? date.hashCode() : 0;
        result = 31 * result + (getChannel() != null ? getChannel().hashCode() : 0);
        result = 31 * result + getCount();
        return result;
    }

    @Override
    public String toString() {
        return "SiteReport{" +
                "date=" + date +
                ", channel='" + getChannel() + '\'' +
                ", count=" + getCount() +
                '}';
    }
}
