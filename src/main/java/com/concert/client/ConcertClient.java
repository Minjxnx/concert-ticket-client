package com.concert.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.kv.GetResponse;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import java.util.concurrent.TimeUnit;
import java.nio.charset.StandardCharsets;

/**
 * Base class for all client implementations.
 * Handles service discovery and connection management.
 */
public abstract class ConcertClient {
    private static final Logger logger = Logger.getLogger(ConcertClient.class.getName());
    protected List<String> serverAddresses;
    protected ManagedChannel channel;
    protected int currentServerIndex = 0;
    protected Properties config;
    protected int retryAttempts;
    protected int retryInterval;

    /**
     * Initialize the client with configuration from properties file
     */
    public ConcertClient() {
        config = loadConfiguration();
        retryAttempts = Integer.parseInt(config.getProperty("client.retry.attempts", "3"));
        retryInterval = Integer.parseInt(config.getProperty("client.retry.interval", "2000"));

        try {
            discoverServers();
            connectToServer();
        } catch (Exception e) {
            logger.severe("Failed to initialize client: " + e.getMessage());
            throw new RuntimeException("Failed to initialize client", e);
        }
    }

    /**
     * Load configuration from application.properties file
     */
    private Properties loadConfiguration() {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("src/main/resources/application.properties")) {
            props.load(fis);
        } catch (IOException e) {
            logger.warning("Could not load properties file. Using defaults.");
        }
        return props;
    }

    /**
     * Discover server nodes using etcd
     */
    private void discoverServers() throws ExecutionException, InterruptedException {
        serverAddresses = new ArrayList<>();
        String discoveryMode = config.getProperty("server.discovery.mode", "etcd");

        if ("etcd".equals(discoveryMode)) {
            String etcdEndpoint = config.getProperty("server.etcd.endpoints", "http://localhost:2379");
            Client etcdClient = Client.builder().endpoints(etcdEndpoint).build();

            try {
                GetResponse response = etcdClient.getKVClient()
                        .get(io.etcd.jetcd.ByteSequence.from("concert-servers", StandardCharsets.UTF_8))
                        .get();

                for (KeyValue kv : response.getKvs()) {
                    String serverAddress = kv.getValue().toString(StandardCharsets.UTF_8);
                    serverAddresses.add(serverAddress);
                    logger.info("Discovered server: " + serverAddress);
                }

                if (serverAddresses.isEmpty()) {
                    // If no servers discovered, use default addresses
                    serverAddresses.add("localhost:50051");
                    serverAddresses.add("localhost:50052");
                    serverAddresses.add("localhost:50053");
                    logger.warning("No servers discovered from etcd. Using default addresses.");
                }
            } finally {
                etcdClient.close();
            }
        } else {
            // Fallback to hardcoded server addresses
            serverAddresses.add("localhost:50051");
            serverAddresses.add("localhost:50052");
            serverAddresses.add("localhost:50053");
        }
    }

    /**
     * Connect to a server node
     */
    protected void connectToServer() {
        if (channel != null && !channel.isShutdown()) {
            channel.shutdown();
            try {
                channel.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                logger.warning("Channel shutdown interrupted");
            }
        }

        String serverAddress = serverAddresses.get(currentServerIndex);
        String[] parts = serverAddress.split(":");
        String host = parts[0];
        int port = Integer.parseInt(parts[1]);

        channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();

        logger.info("Connected to server: " + serverAddress);
    }

    /**
     * Try connecting to the next server in the list
     */
    protected void switchServer() {
        currentServerIndex = (currentServerIndex + 1) % serverAddresses.size();
        connectToServer();
    }

    /**
     * Close resources when client is done
     */
    public void shutdown() {
        if (channel != null && !channel.isShutdown()) {
            try {
                channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                logger.warning("Channel shutdown interrupted");
            }
        }
    }
}