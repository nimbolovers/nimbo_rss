package in.nimbo.entity.report;

import java.util.Objects;

public class Report {
    private String channel;
    private int count;

    public Report(String channel, int count) {
        this.channel = channel;
        this.count = count;
    }

    public String getChannel() {
        return channel;
    }

    public int getCount() {
        return count;
    }

    @Override
    public String toString() {
        return "Report{" +
                "channel='" + channel + '\'' +
                ", count=" + count +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Report report = (Report) o;

        return count == report.count &&
                Objects.equals(channel, report.channel);
    }
}
