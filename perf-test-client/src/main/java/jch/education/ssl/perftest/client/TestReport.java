/*
 * Copyright 2018 Jaroslav Chmurny
 *
 * This file is part of SSL Sandbox.
 *
 * SSL Sandbox is free software developed for educational purposes. It
 * is licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
