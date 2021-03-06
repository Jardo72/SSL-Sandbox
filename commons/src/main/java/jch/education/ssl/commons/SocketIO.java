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
package jch.education.ssl.commons;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketIO {

    private SocketIO() {}

    public static BufferedReader readerFrom(Socket socket) throws IOException {
        return new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public static PrintWriter writerFrom(Socket socket) throws IOException {
        final boolean autoFlush = true;
        return new PrintWriter(socket.getOutputStream(), autoFlush);
    }

    public static void closeCooperatively(PrintWriter writer, BufferedReader reader) {
        ResourceCleanupToolkit.close(writer);
        waitForEndOfStream(reader);
        ResourceCleanupToolkit.close(reader);
    }

    public static void closeCooperatively(OutputStream outputStream, InputStream inputStream) {
        ResourceCleanupToolkit.close(outputStream);
        waitForEndOfStream(inputStream);
        ResourceCleanupToolkit.close(inputStream);
    }

    public static void closeImmediately(Socket socket) {
        ResourceCleanupToolkit.close(socket);
    }

    private static void waitForEndOfStream(BufferedReader reader) {
        try {
            while (reader.readLine() != null) {}
        } catch (IOException ignore) {}
    }

    private static void waitForEndOfStream(InputStream inputStream) {
        try {
            while (inputStream.read() != -1) {}
        } catch (IOException ignore) {}
    }
}
