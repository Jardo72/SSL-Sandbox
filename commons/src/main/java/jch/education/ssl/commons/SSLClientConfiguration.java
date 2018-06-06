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

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class SSLClientConfiguration extends AbstractConfiguration {

    @JsonProperty(value = "key_store", required = false)
    private KeyStoreSettings keyStore;

    @JsonProperty(value = "trust_store", required = true)
    private KeyStoreSettings trustStore;

    private SSLClientConfiguration() {}

    public static SSLClientConfiguration fromFile(String path) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        SSLClientConfiguration result = mapper.readValue(new File(path), SSLClientConfiguration.class);
        result.setConfigFile(path);

        if (result.ssl().useClientAuthentication() && (result.keyStore == null)) {
            String message = "Key-store must be defined if client authentication is to be supported.";
            throw new IllegalStateException(message);
        }

        result.trustStore.resolvePath(path);
        if (result.keyStore != null) {
            result.keyStore.resolvePath(path);
        }

        return result;
    }

    @Override
    public KeyStoreSettings keyStore() {
        return this.keyStore;
    }

    @Override
    public KeyStoreSettings trustStore() {
        return this.trustStore;
    }
}
