package jch.education.ssl.perftest.client;

public class TimeSpan {

    private final long startTime;

    private final long endTime;

    TimeSpan(long startTime, long endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public long startTime() {
        return this.startTime;
    }

    public long endTime() {
        return this.endTime;
    }

    public long durationMillis() {
        return this.endTime - this.startTime;
    }
}
