package com.concert.service;

import com.concert.grpc.TicketInventoryServiceGrpc;
import com.concert.grpc.TicketProto.*;

import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;

import java.util.logging.Logger;

/**
 * Client-side service for interacting with the Ticket Inventory Service.
 */
public class TicketClientService {
    private static final Logger logger = Logger.getLogger(TicketClientService.class.getName());
    private TicketInventoryServiceGrpc.TicketInventoryServiceBlockingStub blockingStub;

    public TicketClientService(ManagedChannel channel) {
        this.blockingStub = TicketInventoryServiceGrpc.newBlockingStub(channel);
    }

    /**
     * Update the gRPC channel when switching servers
     *
     * @param channel The new channel to use
     */
    public void updateChannel(ManagedChannel channel) {
        this.blockingStub = TicketInventoryServiceGrpc.newBlockingStub(channel);
    }

    /**
     * Get ticket inventory for a concert
     *
     * @param request The request containing the concert ID
     * @return The response from the server
     * @throws StatusRuntimeException If an RPC error occurs
     */
    public GetTicketInventoryResponse getTicketInventory(GetTicketInventoryRequest request) throws StatusRuntimeException {
        logger.info("Getting ticket inventory for concert: " + request.getConcertId());
        return blockingStub.getTicketInventory(request);
    }

    /**
     * Update ticket inventory for a concert
     *
     * @param request The request containing update details
     * @return The response from the server
     * @throws StatusRuntimeException If an RPC error occurs
     */
    public UpdateTicketInventoryResponse updateTicketInventory(UpdateTicketInventoryRequest request) throws StatusRuntimeException {
        logger.info("Updating ticket inventory for concert: " + request.getConcertId() +
                ", tier: " + request.getSeatTierName() +
                ", adding: " + request.getAdditionalTickets());
        return blockingStub.updateTicketInventory(request);
    }

    /**
     * Update after-party ticket inventory
     *
     * @param request The request containing update details
     * @return The response from the server
     * @throws StatusRuntimeException If an RPC error occurs
     */
    public UpdateAfterPartyInventoryResponse updateAfterPartyInventory(UpdateAfterPartyInventoryRequest request) throws StatusRuntimeException {
        logger.info("Updating after-party inventory for concert: " + request.getConcertId() +
                ", adding: " + request.getAdditionalTickets());
        return blockingStub.updateAfterPartyInventory(request);
    }

    /**
     * Check availability for a reservation
     *
     * @param request The request containing reservation details
     * @return The response from the server
     * @throws StatusRuntimeException If an RPC error occurs
     */
    public CheckAvailabilityResponse checkAvailability(CheckAvailabilityRequest request) throws StatusRuntimeException {
        logger.info("Checking availability for concert: " + request.getConcertId() +
                ", tier: " + request.getSeatTierName() +
                ", seats: " + request.getSeatCount() +
                ", after-party: " + request.getIncludeAfterParty());
        return blockingStub.checkAvailability(request);
    }
}