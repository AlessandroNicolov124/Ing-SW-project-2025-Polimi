package it.polimi.ingsw.view.tui.components;

import java.util.Scanner;

public class ConsoleManager {
    private final Scanner scanner;

    public ConsoleManager() {
        this.scanner = new Scanner(System.in);
    }

    public void printBanner() {
        System.out.println("===================================");
        System.out.println("=          GALAXY TRUCKER         =");
        System.out.println("===================================");
        System.out.println();
    }


    public void printMessage(String message) {
        System.out.println(message);
    }

    public void printError(String message) {
        System.err.println("Error: " + message);
    }

    public String readCommand() {
        System.out.print("> ");
        return scanner.nextLine().trim();
    }

    public String askUsername() {
        System.out.print("Enter username: ");
        return scanner.nextLine().trim();
    }

    public String askConnectionType() {
        while (true) {
            System.out.print("Choose connection type (rmi/socket): ");
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("rmi") || input.equals("socket")) {
                return input;
            }
            printError("Invalid connection type. Please enter 'rmi' or 'socket'");
        }
    }

    public String askServerAddress() {
        System.out.print("Enter server address (default: localhost): ");
        String address = scanner.nextLine().trim();
        return address.isEmpty() ? "localhost" : address;
    }

    public int askServerPort(String connectionType) {
        int defaultPort = connectionType.equals("rmi") ? 1099 : 7777;
        while (true) {
            System.out.print("Enter server port (default: " + defaultPort + "): ");
            String portStr = scanner.nextLine().trim();
            
            if (portStr.isEmpty()) {
                return defaultPort;
            }
            
            try {
                int port = Integer.parseInt(portStr);
                if (port > 0 && port <= 65535) {
                    return port;
                }
                printError("Port must be between 1 and 65535");
            } catch (NumberFormatException e) {
                printError("Invalid port number");
            }
        }
    }

    public void printHelp() {
        System.out.println("Available commands:");
        System.out.println("  help  - Show this help message");
        System.out.println("  quit  - Exit the application");
    }

    public void printConnectingMessage(String type, String address, int port) {
        System.out.println("Connecting to " + address + ":" + port + " using " + type.toUpperCase() + "...");
    }

    public void AskAreYouReadyQuestion() {
        System.out.println("Press enter to continue start the game when you are ready.");
        String input = scanner.nextLine().trim();
    }

    public void close() {
        scanner.close();
    }
}