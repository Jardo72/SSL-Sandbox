package jch.education.ssl.perftest.client;

import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.stream.Collectors;

public class TestSummary {

    private final long overallDurationMillis;

    private final LongSummaryStatistics iterationsSummary;

    public TestSummary(long overallDurationMillis, List<IterationSummary> iterations) {
        this.overallDurationMillis = overallDurationMillis;
        this.iterationsSummary = iterations.stream()
                .collect(Collectors.summarizingLong(summary -> summary.durationMillis()));
    }

    public long overallDurationMillis() {
        return this.overallDurationMillis;
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
