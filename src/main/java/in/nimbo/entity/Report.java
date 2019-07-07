package in.nimbo.entity;

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
}
