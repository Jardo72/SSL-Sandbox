## Failed Handshake Due to CA Certificate with Inappropriate Extensions
This scenario illustrates how the handshake is aborted when the server certificate is issued by a CA whose certificate is marked as non-CA certificate. The configurations for the client and server are virtually identical as the set of enabled protocol versions as well as the set of enabled cipher suites is equal for both. In addition, client authentication is not requested by the server, and it is disabled for the client. The only certificate present in server's key-store is issued by a CA whose certificate is not defined as CA certificate. In concrete terms, the certificate has *Basic Constraints* extension marked as critical, and the extension states that the subject is not a CA. After receiving the *ClientHello* message from the client, the server responds with its usual sequence of handshake messages, including the *Certificate* message. After receiving the sequence of handshake messages from the server, the validation of the server's certificate chain fails, and the client aborts the handshake with fatal *Alert*. The description seems to depend on the SSL/TLS implementation used by the client - *bad_certificate* and *certificate_unknown* are the most likely descriptions.

There are actually two client configurations for this scenario - one using the default JSSE provider, the other using Bouncy Castle provider. Correspondingly, there are two capture files, each based on one of the above mentioned client configurations. As I used Oracle Java when performing the captures, the default JSSE provider was the Sun provider in my case.