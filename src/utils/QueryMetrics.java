package utils;

/**
 * <p>
 * <strong> Description </strong>
 * </p>
 * <p>
 * The class <code>QueryMetrics</code> is a props class that contains the
 * metrics of a query.
 * </p>
 */
public class QueryMetrics {
    protected long startTime;
    protected long endTime;
    protected long interval;
    protected long duration;
    protected int nbSensors;

    /**
     * Constructor of the QueryMetrics
     * 
     * @param startTime
     * @param endTime
     * @param interval
     * @param duration
     * @param nbSensors
     */
    public QueryMetrics(long startTime, long endTime, long interval, long duration, int nbSensors) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.interval = interval;
        this.duration = duration;
        this.nbSensors = nbSensors;
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

    public int getNbSensors() {
        return nbSensors;
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

    public void setNbSensors(int nbSensors) {
        this.nbSensors = nbSensors;
    }

    public String toString() {
        return "QueryMetrics [startTime=" + startTime + ", endTime=" + endTime + ", interval=" + interval
                + ", duration=" + duration + "]";
    }

}
