package it.polimi.ingsw.GUI;

import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.GUI.GameStateUpdateListener.GameStateUpdateListener;
import it.polimi.ingsw.GUI.GameStateUpdateListener.LobbyListener;
import it.polimi.ingsw.Player;
import it.polimi.ingsw.Position;
import it.polimi.ingsw.network.Difficulty;
import it.polimi.ingsw.network.client.GeneralClientIf;
import it.polimi.ingsw.network.client.listeners.GameListener;
import it.polimi.ingsw.network.messages.RotationType;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AppContext implements GameListener {
    private String username;
    private String connectionType;
    private String serverAddress;
    private Player localPlayerReference;
    private int serverPort;
    private int playerId;
    private UUID currentMatchId;
    private String selectedCabinColor;

    private GeneralClientIf client;

    private boolean needsRefresh = false;
    private List<GameStateUpdateListener> guiUpdateListeners = new ArrayList<>();
    private List<LobbyListener> lobbyListeners = new ArrayList<>();

    public AppContext() {
        username = "";
        connectionType = "";
        serverAddress = "";
        serverPort = 0;
        playerId = 0;

        client = null;
    }

    public void addLobbyListener(LobbyListener l) { 
        lobbyListeners.add(l); 
    }

    public void removeLobbyListener(LobbyListener l){ 
        lobbyListeners.remove(l); 
    }

    // Utility methods for showing messages, consider using Notifications instead 
    // of Alerts for a more modern look maybe
    private void showInfoMessage(String title, String message) {
        showMessage(Alert.AlertType.INFORMATION, title, message);
    }

    private void showSuccessMessage(String title, String message) {
        showMessage(Alert.AlertType.INFORMATION, title, message);
    }

    private void showErrorMessage(String title, String message) {
        showMessage(Alert.AlertType.ERROR, title, message);
    }

    private void showMessage(Alert.AlertType alertType, String title, String message) {
        try {
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        } catch (Exception e) {
            // Fallback to simple dialog if Alert fails
            showSimpleDialog(title, message);
        }
    }

    private void showSimpleDialog(String title, String message) {
        try {
            final Stage dialog = new Stage();
            VBox dialogVbox = new VBox(20);
            dialogVbox.getChildren().add(new Text(title));
            dialogVbox.getChildren().add(new Text(message));
            Scene dialogScene = new Scene(dialogVbox, 300, 150);
            dialog.setScene(dialogScene);
            dialog.setTitle(title);
            dialog.show();
        } catch (Exception e) {
            System.err.println("Failed to show dialog: " + e.getMessage());
        }
    }

    public Player getLocalPlayerReference() {
        return localPlayerReference;
    }

    public void setLocalPlayerReference(Player localPlayerReference) {
        this.localPlayerReference = localPlayerReference;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(String connectionType) {
        this.connectionType = connectionType;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public GeneralClientIf getClient() {
        return client;
    }

    public void setClient(GeneralClientIf client) {
        this.client = client;
    }

    public void addGameStateUpdateListener(GameStateUpdateListener listener) {
        guiUpdateListeners.add(listener);
    }

    public void removeGameStateUpdateListener(GameStateUpdateListener listener) {
        guiUpdateListeners.remove(listener);
    }

    public boolean needsRefresh() {
        return needsRefresh;
    }

    public void setRefreshed() {
        needsRefresh = false;
    }

    public void setCurrentMatchId(UUID currentMatchId) {
        this.currentMatchId = currentMatchId;
    }

    public String getSelectedCabinColor() {
        return selectedCabinColor;
    }
    
    public void setSelectedCabinColor(String selectedCabinColor) {
        this.selectedCabinColor = selectedCabinColor;
    }

    @Override
    public void playerCreated(int playerId, String playerName) {
        this.playerId = playerId;

        localPlayerReference = new Player(playerId, playerName, true);

        System.out.println("GUI received playerId = " + playerId);

        Platform.runLater(() -> {
            try { 
                GUIApp.showLobbyPage(); 
            }
            catch (Exception e) {
                e.printStackTrace(); 
            }
        });
    }


    @Override
    public void usernameRejected(String errorMessage) {
        Platform.runLater(() ->
            showMessage(Alert.AlertType.ERROR, "Username rejected", errorMessage));
    }

    @Override
    public void updateAvailableMatches(List<String> matches) {
        Platform.runLater(() -> {
            for (LobbyListener l : lobbyListeners) {
                l.availableMatches(matches);
            }
        });
    }


    @Override
    public void matchCreated(UUID matchId, String matchName, String playerName, int playerId, Difficulty difficulty) {
        Platform.runLater(() -> {
            if (playerId == this.playerId) {         
                this.currentMatchId = matchId;
            }

            for (LobbyListener l : new ArrayList<>(lobbyListeners)) {
                l.matchCreated(matchId, matchName, playerName, playerId, difficulty);
            }
        });
    }

    @Override
    public void playerJoinedMatch(UUID matchId, String playerName) {
        Platform.runLater(() -> {
            System.out.println("[GUI] Player joined match: " + playerName + " in match " + matchId);
            for (LobbyListener l : lobbyListeners) {
                l.playerJoinedMatch(matchId, playerName);
            }
        });
    }

    @Override
    public void playerLeftMatch(UUID matchId, int playerId, String playerName) {
        Platform.runLater(() -> {
            System.out.println("[GUI] Player left match: " + playerName + " (ID: " + playerId + ") in match " + matchId);
            
            if (playerId == this.playerId) {
                this.currentMatchId = null;
                this.selectedCabinColor = null;
                System.out.println("[GUI] Local player left match - reset cabin color and match ID");
            }
            
            for (LobbyListener l : lobbyListeners) {
                l.playerLeftMatch(matchId, playerId, playerName);
            }
        });
    }

    @Override
    public void updateHost(UUID matchId, int playerId, String playerName) {
        Platform.runLater(() -> {
            System.out.println("[GUI] Host updated for match " + matchId + ": Player " + playerName + " (ID: " + playerId + ")");
            for (LobbyListener l : lobbyListeners) {
                l.updateHost(matchId, playerId, playerName);
            }
        });
    }

    @Override
    public void matchStarted(UUID matchId, String username) {
        Platform.runLater(() -> {
            try { 
                client.loadingAck(matchId); 
            }
            catch (Exception e) { 
                System.err.println("Err send loadingAck: " + e); 
            }

            for (LobbyListener l : new ArrayList<>(lobbyListeners)) {
                l.matchStarted(matchId, username);
            }
        });
    }

    @Override
    public void loadingStatus(UUID matchId, int playersReady, int totalPlayers) {
        Platform.runLater(() -> {
            System.out.println("[GUI] Loading status for match " + matchId + ": " + playersReady + "/" + totalPlayers + " players ready");
            for (LobbyListener l : lobbyListeners) {
                l.loadingStatus(matchId, playersReady, totalPlayers);
            }
        });
    }

    @Override
    public void playersInMatch(UUID matchId, List<String> players, int hostId) {
        Platform.runLater(() -> {
            System.out.println("[GUI] Players in match " + matchId + ": " + players + " (Host ID: " + hostId + ")");
            for (LobbyListener l : lobbyListeners) {
                l.playersInMatch(matchId, players, hostId);
            }
        });
    }

    @Override
    public void playerReady(UUID matchId, int readyPlayers, int totalPlayers) {
        Platform.runLater(() -> {
            System.out.println("[GUI] Player ready status for match " + matchId + ": " + readyPlayers + "/" + totalPlayers + " players ready");
            for (LobbyListener l : lobbyListeners) {
                l.playerReady(matchId, readyPlayers, totalPlayers);
            }
        });
    }

    @Override
    public void shipBuildingStart(UUID matchId, int readyPlayers) {
        Platform.runLater(() -> {
            System.out.println("[GUI] Ship building phase started for match " + matchId + " with " + readyPlayers + " players ready");
            for (LobbyListener l : lobbyListeners) {
                l.shipBuildingStart(matchId, readyPlayers);
            }
        });
    }
    
    @Override
    public void displayRefresh() {
        Platform.runLater(() -> {
            System.out.println("[GUI] Display refresh requested");
            needsRefresh = true;
            
            // publish subscribe style or observer pattern
            for (GameStateUpdateListener listener : guiUpdateListeners) {
                try {
                    listener.gameStateUpdate();
                } catch (Exception e) {
                    System.err.println("Error notifying GUI listener: " + e.getMessage());
                }
            }
        });
    }

    @Override
    public void componentPlaced(Component component, Position position) {
        Platform.runLater(() -> {
            System.out.println("[GUI] Component placed: " + component.getClass().getSimpleName() + " at position " + position);
            
            for (GameStateUpdateListener listener : guiUpdateListeners) {
                try {
                    listener.gameComponentPlaced(component, position);
                } catch (Exception e) {
                    System.err.println("Error notifying component placed: " + e.getMessage());
                }
            }
        });
    }

    @Override
    public void componentPicked(int playerId, Component component, boolean success, String message) {
        Platform.runLater(() -> {
            if (playerId == this.playerId) {
                if (success && component != null) {
                    for (GameStateUpdateListener listener : guiUpdateListeners) {
                        listener.gameComponentPicked(component);
                    }

                } else {
                    showErrorMessage("Component Pick Failed", message);
                }
            }
        });
    }

    @Override
    public void componentRemoved(Position position) {
        Platform.runLater(() -> {
            System.out.println("[GUI] Component removed from position: " + position);
            
            for (GameStateUpdateListener listener : guiUpdateListeners) {
                try {
                    listener.gameComponentRemoved(position);
                } catch (Exception e) {
                    System.err.println("Error notifying component removed: " + e.getMessage());
                }
            }
        });
    }

    @Override
    public void componentRotated(int playerId, Position position, RotationType rotationType, boolean isValid) {
        Platform.runLater(() -> {
            System.out.println("[GUI] Component rotated - Player: " + playerId + ", Position: " + position + ", Valid: " + isValid);
            
            for (GameStateUpdateListener l : guiUpdateListeners) {
                try {
                    l.gameComponentRotated(position, rotationType);
                } catch (Exception e) {
                    System.err.println("Error notifying rotation: " + e.getMessage());
                }
            }
            
            if (!isValid) {
                showErrorMessage("Invalid rotation", "The rotation at position (" + 
                                position.getX() + ", " + position.getY() + ") was rejected by the server.");
            }
        });
    }

    @Override
    public void shipValidated(boolean isValid, String message) {
        Platform.runLater(() -> {
            System.out.println("[GUI] Ship validation result: " + isValid + " - " + message);
            
            for (GameStateUpdateListener listener : guiUpdateListeners) {
                try {
                    listener.gameShipValidated(isValid, message);
                } catch (Exception e) {
                    System.err.println("Error notifying ship validated: " + e.getMessage());
                }
            }
        });
    }

    @Override
    public boolean notifyNewPlayerPosition(int playerId, int position) {
        Platform.runLater(() -> {
            System.out.println("[GUI] New player position - Player " + playerId + " at position " + position);
            
            if (playerId != this.playerId) {
                showInfoMessage("Player Update", 
                    "Player " + playerId + " moved to position " + position);
            }
        });
        return true;
    }

    @Override
    public void printTextMsg(String message) {
        Platform.runLater(() -> {
            System.out.println("[GUI] Server message: " + message);
            showInfoMessage("Server Message", message);
        });
    }

    @Override
    public void cabinColorSelected(String color) {
        Platform.runLater(() -> {
            this.selectedCabinColor = color;
            for (LobbyListener listener : lobbyListeners) {
                listener.cabinColorSelected(color);
            }
        });
    }
    
    @Override
    public void flightPreparationStart(UUID matchId) {
        Platform.runLater(() -> {
            System.out.println("[GUI] Flight preparation started for match " + matchId);
            try {
                //GUIApp.showFlightPreparation();
            } catch (Exception e) {
                showErrorMessage("Navigation Error", "Cannot open flight preparation: " + e.getMessage());
            }
        });
    }
    
    @Override
    public void playerReadyForFlight(UUID matchId, int playerId, String playerName, boolean shipValid) {
        Platform.runLater(() -> {
            String message = playerName + (shipValid ? " is ready for flight!" : " has an invalid ship");
            showInfoMessage("Player Update", message);
        });
    }
    
    @Override
    public void allPlayersReadyForFlight(UUID matchId) {
        Platform.runLater(() -> {
            showSuccessMessage("Flight Started", "All players are ready! The flight begins now!");
            // Qui inizierà la fase di volo vera e propria
        });
    }
    
}
