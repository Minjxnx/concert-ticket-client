package com.concert.service;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;

import com.concert.proto.reservation.*;
import com.concert.proto.common.Status;

/**
 * Client service for handling reservation operations
 */
public class ReservationClientService {
    private static final Logger logger = Logger.getLogger(ReservationClientService.class.getName());

    private final ManagedChannel channel;
    private final ReservationServiceGrpc.ReservationServiceBlockingStub blockingStub;
    private final ReservationServiceGrpc.ReservationServiceStub asyncStub;

    /**
     * Constructor with server address
     * @param host Server host
     * @param port Server port
     */
    public ReservationClientService(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build());
    }

    /**
     * Constructor with pre-configured channel
     * @param channel The ManagedChannel to use
     */
    public ReservationClientService(ManagedChannel channel) {
        this.channel = channel;
        blockingStub = ReservationServiceGrpc.newBlockingStub(channel);
        asyncStub = ReservationServiceGrpc.newServiceStub(channel);
    }

    /**
     * Shuts down the channel
     * @throws InterruptedException if the shutdown is interrupted
     */
    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    /**
     * Make a reservation for a concert with optional after-party tickets
     * @param concertId The ID of the concert
     * @param customerName Customer's name
     * @param seatType Seat type (e.g., "Regular", "VIP")
     * @param seatCount Number of seats to reserve
     * @param includeAfterParty Whether to include after-party tickets
     * @return The reservation ID if successful, null otherwise
     */
    public String makeReservation(String concertId, String customerName, String seatType,
                                  int seatCount, boolean includeAfterParty) {
        logger.info("Making reservation for concert: " + concertId);

        // Build the reservation request
        ReservationRequest request = ReservationRequest.newBuilder()
                .setConcertId(concertId)
                .setCustomerName(customerName)
                .setSeatType(seatType)
                .setSeatCount(seatCount)
                .setIncludeAfterParty(includeAfterParty)
                .build();

        ReservationResponse response;
        try {
            response = blockingStub.makeReservation(request);
            if (response.getStatus() == Status.SUCCESS) {
                logger.info("Reservation successful with ID: " + response.getReservationId());
                return response.getReservationId();
            } else {
                logger.warning("Reservation failed: " + response.getMessage());
                return null;
            }
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return null;
        }
    }

    /**
     * Make a bulk reservation (typically used by event coordinators for special guest groups)
     * @param concertId The ID of the concert
     * @param groupName Name of the group
     * @param seatType Seat type (e.g., "Regular", "VIP")
     * @param seatCount Number of seats to reserve
     * @param includeAfterParty Whether to include after-party tickets
     * @return The reservation ID if successful, null otherwise
     */
    public String makeBulkReservation(String concertId, String groupName, String seatType,
                                      int seatCount, boolean includeAfterParty) {
        logger.info("Making bulk reservation for concert: " + concertId + " for group: " + groupName);

        // Build the bulk reservation request
        BulkReservationRequest request = BulkReservationRequest.newBuilder()
                .setConcertId(concertId)
                .setGroupName(groupName)
                .setSeatType(seatType)
                .setSeatCount(seatCount)
                .setIncludeAfterParty(includeAfterParty)
                .build();

        BulkReservationResponse response;
        try {
            response = blockingStub.makeBulkReservation(request);
            if (response.getStatus() == Status.SUCCESS) {
                logger.info("Bulk reservation successful with ID: " + response.getReservationId());
                return response.getReservationId();
            } else {
                logger.warning("Bulk reservation failed: " + response.getMessage());
                return null;
            }
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return null;
        }
    }

    /**
     * Get a reservation by ID
     * @param reservationId The ID of the reservation to retrieve
     * @return The reservation details if found, null otherwise
     */
    public ReservationDetails getReservation(String reservationId) {
        logger.info("Getting reservation details for ID: " + reservationId);

        GetReservationRequest request = GetReservationRequest.newBuilder()
                .setReservationId(reservationId)
                .build();

        GetReservationResponse response;
        try {
            response = blockingStub.getReservation(request);
            if (response.getStatus() == Status.SUCCESS) {
                logger.info("Retrieved reservation details successfully");
                return response.getReservation();
            } else {
                logger.warning("Failed to get reservation: " + response.getMessage());
                return null;
            }
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return null;
        }
    }

    /**
     * Cancel a reservation
     * @param reservationId The ID of the reservation to cancel
     * @return true if cancellation was successful, false otherwise
     */
    public boolean cancelReservation(String reservationId) {
        logger.info("Cancelling reservation with ID: " + reservationId);

        CancelReservationRequest request = CancelReservationRequest.newBuilder()
                .setReservationId(reservationId)
                .build();

        CancelReservationResponse response;
        try {
            response = blockingStub.cancelReservation(request);
            if (response.getStatus() == Status.SUCCESS) {
                logger.info("Reservation cancelled successfully");
                return true;
            } else {
                logger.warning("Failed to cancel reservation: " + response.getMessage());
                return false;
            }
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return false;
        }
    }

    /**
     * Get all reservations for a customer
     * @param customerName The name of the customer
     * @return List of reservation details for the customer
     */
    public List<ReservationDetails> getCustomerReservations(String customerName) {
        logger.info("Getting reservations for customer: " + customerName);

        CustomerReservationsRequest request = CustomerReservationsRequest.newBuilder()
                .setCustomerName(customerName)
                .build();

        List<ReservationDetails> reservations = new ArrayList<>();
        try {
            CustomerReservationsResponse response = blockingStub.getCustomerReservations(request);
            if (response.getStatus() == Status.SUCCESS) {
                reservations.addAll(response.getReservationsList());
                logger.info("Retrieved " + reservations.size() + " reservations for the customer");
            } else {
                logger.warning("Failed to get customer reservations: " + response.getMessage());
            }
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
        }

        return reservations;
    }

    /**
     * Stream reservations for a specific concert
     * @param concertId The ID of the concert
     * @return List of reservations for the concert
     */
    public List<ReservationDetails> getConcertReservations(String concertId) {
        logger.info("Getting reservations for concert: " + concertId);

        final List<ReservationDetails> reservations = new ArrayList<>();
        final CountDownLatch finishLatch = new CountDownLatch(1);

        ConcertReservationsRequest request = ConcertReservationsRequest.newBuilder()
                .setConcertId(concertId)
                .build();

        // Using async stub for streaming
        asyncStub.getConcertReservations(request, new StreamObserver<ConcertReservationsResponse>() {
            @Override
            public void onNext(ConcertReservationsResponse response) {
                if (response.getStatus() == Status.SUCCESS) {
                    reservations.addAll(response.getReservationsList());
                    logger.info("Received batch of reservations: " + response.getReservationsCount());
                }
            }

            @Override
            public void onError(Throwable t) {
                logger.log(Level.SEVERE, "Error in streaming reservations: " + t.getMessage());
                finishLatch.countDown();
            }

            @Override
            public void onCompleted() {
                logger.info("Completed streaming reservations. Total: " + reservations.size());
                finishLatch.countDown();
            }
        });

        try {
            // Wait for the stream to complete or timeout after 10 seconds
            if (!finishLatch.await(10, TimeUnit.SECONDS)) {
                logger.warning("Stream did not complete within timeout");
            }
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "Stream interrupted", e);
        }

        return reservations;
    }
}