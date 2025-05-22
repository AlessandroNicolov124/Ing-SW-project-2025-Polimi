package it.polimi.ingsw.view.gui.scenes;

import it.polimi.ingsw.GameController;
import it.polimi.ingsw.Player;
import it.polimi.ingsw.view.gui.GalaxyTruckerFX;
import it.polimi.ingsw.view.gui.utils.GameStateManager;
import it.polimi.ingsw.view.tui.components.TuiState;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

/**
 * Scene shown while waiting for all players to be ready
 */
public class WaitingForPlayersScene {

    private final GalaxyTruckerFX application;
    private final GameController controller;
    private final GameStateManager stateManager;
    private Scene scene;

    // UI Components
    private Label statusLabel;
    private ListView<String> playersListView;
    private ProgressIndicator loadingIndicator;
    private Button readyButton;
    private Button backButton;
    private Label countdownLabel;

    // State
    private Timeline updateTimeline;
    private boolean currentPlayerReady = false;

    public WaitingForPlayersScene(GalaxyTruckerFX application, GameController controller, GameStateManager stateManager) {
        this.application = application;
        this.controller = controller;
        this.stateManager = stateManager;
        createScene();
        startPeriodicUpdate();
    }

    private void createScene() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));

        // Title
        Label titleLabel = new Label("Waiting for Players");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));

        // Status
        statusLabel = new Label("Waiting for all players to join and be ready...");
        statusLabel.setFont(Font.font("Arial", 16));
        statusLabel.setWrapText(true);
        statusLabel.setAlignment(Pos.CENTER);

        // Loading indicator
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setPrefSize(50, 50);

        // Players list
        VBox playersSection = createPlayersSection();

        // Controls
        HBox controlsBox = createControlsBox();

        // Countdown (initially hidden)
        countdownLabel = new Label();
        countdownLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        countdownLabel.setTextFill(Color.GREEN);
        countdownLabel.setVisible(false);

        root.getChildren().addAll(
                titleLabel,
                statusLabel,
                loadingIndicator,
                playersSection,
                controlsBox,
                countdownLabel
        );

        scene = new Scene(root);
    }

    private VBox createPlayersSection() {
        VBox section = new VBox(10);
        section.setAlignment(Pos.CENTER);

        Label playersLabel = new Label("Connected Players:");
        playersLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        playersListView = new ListView<>();
        playersListView.setPrefHeight(150);
        playersListView.setPrefWidth(300);

        section.getChildren().addAll(playersLabel, playersListView);
        return section;
    }

    private HBox createControlsBox() {
        HBox controlsBox = new HBox(15);
        controlsBox.setAlignment(Pos.CENTER);

        // Ready button
        readyButton = new Button("I'm Ready");
        readyButton.setPrefWidth(120);
        readyButton.setStyle("-fx-background-color: green; -fx-text-fill: white; -fx-font-weight: bold;");
        readyButton.setOnAction(e -> toggleReady());

        // Back button
        backButton = new Button("Back to Connection");
        backButton.setPrefWidth(150);
        backButton.setStyle("-fx-background-color: gray; -fx-text-fill: white;");
        backButton.setOnAction(e -> goBackToConnection());

        // Force start button (for testing/admin)
        Button forceStartButton = new Button("Force Start");
        forceStartButton.setPrefWidth(120);
        forceStartButton.setStyle("-fx-background-color: orange; -fx-text-fill: white;");
        forceStartButton.setOnAction(e -> forceStartGame());

        controlsBox.getChildren().addAll(readyButton, backButton, forceStartButton);
        return controlsBox;
    }

    private void startPeriodicUpdate() {
        // Update the UI every 2 seconds
        updateTimeline = new Timeline(new KeyFrame(Duration.seconds(2), e -> updatePlayersList()));
        updateTimeline.setCycleCount(Timeline.INDEFINITE);
        updateTimeline.play();

        // Initial update
        updatePlayersList();
    }

    private void updatePlayersList() {
        Platform.runLater(() -> {
            playersListView.getItems().clear();

            int readyCount = 0;
            int totalCount = controller.getPlayers().size();

            for (Player player : controller.getPlayers()) {
                String status = player.getReady() ? "✓ Ready" : "⏳ Not Ready";
                String displayName = player.getNickName() + " - " + status;

                if (player.equals(stateManager.getCurrentPlayer())) {
                    displayName = "You (" + displayName + ")";
                }

                playersListView.getItems().add(displayName);

                if (player.getReady()) {
                    readyCount++;
                }
            }

            // Update status
            if (totalCount == 0) {
                statusLabel.setText("No players connected. Waiting for players to join...");
            } else if (readyCount == totalCount && totalCount > 1) {
                statusLabel.setText("All players are ready! Starting ship building phase...");
                startCountdownToShipBuilding();
            } else {
                statusLabel.setText(String.format("Players ready: %d/%d", readyCount, totalCount));
            }

            // Check for automatic state transitions
            stateManager.checkForStateTransition();

            // If state changed to SHIP_BUILDING, transition to that scene
            if (stateManager.getCurrentState() == TuiState.SHIP_BUILDING) {
                stopPeriodicUpdate();
                // We need connection details to proceed - for now use defaults
                application.showShipBuildingScene("Player", "socket", "localhost", 7777);
            }
        });
    }

    private void toggleReady() {
        Player currentPlayer = stateManager.getCurrentPlayer();
        if (currentPlayer != null) {
            currentPlayerReady = !currentPlayerReady;
            currentPlayer.setReady(currentPlayerReady);

            if (currentPlayerReady) {
                readyButton.setText("Not Ready");
                readyButton.setStyle("-fx-background-color: red; -fx-text-fill: white; -fx-font-weight: bold;");
            } else {
                readyButton.setText("I'm Ready");
                readyButton.setStyle("-fx-background-color: green; -fx-text-fill: white; -fx-font-weight: bold;");
            }

            // Immediately update the display
            updatePlayersList();
        }
    }

    private void goBackToConnection() {
        stopPeriodicUpdate();
        stateManager.setState(TuiState.ASK_USERNAME);
        application.showConnectionScene();
    }

    private void forceStartGame() {
        // For testing purposes - force all players to be ready
        for (Player player : controller.getPlayers()) {
            player.setReady(true);
        }
        updatePlayersList();
    }

    private void startCountdownToShipBuilding() {
        if (countdownLabel.isVisible()) {
            return; // Already counting down
        }

        countdownLabel.setVisible(true);
        loadingIndicator.setVisible(false);

        // 5 second countdown
        Task<Void> countdownTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                for (int i = 5; i > 0; i--) {
                    final int count = i;
                    Platform.runLater(() ->
                            countdownLabel.setText("Starting ship building in " + count + " seconds...")
                    );
                    Thread.sleep(1000);
                }
                return null;
            }
        };

        countdownTask.setOnSucceeded(e -> {
            stopPeriodicUpdate();
            stateManager.setState(TuiState.SHIP_BUILDING);
            // Transition to ship building scene
            application.showShipBuildingScene("Player", "socket", "localhost", 7777);
        });

        new Thread(countdownTask).start();
    }

    private void stopPeriodicUpdate() {
        if (updateTimeline != null) {
            updateTimeline.stop();
        }
    }

    public Scene getScene() {
        return scene;
    }
}