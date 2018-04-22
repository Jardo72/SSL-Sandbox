package jch.education.ssl.commons;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class SSLServerConfiguration extends AbstractConfiguration {

    @JsonProperty(value = "key_store", required = true)
    private KeyStoreSettings keyStore;

    @JsonProperty(value = "trust_store", required = false)
    private KeyStoreSettings trustStore;

    private SSLServerConfiguration() {}

    public static SSLServerConfiguration fromFile(String path) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        SSLServerConfiguration result = mapper.readValue(new File(path), SSLServerConfiguration.class);
        result.setConfigFile(path);

        if (result.ssl().useClientAuthentication() && (result.trustStore == null)) {
            String message = "Trust-store must be defined if client authentication is required.";
            throw new IllegalStateException(message);
        }

        result.keyStore.resolvePath(path);
        if (result.trustStore != null) {
            result.trustStore.resolvePath(path);
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
