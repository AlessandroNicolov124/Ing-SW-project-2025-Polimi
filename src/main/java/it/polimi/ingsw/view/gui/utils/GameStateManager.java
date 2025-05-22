package it.polimi.ingsw.view.gui.utils;

import it.polimi.ingsw.GameController;
import it.polimi.ingsw.Player;
import it.polimi.ingsw.view.tui.components.TuiState;
import javafx.application.Platform;

/**
 * Manages game state transitions and updates for the GUI
 * Uses the same states as the TUI for consistency
 */
public class GameStateManager {

    private TuiState currentState;
    private GameController controller;
    private Player currentPlayer;

    public GameStateManager(GameController controller) {
        this.controller = controller;
        this.currentState = TuiState.INTRO;
    }

    public void setState(TuiState state) {
        TuiState oldState = this.currentState;
        this.currentState = state;

        // Notify listeners about state change
        onStateChanged(oldState, state);
    }

    public TuiState getCurrentState() {
        return currentState;
    }

    public void setCurrentPlayer(Player player) {
        this.currentPlayer = player;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    private void onStateChanged(TuiState oldState, TuiState newState) {
        System.out.println("Game state changed from " + oldState + " to " + newState);

        // Handle state-specific logic
        switch (newState) {
            case INTRO:
                handleIntroState();
                break;
            case ASK_USERNAME:
                handleAskUsernameState();
                break;
            case ASK_NET:
                handleAskNetState();
                break;
            case ASK_ADDRESS:
                handleAskAddressState();
                break;
            case CONNECTING:
                handleConnectingState();
                break;
            case WAIT_PLAYER_READY:
                handleWaitPlayerReadyState();
                break;
            case SHIP_BUILDING:
                handleShipBuildingState();
                break;
            case RUNNING:
                handleRunningState();
                break;
            case TERMINATED:
                handleTerminatedState();
                break;
        }
    }

    private void handleIntroState() {
        System.out.println("Showing intro/banner...");
    }

    private void handleAskUsernameState() {
        System.out.println("Ready to ask for username...");
    }

    private void handleAskNetState() {
        System.out.println("Ready to ask for network type...");
    }

    private void handleAskAddressState() {
        System.out.println("Ready to ask for server address...");
    }

    private void handleConnectingState() {
        System.out.println("Connecting to server...");
    }

    private void handleWaitPlayerReadyState() {
        System.out.println("Waiting for player to be ready...");
        // Reset all players' ready status if needed
        for (Player player : controller.getPlayers()) {
            if (player != currentPlayer) {
                player.setReady(false);
            }
        }
    }

    private void handleShipBuildingState() {
        System.out.println("Initializing ship building phase...");
        // Reset all players' ready status for ship building
        for (Player player : controller.getPlayers()) {
            player.setReady(false);
        }
    }

    private void handleRunningState() {
        System.out.println("Game is now running...");
        // Initialize game logic for running state
    }

    private void handleTerminatedState() {
        System.out.println("Game terminated.");
        // Cleanup resources, close connections, etc.
        cleanupGame();
    }

    private void cleanupGame() {
        // Close any open connections
        // Save game state if needed
        // Release resources
    }

    public boolean areAllPlayersReady() {
        if (controller.getPlayers().isEmpty()) {
            return false;
        }

        for (Player player : controller.getPlayers()) {
            if (!player.getReady()) {
                return false;
            }
        }
        return true;
    }

    public int getReadyPlayerCount() {
        int count = 0;
        for (Player player : controller.getPlayers()) {
            if (player.getReady()) {
                count++;
            }
        }
        return count;
    }

    public int getTotalPlayerCount() {
        return controller.getPlayers().size();
    }

    public void checkForStateTransition() {
        Platform.runLater(() -> {
            switch (currentState) {
                case WAIT_PLAYER_READY:
                    if (areAllPlayersReady()) {
                        setState(TuiState.SHIP_BUILDING);
                    }
                    break;
                case SHIP_BUILDING:
                    if (areAllPlayersReady()) {
                        setState(TuiState.RUNNING);
                    }
                    break;
                // Add other state transition checks as needed
            }
        });
    }

    /**
     * Get the next expected state based on current state
     */
    public TuiState getNextState() {
        switch (currentState) {
            case INTRO:
                return TuiState.ASK_USERNAME;
            case ASK_USERNAME:
                return TuiState.ASK_NET;
            case ASK_NET:
                return TuiState.ASK_ADDRESS;
            case ASK_ADDRESS:
                return TuiState.CONNECTING;
            case CONNECTING:
                return TuiState.WAIT_PLAYER_READY;
            case WAIT_PLAYER_READY:
                return TuiState.SHIP_BUILDING;
            case SHIP_BUILDING:
                return TuiState.RUNNING;
            case RUNNING:
                return TuiState.TERMINATED; // or stay in RUNNING
            default:
                return TuiState.TERMINATED;
        }
    }

    /**
     * Progress to the next logical state
     */
    public void progressToNextState() {
        TuiState nextState = getNextState();
        if (nextState != currentState) {
            setState(nextState);
        }
    }

    /**
     * Check if we can transition to a specific state
     */
    public boolean canTransitionTo(TuiState targetState) {
        switch (targetState) {
            case SHIP_BUILDING:
                return currentState == TuiState.WAIT_PLAYER_READY || currentState == TuiState.CONNECTING;
            case RUNNING:
                return currentState == TuiState.SHIP_BUILDING && areAllPlayersReady();
            case TERMINATED:
                return true; // Can always terminate
            default:
                return false;
        }
    }
}