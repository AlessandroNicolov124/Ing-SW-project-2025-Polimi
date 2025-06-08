package it.polimi.ingsw.GUI;

import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.GUI.Controllers.LobbyController;
import it.polimi.ingsw.GUI.GameStateUpdateListener.GameStateUpdateListener;
import it.polimi.ingsw.Player;
import it.polimi.ingsw.Position;
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

import static it.polimi.ingsw.network.messages.RotationType.*;
import static java.lang.System.in;

public class AppContext implements GameListener {
    private String username;
    private String connectionType;
    private String serverAddress;
    private Player localPlayerReference;
    private int serverPort;
    private int playerId;
    private UUID currentMatchId;

    private GeneralClientIf client;

    private boolean needsRefresh = false;
    private List<GameStateUpdateListener> guiUpdateListeners = new ArrayList<>();

    public AppContext() {
        username = "";
        connectionType = "";
        serverAddress = "";
        serverPort = 0;
        playerId = 0;

        client = null;
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

    @Override
    public void playerCreated(int playerId, String playerName) {

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
            
            showInfoMessage("Component Placed", 
                "Successfully placed " + component.getClass().getSimpleName() + 
                " at position (" + position.getX() + ", " + position.getY() + ")");
            
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
    public void componentRemoved(Position position) {
        Platform.runLater(() -> {
            System.out.println("[GUI] Component removed from position: " + position);
            
            showInfoMessage("Component Removed", 
                "Component removed from position (" + position.getX() + ", " + position.getY() + ")");
            
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
    public void componentRotated(Position position, RotationType rotationType) {
        Platform.runLater(() -> {
            System.out.println("[GUI] Component at position (" + position.getX() + ", " + position.getY() + ") rotated ");
            
            String rotationMsg = switch (rotationType) {
                case CLOCKWISE -> "clockwise";
                case ANTI_CLOCKWISE -> "counter-clockwise";
                case OPPOSITE -> "180 degrees";
            };
            
            showInfoMessage("Component Rotated", 
                "Component at position (" + position.getX() + ", " + position.getY() + ") rotated " + rotationMsg);
            
            for (GameStateUpdateListener listener : guiUpdateListeners) {
                try {
                    listener.gameComponentRotated(position, rotationType);
                } catch (Exception e) {
                    System.err.println("Error notifying component rotated: " + e.getMessage());
                }
            }
        });
    }

    @Override
    public void shipValidated(boolean isValid, String message) {
        Platform.runLater(() -> {
            System.out.println("[GUI] Ship validation result: " + isValid + " - " + message);
            
            if (isValid) {
                showSuccessMessage("Ship Validation", message);
            } else {
                showErrorMessage("Ship Validation Failed", message);
            }
            
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
    public void showAvailablePositions(List<Integer> positions) {
        Platform.runLater(() -> {
            System.out.println("[GUI] Available positions updated: " + positions);
            
            for (GameStateUpdateListener listener : guiUpdateListeners) {
                try {
                    listener.gameAvailablePositionsUpdate(positions);
                } catch (Exception e) {
                    System.err.println("Error notifying available positions: " + e.getMessage());
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
    public void matchCreated(UUID matchId, String matchName, String playerName, int playerId) {
        this.currentMatchId = matchId;

        LobbyController.matchCreatedListener(matchId.toString());
    }

    @Override
    public void playerJoinedMatch(UUID matchId, String playerName) {
        if (matchId == this.currentMatchId) {
            //LobbyController.playerJoinedMatchNotification(playerName);
        }
    }

    @Override
    public void playerLeftMatch(UUID matchId, int playerId, String playerName) {
        if (matchId == this.currentMatchId) {

        }
    }

    @Override
    public void updateAvailableMatches(List<String> matchNames) {
        System.out.println("[GUI] Available matches updated: " + matchNames);
        LobbyController.updateMatchListListener(matchNames);
    }

    @Override
    public void updateHost(UUID matchId, int playerId, String playerName) {

    }

    @Override
    public void matchStarted(UUID matchId, String username) {

    }

    @Override
    public void loadingStatus(UUID matchId, int playersReady, int totalPlayers) {

    }

    @Override
    public void playersInMatch(UUID matchId, List<String> players, int hostId) {
            LobbyController.playerJoinedMatchNotification(players);
    }

    @Override
    public void playerReady(UUID matchId, int readyPlayers, int totalPlayers) {

    }

    @Override
    public void shipBuildingStart(UUID matchId, int readyPlayers) {

    }

    @Override
    public void usernameRejected(String errorMessage) {

    }
}
