package in.nimbo.entity;

import java.util.Date;

public class SiteReport {
    private Date date;
    private String channel;
    private int count;

    public SiteReport() {
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SiteReport report = (SiteReport) o;

        if (count != report.count) return false;
        if (date != null ? !date.equals(report.date) : report.date != null) return false;
        return channel != null ? channel.equals(report.channel) : report.channel == null;
    }

    @Override
    public int hashCode() {
        int result = date != null ? date.hashCode() : 0;
        result = 31 * result + (channel != null ? channel.hashCode() : 0);
        result = 31 * result + count;
        return result;
    }

    @Override
    public String toString() {
        return "SiteReport{" +
                "date=" + date +
                ", channel='" + channel + '\'' +
                ", count=" + count +
                '}';
    }
}
