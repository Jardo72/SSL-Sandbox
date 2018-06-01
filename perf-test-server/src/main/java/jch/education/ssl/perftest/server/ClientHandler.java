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
package jch.education.ssl.perftest.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.SSLSocket;

import jch.education.ssl.commons.SocketIO;
import jch.education.ssl.commons.Stdout;

public class ClientHandler implements Callable<Boolean> {

    private static final AtomicInteger sequence = new AtomicInteger(1);

    private final int id = sequence.getAndIncrement();

    private final SSLSocket socket;

    public ClientHandler(SSLSocket socket) {
        this.socket = socket;
    }

    @Override
    public Boolean call() throws Exception {
        OutputStream outputStream = null;
        InputStream inputStream = null;

        try {
            String threadName = String.format("ClientHandler-%d", this.id);
            Thread.currentThread().setName(threadName);

            Stdout.traceln("New connection accepted from %s...", this.socket.getRemoteSocketAddress());
            Stdout.printSSLSession(this.socket.getSession());

            outputStream = this.socket.getOutputStream();
            inputStream = this.socket.getInputStream();
            Stdout.traceln("I/O streams obtained from the socket...");

            final byte[] buffer = new byte[50 * 1024];
            long overallByteCount = 0;
            int bytesRead = inputStream.read(buffer);
            while (bytesRead != -1) {
                overallByteCount += bytesRead;
                bytesRead = inputStream.read(buffer);
            }
            Stdout.traceln("Totally %d bytes read from the socket...", overallByteCount);

            SocketIO.closeCooperatively(outputStream, inputStream);
            Stdout.traceln("Writer/reader closed using EOF...");

            return true;
        } catch (IOException e) {
            Stdout.traceln("Going to close the socket...");
            SocketIO.closeImmediately(this.socket);
            throw e;
        }
    }
}
