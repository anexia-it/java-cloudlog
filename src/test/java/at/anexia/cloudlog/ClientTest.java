package at.anexia.cloudlog;


import org.junit.Test;

import java.io.File;
import java.net.InetAddress;

import static org.junit.Assert.assertEquals;


public class ClientTest {

    private Client createClient() {

        File trustStore = null, keyStore = null;
        try {
            trustStore = new File(getClass().getClassLoader().getResource("truststore.jks").getFile());
            keyStore = new File(getClass().getClassLoader().getResource("keystore.jks").getFile());
        } catch (Exception e) {}

        Client client = new Client("index", trustStore, "password", keyStore, "password");
        return client;

    }

    private String getHostname() {
        String hostname;
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch(Exception e) {
            hostname = "";
        }
        return hostname;
    }

    @Test
    public void testSimpleMessage() {

        Client client = createClient();
        String hostname = getHostname();

        String message = "test";
        long timestamp = 1;

        String event = client.addMetadata(message, timestamp);
        assertEquals(event, "{\"message\":\""+message+"\",\"timestamp\":"+timestamp+",\"cloudlog_client_type\":\"java-client-kafka\",\"cloudlog_source_host\":\""+hostname+"\"}");

    }

    @Test
    public void testJSONMessage() {

        Client client = createClient();
        String hostname = getHostname();

        String message = "{\"message\":\"test event\",\"timestamp\":\"2017-01-01 10:00:00\"}";

        String event = client.addMetadata(message, 0L);
        assertEquals(event, "{\"message\":\"test event\",\"timestamp\":\"2017-01-01 10:00:00\",\"cloudlog_client_type\":\"java-client-kafka\",\"cloudlog_source_host\":\""+hostname+"\"}");

    }
}
