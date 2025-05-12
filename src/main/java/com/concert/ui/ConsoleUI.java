package com.concert.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Console UI for interacting with the Concert Ticket Reservation System.
 * Provides different interfaces for different user roles.
 */
public class ConsoleUI {
    private Scanner scanner;
    private OrganizerClient organizerClient;
    private BoxOfficeClient boxOfficeClient;
    private CustomerClient customerClient;
    private CoordinatorClient coordinatorClient;
    private boolean running;

    public ConsoleUI(String[] serverAddresses) {
        this.scanner = new Scanner(System.in);
        this.organizerClient = new OrganizerClient(serverAddresses);
        this.boxOfficeClient = new BoxOfficeClient(serverAddresses);
        this.customerClient = new CustomerClient(serverAddresses);
        this.coordinatorClient = new CoordinatorClient(serverAddresses);
        this.running = true;
    }

    /**
     * Start the console UI
     */
    public void start() {
        System.out.println("Welcome to Concert Ticket Reservation System");
        while (running) {
            displayMainMenu();
            int choice = getChoice(4);
            handleMainMenuChoice(choice);
        }
        cleanup();
    }

    /**
     * Clean up resources before exiting
     */
    private void cleanup() {
        organizerClient.shutdown();
        boxOfficeClient.shutdown();
        customerClient.shutdown();
        coordinatorClient.shutdown();
        scanner.close();
        System.out.println("Thank you for using Concert Ticket Reservation System. Goodbye!");
    }

    /**
     * Display the main menu options
     */
    private void displayMainMenu() {
        System.out.println("\n==== MAIN MENU ====");
        System.out.println("1. Concert Organizer");
        System.out.println("2. Box Office Clerk");
        System.out.println("3. Customer");
        System.out.println("4. Event Coordinator");
        System.out.println("0. Exit");
        System.out.print("Choose role: ");
    }

    /**
     * Get user input as an integer choice
     */
    private int getChoice(int maxOption) {
        int choice = -1;
        while (choice < 0 || choice > maxOption) {
            try {
                String input = scanner.nextLine();
                choice = Integer.parseInt(input);
                if (choice < 0 || choice > maxOption) {
                    System.out.print("Invalid choice. Try again (0-" + maxOption + "): ");
                }
            } catch (NumberFormatException e) {
                System.out.print("Please enter a number (0-" + maxOption + "): ");
            }
        }
        return choice;
    }

    /**
     * Handle the main menu selection
     */
    private void handleMainMenuChoice(int choice) {
        switch (choice) {
            case 0:
                running = false;
                break;
            case 1:
                organizerMenu();
                break;
            case 2:
                boxOfficeMenu();
                break;
            case 3:
                customerMenu();
                break;
            case 4:
                coordinatorMenu();
                break;
        }
    }

    /**
     * Concert Organizer menu
     */
    private void organizerMenu() {
        boolean inOrganizerMenu = true;
        while (inOrganizerMenu) {
            System.out.println("\n==== CONCERT ORGANIZER MENU ====");
            System.out.println("1. Add New Concert");
            System.out.println("2. Update Concert Details");
            System.out.println("3. Cancel Concert");
            System.out.println("4. List All Concerts");
            System.out.println("0. Back to Main Menu");
            System.out.print("Choose option: ");

            int choice = getChoice(4);
            switch (choice) {
                case 0:
                    inOrganizerMenu = false;
                    break;
                case 1:
                    addNewConcert();
                    break;
                case 2:
                    updateConcertDetails();
                    break;
                case 3:
                    cancelConcert();
                    break;
                case 4:
                    listAllConcerts();
                    break;
            }
        }
    }

    /**
     * Box Office Clerk menu
     */
    private void boxOfficeMenu() {
        boolean inBoxOfficeMenu = true;
        while (inBoxOfficeMenu) {
            System.out.println("\n==== BOX OFFICE CLERK MENU ====");
            System.out.println("1. Update Concert Ticket Stock");
            System.out.println("2. Update After-Party Ticket Stock");
            System.out.println("3. Update Ticket Pricing");
            System.out.println("4. List All Concerts");
            System.out.println("0. Back to Main Menu");
            System.out.print("Choose option: ");

            int choice = getChoice(4);
            switch (choice) {
                case 0:
                    inBoxOfficeMenu = false;
                    break;
                case 1:
                    updateConcertTicketStock();
                    break;
                case 2:
                    updateAfterPartyTicketStock();
                    break;
                case 3:
                    updateTicketPricing();
                    break;
                case 4:
                    listAllConcerts();
                    break;
            }
        }
    }

