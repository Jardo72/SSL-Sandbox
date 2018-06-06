# SSL Sandbox


## Introduction
SSL Sandbox is an educational/experimental project allowing to play around with:

- SSL/TLS protocols
- Java API related to SSL/TLS

The project involves two pairs of console Java applications:
- Dummy SSL server plus dummy SSL client. The client establishes a secure (i.e. SSL/TLS) connection to the server. Subsequently, several random messages are exchanged between the two endpoints. This pair of applications is primarily meant to play around with the handshake.
- Performance test SSL server plus SSL client. This pair of applications allows to measure the performance impact of various cipher suites and/or key sizes. The client establishes the prescribed number of connections, and each connection is used to send the given number of messages (the messages size is configurable as well). The connections are established sequentially, one after the termination of the other.

In order to get an idea about the internals of SSL/TLS, you can use the first pair of the applications and:

- Capture the network traffic between the client and the server and use [Wireshark](https://www.wireshark.org) to analyze it.
- Activate the tracing for the SSL/TLS implementation you use and analyze the produced output. For instance, if you use Oracle Java and the default JSSE provider, use the `javax.net.debug` system property to do so.

The server is able to handle two or more clients simultaneously. For each accepted connection, it starts a separate thread. During the execution, both endpoints write status messages to the standard output. The messages also involve the current timestamp, so you can easily correlate them with the captured network traffic.

Each of the two endpoints can be easily configured via a configuration file in YAML format. Besides other aspects, the configuration allows to specify:

- JSSE provider to be used
- enabled protocol version(s)
- enabled cipher suite(s)
- whether client authentication is to be requested or not
- key-store/trust-store to be used by each of the endpoints
- provider and algorithm for the key-manager/trust-manager to be used by each end-point

The details of the configuration are described in a later section of this document. The project also involves several ready to use configurations with keys and certificates demonstrating the most common scenarios, so you can use them out of the box. These are also described in a later section of this document. The key-stores/trust-stores containing the above mentioned keys and certificates can be easily viewed with [KeyStore Explorer](http://keystore-explorer.org) or other tools.

The above mentioned variability allows to experiment with various handshake scenarios like successful handshake with/without client authentication, error scenarios like no common cipher suite, protocol version mismatch etc. The current version of the project does not support advanced stuff like:

- custom hostname verifier
- Server Name Indication (SNI)
- certificate pinning
- certificate revocation
- etc.

Some of them will eventually be added in the future.


## Java Secure Socket Extension (JSSE) Providers
Java Cryptography Architecture (JCA) is a pluggable architecture allowing applications to use various providers of security algorithms, which can be simply plugged into the JDK/JRE. Java Secure Socket Extension (JSSE) is one of the JCA elements providing access to Secure Socket Layer (SSL) and Transport Layer Security (TLS) implementations. SSL Sandbox illustrates this pluggable nature. When configuring the client or the server, you can omit the provider from the configuration. Such a setup will use the default provider, for instance the Sun provider in case of Oracle Java. However, you can also specify a custom provider like [Bouncy Castle](https://www.bouncycastle.org/java.html). In order to use Bouncy Castle, you just have to specify it in the configuration - you do not have to install/register it into the JDK/JRE. However, if you would like to use some other provider which is not part of the JDK/JRE you use, you have to install/register it.


## Source Code Organization and Building
The Java source code is organized as a multi-module Maven project consisting of five modules:

- `commons` module provides reusable utilities common to all applications listed below
- `client` module implements the dummy SSL client
- `server` module implements the dummy SSL server
- `perf-test-client` module implements the SSL client for performance testing
- `perf-test-server` module implements the SSL server for performance testing

In order to build the project, Java 8 (or higher) is needed. Parsing of the configuration in YAML format is based on [FasterXML/Jackson](https://github.com/FasterXML/jackson). In addition, all applications involve [Bouncy Castle](https://www.bouncycastle.org/java.html) crypto package, so that you are not limited to algorithms supported by the standard providers which are part of the JDK you use. Maven will automatically take care for the dependencies, so you do not have to do anything to make them available at compile-time. For each of the four applications, Maven will also produce fat runnable JAR file containing all dependencies, so you do not have to care about the dependencies at run-time either.

In order to build the applications, just navigate to the root directory of the project and execute the following command:

```
mvn clean package
```

The command above will automatically build all five modules comprising the project. After successful build, there will be four runnable fat JAR files, one for each of the four applications.


## How to Start the Dummy Server and the Client
As already mentioned above, there is a runnable fat JAR for the dummy client as well as for the dummy server. Therefore, it is very easy to start the two endpoints. The client as well as the server expects just a single command line argument, namely the name of the YAML configuration file to be used. The following snippet illustrates how to start the client and the server from the root directory of the project, using the minimal configurations present there:

```
# start the server (default provider, Sun in case of Oracle Java)
java -jar server/target/ssl-sandbox-server-0.1-jar-with-dependencies.jar server-config-sun.yml

# start the server (Bouncy Castle provider)
java -jar server/target/ssl-sandbox-server-0.1-jar-with-dependencies.jar server-config-bc.yml

# start the client (default provider, Sun in case of Oracle Java)
java -jar client/target/ssl-sandbox-client-0.1-jar-with-dependencies.jar client-config-sun.yml

# start the client (Bouncy Castle provider)
java -jar client/target/ssl-sandbox-client-0.1-jar-with-dependencies.jar client-config-bc.yml
```

Bouncy Castle provider is automatically registered at run-time. The registration does not affect the persistent configuration of your JRE, it only affects the process currently being executed (i.e. client or server).


## How to Start the Server and the Client for Performance Testing
As there are runnable fat JARs for the two performance-related applications as well, just use the same approach as described in the previous section of this document:

```
# start the server for performance testing
java -jar perf-test-server/target/ssl-sandbox-perf-test-server-0.1-jar-with-dependencies.jar <server-config-file>

# start the client for performance testing
java -jar client/target/ssl-sandbox-perf-test-client-0.1-jar-with-dependencies.jar <client-config-file> <test-params-file>
```

Again, Bouncy Castle provider is automatically registered at run-time. Be aware of the fact that the client for performance testing requires one additional command line argument, namely the name of the configuration file prescribing the test parameters.


## Configuration
The same YAML structure is used for the client as well as for the server. The (hopefully self-describing) configuration is divided to six sections:

- The `tcp` section defines the IP address and the TCP port to be used. The server will bind to that TCP endpoint, the client will connect to it.
- The `ssl` section specifies enabled protocol version(s) and cipher suite(s). It also allows to specify alternative JSEE provider like [Bouncy Castle](https://www.bouncycastle.org/java.html) and enable/disable client authentication.
- The `key_store` section specifies the key-store to be used, its format (e.g. JKS) and password.
- The `trust_store` section specifies the trust-store to be used, its format (e.g. JKS) and password.
- The `key_manager` section specifies the desired key-manager algorithm and provider.
- The `trust_manager` section specifies the desired trust-manager algorithm and provider.

The following snippet is an example of a server configuration file.

```yaml
# IP address and TCP port the server has to bind to
tcp:
    address: 192.168.0.10
    port: 1234

# SSL/TLS settings like JSSE provider, enabled protocol
# versions & cipher suites etc.
ssl:
    provider: BCJSSE
    protocols:
        - TLSv1.2
        - TLSv1.1
        - TLSv1
    cipher_suites:
        - TLS_RSA_WITH_AES_256_GCM_SHA384
        - TLS_RSA_WITH_AES_256_CBC_SHA256
        - TLS_RSA_WITH_AES_128_GCM_SHA256
        - TLS_RSA_WITH_AES_128_CBC_SHA256
    use_client_authentication: true

# properties of the key-store to be used by the server
key_store:
    path: client-key-store.jks
    algorithm: JKS
    password: aardwark

# properties of the trust-store to be used by the server
trust_store:
    path: client-trust-store.jks
    algorithm: JKS
    password: aardwark

# settings passed to KeyManagerFactory
key_manager:
    algorithm: PKIX
    provider: BCJSSE

# settings passed to TrustManagerFactory
trust_manager:
    algorithm: PKIX
    provider: BCJSSE
```

The last four sections (i.e. `key_store`, `trust_store`, `key_manager` and `trust_manager`) are optional - they can be omitted if not needed. For instance, there is no need to configure `key_store` and `key_manager` for a client which is not supposed to support client authentication. Similarly, it does not make any sense to configure `trust_store` and `trust_manager` for a server which is not supposed to require client authentication. In addition, the `key_manager` and `trust_manager` sections can be omitted if the default algorithm and provider are to be used.

The `key_store` and `trust_store` sections involve the `path` property, whose value is the path to the key-store or trust-store. The path should be relative to the location of the configuration file. In other words, if your key-store and/or trust-store file(s) are located in the same directory as the configuration file referencing them, just specify the short filename without any path, and the server as well the client will find them.

The root directory of the project contains some sort of minimal configurations. These configurations prescribe that the client and server will connect via the loopback interface (i.e. 127.0.0.1). Therefore, it is rather meant for a quick test whether both endpoints work properly after the build. The set of minimal configurations involves these files:

- for the client: client-config-sun.yml, client-config-bc.yml and client-key-store.jks
- for the server: server-config-sun.yml, server-config-bc.yml and server-key-store.jks
- for both client and server: trust-store.jks

As you can see above, there are in fact two pairs of configurations - one using the default JSSE provider (Sun provider in case of Oracle Java), and one using the Bouncy Castle provider.

The above described structure of configuration file is also valid for the client and server meant for performance testing. Just be aware of the fact that when you want to measure performance, you typically want to do it for a particular cipher suite. Therefore, it is highly recommended to enable just a single cipher suite in the configuration files for performance testing. Such an approach can help you to avoid situations when you will accidentally test the performance of a cipher suite distinct from the one you wanted to measure.

## Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files
By default, JDK implementations typically limit the encryption key size in accordance with the United States of America export restrictions. For instance, both Oracle and IBM JDKs do that. However, the ready-to-use configurations which are part of this project require unrestricted encryption key size. In order to use them, you need to download and install JCE Unlimited Strength Jurisdiction Policy Files. Look at the documentation of the JDK you use for instructions how to do that.

## Scenarios
The [scenarios](./scenarios) directory contains ready-to-use configurations for various scenarios. There are configurations for successful as well as failed handshakes. For each of the scenarios, the following is provided:

- pair of YAML configuration files, one for the client and one for the server
- key-store(s) with key pair(s) and client/server certificate(s)
- trust-store(s) with CA certificate(s)
- PCAP file(s) containing captured network traffic for the scenario

You have several options how to experiment with the SSL sandbox:

- You can use the provided configuration files and key-stores/trust-stores and reproduce the scenarios yourself.
- You can simply study the provided captures files, without repeating the scenarios yourself.
- You can create your own configurations and arrange your own scenarios.

The following list provides further details you should be aware of:

- The [scenarios](./scenarios) directory contains a separate subdirectory for each of the scenarios. All files related to a single scenario are always concentrated within a single subdirectory.
- There is a [CA_KeyStores](./scenarios/CA_KeyStores) subdirectory where you can find the CA key-stores (including the CA private keys). You can use these CAs to issue further certificates if you decide to arrange your own scenarios. The trust-stores contained in the subdirectories for particular scenarios contain only CA certificates (i.e. the private keys are not present there).
- All key-stores and trust-stores are in JKS format.
- All certificates which are supposed to be valid (i.e. certificates for scenarios which are not supposed to fail due to expired certificate) will be valid at least till February 2038.
- The password for each and every key-store/trust-store is always the same: `aardwark`.
- The same password is used for any key-store/trust-store entry.
- In each of the provided capture files, the IP address 192.168.1.11 corresponds to the server, and the IP address 192.168.1.15 corresponds to the client.
- All provided YAML configuration files contain the IP address 192.168.1.11, which is likely to deviate from your environment. In practical terms, it is very likely that you have to change this if you want to reproduce the scenarios yourself.
- For some of the scenarios, there are two versions of client or server configuration. In such a case, one variant of the configuration uses the default JSSE provider, whereas the other uses Bouncy Castle provider. For such scenarios, there are either two capture files, or the single capture file involves two SSL sessions, each based a distinct configuration variant.

### Scenarios with Successful Handshake

- [Successful Handshake without Client Authentication](./scenarios/Successful_Handshake_without_Client_Authentication)
- [Successful Handshake with Client Authentication](./scenarios/Successful_Handshake_with_Client_Authentication)
- [Successful Handshake with NULL Encryption](./scenarios/Successful_Handshake_with_NULL_Encryption)
- [Two Successful Handshakes, Each Using Distinct Server Certificate](./scenarios/Successful_Handshake_with_Multiple_Keys)


### Scenarios with Failed Handshake

- [Failed Handshake Due to Absence of Common Cipher Suite](./scenarios/Failed_Handshake_Due_to_Absence_of_Common_Cipher_Suite)
- [Failed Handshake Due to Absence of Common Protocol Version](./scenarios/Failed_Handshake_Due_to_Absence_of_Common_Protocol_Version)
- [Failed Handshake Due to Expired Server Certificate](./scenarios/Failed_Handshake_Due_to_Expired_Server_Certificate)
- [Failed Handshake Due to Expired Client Certificate](./scenarios/Failed_Handshake_Due_to_Expired_Client_Certificate)
- [Failed Handshake Due to Absence of Server Certificate](./scenarios/Failed_Handshake_Due_to_Absence_of_Server_Certificate)
- [Failed Handshake Due To Absence of Client Certificate](./scenarios/Failed_Handshake_Due_to_Absence_of_Client_Certificate)
- [Failed Handshake Due to Broken Server Certificate Chain](./scenarios/Failed_Handshake_Due_to_Broken_Server_Certificate_Chain)
- [Failed Handshake Due to Broken Client Certificate Chain](./scenarios/Failed_Handshake_Due_to_Broken_Client_Certificate_Chain)
- [Failed Handshake Due To Absence of Trust Anchor for Server Certificate](./scenarios/Failed_Handshake_Due_to_Absence_of_Trust_Anchor_for_Server_Certificate_Chain)
- [Failed Handshake Due To Absence of Trust Anchor for Client Certificate](./scenarios/Failed_Handshake_Due_to_Absence_of_Trust_Anchor_for_Client_Certificate_Chain)
- [Failed Handshake Due to CA Certificate with Inappropriate Extensions](./scenarios/Failed_Handshake_Due_to_CA_Certificate_with_Inappropriate_Extensions)


## Performance Testing
The [perf-tests](./perf-tests) directory contains ready-to-use configurations for several performance tests. It also contains example of the configuration file with test parameters which is needed by the client. Be aware of the fact that you performance tests can address various aspects of the cipher suite. If you rather want to measure the impact of the key exchange and authentication aspects, specify rather a high number of connections (and thus enforce high number of handshakes to be established), and combine it with small number of messages (even zero) per connection. On the other hand, if you rather want to measure the impact of the symmetric encryption and MAC, specify rather a high number of messages per connection, plus a high message size. This way, every connection will be forced to transfer (and encrypt/decrypt) a large amount of data.