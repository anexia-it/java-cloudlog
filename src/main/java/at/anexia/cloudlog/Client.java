package at.anexia.cloudlog;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.kafka.clients.producer.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.time.Instant;
import java.util.Properties;

/**
 * Java client to push and query data to Anexia CloudLog
 */
public class Client {

    private String index;
    private File trustStoreFile;
    private File keyStoreFile;
    private String keyStorePassword;
    private String trustStorePassword;
    private String token;

    private String brokers = "kafka0401.bdp.anexia-it.com:8443";
    private String api = "https://api0401.bdp.anexia-it.com";

    private Producer<String, String> producer;
    private JsonParser jsonParser;
    private String hostname;

    private Boolean isHttp;
    private String clientType;

    private HttpClient httpClient;
    private HttpPost httpPost;


    /**
     * Creates a new client (kafka)
     *
     * @param index Index identifier
     * @param trustStoreFile truststore file path
     * @param trustStorePassword truststore password
     * @param keyStoreFile keystore file path
     * @param keyStorePassword keystore password
     */
    public Client(String index, File trustStoreFile, String trustStorePassword, File keyStoreFile, String keyStorePassword) {

        this.init();
        this.index = index;
        this.trustStoreFile = trustStoreFile;
        this.keyStoreFile = keyStoreFile;
        this.trustStorePassword = trustStorePassword;
        this.keyStorePassword = keyStorePassword;
        this.isHttp = false;
        this.clientType = "java-client-kafka";

        try {
            this.checkKafkaConfiguration();
            this.createProducer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a new client (http)
     *
     * @param index Index identifier
     * @param token Auth token
     */
    public Client(String index, String token) {

        this.init();
        this.index = index;
        this.token = token;
        this.isHttp = true;
        this.clientType = "java-client-http";

        try {
            this.checkHttpConfiguration();
        } catch (Exception e) {
            e.printStackTrace();
        }

        httpClient = HttpClientBuilder.create().build();
        httpPost = new HttpPost(api + "/v1/index/" + index + "/data");
        httpPost.addHeader("Content-type", "application/json");
        httpPost.addHeader("Authorization", token);

    }

    /**
     * Init utils and system information
     */
    private void init() {

        jsonParser = new JsonParser();

        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch(Exception e) {
            hostname = "";
        }

    }

    /**
     * Check http configuration
     * @throws Exception if configuration is invalid
     */
    private void checkHttpConfiguration() throws Exception {

        if(token == null || token.isEmpty()) {
            throw new Exception("missing token");
        }
        if(index == null || index.isEmpty()) {
            throw new Exception("missing index");
        }

    }

    /**
     * Check kafka configuration
     * @throws Exception if configuration is invalid
     */
    private void checkKafkaConfiguration() throws Exception {

        if(!trustStoreFile.exists()) {
            throw new FileNotFoundException("truststore file not found at: "+ trustStoreFile);
        }
        if(!keyStoreFile.exists()) {
            throw new FileNotFoundException("keystore file not found at: "+ keyStoreFile);
        }
        if(keyStorePassword == null || keyStorePassword.isEmpty()) {
            throw new Exception("missing keystore password");
        }
        if(trustStorePassword == null || trustStorePassword.isEmpty()) {
            throw new Exception("missing truststore password");
        }
        if(index == null || index.isEmpty()) {
            throw new Exception("missing index");
        }

    }

    /**
     * Create kafka producer
     */
    private void createProducer() {

        Properties props = new Properties();
        props.put("bootstrap.servers", brokers);
        props.put("security.protocol", "ssl");
        props.put("ssl.truststore.location", trustStoreFile.getAbsolutePath());
        props.put("ssl.truststore.password", trustStorePassword);
        props.put("ssl.keystore.location", keyStoreFile.getAbsolutePath());
        props.put("ssl.keystore.password", keyStorePassword);
        props.put("request.required.acks", "all");
        props.put("compression.codec", "gzip");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("retries", 10);

        producer = new KafkaProducer<>(props);

    }

    /**
     * Sends one or more events to CloudLog
     * @param events Event strings
     */
    public void pushEvents(String[] events) {

        long timestamp = Instant.now().toEpochMilli();

        if(isHttp) {

            JsonArray array = new JsonArray();
            for(String event : events) {
                event = addMetadata(event, timestamp);
                array.add(event);
            }
            sendOverHttp(array.toString());

        } else {

            for(String event : events) {
                event = addMetadata(event, timestamp);
                producer.send(new ProducerRecord<>(index, null, event), (RecordMetadata recordMetadata, Exception e) -> {
                    if (e != null) {
                        e.printStackTrace();
                    }
                });
            }

        }

    }

    /**
     * Sends an events to CloudLog
     * @param event Event string
     */
    public void pushEvent(String event) {
        String[] events = {event};
        pushEvents(events);
    }

    /**
     * Send event to http api
     */
    private void sendOverHttp(String events) {

        try {
            httpPost.setEntity(new StringEntity("{ \"records\" : "+events+"}"));
            HttpResponse response = httpClient.execute(httpPost);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Close the connection/producer
     */
    public void close() {
        if(producer != null) {
            producer.close();
            producer = null;
        }
    }

    /**
     * Parse and add meta data to event
     * @param event Event string
     * @param timestamp Timestamp
     */
    protected String addMetadata(String event, long timestamp) {
        JsonObject data;
        try {
            data = jsonParser.parse(event).getAsJsonObject();
        } catch(Exception e) {
            data = null;
        }
        if(data == null) {
            data = new JsonObject();
            data.addProperty("message", event);
        }
        if(!data.has("timestamp")) {
            data.addProperty("timestamp", timestamp);
        }
        data.addProperty("cloudlog_client_type", clientType);
        data.addProperty("cloudlog_source_host", hostname);
        return data.toString();
    }

}
