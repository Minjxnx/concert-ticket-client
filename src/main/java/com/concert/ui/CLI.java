package com.concert.client.ui;

import com.concert.client.ConcertClient;
import com.concert.client.OrganizerClient;
import com.concert.client.BoxOfficeClient;
import com.concert.client.CustomerClient;
import com.concert.client.CoordinatorClient;

import java.util.Scanner;

/**
 * Command Line Interface for the Concert Ticket Reservation System.
 * This class handles the main command line interface for different user roles.
 */
public class CLI {
    private final Scanner scanner;
    private final ConsoleUI consoleUI;
    private ConcertClient activeClient;

    /**
     * Constructor for CLI
     */
    public CLI() {
        this.scanner = new Scanner(System.in);
        this.consoleUI = new ConsoleUI();
    }

    /**
     * Start the CLI application
     */
    public void start() {
        System.out.println("===========================================");
        System.out.println("  CONCERT TICKET RESERVATION SYSTEM");
        System.out.println("===========================================");

        boolean running = true;
        while (running) {
            displayMainMenu();
            int choice = getIntInput("Enter your choice: ");

            switch (choice) {
                case 1:
                    loginAsOrganizer();
                    break;
                case 2:
                    loginAsBoxOffice();
                    break;
                case 3:
                    loginAsCustomer();
                    break;
                case 4:
                    loginAsCoordinator();
                    break;
                case 0:
                    running = false;
                    System.out.println("Thank you for using Concert Ticket Reservation System!");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }

        // Close the scanner when done
        scanner.close();
    }

    /**
     * Display the main menu options
     */
    private void displayMainMenu() {
        System.out.println("\nSelect Your Role:");
        System.out.println("1. Concert Organizer");
        System.out.println("2. Box Office Clerk");
        System.out.println("3. Customer");
        System.out.println("4. Event Coordinator");
        System.out.println("0. Exit");
    }

    /**
     * Login as Concert Organizer
     */
    private void loginAsOrganizer() {
        System.out.println("\n===== CONCERT ORGANIZER LOGIN =====");
        String username = getStringInput("Username: ");
        String password = getStringInput("Password: ");

        try {
            activeClient = new OrganizerClient(username);
            System.out.println("Login successful!");
            organizerMenu();
        } catch (Exception e) {
            System.out.println("Login failed: " + e.getMessage());
        }
    }

    /**
     * Login as Box Office Clerk
     */
    private void loginAsBoxOffice() {
        System.out.println("\n===== BOX OFFICE CLERK LOGIN =====");
        String username = getStringInput("Username: ");
        String password = getStringInput("Password: ");
        String location = getStringInput("Location: ");

        try {
            activeClient = new BoxOfficeClient(username, location);
            System.out.println("Login successful!");
            boxOfficeMenu();
        } catch (Exception e) {
            System.out.println("Login failed: " + e.getMessage());
        }
    }

    /**
     * Login as Customer
     */
    private void loginAsCustomer() {
        System.out.println("\n===== CUSTOMER LOGIN =====");
        String username = getStringInput("Username (or 'guest' for guest access): ");

        try {
            activeClient = new CustomerClient(username);
            System.out.println("Login successful!");
            customerMenu();
        } catch (Exception e) {
            System.out.println("Login failed: " + e.getMessage());
        }
    }

    /**
     * Login as Event Coordinator
     */
    private void loginAsCoordinator() {
        System.out.println("\n===== EVENT COORDINATOR LOGIN =====");
        String username = getStringInput("Username: ");
        String password = getStringInput("Password: ");

        try {
            activeClient = new CoordinatorClient(username);
            System.out.println("Login successful!");
            coordinatorMenu();
        } catch (Exception e) {
            System.out.println("Login failed: " + e.getMessage());
        }
    }

    /**
     * Display and handle Organizer menu
     */
    private void organizerMenu() {
        OrganizerClient client = (OrganizerClient) activeClient;
        boolean running = true;

        while (running) {
            System.out.println("\n===== ORGANIZER MENU =====");
            System.out.println("1. Add New Concert");
            System.out.println("2. Update Concert Details");
            System.out.println("3. Cancel Concert");
            System.out.println("4. View All Concerts");
            System.out.println("0. Logout");

            int choice = getIntInput("Enter your choice: ");

            switch (choice) {
                case 1:
                    consoleUI.addNewConcert(client);
                    break;
                case 2:
                    consoleUI.updateConcertDetails(client);
                    break;
                case 3:
                    consoleUI.cancelConcert(client);
                    break;
                case 4:
                    consoleUI.viewAllConcerts(client);
                    break;
                case 0:
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    /**
     * Display and handle Box Office menu
     */
    private void boxOfficeMenu() {
        BoxOfficeClient client = (BoxOfficeClient) activeClient;
        boolean running = true;

        while (running) {
            System.out.println("\n===== BOX OFFICE MENU =====");
            System.out.println("1. Update Ticket Stock");
            System.out.println("2. Update Ticket Pricing");
            System.out.println("3. View Available Concerts");
            System.out.println("0. Logout");

            int choice = getIntInput("Enter your choice: ");

            switch (choice) {
                case 1:
                    consoleUI.updateTicketStock(client);
                    break;
                case 2:
                    consoleUI.updateTicketPricing(client);
                    break;
                case 3:
                    consoleUI.viewAllConcerts(client);
                    break;
                case 0:
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    /**
     * Display and handle Customer menu
     */
    private void customerMenu() {
        CustomerClient client = (CustomerClient) activeClient;
        boolean running = true;

        while (running) {
            System.out.println("\n===== CUSTOMER MENU =====");
            System.out.println("1. Browse Available Concerts");
            System.out.println("2. Make Reservation");
            System.out.println("3. View My Reservations");
            System.out.println("0. Logout");

            int choice = getIntInput("Enter your choice: ");

            switch (choice) {
                case 1:
                    consoleUI.viewAllConcerts(client);
                    break;
                case 2:
                    consoleUI.makeReservation(client);
                    break;
                case 3:
                    consoleUI.viewMyReservations(client);
                    break;
                case 0:
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    /**
     * Display and handle Event Coordinator menu
     */
    private void coordinatorMenu() {
        CoordinatorClient client = (CoordinatorClient) activeClient;
        boolean running = true;

        while (running) {
            System.out.println("\n===== EVENT COORDINATOR MENU =====");
            System.out.println("1. View Available Concerts");
            System.out.println("2. Make Bulk Reservation");
            System.out.println("3. View Special Reservations");
            System.out.println("0. Logout");

            int choice = getIntInput("Enter your choice: ");

            switch (choice) {
                case 1:
                    consoleUI.viewAllConcerts(client);
                    break;
                case 2:
                    consoleUI.makeBulkReservation(client);
                    break;
                case 3:
                    consoleUI.viewSpecialReservations(client);
                    break;
                case 0:
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    /**
     * Helper method to get integer input from user
     * @param prompt The message to display to the user
     * @return The integer input from the user
     */
    private int getIntInput(String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextInt()) {
            System.out.println("Please enter a valid number.");
            System.out.print(prompt);
            scanner.next(); // Consume the invalid input
        }
        return scanner.nextInt();
    }

    /**
     * Helper method to get string input from user
     * @param prompt The message to display to the user
     * @return The string input from the user
     */
    private String getStringInput(String prompt) {
        System.out.print(prompt);
        scanner.nextLine(); // Consume any leftover newline
        return scanner.nextLine();
    }

    /**
     * Main method to start the application
     */
    public static void main(String[] args) {
        CLI cli = new CLI();
        cli.start();
    }
}