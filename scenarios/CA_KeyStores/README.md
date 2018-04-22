## Key-Stores of Certification Authorities
This directory contains key-stores for certification authorities which issued the certificates are used by the scenarios. In concrete terms, there is one root CA key-store, plus several intermediate CA key-stores. The trust-stores for particular scenarios contain only certificates of the certification authorities. The key-stores in this directory contain also the private keys, so you can easily use them to issue further certificates. The password for all key-stores is the same: `aardwark`. The same password is also used for all key-store entries.