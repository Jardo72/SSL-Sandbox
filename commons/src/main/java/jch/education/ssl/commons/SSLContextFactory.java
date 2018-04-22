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

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.Security;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jsse.provider.BouncyCastleJsseProvider;

public class SSLContextFactory {

    private static final String PROTOCOL = "TLS";

    private SSLContextFactory() {}

    public static SSLContext createClientSSLContext(AbstractConfiguration config) throws Exception {
        registerBouncyCastle();
        SSLContext sslContext = createSSLContext(config);

        TrustManager[] trustManagers = createTrustManagers(config);

        KeyManager[] keyManagers = null;
        if (config.ssl().useClientAuthentication()) {
            keyManagers = createKeyManagers(config);
        }

        sslContext.init(keyManagers, trustManagers, null);
        Stdout.traceln("Client SSL context initialized, provider = %s...", sslContext.getProvider().getClass().getName());

        return sslContext;
    }

    public static SSLContext createServerSSLContext(AbstractConfiguration config) throws Exception {
        registerBouncyCastle();
        SSLContext sslContext = createSSLContext(config);

        KeyManager[] keyManagers = createKeyManagers(config);

        TrustManager[] trustManagers = null;
        if (config.ssl().useClientAuthentication()) {
            trustManagers = createTrustManagers(config);
        }

        sslContext.init(keyManagers, trustManagers, null);
        Stdout.traceln("Server SSL context initialized, provider = %s...", sslContext.getProvider().getClass().getName());

        return sslContext;
    }

    private static SSLContext createSSLContext(AbstractConfiguration config) throws Exception {
        if (config.ssl().useCustomProvider()) {
            return SSLContext.getInstance(PROTOCOL, config.ssl().provider());
        } else {
            return SSLContext.getInstance(PROTOCOL);
        }
    }

    private static KeyManager[] createKeyManagers(AbstractConfiguration config) throws Exception {
        KeyStoreSettings settings = config.keyStore();
        Stdout.traceln("Going to load key-store from file %s...", settings.path());
        KeyStore keyStore = loadKeyStore(settings);

        KeyManagerFactory keyManagerFactory = null;
        if (config.hasKeyManagerSettings()) {
            String algorithm = config.keyManagerSettings().algorithm();
            String provider = config.keyManagerSettings().provider();
            Stdout.traceln("Key-manager algorithm = %s, provider = %s...", algorithm, provider);
            keyManagerFactory = KeyManagerFactory.getInstance(algorithm, provider);
        } else {
            String algorithm = KeyManagerFactory.getDefaultAlgorithm();
            Stdout.traceln("Key-manager algorithm = %s...", algorithm);
            keyManagerFactory = KeyManagerFactory.getInstance(algorithm);
        }

        keyManagerFactory.init(keyStore, settings.password().toCharArray());
        return keyManagerFactory.getKeyManagers();
    }

    private static TrustManager[] createTrustManagers(AbstractConfiguration config) throws Exception {
        KeyStoreSettings settings = config.trustStore();
        Stdout.traceln("Going to load trust-store from file %s...", settings.path());
        KeyStore trustStore = loadKeyStore(settings);

        TrustManagerFactory trustManagerFactory = null;
        if (config.hasTrustManagerSettings()) {
            String algorithm = config.trustManagerSettings().algorithm();
            String provider = config.trustManagerSettings().provider();
            Stdout.traceln("Trust-manager algorithm = %s, provider = %s...", algorithm, provider);
            trustManagerFactory = TrustManagerFactory.getInstance(algorithm, provider);
        } else {
            String algorithm = TrustManagerFactory.getDefaultAlgorithm();
            Stdout.traceln("Trust-manager algorithm = %s...", algorithm);
            trustManagerFactory = TrustManagerFactory.getInstance("PKIX");
        }
        trustManagerFactory.init(trustStore);
        return trustManagerFactory.getTrustManagers();
    }

    private static KeyStore loadKeyStore(KeyStoreSettings settings) throws Exception {
        KeyStore keyStore = KeyStore.getInstance(settings.algorithm());
        InputStream inputStream = null;

        try {
            inputStream = new FileInputStream(settings.path());
            keyStore.load(inputStream, settings.password().toCharArray());
            return keyStore;
        } finally {
            ResourceCleanupToolkit.close(inputStream);
        }
    }

    private static void registerBouncyCastle() {
        Security.addProvider(new BouncyCastleProvider());
        Security.addProvider(new BouncyCastleJsseProvider());
    }
}
