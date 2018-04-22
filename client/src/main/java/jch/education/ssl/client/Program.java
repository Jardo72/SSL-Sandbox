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
package jch.education.ssl.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import jch.education.ssl.commons.MessageFactory;
import jch.education.ssl.commons.ResourceCleanupToolkit;
import jch.education.ssl.commons.SSLClientConfiguration;
import jch.education.ssl.commons.SSLContextFactory;
import jch.education.ssl.commons.SocketIO;
import jch.education.ssl.commons.Stdout;
import jch.education.ssl.commons.Timing;

public class Program {

    public static void main(String[] args) throws Exception {
        SSLClientConfiguration clientConfig = readConfiguration(args);
        final SSLContext sslContext = SSLContextFactory.createClientSSLContext(clientConfig);

        Socket socket = null;

        try {
            socket = connectToServer(clientConfig, sslContext);
            sendAndReceiveMessages(socket);
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

    private static void sendAndReceiveMessages(Socket socket) throws Exception {
        final int messageCount = 3 + new Random().nextInt(7);

        BufferedReader reader = null;
        PrintWriter writer = null;

        try {
            reader = SocketIO.readerFrom(socket);
            writer = SocketIO.writerFrom(socket);
            Stdout.traceln("Reader/writer obtained from the socket...");

            String welcomeMessage = reader.readLine();
            Stdout.traceln(welcomeMessage);

            for (int i = 1; i <= messageCount; i++) {
                String clientMessage = MessageFactory.createClientMessage(i);
                Stdout.traceln(clientMessage);
                writer.println(clientMessage);

                String serverMessage = reader.readLine();
                Stdout.traceln(serverMessage);

                Timing.randomSleep(3000, 10000, TimeUnit.MILLISECONDS);
            }

            SocketIO.closeCooperatively(writer, reader);
            Stdout.traceln("Writer/reader closed using EOF...");
        } catch (IOException e) {
            Stdout.traceln("Going to close the connection...");
            SocketIO.closeImmediately(socket);
            throw e;
        }
    }
}
