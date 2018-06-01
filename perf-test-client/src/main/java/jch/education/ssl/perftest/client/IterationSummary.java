package jch.education.ssl.perftest.client;

public class IterationSummary {

    private final int overallMessageCount;

    private final long overallByteCount;

    private final long durationMillis;

    public IterationSummary(int overallMessageCount, long overallByteCount, long durationMillis) {
        this.overallMessageCount = overallMessageCount;
        this.overallByteCount = overallByteCount;
        this.durationMillis = durationMillis;
    }

    public int overallMessageCount() {
        return this.overallMessageCount;
    }

    public long overallByteCount() {
        return this.overallByteCount;
    }

    public long durationMillis() {
        return this.durationMillis;
    }
}