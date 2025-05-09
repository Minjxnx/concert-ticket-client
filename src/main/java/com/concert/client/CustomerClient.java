
package com.concert.client;

import com.concert.service.ReservationClientService;
import com.concert.service.ConcertClientService;
import com.concert.service.TicketClientService;
import com.concert.grpc.ReservationProto.*;
import com.concert.grpc.ConcertProto.*;
import com.concert.grpc.TicketProto.*;

import io.grpc.StatusRuntimeException;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Client implementation for Customers.
 * Provides functionality to browse concerts and make reservations.
 */
public class CustomerClient extends ConcertClient {
    private static final Logger logger = Logger.getLogger(CustomerClient.class.getName());
    private final ReservationClientService reservationService;
    private final ConcertClientService concertService;
    private final TicketClientService ticketService;

    public CustomerClient() {
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
    public List<Concert> browseConcerts() {
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
     * Get details of a specific concert
     *
     * @param concertId The ID of the concert
     * @return The concert object, or null if not found
     */
    public Concert getConcertDetails(String concertId) {
        GetConcertRequest request = GetConcertRequest.newBuilder()
                .setConcertId(concertId)
                .build();

        for (int attempt = 0; attempt < retryAttempts; attempt++) {
            try {
                GetConcertResponse response = concertService.getConcert(request);
                logger.info("Retrieved details for concert: " + concertId);
                return response.getConcert();
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

        return null;
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
     * Make a reservation for a concert
     *
     * @param concertId The ID of the concert
     * @param customerName The name of the customer
     * @param seatTierName The name of the seat tier
     * @param seatCount The number of seats to reserve
     * @param includeAfterParty Whether to include after-party tickets
     * @param paymentMethod The payment method
     * @return The reservation details if successful, null otherwise
     */
    public Reservation makeReservation(String concertId,
                                       String customerName,
                                       String seatTierName,
                                       int seatCount,
                                       boolean includeAfterParty,
                                       String paymentMethod) {
        String reservationId = UUID.randomUUID().toString();

        ReservationRequest request = ReservationRequest.newBuilder()
                .setReservationId(reservationId)
                .setConcertId(concertId)
                .setCustomerName(customerName)
                .setSeatTierName(seatTierName)
                .setSeatCount(seatCount)
                .setIncludeAfterParty(includeAfterParty)
                .setPaymentMethod(paymentMethod)
                .build();

        for (int attempt = 0; attempt < retryAttempts; attempt++) {
            try {
                ReservationResponse response = reservationService.makeReservation(request);
                if (response.getSuccess()) {
                    logger.info("Successfully made reservation: " + reservationId);
                    return response.getReservation();
                } else {
                    logger.warning("Failed to make reservation: " + response.getMessage());
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
     * Cancel a reservation
     *
     * @param reservationId The ID of the reservation to cancel
     * @return true if cancellation was successful, false otherwise
     */
    public boolean cancelReservation(String reservationId) {
        CancelReservationRequest request = CancelReservationRequest.newBuilder()
                .setReservationId(reservationId)
                .build();

        for (int attempt = 0; attempt < retryAttempts; attempt++) {
            try {
                CancelReservationResponse response = reservationService.cancelReservation(request);
                if (response.getSuccess()) {
                    logger.info("Successfully cancelled reservation: " + reservationId);
                    return true;
                } else {
                    logger.warning("Failed to cancel reservation: " + response.getMessage());
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
     * Get details of a specific reservation
     *
     * @param reservationId The ID of the reservation
     * @return The reservation object, or null if not found
     */
    public Reservation getReservationDetails(String reservationId) {
        GetReservationRequest request = GetReservationRequest.newBuilder()
                .setReservationId(reservationId)
                .build();

        for (int attempt = 0; attempt < retryAttempts; attempt++) {
            try {
                GetReservationResponse response = reservationService.getReservation(request);
                logger.info("Retrieved details for reservation: " + reservationId);
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
}