## Successful Handshake with Client Authentication
This scenario illustrates successful handshake with client authentication. The configurations for the client and the server are distinct but compatible. TLSv1.2 is enabled for both end-points. The set of cipher suites enabled for the client differs from the set of cipher suites enabled for the server, but there are few cipher suites which are enabled for both end-points. There is a distinction between client's order of preference and server's order of preference. 

There are actually two server configurations illustrating that different SSL/TLS implementations can behave in a distinct way. Correspondingly, there are also two capture files for this scenario, each corresponding to one of the above mentioned server configuration.

- One server configuration uses the default provider. As I used Oracle Java when capturing the scenarios, the capture file corresponding to this configuration reflects the behavior of the Sun provider whose choice of cipher suite takes the client's order of preference into account.
- The other server configuration uses the Bouncy Castle provider, whose choice of cipher suite reflects the server's order of preference into account.

At the bottom line, distinct cipher suites are chosen for the two captured handshakes, despite of the fact that:

- there is only one client configuration, which was used for both captures
- there are two server configurations, but they have completely identical list of enabled cipher suites (including the order of preference)

For this scenario, the key-stores for both end-points (i.e. client and server) contain incomplete certificate chains. In concrete terms, each of the two key-stores contains:

- end-entity certificate (i.e. client certificate or server certificate)
- the certificate of an intermediate CA which issued the above mentioned end-entity certificate

During the handshake, each end-point sends the incomplete certificate chain without the root CA certificate to the counterpart. However, as the counterpart has the root CA certificate in its trust-store, there is no reason to abort the handshake. Compare this scenario with the [Successful Handshake without Client Authentication](../Successful_Handshake_without_Client_Authentication) scenario, which illustrates a different approach with regard to the certificate chains.

Be aware of the fact that the incomplete certificate chains illustrated by this scenario are not related to client authentication. Client authentication and completeness of certificate chain(s) in key-store(s) are different aspects of the handshake which are completely independent of each other. I just wanted to demonstrate both approaches (i.e. key-stores with complete as well as incomplete certificate chains), and I decided to use this scenario to illustrate the approach with incomplete certificate chain. I could have used key-stores with complete certificate chains for this scenario, and key-store(s) with incomplete certificate chain(s) for some other scenario demonstrating successful handshake.