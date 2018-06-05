package jch.education.ssl.perftest.client;

import java.util.Date;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.stream.Collectors;

public class TestSummary {

    private final TimeSpan timeSpan;

    private final long overallMessageCount;

    private final long overallByteCount;

    private final LongSummaryStatistics iterationsSummary;

    public TestSummary(TimeSpan timeSpan, List<IterationSummary> iterations) {
        this.timeSpan = timeSpan;
        this.iterationsSummary = iterations.stream()
                .collect(Collectors.summarizingLong(summary -> summary.durationMillis()));
        this.overallMessageCount = iterations.stream()
                .collect(Collectors.summarizingInt(summary -> summary.overallMessageCount())).getSum();
        this.overallByteCount = iterations.stream()
                .collect(Collectors.summarizingLong(summary -> summary.overallByteCount())).getSum();
    }

    public Date startTime() {
        return new Date(this.timeSpan.startTime());
    }

    public Date endTime() {
        return new Date(this.timeSpan.endTime());
    }

    public long overallDurationMillis() {
        return this.timeSpan.durationMillis();
    }

    public long iterationCount() {
        return this.iterationsSummary.getCount();
    }

    public long overallMessageCount() {
        return this.overallMessageCount;
    }

    public long overallByteCount() {
        return this.overallByteCount;
    }

    public long minIterationDuration() {
        return this.iterationsSummary.getMin();
    }

    public long maxIterationDuration() {
        return this.iterationsSummary.getMax();
    }

    public double averageIterationDuration() {
        return this.iterationsSummary.getAverage();
    }
}
