package it.polimi.ingsw.view.tui.components;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ConsoleManager {
    private final BufferedReader reader;
    private final BlockingQueue<String> queue = new LinkedBlockingQueue<>();
    private volatile boolean closed = false;

    public ConsoleManager() {
        this.reader = new BufferedReader(new InputStreamReader(System.in));

        Thread inputThread = new Thread(() -> {
            try {
                String line;
                while (!closed && (line = reader.readLine()) != null) {
                    queue.offer(line.trim());
                }
            } catch (IOException e) {
                if (!closed) {
                    System.err.println("Error reading input: " + e.getMessage());
                }
            }
        }, "stdin-input-reader");
        
        inputThread.setDaemon(true);
        inputThread.start();
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
        System.out.println(message);
    }

    public void printError(String message) {
        System.err.println("Error: " + message);
    }

    public String readCommand() {
        System.out.print("> ");
        System.out.flush();
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
        while (true) {
            System.out.print("Enter username: ");
            System.out.flush();
            
            String input = blockingReadLine();
            if (input != null && !input.trim().isEmpty()) {
                return input.trim();
            }
            printError("Username cannot be empty!");
        }
    }

    public String askConnectionType() {
        while (true) {
            System.out.print("Choose connection type (rmi/socket): ");
            System.out.flush();
            
            String input = blockingReadLine();
            if (input != null) {
                input = input.toLowerCase().trim();
                if (input.equals("rmi") || input.equals("socket")) {
                    return input;
                }
            }
            printError("Invalid connection type. Please enter 'rmi' or 'socket'");
        }
    }

    public String askServerAddress() {
        System.out.print("Enter server address (default: localhost): ");
        System.out.flush();
        
        String address = blockingReadLine();
        return (address == null || address.isEmpty()) ? "localhost" : address;
    }

    public int askServerPort(String connectionType) {
        int defaultPort = connectionType.equals("rmi") ? 1099 : 7777;
        while (true) {
            System.out.print("Enter server port (default: " + defaultPort + "): ");
            System.out.flush();
            
            String portStr = blockingReadLine();
            
            if (portStr == null || portStr.isEmpty()) {
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
        System.out.println("Connecting to " + address + ":" + port + " using " + type.toUpperCase() + "...");
    }

    public String askQuestion(String prompt) {
        System.out.print(prompt + " ");
        System.out.flush();
        
        String input = blockingReadLine();
        return input != null ? input : "";
    }

    public void flushInput() {
        try {
            while (System.in.available() > 0) {
                System.in.read();
            }
        } catch (IOException e) {
        }
    }

    public void close() {
        closed = true;
        try {
            reader.close();
        } catch (IOException e) {
            System.err.println("Error closing reader: " + e.getMessage());
        }
    }
}