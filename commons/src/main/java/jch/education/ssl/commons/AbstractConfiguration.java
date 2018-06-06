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
        out.println("SSL Configuration -----------------------------------");
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
