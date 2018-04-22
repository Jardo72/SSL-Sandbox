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

import java.util.UUID;

public class MessageFactory {

    private static final String CLIENT = "CLIENT";

    private static final String SERVER = "SERVER";

    private MessageFactory() {}

    public static String createServerGreeting() {
        return String.format("%s -> %s: %s", SERVER, CLIENT, "Welcome to SSL Sandbox Server");
    }

    public static String createClientMessage(int sequenceNumber) {
        return createMessage(CLIENT, SERVER, sequenceNumber);
    }

    public static String createServerMessage(int sequenceNumber) {
        return createMessage(SERVER, CLIENT, sequenceNumber);
    }

    private static String createMessage(String sender, String recipient, int sequenceNumber) {
        return String.format("%s -> %s (seq-no %d): %s", sender, recipient, sequenceNumber, UUID.randomUUID());
    }
}