    /**
     * Customer menu
     */
    private void customerMenu() {
        boolean inCustomerMenu = true;
        while (inCustomerMenu) {
            System.out.println("\n==== CUSTOMER MENU ====");
            System.out.println("1. Browse Concerts");
            System.out.println("2. Make Concert Reservation");
            System.out.println("3. Make Concert + After-Party Reservation");
            System.out.println("4. View Reservation");
            System.out.println("0. Back to Main Menu");
            System.out.print("Choose option: ");

            int choice = getChoice(4);
            switch (choice) {
                case 0:
                    inCustomerMenu = false;
                    break;
                case 1:
                    browseConcerts();
                    break;
                case 2:
                    makeConcertReservation();
                    break;
                case 3:
                    makeConcertAndAfterPartyReservation();
                    break;
                case 4:
                    viewReservation();
                    break;
            }
        }
    }

    /**
     * Event Coordinator menu
     */
    private void coordinatorMenu() {
        boolean inCoordinatorMenu = true;
        while (inCoordinatorMenu) {
            System.out.println("\n==== EVENT COORDINATOR MENU ====");
            System.out.println("1. Make Bulk Reservation (Concert + After-Party)");
            System.out.println("2. List All Concerts");
            System.out.println("0. Back to Main Menu");
            System.out.print("Choose option: ");

            int choice = getChoice(2);
            switch (choice) {
                case 0:
                    inCoordinatorMenu = false;
                    break;
                case 1:
                    makeBulkReservation();
                    break;
                case 2:
                    listAllConcerts();
                    break;
            }
        }
    }

    // Concert Organizer operations

    private void addNewConcert() {
        try {
            System.out.println("\n==== ADD NEW CONCERT ====");

            System.out.print("Concert Name: ");
            String name = scanner.nextLine();

            System.out.print("Date (YYYY-MM-DD): ");
            String date = scanner.nextLine();

            System.out.print("Venue: ");
            String venue = scanner.nextLine();

            System.out.print("Number of Seating Tiers (e.g., Regular, VIP): ");
            int tiers = Integer.parseInt(scanner.nextLine());

            Map<String, Integer> seatQuantities = new HashMap<>();
            Map<String, Double> seatPrices = new HashMap<>();

            for (int i = 0; i < tiers; i++) {
                System.out.print("Tier " + (i + 1) + " Name: ");
                String tierName = scanner.nextLine();

                System.out.print("Number of " + tierName + " Seats: ");
                int quantity = Integer.parseInt(scanner.nextLine());

                System.out.print("Price for " + tierName + " Seats: $");
                double price = Double.parseDouble(scanner.nextLine());

                seatQuantities.put(tierName, quantity);
                seatPrices.put(tierName, price);
            }

            System.out.print("Include After-Party? (y/n): ");
            boolean hasAfterParty = scanner.nextLine().trim().toLowerCase().startsWith("y");

            int afterPartyQuantity = 0;
            double afterPartyPrice = 0.0;

            if (hasAfterParty) {
                System.out.print("Number of After-Party Tickets: ");
                afterPartyQuantity = Integer.parseInt(scanner.nextLine());

                System.out.print("Price for After-Party Tickets: $");
                afterPartyPrice = Double.parseDouble(scanner.nextLine());
            }

            boolean success = organizerClient.addConcert(
                    name, date, venue, seatQuantities, seatPrices,
                    hasAfterParty, afterPartyQuantity, afterPartyPrice
            );

            if (success) {
                System.out.println("Concert added successfully!");
            } else {
                System.out.println("Failed to add concert. Please try again.");
            }
        } catch (Exception e) {
            System.out.println("Error adding concert: " + e.getMessage());
        }
    }

    private void updateConcertDetails() {
        try {
            listAllConcerts();

            System.out.println("\n==== UPDATE CONCERT DETAILS ====");
            System.out.print("Enter Concert ID to update: ");
            String concertId = scanner.nextLine();

            System.out.print("Update Name? (y/n): ");
            boolean updateName = scanner.nextLine().trim().toLowerCase().startsWith("y");
            String name = null;
            if (updateName) {
                System.out.print("New Name: ");
                name = scanner.nextLine();
            }

            System.out.print("Update Date? (y/n): ");
            boolean updateDate = scanner.nextLine().trim().toLowerCase().startsWith("y");
            String date = null;
            if (updateDate) {
                System.out.print("New Date (YYYY-MM-DD): ");
                date = scanner.nextLine();
            }

            System.out.print("Update Venue? (y/n): ");
            boolean updateVenue = scanner.nextLine().trim().toLowerCase().startsWith("y");
            String venue = null;
            if (updateVenue) {
                System.out.print("New Venue: ");
                venue = scanner.nextLine();
            }

            boolean success = organizerClient.updateConcertDetails(concertId, name, date, venue);

            if (success) {
                System.out.println("Concert details updated successfully!");
            } else {
                System.out.println("Failed to update concert details. Please try again.");
            }
        } catch (Exception e) {
            System.out.println("Error updating concert details: " + e.getMessage());
        }
    }

