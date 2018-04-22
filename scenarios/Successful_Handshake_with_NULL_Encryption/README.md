## Successful Handshake with NULL Encryption (Session without Encryption of Application Data)
This scenario illustrates how you can achieve SSL/TLS sessions without encryption of application data. Such a setup does not provide confidentiality and thus should **not** be used in situations when security is a must. However, it might be handy if you have to trouble-shoot SSL/TLS connections, and you need to see the application payload. Both client and server support TLSv1.2, and the only cipher suite enabled for both endpoints is *TLS_RSA_WITH_NULL_SHA256*. Besides the application payload, the following protocol messages are not encrypted either:

- Pair of *Finished* messages indicating successful completion of the handshake.
- Pair of *Alert* messages indicating termination of connection (*close_notify* alerts).

In case of session with encryption, the above mentioned protocol messages are encrypted, so you usually do not see their message type in Wireshark. Instead, you typically see *Encrypted Handshake Message* and *Encrypted Alert* instead.
