package jch.education.ssl.perftest.client;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.stream.Collectors;

public class TestSummary {

    private final TimeSpan timeSpan;

    private final long overallMessageCount;

    private final long overallByteCount;

    private final List<IterationSummary> iterations;

    private final LongSummaryStatistics iterationsSummary;

    public TestSummary(TimeSpan timeSpan, List<IterationSummary> iterations) {
        this.timeSpan = timeSpan;
        this.iterations = iterations;
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

    public void dumpTo(PrintStream out) {
        dumpSummaryTo(out);
        dumpIterationsTo(out);
    }

    private void dumpSummaryTo(PrintStream out) {
        out.println();
        out.println("Test summary ----------------------------------------");
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        out.printf("Start time:               %s%n", dateFormat.format(this.timeSpan.startTime()));
        out.printf("End time:                 %s%n", dateFormat.format(this.timeSpan.endTime()));
        out.printf("Overall duration:         %d ms%n", this.timeSpan.durationMillis());
        out.printf("Iteration count:          %d%n", iterationCount());
        out.printf("Overall message count:    %d%n", overallMessageCount());
        out.printf("Overall byte count:       %d%n", overallByteCount());
        out.println();
        out.printf("Min. iteration duration:  %d ms%n", minIterationDuration());
        out.printf("Max. iteration duration:  %d ms%n", maxIterationDuration());
        out.printf("Avg. iteration duration:  %.1f ms%n", averageIterationDuration());
        out.println("-----------------------------------------------------");
        out.println();
    }

    private void dumpIterationsTo(PrintStream out) {
        out.println();
        out.println("Test iterations -------------------------------------");
        int i = 1;
        for (IterationSummary singleIteration : this.iterations) {
            out.printf("Iteration %d/%d: %d messages/%d bytes sent, duration = %d millis%n", i,
                    this.iterations.size(), singleIteration.overallMessageCount(),
                    singleIteration.overallByteCount(), singleIteration.durationMillis());
            i++;
        }
        out.println("-----------------------------------------------------");
        out.println();
    }
}
