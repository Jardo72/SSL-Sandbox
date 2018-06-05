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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import jch.education.ssl.commons.MessageFactory;
import jch.education.ssl.commons.ResourceCleanupToolkit;
import jch.education.ssl.commons.SSLClientConfiguration;
import jch.education.ssl.commons.SSLContextFactory;
import jch.education.ssl.commons.SocketIO;
import jch.education.ssl.commons.Stdout;

public class Program {

    public static void main(String[] args) throws Exception {
        SSLClientConfiguration clientConfig = readSSLConfiguration(args);
        TestParameters testParameters = readTestParameters(args);
        final SSLContext sslContext = SSLContextFactory.createClientSSLContext(clientConfig);

        Socket socket = null;

        try {
            List<IterationSummary> iterationSummaries = new LinkedList<>();
            final int iterationCount = 200;

            long startTime = System.currentTimeMillis();
            for (int i = 1; i <= testParameters.connectionCount(); i++) {
                socket = connectToServer(clientConfig, sslContext);
                IterationSummary summary = sendAndReceiveMessages(socket, testParameters);
                iterationSummaries.add(summary);
                Stdout.traceln("Iteration %d/%d: %d messages/%d bytes sent, duration = %d millis", i,
                        iterationCount, summary.overallMessageCount(), summary.overallByteCount(),
                        summary.durationMillis());
            }
            TimeSpan timeSpan = new TimeSpan(startTime, System.currentTimeMillis());

            TestSummary testSummary = new TestSummary(timeSpan, iterationSummaries);
            print(testSummary);
        } finally {
            ResourceCleanupToolkit.close(socket);
        }
    }

    private static SSLClientConfiguration readSSLConfiguration(String args[]) throws IOException {
        validateCommandLineArguments(args);
        SSLClientConfiguration clientConfig = SSLClientConfiguration.fromFile(args[0]);
        clientConfig.dumpTo(System.out);
        return clientConfig;
    }

    private static TestParameters readTestParameters(String[] args) throws IOException {
        validateCommandLineArguments(args);
        TestParameters testParameters = TestParameters.fromFile(args[1]);
        testParameters.dumpTo(System.out);
        return testParameters;
    }

    private static void validateCommandLineArguments(String[] args) {
        if ((args == null) || (args.length < 2)) {
            System.out.println("ERROR!!! Missing command line argument.");
            System.out.println("Single command line argument specifying client config. file is expected.");
            System.exit(1);
        }
    }

    private static Socket connectToServer(SSLClientConfiguration config, SSLContext sslContext) throws Exception {
        SSLSocketFactory socketFactory = sslContext.getSocketFactory();
        SSLSocket socket = (SSLSocket) socketFactory.createSocket();
        socket.setEnabledProtocols(config.ssl().protocols());
        socket.setEnabledCipherSuites(config.ssl().cipherSuites());
        if (config.ssl().useClientAuthentication()) {
            socket.setNeedClientAuth(true);
        }
        socket.connect(config.tcp().socketAddress());
        Stdout.traceln("Connected to server, local endpoint %s...", socket.getLocalSocketAddress());
        Stdout.printSSLSession(socket.getSession());

        // this prevents session caching, which would make the performance test useless
        socket.getSession().invalidate();

        return socket;
    }

    private static IterationSummary sendAndReceiveMessages(Socket socket, TestParameters testParameters) throws Exception {
        final LinkedList<byte[]> messages = createMessages(testParameters);
        OutputStream outputStream = null;
        InputStream inputStream = null;

        try {
            outputStream = socket.getOutputStream();
            inputStream = socket.getInputStream();
            Stdout.traceln("I/O streams obtained from the socket...");

            long startTime = System.currentTimeMillis();
            for (int i = 1; i <= testParameters.messagesPerConnection(); i++) {
                byte[] singleMessage = messages.removeFirst();
                outputStream.write(singleMessage);

                if ((i % 1000) == 0) {
                    Stdout.traceln("Message %d/%d written to socket...", i, testParameters.messagesPerConnection());
                }
            }

            SocketIO.closeCooperatively(outputStream, inputStream);
            long durationMillis = System.currentTimeMillis() - startTime;
            Stdout.traceln("Writer/reader closed using EOF...");

            if (!messages.isEmpty()) {
                Stdout.traceln("ERROR!!! Remaining messages found (totally %d)...", messages.size());
            }

            return new IterationSummary(testParameters, durationMillis);
        } catch (IOException e) {
            Stdout.traceln("Going to close the connection...");
            SocketIO.closeImmediately(socket);
            throw e;
        }
    }

    private static LinkedList<byte[]> createMessages(TestParameters testParameters) {
        LinkedList<byte[]> result = new LinkedList<>();
        for (int i = 0; i < testParameters.connectionCount(); i++) {
            byte[] message = MessageFactory.createRandomBinaryMessage(testParameters.messageSizeInBytes());
            result.add(message);
        }
        return result;
    }

    private static void print(TestSummary testSummary) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        System.out.printf("Start time:               %s%n", dateFormat.format(testSummary.startTime()));
        System.out.printf("End time:                 %s%n", dateFormat.format(testSummary.endTime()));
        System.out.printf("Overall duration:         %d ms%n", testSummary.overallDurationMillis());
        System.out.printf("Iteration count:          %d%n", testSummary.iterationCount());
        System.out.printf("Overall message count:    %d%n", testSummary.overallMessageCount());
        System.out.printf("Overall byte count:       %d%n", testSummary.overallByteCount());
        System.out.println();
        System.out.printf("Min. iteration duration:  %d ms%n", testSummary.minIterationDuration());
        System.out.printf("Max. iteration duration:  %d ms%n", testSummary.maxIterationDuration());
        System.out.printf("Avg. iteration duration:  %.1f ms%n", testSummary.averageIterationDuration());
    }
}
