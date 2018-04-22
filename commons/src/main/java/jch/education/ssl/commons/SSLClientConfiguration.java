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