    private void cancelConcert() {
        try {
            listAllConcerts();

            System.out.println("\n==== CANCEL CONCERT ====");
            System.out.print("Enter Concert ID to cancel: ");
            String concertId = scanner.nextLine();

            System.out.print("Are you sure you want to cancel this concert? (y/n): ");
            boolean confirm = scanner.nextLine().trim().toLowerCase().startsWith("y");

            if (confirm) {
                boolean success = organizerClient.cancelConcert(concertId);

                if (success) {
                    System.out.println("Concert cancelled successfully!");
                } else {
                    System.out.println("Failed to cancel concert. Please try again.");
                }
            } else {
                System.out.println("Cancellation aborted.");
            }
        } catch (Exception e) {
            System.out.println("Error cancelling concert: " + e.getMessage());
        }
    }

    // Box Office Clerk Operations

    private void updateConcertTicketStock() {
        try {
            listAllConcerts();

            System.out.println("\n==== UPDATE CONCERT TICKET STOCK ====");
            System.out.print("Enter Concert ID: ");
            String concertId = scanner.nextLine();

            System.out.print("Enter Seat Type: ");
            String seatType = scanner.nextLine();

            System.out.print("Additional Ticket Quantity: ");
            int quantity = Integer.parseInt(scanner.nextLine());

            boolean success = boxOfficeClient.updateConcertTicketStock(concertId, seatType, quantity);

            if (success) {
                System.out.println("Ticket stock updated successfully!");
            } else {
                System.out.println("Failed to update ticket stock. Please try again.");
            }
        } catch (Exception e) {
            System.out.println("Error updating ticket stock: " + e.getMessage());
        }
    }

    private void updateAfterPartyTicketStock() {
        try {
            listAllConcerts();

            System.out.println("\n==== UPDATE AFTER-PARTY TICKET STOCK ====");
            System.out.print("Enter Concert ID: ");
            String concertId = scanner.nextLine();

            System.out.print("Additional After-Party Ticket Quantity: ");
            int quantity = Integer.parseInt(scanner.nextLine());

            boolean success = boxOfficeClient.updateAfterPartyTicketStock(concertId, quantity);

            if (success) {
                System.out.println("After-party ticket stock updated successfully!");
            } else {
                System.out.println("Failed to update after-party ticket stock. Please try again.");
            }
        } catch (Exception e) {
            System.out.println("Error updating after-party ticket stock: " + e.getMessage());
        }
    }

    private void updateTicketPricing() {
        try {
            listAllConcerts();

            System.out.println("\n==== UPDATE TICKET PRICING ====");
            System.out.print("Enter Concert ID: ");
            String concertId = scanner.nextLine();

            System.out.print("Update Concert Ticket Price? (y/n): ");
            boolean updateConcertPrice = scanner.nextLine().trim().toLowerCase().startsWith("y");

            String seatType = null;
            double concertPrice = 0.0;

            if (updateConcertPrice) {
                System.out.print("Seat Type: ");
                seatType = scanner.nextLine();

                System.out.print("New Price: $");
                concertPrice = Double.parseDouble(scanner.nextLine());
            }

            System.out.print("Update After-Party Ticket Price? (y/n): ");
            boolean updateAfterPartyPrice = scanner.nextLine().trim().toLowerCase().startsWith("y");

            double afterPartyPrice = 0.0;

            if (updateAfterPartyPrice) {
                System.out.print("New After-Party Price: $");
                afterPartyPrice = Double.parseDouble(scanner.nextLine());
            }

            boolean success = false;

            if (updateConcertPrice && updateAfterPartyPrice) {
                success = boxOfficeClient.updateTicketPricing(concertId, seatType, concertPrice, true, afterPartyPrice);
            } else if (updateConcertPrice) {
                success = boxOfficeClient.updateConcertTicketPrice(concertId, seatType, concertPrice);
            } else if (updateAfterPartyPrice) {
                success = boxOfficeClient.updateAfterPartyTicketPrice(concertId, afterPartyPrice);
            }

            if (success) {
                System.out.println("Ticket pricing updated successfully!");
            } else {
                System.out.println("Failed to update ticket pricing. Please try again.");
            }
        } catch (Exception e) {
            System.out.println("Error updating ticket pricing: " + e.getMessage());
        }
    }

    // Customer Operations

