package jch.education.ssl.perftest.client;

public class IterationSummary {

    private final TestParameters testParameters;

    private final long durationMillis;

    public IterationSummary(TestParameters testParameters, long durationMillis) {
        this.testParameters = testParameters;
        this.durationMillis = durationMillis;
    }

    public int overallMessageCount() {
        return this.testParameters.messagesPerConnection();
    }

    public long overallByteCount() {
        return this.testParameters.messagesPerConnection() * this.testParameters.messageSizeInBytes();
    }

    public long durationMillis() {
        return this.durationMillis;
    }
}
