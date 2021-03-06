java-cloudlog
===

[![Build Status](https://travis-ci.org/anexia-it/java-cloudlog.svg?branch=master)](https://travis-ci.org/anexia-it/java-cloudlog)

java-cloudlog is a client library for Anexia CloudLog.

Currently it only provides to push events to CloudLog. Querying is possible in a future release.

There are two connection types:
- directly connected for high throughput
- http client


## Get started with Maven

```sh
<repositories>
    <repository>
        <id>anexia</id>
        <url>https://raw.github.com/anexia-it/maven-repo/master/</url>
        <snapshots>
            <enabled>true</enabled>
            <updatePolicy>always</updatePolicy>
        </snapshots>
    </repository>
</repositories>

<dependency>
    <groupId>at.anexia.cloudlog</groupId>
    <artifactId>client</artifactId>
    <version>0.1</version>
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