    private void browseConcerts() {
        System.out.println("\n==== BROWSE CONCERTS ====");
        listAllConcerts();
    }

    private void makeConcertReservation() {
        try {
            listAllConcerts();

            System.out.println("\n==== MAKE CONCERT RESERVATION ====");
            System.out.print("Enter Concert ID: ");
            String concertId = scanner.nextLine();

            System.out.print("Enter Seat Type: ");
            String seatType = scanner.nextLine();

            System.out.print("Number of Tickets: ");
            int quantity = Integer.parseInt(scanner.nextLine());

            System.out.print("Customer Name: ");
            String customerName = scanner.nextLine();

            System.out.print("Customer Email: ");
            String customerEmail = scanner.nextLine();

            String reservationId = customerClient.makeReservation(
                    concertId, seatType, quantity, customerName, customerEmail, false, 0
            );

            if (reservationId != null) {
                System.out.println("Reservation successful! Your reservation ID is: " + reservationId);
            } else {
                System.out.println("Reservation failed. Please try again.");
            }
        } catch (Exception e) {
            System.out.println("Error making reservation: " + e.getMessage());
        }
    }

    private void makeConcertAndAfterPartyReservation() {
        try {
            listAllConcerts();

            System.out.println("\n==== MAKE CONCERT + AFTER-PARTY RESERVATION ====");
            System.out.print("Enter Concert ID: ");
            String concertId = scanner.nextLine();

            System.out.print("Enter Seat Type: ");
            String seatType = scanner.nextLine();

            System.out.print("Number of Concert Tickets: ");
            int concertQuantity = Integer.parseInt(scanner.nextLine());

            System.out.print("Number of After-Party Tickets: ");
            int afterPartyQuantity = Integer.parseInt(scanner.nextLine());

            System.out.print("Customer Name: ");
            String customerName = scanner.nextLine();

            System.out.print("Customer Email: ");
            String customerEmail = scanner.nextLine();

            String reservationId = customerClient.makeReservation(
                    concertId, seatType, concertQuantity, customerName, customerEmail, true, afterPartyQuantity
            );

            if (reservationId != null) {
                System.out.println("Reservation successful! Your reservation ID is: " + reservationId);
            } else {
                System.out.println("Reservation failed. Please try again.");
            }
        } catch (Exception e) {
            System.out.println("Error making reservation: " + e.getMessage());
        }
    }

    private void viewReservation() {
        try {
            System.out.println("\n==== VIEW RESERVATION ====");
            System.out.print("Enter Reservation ID: ");
            String reservationId = scanner.nextLine();

            String reservationDetails = customerClient.getReservationDetails(reservationId);

            if (reservationDetails != null) {
                System.out.println(reservationDetails);
            } else {
                System.out.println("Reservation not found. Please check your reservation ID.");
            }
        } catch (Exception e) {
            System.out.println("Error viewing reservation: " + e.getMessage());
        }
    }

    // Event Coordinator Operations

    private void makeBulkReservation() {
        try {
            listAllConcerts();

            System.out.println("\n==== MAKE BULK RESERVATION ====");
            System.out.print("Enter Concert ID: ");
            String concertId = scanner.nextLine();

            System.out.print("Enter Seat Type: ");
            String seatType = scanner.nextLine();

            System.out.print("Number of Concert Tickets: ");
            int concertQuantity = Integer.parseInt(scanner.nextLine());

            System.out.print("Number of After-Party Tickets: ");
            int afterPartyQuantity = Integer.parseInt(scanner.nextLine());

            System.out.print("Group Name: ");
            String groupName = scanner.nextLine();

            System.out.print("Coordinator Name: ");
            String coordinatorName = scanner.nextLine();

            System.out.print("Coordinator Email: ");
            String coordinatorEmail = scanner.nextLine();

            String reservationId = coordinatorClient.makeBulkReservation(
                    concertId, seatType, concertQuantity, afterPartyQuantity,
                    groupName, coordinatorName, coordinatorEmail
            );

            if (reservationId != null) {
                System.out.println("Bulk reservation successful! Reservation ID: " + reservationId);
            } else {
                System.out.println("Bulk reservation failed. Please try again.");
            }
        } catch (Exception e) {
            System.out.println("Error making bulk reservation: " + e.getMessage());
        }
    }

    // Utility Methods

    private void listAllConcerts() {
        try {
            List<String> concerts = customerClient.listConcerts();

            if (concerts.isEmpty()) {
                System.out.println("No concerts available.");
            } else {
                System.out.println("\nAvailable Concerts:");
                for (String concert : concerts) {
                    System.out.println(concert);
                }
            }
        } catch (Exception e) {
            System.out.println("Error listing concerts: " + e.getMessage());
        }
    }
}