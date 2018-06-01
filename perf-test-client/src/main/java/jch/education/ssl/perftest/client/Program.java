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
        SSLClientConfiguration clientConfig = readConfiguration(args);
        final SSLContext sslContext = SSLContextFactory.createClientSSLContext(clientConfig);

        Socket socket = null;

        try {
            List<IterationSummary> iterationSummaries = new LinkedList<>();
            final int iterationCount = 10;

            long startTime = System.currentTimeMillis();
            for (int i = 1; i <= iterationCount; i++) {
                socket = connectToServer(clientConfig, sslContext);
                IterationSummary summary = sendAndReceiveMessages(socket);
                iterationSummaries.add(summary);
                Stdout.traceln("Iteration %d/%d: %d messages/%d bytes sent, duration = %d millis", i,
                        iterationCount, summary.overallMessageCount(), summary.overallByteCount(),
                        summary.durationMillis());
            }
            long durationMillis = System.currentTimeMillis() - startTime;

            TestSummary testSummary = new TestSummary(durationMillis, iterationSummaries);
            System.out.printf("Overall duration:         %d ms%n", testSummary.overallDurationMillis());
            System.out.printf("Min. iteration duration:  %d ms%n", testSummary.minIterationDuration());
            System.out.printf("Max. iteration duration:  %d ms%n", testSummary.maxIterationDuration());
            System.out.printf("Avg. iteration duration:  %.1f ms%n", testSummary.averageIterationDuration());
        } finally {
            ResourceCleanupToolkit.close(socket);
        }
    }

    private static SSLClientConfiguration readConfiguration(String args[]) throws IOException {
        if ((args == null) || (args.length == 0)) {
            System.out.println("ERROR!!! Missing command line argument.");
            System.out.println("Single command line argument specifying client config. file is expected.");
            System.exit(1);
        }

        SSLClientConfiguration clientConfig = SSLClientConfiguration.fromFile(args[0]);
        clientConfig.dumpTo(System.out);
        return clientConfig;
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

        return socket;
    }

    private static IterationSummary sendAndReceiveMessages(Socket socket) throws Exception {
        final int messageSize = 25 * 1024;
        final int messageCount = 50000;

        final LinkedList<byte[]> messages = createMessages(messageCount, messageSize);
        OutputStream outputStream = null;
        InputStream inputStream = null;

        try {
            outputStream = socket.getOutputStream();
            inputStream = socket.getInputStream();
            Stdout.traceln("I/O streams obtained from the socket...");

            long startTime = System.currentTimeMillis();
            for (int i = 1; i <= messageCount; i++) {
                byte[] singleMessage = messages.removeFirst();
                outputStream.write(singleMessage);

                if ((i % 500) == 0) {
                    Stdout.traceln("Message %d/%d written to socket...", i, messageCount);
                }
            }

            SocketIO.closeCooperatively(outputStream, inputStream);
            long durationMillis = System.currentTimeMillis() - startTime;
            Stdout.traceln("Writer/reader closed using EOF...");

            if (!messages.isEmpty()) {
                Stdout.traceln("ERROR!!! Remaining messages found (totally %d)...", messages.size());
            }

            return new IterationSummary(messageCount, messageCount * messageSize, durationMillis);
        } catch (IOException e) {
            Stdout.traceln("Going to close the connection...");
            SocketIO.closeImmediately(socket);
            throw e;
        }
    }

    private static LinkedList<byte[]> createMessages(int messageCount, int messageSize) {
        LinkedList<byte[]> result = new LinkedList<>();
        for (int i = 0; i < messageCount; i++) {
            byte[] message = MessageFactory.createRandomBinaryMessage(messageSize);
            result.add(message);
        }
        return result;
    }
}
