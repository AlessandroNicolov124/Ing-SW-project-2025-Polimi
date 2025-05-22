package it.polimi.ingsw.view.gui;

import it.polimi.ingsw.GameController;
import it.polimi.ingsw.view.gui.scenes.ConnectionScene;
import it.polimi.ingsw.view.gui.scenes.IntroScene;
import it.polimi.ingsw.view.gui.scenes.ShipBuildingScene;
import it.polimi.ingsw.view.gui.scenes.WaitingForPlayersScene;
import it.polimi.ingsw.view.gui.utils.GameStateManager;
import it.polimi.ingsw.view.tui.components.TuiState;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * Main JavaFX application class for Galaxy Trucker
 * Now uses GameStateManager to track application state
 */
public class GalaxyTruckerFX extends Application {

    private Stage primaryStage;
    private GameController controller;
    private GameStateManager stateManager;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.controller = new GameController();
        this.stateManager = new GameStateManager(controller);

        primaryStage.setTitle("Galaxy Trucker");
        primaryStage.setWidth(1000);
        primaryStage.setHeight(700);

        // Handle window close event
        primaryStage.setOnCloseRequest(e -> {
            stateManager.setState(TuiState.TERMINATED);
            Platform.exit();
        });

        // Set initial scene based on current state
        showSceneForCurrentState();

        primaryStage.show();
    }

    /**
     * Shows the appropriate scene based on current game state
     */
    public void showSceneForCurrentState() {
        TuiState currentState = stateManager.getCurrentState();

        switch (currentState) {
            case INTRO:
                showIntroScene();
                break;
            case ASK_USERNAME:
            case ASK_NET:
            case ASK_ADDRESS:
            case CONNECTING:
                showConnectionScene();
                break;
            case WAIT_PLAYER_READY:
                showWaitingForPlayersScene();
                break;
            case SHIP_BUILDING:
                // This will be called with parameters from ConnectionScene
                break;
            case RUNNING:
                showGameScene();
                break;
            case TERMINATED:
                Platform.exit();
                break;
        }
    }

    /**
     * Shows the intro scene with game banner
     */
    public void showIntroScene() {
        stateManager.setState(TuiState.INTRO);
        IntroScene introScene = new IntroScene(this);
        primaryStage.setScene(introScene.getScene());
    }

    /**
     * Shows the username and connection setup scene
     */
    public void showConnectionScene() {
        // Progress through the connection states
        if (stateManager.getCurrentState() == TuiState.INTRO) {
            stateManager.setState(TuiState.ASK_USERNAME);
        }

        ConnectionScene connectionScene = new ConnectionScene(this, controller, stateManager);
        primaryStage.setScene(connectionScene.getScene());
    }

    /**
     * Shows waiting for players scene
     */
    public void showWaitingForPlayersScene() {
        stateManager.setState(TuiState.WAIT_PLAYER_READY);
        WaitingForPlayersScene waitingScene = new WaitingForPlayersScene(this, controller, stateManager);
        primaryStage.setScene(waitingScene.getScene());
    }

    /**
     * Shows the ship building scene
     */
    public void showShipBuildingScene(String username, String connectionType, String serverAddress, int serverPort) {
        stateManager.setState(TuiState.SHIP_BUILDING);
        ShipBuildingScene shipBuildingScene = new ShipBuildingScene(this, controller, stateManager, username, connectionType, serverAddress, serverPort);
        primaryStage.setScene(shipBuildingScene.getScene());
    }

    /**
     * Shows the main game scene
     */
    public void showGameScene() {
        stateManager.setState(TuiState.RUNNING);
        GameScene gameScene = new GameScene(this, controller, stateManager);
        primaryStage.setScene(gameScene.getScene());
    }

    /**
     * Transition to next logical state
     */
    public void progressToNextState() {
        stateManager.progressToNextState();
        showSceneForCurrentState();
    }

    // Getters
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public GameController getController() {
        return controller;
    }

    public GameStateManager getStateManager() {
        return stateManager;
    }

    public static void main(String[] args) {
        launch(args);
    }
}