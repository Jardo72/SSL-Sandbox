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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.SSLSocket;

import jch.education.ssl.commons.MessageFactory;
import jch.education.ssl.commons.SocketIO;
import jch.education.ssl.commons.Stdout;
import jch.education.ssl.commons.Timing;

public class ClientHandler implements Callable<Boolean> {

    private static final AtomicInteger sequence = new AtomicInteger(1);

    private final int id = sequence.getAndIncrement();

    private final SSLSocket socket;

    private int messageSeqNo = 1;

    public ClientHandler(SSLSocket socket) {
        this.socket = socket;
    }

    @Override
    public Boolean call() throws Exception {
        BufferedReader reader = null;
        PrintWriter writer = null;

        try {
            String threadName = String.format("ClientHandler-%d", this.id);
            Thread.currentThread().setName(threadName);

            Stdout.traceln("New connection accepted from %s...", this.socket.getRemoteSocketAddress());
            Stdout.printSSLSession(this.socket.getSession());

            reader = SocketIO.readerFrom(this.socket);
            writer = SocketIO.writerFrom(this.socket);
            Stdout.traceln("Reader/writer obtained from the socket...");

            String welcomeMessage = MessageFactory.createServerGreeting();
            Stdout.traceln(welcomeMessage);
            writer.println(welcomeMessage);

            String clientMessage = reader.readLine();
            while (clientMessage != null) {
                Stdout.traceln(clientMessage);
                Timing.randomSleep(800, 3500, TimeUnit.MILLISECONDS);
                String serverMessage = MessageFactory.createServerMessage(this.messageSeqNo++);
                Stdout.traceln(serverMessage);
                writer.println(serverMessage);
                clientMessage = reader.readLine();
            }

            SocketIO.closeCooperatively(writer, reader);
            Stdout.traceln("Writer/reader closed using EOF...");

            return true;
        } catch (IOException e) {
            Stdout.traceln("Going to close the socket...");
            SocketIO.closeImmediately(this.socket);
            throw e;
        }
    }
}
