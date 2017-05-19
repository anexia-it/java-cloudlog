java-cloudlog
===

java-cloudlog is a client library for Anexia CloudLog.

Currently it only provides to push events to CloudLog. Querying is possible in a future release.

There are two connection types:
- directly connected for high throughput
- http client


## Get started with Maven

```sh
<dependency>
    <groupId>at.anexia.cloudlog</groupId>
    <artifactId>client</artifactId>
    <version>1.0</version>
</dependency>
```

## Quickstart

```java
// Init CloudLog client
Client client = new Client("index", trustStoreFile, "truststore-password", keyStoreFile, "keystore-password");

// Alternative CloudLog client (http)
Client client = new Client("index", "token");

// Push simple message
$client->pushEvent("message");

// Push document as json string
client.pushEvent("{\n" +
        "  \"timestamp\": 1495024205123,\n" +
        "  \"user\": \"test\",\n" +
        "  \"severity\": 1,\n" +
        "  \"message\": \"My first CloudLog event\"\n" +
        "}");
```
