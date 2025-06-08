package it.polimi.ingsw.view.tui.components;

import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ConsoleManager {
    private final Scanner scanner;
    private final BlockingQueue<String> queue = new LinkedBlockingQueue<>();
    private final Object outputMutex = new Object(); // Add synchronization for console output

    public ConsoleManager() {
        this.scanner = new Scanner(System.in);

        Thread reader = new Thread(() -> {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                queue.offer(line);
            }
        }, "stdin-input-reader");
        reader.setDaemon(true);
        reader.start();
    }

    public void printBanner() {
        System.out.println();
        System.out.println();
        System.out.println("   ********             **                                  **********                        **                   ");
        System.out.println("  **//////**           /**                    **   **      /////**///                        /**                   ");
        System.out.println(" **      //   ******   /**  ******   **   ** //** **           /**     ****** **   **  ***** /**  **  *****  ******");
        System.out.println("/**          //////**  /** //////** //** **   //***            /**    //**//*/**  /** **///**/** **  **///**//**//*");
        System.out.println("/**    *****  *******  /**  *******  //***     /**             /**     /** / /**  /**/**  // /****  /******* /** / ");
        System.out.println("//**  ////** **////**  /** **////**   **/**    **              /**     /**   /**  /**/**   **/**/** /**////  /**   ");
        System.out.println(" //******** //******** ***//******** ** //**  **               /**    /***   //******//***** /**//**//******/***   ");
        System.out.println("  ////////   //////// ///  //////// //   //  //                //     ///     //////  /////  //  //  ////// ///    ");
        System.out.println();
        System.out.println();
    }

    public void printMessage(String message) {
        synchronized (outputMutex) {
            System.out.println(message);
        }
    }

    public void printError(String message) {
        synchronized (outputMutex) {
            System.err.println("Error: " + message);
        }
    }

    public String readCommand() {
        synchronized (outputMutex) {
            System.out.print("> ");
        }
        return blockingReadLine();
    }

    public String pollCommand(long millis) {
        try {
            return queue.poll(millis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    private String blockingReadLine() {
        try {
            return queue.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "";
        }
    }

    public String askUsername() {
        synchronized (outputMutex) {
            System.out.print("Enter username: ");
        }
        return blockingReadLine();
    }

    public String askConnectionType() {
        while (true) {
            synchronized (outputMutex) {
                System.out.print("Choose connection type (rmi/socket): ");
            }
            String input = blockingReadLine().toLowerCase();
            if (input.equals("rmi") || input.equals("socket")) {
                return input;
            }
            printError("Invalid connection type. Please enter 'rmi' or 'socket'");
        }
    }

    public String askServerAddress() {
        synchronized (outputMutex) {
            System.out.print("Enter server address (default: localhost): ");
        }
        String address = blockingReadLine();
        return address.isEmpty() ? "localhost" : address;
    }

    public int askServerPort(String connectionType) {
        int defaultPort = connectionType.equals("rmi") ? 1099 : 7777;
        while (true) {
            synchronized (outputMutex) {
                System.out.print("Enter server port (default: " + defaultPort + "): ");
            }
            String portStr = blockingReadLine();
            
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

    public void printConnectingMessage(String type, String address, int port) {
        synchronized (outputMutex) {
            System.out.println("Connecting to " + address + ":" + port + " using " + type.toUpperCase() + "...");
        }
    }

    public String askQuestion(String prompt) {
        synchronized (outputMutex) {
            System.out.print(prompt + " ");
        }
        return blockingReadLine();
    }

    public Scanner getScanner() {
        return scanner;
    }

    public void close() {
        scanner.close();
    }
}