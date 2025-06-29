package it.polimi.ingsw.GUI;

import it.polimi.ingsw.AdventureCards.AdventureCard;
import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.GUI.GameStateUpdateListener.FlightBoardListener;
import it.polimi.ingsw.GUI.GameStateUpdateListener.GameStateUpdateListener;
import it.polimi.ingsw.GUI.GameStateUpdateListener.LobbyListener;
import it.polimi.ingsw.GUI.GameStateUpdateListener.PreparationFlightListener;
import it.polimi.ingsw.Player;
import it.polimi.ingsw.Position;
import it.polimi.ingsw.TokenType;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
    private String previousController;

    private GeneralClientIf client;

    private final List<String> playersInMatchList = new ArrayList<>();

    private boolean needsRefresh = false;
    private List<GameStateUpdateListener> guiUpdateListeners = new ArrayList<>();
    private List<LobbyListener> lobbyListeners = new ArrayList<>();
    private List<FlightBoardListener> flightBoardListeners = new ArrayList<>();
    private List<PreparationFlightListener> preparationFlightListeners = new ArrayList<>();

    public AppContext() {
        username = "";
        connectionType = "";
        serverAddress = "";
        serverPort = 0;
        playerId = 0;

        client = null;
    }

    public void addFlightboardListener(FlightBoardListener l) {
        flightBoardListeners.add(l);
    }
    public void removeFlightboardListener(FlightBoardListener l) { flightBoardListeners.remove(l); }

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

    protected static void showErrorDialog(String title, String message) {
        try {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle(title);
                alert.setHeaderText(null);
                alert.setContentText(message);
                alert.showAndWait();
            });
        } catch (Exception e) {
            System.err.println("Failed to show error dialog: " + e.getMessage());
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

    public UUID getCurrentMatchId() {
        return currentMatchId;
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

    public void addPreparationFlightListener(PreparationFlightListener listener) {
        preparationFlightListeners.add(listener);
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

    public List<String> getPlayersInMatchList() {
        return Collections.unmodifiableList(playersInMatchList);
    }

    public void setPreviousController(String controllerName) {
        this.previousController = controllerName;
    }

    public String getPreviousController() {
        return previousController;
    }

    public void returnToPreviousView() {
        try {
            GUIApp.returnToPreviousPage();
        } catch (Exception e) {
            System.err.println("Failed to return to previous page: " + e.getMessage());
            try {
                GUIApp.showFlightPreparation(); // or lobby? boh
            } catch (Exception fallbackError) {
                System.err.println("Fallback navigation failed: " + fallbackError.getMessage());
            }
        }
    }

    public void removePreparationFlightListener(PreparationFlightListener listener) {
        preparationFlightListeners.remove(listener);
    }

    // Aggiungi un metodo per pulire tutti i listener di un tipo
    public void clearPreparationFlightListeners() {
        preparationFlightListeners.clear();
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
                playersInMatchList.clear();
                playersInMatchList.add(playerName);
            }

            for (LobbyListener l : new ArrayList<>(lobbyListeners)) {
                l.matchCreated(matchId, matchName, playerName, playerId, difficulty);
            }
        });
    }

    @Override
    public void playerJoinedMatch(UUID matchId, String playerName) {
        Platform.runLater(() -> {
            if (playerName.equals(username)) {          
                this.currentMatchId = matchId;
            }
            if (!playersInMatchList.contains(playerName) && matchId.equals(this.currentMatchId)) {
                playersInMatchList.add(playerName);
            }
            lobbyListeners.forEach(l -> l.playerJoinedMatch(matchId, playerName));
        });
    }


    @Override
    public void playerLeftMatch(UUID matchId, int playerId, String playerName) {
        Platform.runLater(() -> {
            System.out.println("[GUI] Player left match: " + playerName + " (ID: " + playerId + ") in match " + matchId);
            
            if (playerId == this.playerId) {
                this.currentMatchId = null;
                this.selectedCabinColor = null;
                playersInMatchList.clear();
                System.out.println("[GUI] Local player left match - reset cabin color and match ID");
            }

            if (playersInMatchList.contains(playerName) && matchId.equals(this.currentMatchId)) {
                playersInMatchList.remove(playerName);
                System.out.println("[GUI] Player " + playerName + " removed from playersInMatchList");
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
            
            playersInMatchList.clear();
            if (players != null) {
                playersInMatchList.addAll(players);
            }
            
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
                    for (GameStateUpdateListener listener : guiUpdateListeners) {
                        try {
                            listener.gameComponentPickedFailed(message);
                        } catch (Exception e) {
                            System.err.println("Error notifying component pick failed: " + e.getMessage());
                        }
                    }
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
            
            for (GameStateUpdateListener listener : guiUpdateListeners) {
                try {
                    listener.gameComponentRotated(position, rotationType);
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
    public void cabinColorSelectionFailed(String errorMsg) {
        Platform.runLater(() -> {
            for (LobbyListener listener : lobbyListeners) {
                listener.cabinColorSelectionFailed(errorMsg);
            }
        });
    }
    
    @Override
    public void flightPreparationStart(UUID matchId) {
        Platform.runLater(() -> {
            System.out.println("[GUI] Flight preparation started for match: " + matchId);
            try {
                GUIApp.showFlightPreparation();
            } catch (IOException e) {
                showErrorDialog("Flight preparation failed", "Cannot open flight preparation: ");
            }
        });
    }
    
    @Override
    public void playerReadyForFlight(UUID matchId, int playerId, String playerName, boolean shipValid) {
        Platform.runLater(() -> {
            System.out.println("[GUI] Player " + playerName + " (ID: " + playerId + ") ready for flight - Ship valid: " + shipValid);
            
            for (GameStateUpdateListener listener : guiUpdateListeners) {
                if (playerId == this.playerId) {
                    listener.gamePlayerReadyForFlightSuccess("You are ready for flight! Waiting for others...");
                } else {
                    showInfoMessage("Player Update", playerName + " is ready for flight");
                }
            }
        });
    }
    
    @Override
    public void matchCreatedFailed(String message) {
        Platform.runLater(() -> {
            showErrorDialog("Create Match Failed", message);
            for (LobbyListener l : lobbyListeners) {
                l.matchCreatedFailed(message);
            }
        });
    }

    @Override
    public void playerJoinedMatchFailed(String message) {
        Platform.runLater(() -> {
            showErrorDialog("Join Match Failed", message);
            for (LobbyListener l : lobbyListeners) {
                l.playerJoinedMatchFailed(message);
            }
        });
    }

    @Override
    public void playerLeftMatchFailed(String message) {
        Platform.runLater(() -> {
            showErrorDialog("Leave Match Failed", message);
            for (LobbyListener l : lobbyListeners) {
                l.playerLeftMatchFailed(message);
            }
        });
    }

    @Override
    public void updateAvailableMatchesFailed(String message) {
        Platform.runLater(() -> {
            showErrorDialog("Update Matches Failed", message);
            for (LobbyListener l : lobbyListeners) {
                l.updateAvailableMatchesFailed(message);
            }
        });
    }

    @Override
    public void updateHostFailed(String message) {
        Platform.runLater(() -> {
            showErrorDialog("Update Host Failed", message);
            for (LobbyListener l : lobbyListeners) {
                l.updateHostFailed(message);
            }
        });
    }

    @Override
    public void matchStartFailed(String message) {
        Platform.runLater(() -> {
            showErrorDialog("Start Match Failed", message);
            for (LobbyListener l : lobbyListeners) {
                l.matchStartedFailed(message);
            }
        });
    }

    @Override
    public void loadingAckResponseFailed(String message) {
        Platform.runLater(() -> {
            showErrorDialog("Loading Acknowledgment Failed", message);
            for (LobbyListener l : lobbyListeners) {
                l.loadingAckResponseFailed(message);
            }
        });
    }

    @Override
    public void loadingStatusFailed(String message) {
        Platform.runLater(() -> {
            showErrorDialog("Loading Status Failed", message);
            for (LobbyListener l : lobbyListeners) {
                l.loadingStatusFailed(message);
            }
        });
    }

    @Override
    public void playersInMatchFailed(String message) {
        Platform.runLater(() -> {
            showErrorDialog("Get Players Failed", message);
            for (LobbyListener l : lobbyListeners) {
                l.playersInMatchFailed(message);
            }
        });
    }

    @Override
    public void playerReadyFailed(String message) {
        Platform.runLater(() -> {
            showErrorDialog("Player Ready Failed", message);
            for (LobbyListener l : lobbyListeners) {
                l.playerReadyFailed(message);
            }
        });
    }

    @Override
    public void shipBuildingStartFailed(String message) {
        Platform.runLater(() -> {
            showErrorDialog("Ship Building Start Failed", message);
            for (LobbyListener l : lobbyListeners) {
                l.shipBuildingStartFailed(message);
            }
        });
    }

    @Override
    public void componentPickFailed(String message) {
        Platform.runLater(() -> {
            showErrorDialog("Component Pick Failed", message);
            for (GameStateUpdateListener listener : guiUpdateListeners) {
                try {
                    listener.gameComponentPickedFailed(message);
                } catch (Exception e) {
                    System.err.println("Error notifying component pick failed: " + e.getMessage());
                }
            }
        });
    }

    @Override
    public void componentPlacedFailed(String message) {
        Platform.runLater(() -> {
            showErrorDialog("Component Placement Failed", message);
            for (GameStateUpdateListener listener : guiUpdateListeners) {
                try {
                    listener.gameComponentPlacedFailed(message);
                } catch (Exception e) {
                    System.err.println("Error notifying component placed failed: " + e.getMessage());
                }
            }
        });
    }

    @Override
    public void componentRemovedFailed(String message) {
        Platform.runLater(() -> {
            showErrorDialog("Component Removal Failed", message);
            for (GameStateUpdateListener listener : guiUpdateListeners) {
                try {
                    listener.gameComponentRemovedFailed(message);
                } catch (Exception e) {
                    System.err.println("Error notifying component removed failed: " + e.getMessage());
                }
            }
        });
    }

    @Override
    public void componentRotatedFailed(String message) {
        Platform.runLater(() -> {
            showErrorDialog("Component Rotation Failed", message);
            for (GameStateUpdateListener listener : guiUpdateListeners) {
                try {
                    listener.gameComponentRotatedFailed(message);
                } catch (Exception e) {
                    System.err.println("Error notifying component rotated failed: " + e.getMessage());
                }
            }
        });
    }

    @Override
    public void shipValidationFailed(String message) {
        Platform.runLater(() -> {
            for (GameStateUpdateListener listener : guiUpdateListeners) {
                try {
                    listener.gameShipValidationFailed(message);
                } catch (Exception e) {
                    System.err.println("Error notifying ship validation failed: " + e.getMessage());
                }
            }
        });
    }

    @Override
    public void notifyNewPlayerPositionFailed(String message) {
        Platform.runLater(() -> {
            showErrorDialog("Player Position Update Failed", message);
        });
    }

    @Override
    public void positionAssigned(int playerId, String playerName, int assignedPosition, boolean success, String message) {

    }

    @Override
    public void flightPreparationStartFailed(String message) {
        Platform.runLater(() -> {
            showErrorDialog("Flight Preparation Failed", message);
        });
    }

    @Override
    public void playerReadyForFlightFailed(String message) {
        Platform.runLater(() -> {
            showErrorDialog("Ready for Flight Failed", message);
            for (GameStateUpdateListener listener : guiUpdateListeners) {
                listener.gamePlayerReadyForFlightFailed(message);
            }
        });
    }

    @Override
    public void adventurePhaseStarted(UUID matchId, Map<Integer, Integer> playerPositions, int firstPlayerToAct) {

    }

    @Override
    public void adventureCardPicked(int playerId, AdventureCard adventureCard, boolean success, String message, int nextPlayerTurn, Map<Integer, Integer> updatedPositions) {

    }


    public void adventurePhaseComplete(UUID matchId, boolean gameComplete, String completionReason) {
        Platform.runLater(() -> {
            System.out.println("Adventure phase complete - Game: " + gameComplete + ", Reason: " + completionReason);
            showInfoMessage("Adventure Phase Complete", completionReason);
            
            if (gameComplete) {
                showInfoMessage("Game Over", "The game has ended! " + completionReason);
            }
        });
    }

    @Override
    public void getPlayerShipResponse(String playerName, Map<Position, Component> shipComponents) {
        Platform.runLater(() -> {
            for (PreparationFlightListener listener : preparationFlightListeners) {
                listener.getPlayerShipResponse(playerName, shipComponents);
            }
        });
    }

    @Override
    public void addTokenToComponent(String playerName, Position position, TokenType tokenType, Boolean success, String errorMessage) {
        Platform.runLater(() -> {
            for (PreparationFlightListener listener : preparationFlightListeners) {
                try {
                    listener.addTokenToComponent(playerName, position, tokenType, success, errorMessage);
                } catch (Exception e) {
                    showErrorDialog("Error Adding Token", errorMessage);
                }
            }
        });
    }

    @Override
    public void getPlayerTokenList(String playerName, Map<Position, List<TokenType>> tokens) {
        Platform.runLater(() -> {
            for (PreparationFlightListener listener : preparationFlightListeners) {
                listener.getPlayerTokenList(playerName, tokens);
            }
        });
    }

    @Override
    public void removeTokenFromComponent(String playerName, Position position, TokenType tokenType, boolean success, String errorMessage) {
        Platform.runLater(() -> {
            for (PreparationFlightListener listener : preparationFlightListeners) {
                if (success) {
                    listener.removeTokenFromComponent(playerName, position, tokenType, success, errorMessage);
                } else {
                    listener.removeTokenFromComponent(playerName, position, tokenType, success, errorMessage);
                }
            }
        });
    }
        

}
