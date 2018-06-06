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

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class TestParameters {

    @JsonProperty(value = "connection_count", required = true)
    private int connectionCount;

    @JsonProperty(value = "messages_per_connection", required = true)
    private int messagesPerConnection;

    @JsonProperty(value = "message_size_in_bytes", required = true)
    private int messageSizeInBytes;

    @JsonProperty(value = "disable_session_resumption", defaultValue = "true")
    private boolean disableSessionResumption;

    private String configFile;

    private TestParameters() {}

    public static TestParameters fromFile(String path) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        TestParameters result = mapper.readValue(new File(path), TestParameters.class);
        result.configFile = path;
        return result;
    }

    public void dumpTo(PrintStream out) {
        out.println();
        out.println("Test parameters -------------------------------------");
        out.printf("Configuration file:      %s%n", this.configFile);
        out.printf("Number of connections:   %d%n", this.connectionCount);
        out.printf("Messages per connection: %d%n", this.messagesPerConnection);
        out.printf("Message size [bytes]:    %d%n", this.messageSizeInBytes);
        out.printf("Session resumption:      %s%n", sessionResumptionStatus());
        out.println("-----------------------------------------------------");
        out.println();
    }

    public int connectionCount() {
        return this.connectionCount;
    }

    public int messagesPerConnection() {
        return this.messagesPerConnection;
    }

    public int messageSizeInBytes() {
        return this.messageSizeInBytes;
    }

    public boolean disableSessionResumption() {
        return this.disableSessionResumption;
    }

    private String sessionResumptionStatus() {
        if (this.disableSessionResumption) {
            return "DISABLED";
        }
        return "ENABLED";
    }
}
