package com.concert.client;

import com.concert.service.ReservationClientService;
import com.concert.service.ConcertClientService;
import com.concert.service.TicketClientService;
import com.concert.grpc.ReservationProto.*;
import com.concert.grpc.ConcertProto.*;
import com.concert.grpc.TicketProto.*;

import io.grpc.StatusRuntimeException;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Client implementation for Event Coordinators.
 * Provides functionality to make bulk reservations for special guests.
 */
public class CoordinatorClient extends ConcertClient {
    private static final Logger logger = Logger.getLogger(CoordinatorClient.class.getName());
    private final ReservationClientService reservationService;
    private final ConcertClientService concertService;
    private final TicketClientService ticketService;

    public CoordinatorClient() {
        super();
        this.reservationService = new ReservationClientService(channel);
        this.concertService = new ConcertClientService(channel);
        this.ticketService = new TicketClientService(channel);
    }

    /**
     * List all available concerts
     *
     * @return List of concert objects
     */
    public List<Concert> listConcerts() {
        ListConcertsRequest request = ListConcertsRequest.newBuilder().build();

        for (int attempt = 0; attempt < retryAttempts; attempt++) {
            try {
                ListConcertsResponse response = concertService.listConcerts(request);
                logger.info("Retrieved " + response.getConcertsCount() + " concerts");
                return response.getConcertsList();
            } catch (StatusRuntimeException e) {
                logger.warning("RPC failed: " + e.getStatus());

                if (attempt < retryAttempts - 1) {
                    try {
                        Thread.sleep(retryInterval);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                    switchServer();
                    this.concertService.updateChannel(channel);
                }
            }
        }

        return List.of();
    }

    /**
     * Get ticket inventory for a concert
     *
     * @param concertId The ID of the concert
     * @return The inventory information
     */
    public TicketInventory getTicketInventory(String concertId) {
        GetTicketInventoryRequest request = GetTicketInventoryRequest.newBuilder()
                .setConcertId(concertId)
                .build();

        for (int attempt = 0; attempt < retryAttempts; attempt++) {
            try {
                GetTicketInventoryResponse response = ticketService.getTicketInventory(request);
                logger.info("Retrieved ticket inventory for concert: " + concertId);
                return response.getInventory();
            } catch (StatusRuntimeException e) {
                logger.warning("RPC failed: " + e.getStatus());

                if (attempt < retryAttempts - 1) {
                    try {
                        Thread.sleep(retryInterval);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                    switchServer();
                    this.ticketService.updateChannel(channel);
                }
            }
        }

        return null;
    }

    /**
     * Make a bulk reservation for a concert + after-party combo
     *
     * @param concertId The ID of the concert
     * @param groupName The name of the group
     * @param seatTierName The name of the seat tier
     * @param seatCount The number of seats to reserve
     * @param paymentMethod The payment method
     * @return The bulk reservation details if successful, null otherwise
     */
    public BulkReservation makeBulkReservation(String concertId,
                                               String groupName,
                                               String seatTierName,
                                               int seatCount,
                                               String paymentMethod) {
        String reservationId = UUID.randomUUID().toString();

        BulkReservationRequest request = BulkReservationRequest.newBuilder()
                .setReservationId(reservationId)
                .setConcertId(concertId)
                .setGroupName(groupName)
                .setSeatTierName(seatTierName)
                .setSeatCount(seatCount)
                .setPaymentMethod(paymentMethod)
                .build();

        for (int attempt = 0; attempt < retryAttempts; attempt++) {
            try {
                BulkReservationResponse response = reservationService.makeBulkReservation(request);
                if (response.getSuccess()) {
                    logger.info("Successfully made bulk reservation: " + reservationId);
                    return response.getReservation();
                } else {
                    logger.warning("Failed to make bulk reservation: " + response.getMessage());
                    return null;
                }
            } catch (StatusRuntimeException e) {
                logger.warning("RPC failed: " + e.getStatus());

                if (attempt < retryAttempts - 1) {
                    try {
                        Thread.sleep(retryInterval);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                    switchServer();
                    this.reservationService.updateChannel(channel);
                }
            }
        }

        return null;
    }

    /**
     * Cancel a bulk reservation
     *
     * @param reservationId The ID of the bulk reservation to cancel
     * @return true if cancellation was successful, false otherwise
     */
    public boolean cancelBulkReservation(String reservationId) {
        CancelBulkReservationRequest request = CancelBulkReservationRequest.newBuilder()
                .setReservationId(reservationId)
                .build();

        for (int attempt = 0; attempt < retryAttempts; attempt++) {
            try {
                CancelBulkReservationResponse response = reservationService.cancelBulkReservation(request);
                if (response.getSuccess()) {
                    logger.info("Successfully cancelled bulk reservation: " + reservationId);
                    return true;
                } else {
                    logger.warning("Failed to cancel bulk reservation: " + response.getMessage());
                    return false;
                }
            } catch (StatusRuntimeException e) {
                logger.warning("RPC failed: " + e.getStatus());

                if (attempt < retryAttempts - 1) {
                    try {
                        Thread.sleep(retryInterval);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                    switchServer();
                    this.reservationService.updateChannel(channel);
                }
            }
        }

        return false;
    }

    /**
     * Get details of a specific bulk reservation
     *
     * @param reservationId The ID of the bulk reservation
     * @return The bulk reservation object, or null if not found
     */
    public BulkReservation getBulkReservationDetails(String reservationId) {
        GetBulkReservationRequest request = GetBulkReservationRequest.newBuilder()
                .setReservationId(reservationId)
                .build();

        for (int attempt = 0; attempt < retryAttempts; attempt++) {
            try {
                GetBulkReservationResponse response = reservationService.getBulkReservation(request);
                logger.info("Retrieved details for bulk reservation: " + reservationId);
                return response.getReservation();
            } catch (StatusRuntimeException e) {
                logger.warning("RPC failed: " + e.getStatus());

                if (attempt < retryAttempts - 1) {
                    try {
                        Thread.sleep(retryInterval);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                    switchServer();
                    this.reservationService.updateChannel(channel);
                }
            }
        }

        return null;
    }

    /**
     * List all bulk reservations for a concert
     *
     * @param concertId The ID of the concert
     * @return List of bulk reservation objects
     */
    public List<BulkReservation> listBulkReservations(String concertId) {
        ListBulkReservationsRequest request = ListBulkReservationsRequest.newBuilder()
                .setConcertId(concertId)
                .build();

        for (int attempt = 0; attempt < retryAttempts; attempt++) {
            try {
                ListBulkReservationsResponse response = reservationService.listBulkReservations(request);
                logger.info("Retrieved " + response.getReservationsCount() + " bulk reservations for concert: " + concertId);
                return response.getReservationsList();
            } catch (StatusRuntimeException e) {
                logger.warning("RPC failed: " + e.getStatus());

                if (attempt < retryAttempts - 1) {
                    try {
                        Thread.sleep(retryInterval);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                    switchServer();
                    this.reservationService.updateChannel(channel);
                }
            }
        }

        return List.of();
    }
}