package utils;

public class QueryMetrics {
    protected long startTime;
    protected long endTime;
    protected long interval;
    protected long duration;

    public QueryMetrics(long startTime, long endTime, long interval, long duration) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.interval = interval;
        this.duration = duration;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public long getInterval() {
        return interval;
    }

    public long getDuration() {
        return duration;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String toString() {
        return "QueryMetrics [startTime=" + startTime + ", endTime=" + endTime + ", interval=" + interval
                + ", duration=" + duration + "]";
    }

}
