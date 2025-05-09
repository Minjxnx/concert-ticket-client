package com.concert.client;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test cases for Concert Ticket Reservation System clients
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ClientTests {

    private static final String[] TEST_SERVERS = {"localhost:50051", "localhost:50052", "localhost:50053"};

    private OrganizerClient organizerClient;
    private BoxOfficeClient boxOfficeClient;
    private CustomerClient customerClient;
    private CoordinatorClient coordinatorClient;

    @BeforeAll
    public void setup() {
        // Initialize the real clients
        organizerClient = new OrganizerClient(TEST_SERVERS);
        boxOfficeClient = new BoxOfficeClient(TEST_SERVERS);
        customerClient = new CustomerClient(TEST_SERVERS);
        coordinatorClient = new CoordinatorClient(TEST_SERVERS);
    }

    @AfterAll
    public void cleanup() {
        // Shutdown the clients
        if (organizerClient != null) organizerClient.shutdown();
        if (boxOfficeClient != null) boxOfficeClient.shutdown();
        if (customerClient != null) customerClient.shutdown();
        if (coordinatorClient != null) coordinatorClient.shutdown();
    }

    @Test
    public void testAddConcert() {
        // Setup test data
        String name = "Test Concert";
        String date = "2025-06-15";
        String venue = "Test Venue";

        Map<String, Integer> seatQuantities = new HashMap<>();
        seatQuantities.put("Regular", 100);
        seatQuantities.put("VIP", 50);

        Map<String, Double> seatPrices = new HashMap<>();
        seatPrices.put("Regular", 50.0);
        seatPrices.put("VIP", 100.0);

        boolean hasAfterParty = true;
        int afterPartyQuantity = 75;
        double afterPartyPrice = 25.0;

        // Call the method
        boolean result = organizerClient.addConcert(
                name, date, venue, seatQuantities, seatPrices,
                hasAfterParty, afterPartyQuantity, afterPartyPrice
        );

        // Assert the result
        assertTrue(result, "Adding a concert should succeed");

        // Verify that the concert was added by listing concerts
        List<String> concerts = customerClient.listConcerts();
        boolean found = false;
        for (String concert : concerts) {
            if (concert.contains(name) && concert.contains(date) && concert.contains(venue)) {
                found = true;
                break;
            }
        }

        assertTrue(found, "The added concert should be in the list of concerts");
    }

    @Test
    public void testUpdateConcertDetails() {
        // First add a concert
        String name = "Update Test Concert";
        String date = "2025-07-20";
        String venue = "Original Venue";

        Map<String, Integer> seatQuantities = new HashMap<>();
        seatQuantities.put("Regular", 100);

        Map<String, Double> seatPrices = new HashMap<>();
        seatPrices.put("Regular", 50.0);

        boolean result = organizerClient.addConcert(
                name, date, venue, seatQuantities, seatPrices,
                false, 0, 0.0
        );

        assertTrue(result, "Adding a concert should succeed");

        // Get the concert ID
        List<String> concerts = customerClient.listConcerts();
        String concertId = null;
        for (String concert : concerts) {
            if (concert.contains(name) && concert.contains(date)) {
                // Extract the concert ID - assuming the format is "ID: XXX - Concert Name - Date"
                concertId = concert.substring(4, concert.indexOf(" - "));
                break;
            }
        }

        assertNotNull(concertId, "Should find the concert ID");

        // Update the venue
        String newVenue = "Updated Venue";
        boolean updateResult = organizerClient.updateConcertDetails(concertId, null, null, newVenue);

        assertTrue(updateResult, "Updating concert details should succeed");

        // Verify the update
        concerts = customerClient.listConcerts();
        boolean found = false;
        for (String concert : concerts) {
            if (concert.contains(name) && concert.contains(date) && concert.contains(newVenue)) {
                found = true;
                break;
            }
        }

        assertTrue(found, "The updated concert should have the new venue");
    }

    @Test
    public void testCancelConcert() {
        // First add a concert
        String name = "Cancel Test Concert";
        String date = "2025-08-10";
        String venue = "Cancel Venue";

        Map<String, Integer> seatQuantities = new HashMap<>();
        seatQuantities.put("Regular", 100);

        Map<String, Double> seatPrices = new HashMap<>();
        seatPrices.put("Regular", 50.0);

        boolean result = organizerClient.addConcert(
                name, date, venue, seatQuantities, seatPrices,
                false, 0, 0.0
        );

        assertTrue(result, "Adding a concert should succeed");

        // Get the concert ID
        List<String> concerts = customerClient.listConcerts();
        String concertId = null;
        for (String concert : concerts) {
            if (concert.contains(name) && concert.contains(date)) {
                // Extract the concert ID
                concertId = concert.substring(4, concert.indexOf(" - "));
                break;
            }
        }

        assertNotNull(concertId, "Should find the concert ID");

        // Cancel the concert
        boolean cancelResult = organizerClient.cancelConcert(concertId);

        assertTrue(cancelResult, "Cancelling a concert should succeed");

        // Verify the cancellation
        concerts = customerClient.listConcerts();
        boolean found = false;
        for (String concert : concerts) {
            if (concert.contains(name) && concert.contains(date)) {
                found = true;
                break;
            }
        }

        assertFalse(found, "The cancelled concert should not be in the list");
    }

    @Test
    public void testUpdateTicketStock() {
        // First add a concert
        String name = "Stock Update Test";
        String date = "2025-09-05";
        String venue = "Stock Venue";

        Map<String, Integer> seatQuantities = new HashMap<>();
        seatQuantities.put("Regular", 50);

        Map<String, Double> seatPrices = new HashMap<>();
        seatPrices.put("Regular", 50.0);

        boolean result = organizerClient.addConcert(
                name, date, venue, seatQuantities, seatPrices,
                false, 0, 0.0
        );

        assertTrue(result, "Adding a concert should succeed");

        // Get the concert ID
        List<String> concerts = customerClient.listConcerts();
        String concertId = null;
        for (String concert : concerts) {
            if (concert.contains(name) && concert.contains(date)) {
                // Extract the concert ID
                concertId = concert.substring(4, concert.indexOf(" - "));
                break;
            }
        }

        assertNotNull(concertId, "Should find the concert ID");

        // Update ticket stock
        int additionalTickets = 25;
        boolean updateResult = boxOfficeClient.updateConcertTicketStock(concertId, "Regular", additionalTickets);

        assertTrue(updateResult, "Updating ticket stock should succeed");

        // Verify the update by making multiple reservations
        String customerName = "Test Customer";
        String customerEmail = "test@example.com";

        // Make a reservation for 60 tickets (original 50 + additional 25 = 75 total)
        String reservationId = customerClient.makeReservation(
                concertId, "Regular", 60, customerName, customerEmail, false, 0
        );

        assertNotNull(reservationId, "Should be able to reserve more tickets than originally available");
    }

    @Test
    public void testMakeConcertReservation() {
        // First add a concert
        String name = "Reservation Test Concert";
        String date = "2025-10-15";
        String venue = "Reservation Venue";

        Map<String, Integer> seatQuantities = new HashMap<>();
        seatQuantities.put("Regular", 100);
        seatQuantities.put("VIP", 50);

        Map<String, Double> seatPrices = new HashMap<>();
        seatPrices.put("Regular", 50.0);
        seatPrices.put("VIP", 100.0);

        boolean result = organizerClient.addConcert(
                name, date, venue, seatQuantities, seatPrices,
                false, 0, 0.0
        );

        assertTrue(result, "Adding a concert should succeed");

        // Get the concert ID
        List<String> concerts = customerClient.listConcerts();
        String concertId = null;
        for (String concert : concerts) {
            if (concert.contains(name) && concert.contains(date)) {
                // Extract the concert ID
                concertId = concert.substring(4, concert.indexOf(" - "));
                break;
            }
        }

        assertNotNull(concertId, "Should find the concert ID");

        // Make a reservation
        String customerName = "John Doe";
        String customerEmail = "john@example.com";
        String seatType = "VIP";
        int quantity = 2;

        String reservationId = customerClient.makeReservation(
                concertId, seatType, quantity, customerName, customerEmail, false, 0
        );

        assertNotNull(reservationId, "Reservation should succeed");

        // Verify the reservation details
        String reservationDetails = customerClient.getReservationDetails(reservationId);

        assertNotNull(reservationDetails, "Should be able to retrieve reservation details");
        assertTrue(reservationDetails.contains(customerName), "Reservation details should contain customer name");
        assertTrue(reservationDetails.contains(seatType), "Reservation details should contain seat type");
        assertTrue(reservationDetails.contains(String.valueOf(quantity)), "Reservation details should contain quantity");
    }

    @Test
    public void testConcertAndAfterPartyReservation() {
        // First add a concert with after-party
        String name = "Combo Test Concert";
        String date = "2025-11-25";
        String venue = "Combo Venue";

        Map<String, Integer> seatQuantities = new HashMap<>();
        seatQuantities.put("Regular", 100);

        Map<String, Double> seatPrices = new HashMap<>();
        seatPrices.put("Regular", 50.0);

        boolean hasAfterParty = true;
        int afterPartyQuantity = 50;
        double afterPartyPrice = 25.0;

        boolean result = organizerClient.addConcert(
                name, date, venue, seatQuantities, seatPrices,
                hasAfterParty, afterPartyQuantity, afterPartyPrice
        );

        assertTrue(result, "Adding a concert with after-party should succeed");

        // Get the concert ID
        List<String> concerts = customerClient.listConcerts();
        String concertId = null;
        for (String concert : concerts) {
            if (concert.contains(name) && concert.contains(date)) {
                // Extract the concert ID
                concertId = concert.substring(4, concert.indexOf(" - "));
                break;
            }
        }

        assertNotNull(concertId, "Should find the concert ID");

        // Make a reservation with after-party tickets
        String customerName = "Jane Smith";
        String customerEmail = "jane@example.com";
        String seatType = "Regular";
        int concertQuantity = 2;
        int afterPartyQuantity = 2;

        String reservationId = customerClient.makeReservation(
                concertId, seatType, concertQuantity, customerName, customerEmail, true, afterPartyQuantity
        );

        assertNotNull(reservationId, "Combo reservation should succeed");

        // Verify the reservation details
        String reservationDetails = customerClient.getReservationDetails(reservationId);

        assertNotNull(reservationDetails, "Should be able to retrieve reservation details");
        assertTrue(reservationDetails.contains("After-Party"),
                "Reservation details should mention after-party tickets");
    }

    @Test
    public void testBulkReservation() {
        // First add a concert with after-party
        String name = "Bulk Reservation Test";
        String date = "2025-12-05";
        String venue = "Bulk Venue";

        Map<String, Integer> seatQuantities = new HashMap<>();
        seatQuantities.put("VIP", 80);

        Map<String, Double> seatPrices = new HashMap<>();
        seatPrices.put("VIP", 120.0);

        boolean hasAfterParty = true;
        int afterPartyQuantity = 100;
        double afterPartyPrice = 30.0;

        boolean result = organizerClient.addConcert(
                name, date, venue, seatQuantities, seatPrices,
                hasAfterParty, afterPartyQuantity, afterPartyPrice
        );

        assertTrue(result, "Adding a concert with after-party should succeed");

        // Get the concert ID
        List<String> concerts = customerClient.listConcerts();
        String concertId = null;
        for (String concert : concerts) {
            if (concert.contains(name) && concert.contains(date)) {
                // Extract the concert ID
                concertId = concert.substring(4, concert.indexOf(" - "));
                break;
            }
        }

        assertNotNull(concertId, "Should find the concert ID");

        // Make a bulk reservation
        String groupName = "VIP Guests";
        String coordinatorName = "Event Manager";
        String coordinatorEmail = "manager@example.com";
        String seatType = "VIP";
        int concertQuantity = 20;
        int afterPartyQuantity = 20;

        String reservationId = coordinatorClient.makeBulkReservation(
                concertId, seatType, concertQuantity, afterPartyQuantity,
                groupName, coordinatorName, coordinatorEmail
        );

        assertNotNull(reservationId, "Bulk reservation should succeed");

        // Verify the reservation details
        String reservationDetails = customerClient.getReservationDetails(reservationId);

        assertNotNull(reservationDetails, "Should be able to retrieve reservation details");
        assertTrue(reservationDetails.contains(groupName), "Reservation details should contain group name");
        assertTrue(reservationDetails.contains(String.valueOf(concertQuantity)),
                "Reservation details should contain concert quantity");
        assertTrue(reservationDetails.contains(String.valueOf(afterPartyQuantity)),
                "Reservation details should contain after-party quantity");
    }

    @Test
    public void testConcurrentReservations() throws InterruptedException {
        // First add a concert with limited seats
        String name = "Concurrent Test Concert";
        String date = "2026-01-10";
        String venue = "Concurrent Venue";

        Map<String, Integer> seatQuantities = new HashMap<>();
        seatQuantities.put("Premium", 10); // Only 10 premium seats

        Map<String, Double> seatPrices = new HashMap<>();
        seatPrices.put("Premium", 150.0);

        boolean result = organizerClient.addConcert(
                name, date, venue, seatQuantities, seatPrices,
                false, 0, 0.0
        );

        assertTrue(result, "Adding a concert should succeed");

        // Get the concert ID
        List<String> concerts = customerClient.listConcerts();
        String concertId = null;
        for (String concert : concerts) {
            if (concert.contains(name) && concert.contains(date)) {
                // Extract the concert ID
                concertId = concert.substring(4, concert.indexOf(" - "));
                break;
            }
        }

        assertNotNull(concertId, "Should find the concert ID");

        // Create multiple customer clients to simulate concurrent reservations
        final String finalConcertId = concertId;
        int numThreads = 15; // More than available seats
        final CountDownLatch latch = new CountDownLatch(1);
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        final List<String> successfulReservations = new java.util.concurrent.CopyOnWriteArrayList<>();

        for (int i = 0; i < numThreads; i++) {
            final int customerNumber = i;
            executorService.submit(() -> {
                try {
                    // Wait for all threads to be ready
                    latch.await();

                    // Each customer tries to book 1 premium seat
                    String customerName = "Concurrent Customer " + customerNumber;
                    String customerEmail = "customer" + customerNumber + "@example.com";
                    String seatType = "Premium";
                    int quantity = 1;

                    String reservationId = customerClient.makeReservation(
                            finalConcertId, seatType, quantity, customerName, customerEmail, false, 0
                    );

                    if (reservationId != null) {
                        successfulReservations.add(reservationId);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        // Start all threads at roughly the same time
        latch.countDown();

        // Wait for all threads to complete
        executorService.shutdown();
        executorService.awaitTermination(30, TimeUnit.SECONDS);

        // Verify that exactly 10 reservations succeeded (first-come-first-served)
        assertEquals(10, successfulReservations.size(),
                "Exactly 10 reservations should succeed (first-come-first-served)");
    }

    @Test
    public void testAtomicityOfComboBooking() {
        // First add a concert with after-party but limited after-party tickets
        String name = "Atomicity Test Concert";
        String date = "2026-02-15";
        String venue = "Atomicity Venue";

        Map<String, Integer> seatQuantities = new HashMap<>();
        seatQuantities.put("Regular", 100);

        Map<String, Double> seatPrices = new HashMap<>();
        seatPrices.put("Regular", 50.0);

        boolean hasAfterParty = true;
        int afterPartyQuantity = 5; // Only 5 after-party tickets
        double afterPartyPrice = 25.0;

        boolean result = organizerClient.addConcert(
                name, date, venue, seatQuantities, seatPrices,
                hasAfterParty, afterPartyQuantity, afterPartyPrice
        );

        assertTrue(result, "Adding a concert with after-party should succeed");

        // Get the concert ID
        List<String> concerts = customerClient.listConcerts();
        String concertId = null;
        for (String concert : concerts) {
            if (concert.contains(name) && concert.contains(date)) {
                // Extract the concert ID
                concertId = concert.substring(4, concert.indexOf(" - "));
                break;
            }
        }

        assertNotNull(concertId, "Should find the concert ID");

        // First book all after-party tickets
        String groupName = "VIP Group";
        String coordinatorName = "VIP Coordinator";
        String coordinatorEmail = "vip@example.com";
        String seatType = "Regular";
        int concertQuantity = 5;

        String bulkReservationId = coordinatorClient.makeBulkReservation(
                concertId, seatType, concertQuantity, afterPartyQuantity,
                groupName, coordinatorName, coordinatorEmail
        );

        assertNotNull(bulkReservationId, "Bulk reservation should succeed");

        // Now try to make another combo booking - should fail atomically
        String customerName = "Failed Customer";
        String customerEmail = "failed@example.com";
        concertQuantity = 1;
        int customerAfterPartyQuantity = 1;

        String reservationId = customerClient.makeReservation(
                concertId, seatType, concertQuantity, customerName, customerEmail, true, customerAfterPartyQuantity
        );

        assertNull(reservationId, "Combo reservation should fail atomically when after-party tickets are unavailable");

        // Verify that a concert-only reservation still works
        customerName = "Concert Only Customer";
        customerEmail = "concertonly@example.com";

        reservationId = customerClient.makeReservation(
                concertId, seatType, concertQuantity, customerName, customerEmail, false, 0
        );

        assertNotNull(reservationId, "Concert-only reservation should still succeed");
    }
}