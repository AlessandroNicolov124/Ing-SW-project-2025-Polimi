package it.polimi.ingsw.view.gui.utils;

import it.polimi.ingsw.network.client.GameClient;
import it.polimi.ingsw.network.rmi.RmiClient;
import it.polimi.ingsw.network.socket.client.SocketClient;
import javafx.application.Platform;
import javafx.concurrent.Task;

/**
 * Utility class for handling network connections asynchronously
 */
public class NetworkUtils {

    /**
     * Result of a connection attempt
     */
    public static class ConnectionResult {
        private final boolean success;
        private final String errorMessage;
        private final GameClient client;

        public ConnectionResult(boolean success, String errorMessage, GameClient client) {
            this.success = success;
            this.errorMessage = errorMessage;
            this.client = client;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public GameClient getClient() {
            return client;
        }

        public static ConnectionResult success(GameClient client) {
            return new ConnectionResult(true, null, client);
        }

        public static ConnectionResult failure(String errorMessage) {
            return new ConnectionResult(false, errorMessage, null);
        }
    }

    /**
     * Callback interface for connection results
     */
    public interface ConnectionCallback {
        void onResult(ConnectionResult result);
    }

    /**
     * Connect to server asynchronously
     *
     * @param connectionType "rmi" or "socket"
     * @param serverAddress Server address (e.g., "localhost")
     * @param serverPort Server port
     * @param username Player username
     * @param callback Callback to handle the result
     */
    public static void connectAsync(String connectionType, String serverAddress,
                                    int serverPort, String username,
                                    ConnectionCallback callback) {

        Task<ConnectionResult> connectionTask = new Task<ConnectionResult>() {
            @Override
            protected ConnectionResult call() throws Exception {
                try {
                    GameClient client;

                    if ("rmi".equalsIgnoreCase(connectionType)) {
                        // RMI Connection
                        client = connectRmi(serverAddress, serverPort, username);
                    } else {
                        // Socket Connection
                        client = connectSocket(serverAddress, serverPort, username);
                    }

                    if (client != null) {
                        return ConnectionResult.success(client);
                    } else {
                        return ConnectionResult.failure("Failed to establish connection");
                    }

                } catch (Exception e) {
                    return ConnectionResult.failure("Connection error: " + e.getMessage());
                }
            }
        };

        connectionTask.setOnSucceeded(e -> {
            Platform.runLater(() -> callback.onResult(connectionTask.getValue()));
        });

        connectionTask.setOnFailed(e -> {
            Throwable exception = connectionTask.getException();
            String errorMessage = exception != null ? exception.getMessage() : "Unknown connection error";
            Platform.runLater(() -> callback.onResult(ConnectionResult.failure(errorMessage)));
        });

        // Run the connection task in a background thread
        Thread connectionThread = new Thread(connectionTask);
        connectionThread.setDaemon(true);
        connectionThread.start();
    }

    /**
     * Establish RMI connection
     */
    private static GameClient connectRmi(String serverAddress, int serverPort, String username) {
        try {
            RmiClient rmiClient = RmiClient.connectTo(serverAddress);
            if (rmiClient != null) {
                // Perform any additional RMI-specific setup here
                // For example, register the player with the server
                return rmiClient;
            }
            return null;
        } catch (Exception e) {
            System.err.println("RMI connection failed: " + e.getMessage());
            return null;
        }
    }

    /**
     * Establish Socket connection
     */
    private static GameClient connectSocket(String serverAddress, int serverPort, String username) {
        try {
            SocketClient socketClient = new SocketClient(serverAddress, serverPort);

            // Start the socket client in a separate thread
            Thread clientThread = new Thread(socketClient);
            clientThread.setDaemon(true);
            clientThread.start();

            // Give the client a moment to establish connection
            Thread.sleep(1000);

            // Check if connection was successful
            if (socketClient != null) {
                // Perform any additional socket-specific setup here
                // For example, send a login message
                return socketClient;
            }

            return null;
        } catch (Exception e) {
            System.err.println("Socket connection failed: " + e.getMessage());
            return null;
        }
    }

    /**
     * Test connection without actually connecting
     * Useful for validating connection parameters
     */
    public static boolean testConnection(String connectionType, String serverAddress, int serverPort) {
        try {
            if ("rmi".equalsIgnoreCase(connectionType)) {
                return testRmiConnection(serverAddress, serverPort);
            } else {
                return testSocketConnection(serverAddress, serverPort);
            }
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean testRmiConnection(String serverAddress, int serverPort) {
        try {
            // Attempt to connect to RMI registry
            java.rmi.registry.Registry registry = java.rmi.registry.LocateRegistry.getRegistry(serverAddress, serverPort);
            registry.list(); // This will throw an exception if the registry is not accessible
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean testSocketConnection(String serverAddress, int serverPort) {
        try (java.net.Socket testSocket = new java.net.Socket()) {
            testSocket.connect(new java.net.InetSocketAddress(serverAddress, serverPort), 5000); // 5 second timeout
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validate network parameters
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;

        public ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public static ValidationResult valid() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult invalid(String errorMessage) {
            return new ValidationResult(false, errorMessage);
        }
    }

    /**
     * Validate connection parameters
     */
    public static ValidationResult validateConnectionParameters(String connectionType,
                                                                String serverAddress,
                                                                int serverPort,
                                                                String username) {
        // Validate username
        if (username == null || username.trim().isEmpty()) {
            return ValidationResult.invalid("Username cannot be empty");
        }
        if (username.trim().length() < 2) {
            return ValidationResult.invalid("Username must be at least 2 characters long");
        }
        if (username.trim().length() > 20) {
            return ValidationResult.invalid("Username cannot be longer than 20 characters");
        }

        // Validate connection type
        if (!"rmi".equalsIgnoreCase(connectionType) && !"socket".equalsIgnoreCase(connectionType)) {
            return ValidationResult.invalid("Connection type must be 'rmi' or 'socket'");
        }

        // Validate server address
        if (serverAddress == null || serverAddress.trim().isEmpty()) {
            return ValidationResult.invalid("Server address cannot be empty");
        }

        // Validate port
        if (serverPort <= 0 || serverPort > 65535) {
            return ValidationResult.invalid("Port must be between 1 and 65535");
        }

        // Check for common port conflicts
        if (serverPort < 1024 && !serverAddress.equals("localhost") && !serverAddress.equals("127.0.0.1")) {
            return ValidationResult.invalid("Ports below 1024 are typically restricted for remote connections");
        }

        return ValidationResult.valid();
    }

    /**
     * Get default port for connection type
     */
    public static int getDefaultPort(String connectionType) {
        if ("rmi".equalsIgnoreCase(connectionType)) {
            return 1099;
        } else {
            return 7777;
        }
    }

    /**
     * Format connection string for display
     */
    public static String formatConnectionString(String connectionType, String serverAddress, int serverPort) {
        return String.format("%s://%s:%d", connectionType.toUpperCase(), serverAddress, serverPort);
    }
}