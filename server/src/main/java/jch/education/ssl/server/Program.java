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
package jch.education.ssl.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import jch.education.ssl.commons.SSLContextFactory;
import jch.education.ssl.commons.SSLServerConfiguration;
import jch.education.ssl.commons.Stdout;

public class Program {

    public static void main(String[] args) throws Exception {
        SSLServerConfiguration serverConfig = readConfiguration(args);
        final SSLContext sslContext = SSLContextFactory.createServerSSLContext(serverConfig);
        ServerSocket serverSocket = openServerSocket(serverConfig, sslContext);
        handleInboundConnections(serverSocket);
    }

    private static SSLServerConfiguration readConfiguration(String args[]) throws IOException {
        if ((args == null) || (args.length == 0)) {
            System.out.println("ERROR!!! Missing command line argument.");
            System.out.println("Single command line argument specifying server config. file is expected.");
            System.exit(1);
        }

        SSLServerConfiguration serverConfig = SSLServerConfiguration.fromFile(args[0]);
        serverConfig.dumpTo(System.out);
        return serverConfig;
    }

    private static ServerSocket openServerSocket(SSLServerConfiguration config, SSLContext sslContext) throws Exception {
        SSLServerSocketFactory factory = sslContext.getServerSocketFactory();
        SSLServerSocket serverSocket = (SSLServerSocket) factory.createServerSocket();
        serverSocket.setEnabledProtocols(config.ssl().protocols());
        serverSocket.setEnabledCipherSuites(config.ssl().cipherSuites());
        if (config.ssl().useClientAuthentication()) {
            serverSocket.setNeedClientAuth(true);
        }
        serverSocket.bind(config.tcp().socketAddress());

        Stdout.traceln("Server socket listening on %s...", serverSocket.getLocalSocketAddress());

        return serverSocket;
    }

    private static void handleInboundConnections(ServerSocket serverSocket) throws Exception {
        final ExecutorService threadPool = Executors.newFixedThreadPool(10);
        Stdout.traceln("Thread pool for client handlers started...");

        while (true) {
            SSLSocket socket = (SSLSocket) serverSocket.accept();
            threadPool.submit(new ClientHandler(socket));
        }
    }
}
