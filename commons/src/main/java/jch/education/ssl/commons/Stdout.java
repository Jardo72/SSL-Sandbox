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

import java.net.SocketAddress;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.net.ssl.SSLSession;

public class Stdout {

    private static final String DATE_FORMAT = "dd-MMM-yyyy HH:mm:ss,SS";

    private Stdout() {}

    public static void traceln(String message, Object... args) {
        preprocessArgs(args);
        message = String.format(message, args);
        synchronized (System.out) {
            System.out.printf("%s [%s]: %s", currentTime(), currentThread(), message);
            System.out.println();
        }
    }

    public static void printSSLSession(SSLSession sslSession) {
        synchronized (System.out) {
            System.out.println();
            traceln("SSL session");
            System.out.printf("  Is valid:     %b%n", sslSession.isValid());
            System.out.printf("  Protocol:     %s%n", sslSession.getProtocol());
            System.out.printf("  Cipher suite: %s%n", sslSession.getCipherSuite());
            System.out.printf("  Peer host:    %s%n", sslSession.getPeerHost());
            System.out.printf("  Peer port:    %d%n", sslSession.getPeerPort());
            System.out.println();
        }
    }

    private static void preprocessArgs(Object... args) {
        if (args == null) {
            return;
        }

        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof SocketAddress) {
                String addressAsString = args[i].toString();
                if (addressAsString.startsWith("/")) {
                    addressAsString = addressAsString.substring(1);
                }
                args[i] = addressAsString;
            }
        }
    }

    private static String currentTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        return dateFormat.format(new Date());
    }

    private static String currentThread() {
        String result = Thread.currentThread().getName();
        if ((result == null) || result.isEmpty()) {
            result = "UNKNOWN";
        }
        return result;
    }
}
