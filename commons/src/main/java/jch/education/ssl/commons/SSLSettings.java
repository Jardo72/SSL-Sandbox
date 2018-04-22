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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SSLSettings {

    private static String[] STRING_ARRAY = new String[0];

    @JsonProperty(value = "provider", required = false)
    private String provider;

    @JsonProperty(value = "protocols", required = true)
    private List<String> protocols;

    @JsonProperty(value = "cipher_suites", required = true)
    private List<String> cipherSuites;

    @JsonProperty(value = "use_client_authentication", defaultValue = "false")
    private boolean useClientAuthentication;

    public String provider() {
        return this.provider;
    }

    public boolean useCustomProvider() {
        return (this.provider != null) && (!this.provider.isEmpty());
    }

    public String[] protocols() {
        return this.protocols.toArray(STRING_ARRAY);
    }

    public String[] cipherSuites() {
        return this.cipherSuites.toArray(STRING_ARRAY);
    }

    public boolean useClientAuthentication() {
        return this.useClientAuthentication;
    }
}
