package jch.education.ssl.perftest.client;

import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import jch.education.ssl.commons.ResourceCleanupToolkit;
import jch.education.ssl.commons.SSLClientConfiguration;

public class TestReport {

    private final SSLClientConfiguration clientConfig;

    private final TestParameters testParams;

    private final TestSummary testSummary;

    private TestReport(SSLClientConfiguration clientConfig, TestParameters testParams, TestSummary testSummary) {
        this.clientConfig = clientConfig;
        this.testParams = testParams;
        this.testSummary = testSummary;
    }

    public static String write(SSLClientConfiguration clientConfig, TestParameters testParams, TestSummary summary) throws IOException {
        return new TestReport(clientConfig, testParams, summary).write();
    }

    private String write() throws IOException {
        String filename = generateFileName();
        PrintStream out = null;

        try {
            out = new PrintStream(filename);
            this.clientConfig.dumpTo(out);
            this.testParams.dumpTo(out);
            this.testSummary.dumpTo(out);
            return filename;
        } finally {
            ResourceCleanupToolkit.close(out);
        }
    }

    private static String generateFileName() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
        return String.format("test-report-%s.txt", dateFormat.format(new Date()));
    }
}
