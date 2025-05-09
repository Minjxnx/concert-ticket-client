package com.concert.client;

import com.concert.service.ConcertClientService;
import com.concert.grpc.ConcertServiceGrpc;
import com.concert.grpc.ConcertProto.*;
import io.grpc.StatusRuntimeException;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.UUID;

/**
 * Client implementation for Concert Organizers.
 * Provides functionality to add, update, and cancel concerts.
 */
public class OrganizerClient extends ConcertClient {
    private static final Logger logger = Logger.getLogger(OrganizerClient.class.getName());
    private final ConcertClientService concertService;

    public OrganizerClient() {
        super();
        this.concertService = new ConcertClientService(channel);
    }

    /**
     * Add a new concert to the system
     *
     * @param name The name of the concert
     * @param date The date of the concert
     * @param venue The venue for the concert
     * @param seatTiers Map of seat tier names to their total counts
     * @param seatPrices Map of seat tier names to their prices
     * @param afterPartyAvailable Whether after-party tickets are available
     * @param afterPartyTickets Number of after-party tickets available
     * @param afterPartyPrice Price of after-party tickets
     * @return The ID of the newly created concert, or null if creation failed
     */
    public String addConcert(String name, String date, String venue,
                             Map<String, Integer> seatTiers,
                             Map<String, Double> seatPrices,
                             boolean afterPartyAvailable,
                             int afterPartyTickets,
                             double afterPartyPrice) {
        String concertId = UUID.randomUUID().toString();

        // Build list of seat tiers
        List<SeatTier> tiers = seatTiers.entrySet().stream()
                .map(entry -> SeatTier.newBuilder()
                        .setName(entry.getKey())
                        .setCapacity(entry.getValue())
                        .setPrice(seatPrices.getOrDefault(entry.getKey(), 0.0))
                        .build())
                .toList();

        // Build after-party details
        AfterPartyDetails afterParty = null;
        if (afterPartyAvailable) {
            afterParty = AfterPartyDetails.newBuilder()
                    .setAvailable(true)
                    .setTotalTickets(afterPartyTickets)
                    .setPrice(afterPartyPrice)
                    .build();
        } else {
            afterParty = AfterPartyDetails.newBuilder()
                    .setAvailable(false)
                    .build();
        }

        // Create concert request
        Concert concert = Concert.newBuilder()
                .setId(concertId)
                .setName(name)
                .setDate(date)
                .setVenue(venue)
                .addAllSeatTiers(tiers)
                .setAfterParty(afterParty)
                .build();

        AddConcertRequest request = AddConcertRequest.newBuilder()
                .setConcert(concert)
                .build();

        for (int attempt = 0; attempt < retryAttempts; attempt++) {
            try {
                AddConcertResponse response = concertService.addConcert(request);
                if (response.getSuccess()) {
                    logger.info("Successfully added concert: " + concertId);
                    return concertId;
                } else {
                    logger.warning("Failed to add concert: " + response.getMessage());
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
                    this.concertService.updateChannel(channel);
                }
            }
        }

        return null;
    }

    /**
     * Update an existing concert's details
     *
     * @param concertId The ID of the concert to update
     * @param updates Map of field names to new values
     * @return true if update was successful, false otherwise
     */
    public boolean updateConcert(String concertId, Map<String, Object> updates) {
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

        // Create updated concert with the changes
        Concert.Builder concertBuilder = currentConcert.toBuilder();

        // Apply updates
        for (Map.Entry<String, Object> update : updates.entrySet()) {
            String field = update.getKey();
            Object value = update.getValue();

            switch (field) {
                case "name":
                    concertBuilder.setName((String) value);
                    break;
                case "date":
                    concertBuilder.setDate((String) value);
                    break;
                case "venue":
                    concertBuilder.setVenue((String) value);
                    break;
                case "afterPartyPrice":
                    AfterPartyDetails.Builder afterPartyBuilder = concertBuilder.getAfterParty().toBuilder();
                    afterPartyBuilder.setPrice((Double) value);
                    concertBuilder.setAfterParty(afterPartyBuilder);
                    break;
                case "afterPartyTickets":
                    AfterPartyDetails.Builder apBuilder = concertBuilder.getAfterParty().toBuilder();
                    apBuilder.setTotalTickets((Integer) value);
                    concertBuilder.setAfterParty(apBuilder);
                    break;
                // Can add more update fields as needed
            }
        }

        // Create update request
        UpdateConcertRequest updateRequest = UpdateConcertRequest.newBuilder()
                .setConcert(concertBuilder.build())
                .build();

        for (int attempt = 0; attempt < retryAttempts; attempt++) {
            try {
                UpdateConcertResponse updateResponse = concertService.updateConcert(updateRequest);
                if (updateResponse.getSuccess()) {
                    logger.info("Successfully updated concert: " + concertId);
                    return true;
                } else {
                    logger.warning("Failed to update concert: " + updateResponse.getMessage());
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
     * Cancel a concert
     *
     * @param concertId The ID of the concert to cancel
     * @return true if cancellation was successful, false otherwise
     */
    public boolean cancelConcert(String concertId) {
        CancelConcertRequest request = CancelConcertRequest.newBuilder()
                .setConcertId(concertId)
                .build();

        for (int attempt = 0; attempt < retryAttempts; attempt++) {
            try {
                CancelConcertResponse response = concertService.cancelConcert(request);
                if (response.getSuccess()) {
                    logger.info("Successfully cancelled concert: " + concertId);
                    return true;
                } else {
                    logger.warning("Failed to cancel concert: " + response.getMessage());
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