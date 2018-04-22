package jch.education.ssl.commons;

import java.io.PrintStream;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonProperty;

abstract class AbstractConfiguration {

    @JsonProperty(value = "tcp", required = true)
    private TCPSettings tcp;

    @JsonProperty(value = "ssl", required = true)
    private SSLSettings ssl;

    @JsonProperty(value = "key_manager", required = false)
    private KeyOrTrustManagerSettings keyManagerSettings;

    @JsonProperty(value = "trust_manager", required = false)
    private KeyOrTrustManagerSettings trustManagerSettings;

    private String configFile;

    protected AbstractConfiguration() {}


    public String configFile() {
        return this.configFile;
    }

    protected void setConfigFile(String configFile) {
        this.configFile = configFile;
    }


    public TCPSettings tcp() {
        return this.tcp;
    }

    public SSLSettings ssl() {
        return this.ssl;
    }

    public abstract KeyStoreSettings keyStore();

    public abstract KeyStoreSettings trustStore();

    public KeyOrTrustManagerSettings keyManagerSettings() {
        return this.keyManagerSettings;
    }

    public KeyOrTrustManagerSettings trustManagerSettings() {
        return this.trustManagerSettings;
    }

    public boolean hasKeyManagerSettings() {
        return this.keyManagerSettings != null;
    }

    public boolean hasTrustManagerSettings() {
        return this.trustManagerSettings != null;
    }

    public void dumpTo(PrintStream out) {
        out.println();
        out.println("Configuration ---------------------------------------");
        out.printf("Configuration file:    %s%n", this.configFile);
        out.println();

        if (this.ssl.useCustomProvider()) {
            out.printf("Provider:              %s%n", this.ssl.provider());
        }
        out.printf("IP address:            %s%n", this.tcp.address());
        out.printf("Port:                  %d%n", this.tcp.port());
        out.println("Protocols:");
        Stream.of(this.ssl.protocols()).forEach(s -> out.printf("- %s%n", s));
        out.println("Cipher suites:");
        Stream.of(this.ssl.cipherSuites()).forEach(s -> out.printf("- %s%n", s));
        out.printf("Client authentication: %s%n", this.ssl.useClientAuthentication());
        out.println("-----------------------------------------------------");
        out.println();
    }
}
