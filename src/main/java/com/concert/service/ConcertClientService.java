package com.concert.service;

import com.concert.grpc.ConcertServiceGrpc;
import com.concert.grpc.ConcertProto.*;

import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;

import java.util.logging.Logger;

/**
 * Client-side service for interacting with the Concert Service.
 */
public class ConcertClientService {
    private static final Logger logger = Logger.getLogger(ConcertClientService.class.getName());
    private ConcertServiceGrpc.ConcertServiceBlockingStub blockingStub;

    public ConcertClientService(ManagedChannel channel) {
        this.blockingStub = ConcertServiceGrpc.newBlockingStub(channel);
    }

    /**
     * Update the gRPC channel when switching servers
     *
     * @param channel The new channel to use
     */
    public void updateChannel(ManagedChannel channel) {
        this.blockingStub = ConcertServiceGrpc.newBlockingStub(channel);
    }

    /**
     * Add a new concert
     *
     * @param request The request containing concert details
     * @return The response from the server
     * @throws StatusRuntimeException If an RPC error occurs
     */
    public AddConcertResponse addConcert(AddConcertRequest request) throws StatusRuntimeException {
        logger.info("Adding concert: " + request.getConcert().getName());
        return blockingStub.addConcert(request);
    }

    /**
     * Update an existing concert
     *
     * @param request The request containing updated concert details
     * @return The response from the server
     * @throws StatusRuntimeException If an RPC error occurs
     */
    public UpdateConcertResponse updateConcert(UpdateConcertRequest request) throws StatusRuntimeException {
        logger.info("Updating concert: " + request.getConcert().getId());
        return blockingStub.updateConcert(request);
    }

    /**
     * Cancel a concert
     *
     * @param request The request containing the concert ID to cancel
     * @return The response from the server
     * @throws StatusRuntimeException If an RPC error occurs
     */
    public CancelConcertResponse cancelConcert(CancelConcertRequest request) throws StatusRuntimeException {
        logger.info("Cancelling concert: " + request.getConcertId());
        return blockingStub.cancelConcert(request);
    }

    /**
     * Get details of a specific concert
     *
     * @param request The request containing the concert ID
     * @return The response from the server
     * @throws StatusRuntimeException If an RPC error occurs
     */
    public GetConcertResponse getConcert(GetConcertRequest request) throws StatusRuntimeException {
        logger.info("Getting concert: " + request.getConcertId());
        return blockingStub.getConcert(request);
    }

    /**
     * List all concerts
     *
     * @param request The request for listing concerts
     * @return The response from the server
     * @throws StatusRuntimeException If an RPC error occurs
     */
    public ListConcertsResponse listConcerts(ListConcertsRequest request) throws StatusRuntimeException {
        logger.info("Listing all concerts");
        return blockingStub.listConcerts(request);
    }
}