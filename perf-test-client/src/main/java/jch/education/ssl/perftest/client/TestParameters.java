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

    private TestParameters() {}

    public static TestParameters fromFile(String path) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        TestParameters result = mapper.readValue(new File(path), TestParameters.class);
        return result;
    }

    public void dumpTo(PrintStream out) {
        out.println();
        out.println("Test parameters -------------------------------------");
        out.printf("Number of connections:   %d%n", this.connectionCount);
        out.printf("Messages per connection: %d%n", this.messagesPerConnection);
        out.printf("Message size [bytes]:    %d%n", this.messageSizeInBytes);
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
}
