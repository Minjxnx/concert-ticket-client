package com.concert.client;

import com.concert.service.TicketClientService;
import com.concert.service.ConcertClientService;
import com.concert.grpc.TicketInventoryServiceGrpc;
import com.concert.grpc.ConcertServiceGrpc;
import com.concert.grpc.TicketProto.*;
import com.concert.grpc.ConcertProto.*;

import io.grpc.StatusRuntimeException;

import java.util.List;
import java.util.logging.Logger;

/**
 * Client implementation for Box Office Clerks.
 * Provides functionality to update ticket inventory and pricing.
 */
public class BoxOfficeClient extends ConcertClient {
    private static final Logger logger = Logger.getLogger(BoxOfficeClient.class.getName());
    private final TicketClientService ticketService;
    private final ConcertClientService concertService;

    public BoxOfficeClient() {
        super();
        this.ticketService = new TicketClientService(channel);
        this.concertService = new ConcertClientService(channel);
    }

    /**
     * Update ticket inventory for a concert
     *
     * @param concertId The ID of the concert
     * @param seatTierName The name of the seat tier to update
     * @param additionalTickets The number of tickets to add
     * @return true if update was successful, false otherwise
     */
    public boolean updateTicketInventory(String concertId, String seatTierName, int additionalTickets) {
        UpdateTicketInventoryRequest request = UpdateTicketInventoryRequest.newBuilder()
                .setConcertId(concertId)
                .setSeatTierName(seatTierName)
                .setAdditionalTickets(additionalTickets)
                .build();

        for (int attempt = 0; attempt < retryAttempts; attempt++) {
            try {
                UpdateTicketInventoryResponse response = ticketService.updateTicketInventory(request);
                if (response.getSuccess()) {
                    logger.info("Successfully updated ticket inventory for concert: " + concertId);
                    return true;
                } else {
                    logger.warning("Failed to update ticket inventory: " + response.getMessage());
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
                    this.ticketService.updateChannel(channel);
                }
            }
        }

        return false;
    }

    /**
     * Update after-party ticket inventory
     *
     * @param concertId The ID of the concert
     * @param additionalTickets The number of after-party tickets to add
     * @return true if update was successful, false otherwise
     */
    public boolean updateAfterPartyInventory(String concertId, int additionalTickets) {
        UpdateAfterPartyInventoryRequest request = UpdateAfterPartyInventoryRequest.newBuilder()
                .setConcertId(concertId)
                .setAdditionalTickets(additionalTickets)
                .build();

        for (int attempt = 0; attempt < retryAttempts; attempt++) {
            try {
                UpdateAfterPartyInventoryResponse response = ticketService.updateAfterPartyInventory(request);
                if (response.getSuccess()) {
                    logger.info("Successfully updated after-party inventory for concert: " + concertId);
                    return true;
                } else {
                    logger.warning("Failed to update after-party inventory: " + response.getMessage());
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
                    this.ticketService.updateChannel(channel);
                }
            }
        }

        return false;
    }

    /**
     * Update ticket pricing for a concert
     *
     * @param concertId The ID of the concert
     * @param seatTierName The name of the seat tier to update
     * @param newPrice The new price for the seat tier
     * @return true if update was successful, false otherwise
     */
    public boolean updateTicketPrice(String concertId, String seatTierName, double newPrice) {
        // First, get the current concert details
        GetConcertRequest getRequest = GetConcertRequest.newBuilder()
                .setConcertId(concertId)
                .build();

        Concert currentConcert = null;

        for (int attempt = 0; attempt < retryAttempts; attempt++) {
            try {
                GetConcertResponse getResponse = concertService.getConcert(getRequest);
                currentConcert = getResponse.getConcert();
                break;
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

        if (currentConcert == null) {
            logger.warning("Failed to retrieve concert with ID: " + concertId);
            return false;
        }

        // Create updated concert with the price change
        Concert.Builder concertBuilder = currentConcert.toBuilder();

        // Find and update the specified seat tier price
        boolean tierFound = false;
        for (int i = 0; i < concertBuilder.getSeatTiersCount(); i++) {
            SeatTier tier = concertBuilder.getSeatTiers(i);
            if (tier.getName().equals(seatTierName)) {
                SeatTier updatedTier = tier.toBuilder()
                        .setPrice(newPrice)
                        .build();
                concertBuilder.setSeatTiers(i, updatedTier);
                tierFound = true;
                break;
            }
        }

        if (!tierFound) {
            logger.warning("Seat tier not found: " + seatTierName);
            return false;
        }

        // Create update request
        UpdateConcertRequest updateRequest = UpdateConcertRequest.newBuilder()
                .setConcert(concertBuilder.build())
                .build();

        for (int attempt = 0; attempt < retryAttempts; attempt++) {
            try {
                UpdateConcertResponse updateResponse = concertService.updateConcert(updateRequest);
                if (updateResponse.getSuccess()) {
                    logger.info("Successfully updated price for seat tier: " + seatTierName);
                    return true;
                } else {
                    logger.warning("Failed to update price: " + updateResponse.getMessage());
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
                    this.concertService.updateChannel(channel);
                }
            }
        }

        return false;
    }

    /**
     * Update after-party ticket price
     *
     * @param concertId The ID of the concert
     * @param newPrice The new price for after-party tickets
     * @return true if update was successful, false otherwise
     */
    public boolean updateAfterPartyPrice(String concertId, double newPrice) {
        // First, get the current concert details
        GetConcertRequest getRequest = GetConcertRequest.newBuilder()
                .setConcertId(concertId)
                .build();

        Concert currentConcert = null;

        for (int attempt = 0; attempt < retryAttempts; attempt++) {
            try {
                GetConcertResponse getResponse = concertService.getConcert(getRequest);
                currentConcert = getResponse.getConcert();
                break;
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

        if (currentConcert == null) {
            logger.warning("Failed to retrieve concert with ID: " + concertId);
            return false;
        }

        // Check if after-party is available
        if (!currentConcert.getAfterParty().getAvailable()) {
            logger.warning("After-party is not available for this concert");
            return false;
        }

        // Create updated concert with the price change
        Concert.Builder concertBuilder = currentConcert.toBuilder();
        AfterPartyDetails.Builder afterPartyBuilder = concertBuilder.getAfterParty().toBuilder();
        afterPartyBuilder.setPrice(newPrice);
        concertBuilder.setAfterParty(afterPartyBuilder);

        // Create update request
        UpdateConcertRequest updateRequest = UpdateConcertRequest.newBuilder()
                .setConcert(concertBuilder.build())
                .build();

        for (int attempt = 0; attempt < retryAttempts; attempt++) {
            try {
                UpdateConcertResponse updateResponse = concertService.updateConcert(updateRequest);
                if (updateResponse.getSuccess()) {
                    logger.info("Successfully updated after-party price for concert: " + concertId);
                    return true;
                } else {
                    logger.warning("Failed to update after-party price: " + updateResponse.getMessage());
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
                    this.concertService.updateChannel(channel);
                }
            }
        }

        return false;
    }

    /**
     * Get current ticket inventory for a concert
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
     * List all concerts
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
}