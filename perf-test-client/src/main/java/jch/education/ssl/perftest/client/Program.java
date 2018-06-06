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
        TestParameters testParams = readTestParameters(args);
        final SSLContext sslContext = SSLContextFactory.createClientSSLContext(clientConfig);

        Socket socket = null;

        try {
            List<IterationSummary> iterationSummaries = new LinkedList<>();

            long startTime = System.currentTimeMillis();
            for (int i = 1; i <= testParams.connectionCount(); i++) {
                socket = connectToServer(clientConfig, sslContext);
                IterationSummary summary = sendAndReceiveMessages(socket, testParams);
                iterationSummaries.add(summary);
                Stdout.traceln("Iteration %d/%d: %d messages/%d bytes sent, duration = %d millis", i,
                        testParams.connectionCount(), summary.overallMessageCount(),
                        summary.overallByteCount(), summary.durationMillis());
            }
            TimeSpan timeSpan = new TimeSpan(startTime, System.currentTimeMillis());
            TestSummary testSummary = new TestSummary(timeSpan, iterationSummaries);

            String reportFile = TestReport.write(clientConfig, testParams, testSummary);
            Stdout.traceln("Test report written to file %s", reportFile);
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
            System.out.println("ERROR!!! Missing command line argument(s).");
            System.out.println("Two command line arguments are expected, specifying:");
            System.out.println("- client config. file");
            System.out.println("- test parameters file");
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

        // TODO: do this only if requested by the configuration
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
        for (int i = 0; i < testParameters.messagesPerConnection(); i++) {
            byte[] message = MessageFactory.createRandomBinaryMessage(testParameters.messageSizeInBytes());
            result.add(message);
        }
        return result;
    }
}
