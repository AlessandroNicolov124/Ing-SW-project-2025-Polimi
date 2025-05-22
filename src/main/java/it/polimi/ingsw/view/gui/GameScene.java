package it.polimi.ingsw.view.gui;

import it.polimi.ingsw.GameController;
import it.polimi.ingsw.Player;
import it.polimi.ingsw.view.gui.components.ShipGrid;
import it.polimi.ingsw.view.gui.utils.GameStateManager;
import it.polimi.ingsw.view.tui.components.TuiState;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Main game scene for gameplay after ship building phase
 * Now integrated with GameStateManager
 */
public class GameScene {

    private final GalaxyTruckerFX application;
    private final GameController controller;
    private final GameStateManager stateManager;
    private Scene scene;
    private Player currentPlayer;

    // UI Components
    private TabPane playerTabPane;
    private VBox gameControlsPanel;
    private TextArea gameLogArea;
    private Label statusLabel;
    private Label roundInfoLabel;
    private Label stateLabel;
    private Button rollDiceButton;

    public GameScene(GalaxyTruckerFX application, GameController controller, GameStateManager stateManager) {
        this.application = application;
        this.controller = controller;
        this.stateManager = stateManager;

        // Get current player from state manager
        this.currentPlayer = stateManager.getCurrentPlayer();
        if (this.currentPlayer == null && !controller.getPlayers().isEmpty()) {
            this.currentPlayer = controller.getPlayers().get(0);
            stateManager.setCurrentPlayer(this.currentPlayer);
        }

        createScene();
    }

    private void createScene() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // Top: Game info and round status
        VBox topPanel = createTopPanel();
        root.setTop(topPanel);

        // Center: Player ships in tabs
        playerTabPane = createPlayerTabPane();
        root.setCenter(playerTabPane);

        // Right: Game controls
        gameControlsPanel = createGameControlsPanel();
        root.setRight(gameControlsPanel);

        // Bottom: Game log
        VBox bottomPanel = createBottomPanel();
        root.setBottom(bottomPanel);

        scene = new Scene(root);
    }

    private VBox createTopPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setAlignment(Pos.CENTER);

        // Game title
        Label titleLabel = new Label("Galaxy Trucker");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        // State indicator
        stateLabel = new Label("State: " + stateManager.getCurrentState());
        stateLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        stateLabel.setStyle("-fx-text-fill: blue;");

        // Round information
        roundInfoLabel = new Label("Round: 1 - Flight Phase");
        roundInfoLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        // Status message
        statusLabel = new Label("Game is running. Choose your actions.");
        statusLabel.setFont(Font.font("Arial", 14));

        panel.getChildren().addAll(titleLabel, stateLabel, roundInfoLabel, statusLabel);
        return panel;
    }

    private TabPane createPlayerTabPane() {
        TabPane tabPane = new TabPane();

        // Create a tab for each player's ship
        for (Player player : controller.getPlayers()) {
            Tab playerTab = new Tab(player.getNickName() + "'s Ship");

            // Create a ship view for this player
            ShipGrid shipGrid = new ShipGrid(controller, player);
            shipGrid.updateShipView();

            // Add player stats above the ship
            VBox playerView = new VBox(10);
            playerView.setPadding(new Insets(10));

            HBox statsBox = createPlayerStatsBox(player);

            ScrollPane scrollPane = new ScrollPane(shipGrid);
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);

            playerView.getChildren().addAll(statsBox, scrollPane);
            playerTab.setContent(playerView);

            // Make tab closable only if it's not the current player
            playerTab.setClosable(false);

            tabPane.getTabs().add(playerTab);
        }

        return tabPane;
    }

    private HBox createPlayerStatsBox(Player player) {
        HBox statsBox = new HBox(20);
        statsBox.setPadding(new Insets(10));
        statsBox.setAlignment(Pos.CENTER_LEFT);

        // Player name and status
        VBox nameBox = new VBox(5);
        Label nameLabel = new Label("Player: " + player.getNickName());
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        Label statusLabel = new Label("Status: " + (player.getReady() ? "Ready" : "Not Ready"));
        nameBox.getChildren().addAll(nameLabel, statusLabel);

        // Credits
        VBox creditsBox = new VBox(5);
        Label creditsTitle = new Label("Credits");
        creditsTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        Label creditsValue = new Label("1000"); // Replace with actual credits
        creditsBox.getChildren().addAll(creditsTitle, creditsValue);

        // Crew
        VBox crewBox = new VBox(5);
        Label crewTitle = new Label("Crew");
        crewTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        Label crewValue = new Label("5"); // Replace with actual crew count
        crewBox.getChildren().addAll(crewTitle, crewValue);

        // Ship structure/health
        VBox healthBox = new VBox(5);
        Label healthTitle = new Label("Ship Health");
        healthTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        ProgressBar healthBar = new ProgressBar(1.0);
        healthBar.setPrefWidth(100);
        healthBox.getChildren().addAll(healthTitle, healthBar);

        statsBox.getChildren().addAll(nameBox, creditsBox, crewBox, healthBox);

        return statsBox;
    }

    private VBox createGameControlsPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(10));
        panel.setPrefWidth(200);
        panel.setAlignment(Pos.TOP_CENTER);

        Label controlsTitle = new Label("Game Controls");
        controlsTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        // Actions section
        VBox actionsBox = new VBox(10);
        actionsBox.setPadding(new Insets(10));
        actionsBox.setStyle("-fx-border-color: gray; -fx-border-radius: 5;");

        Label actionsLabel = new Label("Actions");
        actionsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        rollDiceButton = new Button("Roll Dice");
        rollDiceButton.setPrefWidth(160);
        rollDiceButton.setOnAction(e -> rollDice());

        Button moveButton = new Button("Move Ship");
        moveButton.setPrefWidth(160);
        moveButton.setOnAction(e -> moveShip());

        Button fireButton = new Button("Fire Cannons");
        fireButton.setPrefWidth(160);
        fireButton.setOnAction(e -> fireCannons());

        Button endTurnButton = new Button("End Turn");
        endTurnButton.setPrefWidth(160);
        endTurnButton.setOnAction(e -> endTurn());

        actionsBox.getChildren().addAll(actionsLabel, rollDiceButton, moveButton, fireButton, endTurnButton);

        // Player info section
        VBox infoBox = new VBox(10);
        infoBox.setPadding(new Insets(10));
        infoBox.setStyle("-fx-border-color: gray; -fx-border-radius: 5;");

        Label infoLabel = new Label("Current Player");
        infoLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        Label playerNameLabel = new Label("Name: " + (currentPlayer != null ? currentPlayer.getNickName() : "Unknown"));
        Label playerTurnLabel = new Label("Turn: 1");

        infoBox.getChildren().addAll(infoLabel, playerNameLabel, playerTurnLabel);

        panel.getChildren().addAll(controlsTitle, actionsBox, infoBox);

        return panel;
    }

    private VBox createBottomPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));

        Label logLabel = new Label("Game Log");
        logLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        gameLogArea = new TextArea();
        gameLogArea.setEditable(false);
        gameLogArea.setPrefHeight(100);
        gameLogArea.setText("Game started. Ship building phase completed.\n");

        panel.getChildren().addAll(logLabel, gameLogArea);

        return panel;
    }

    // Game actions
    private void rollDice() {
        // Simulate rolling dice
        int roll = (int)(Math.random() * 6) + 1;
        addToGameLog("Player " + currentPlayer.getNickName() + " rolled " + roll);

        // Update UI based on roll
        rollDiceButton.setDisable(true);
        statusLabel.setText("You rolled a " + roll + ". Choose your action.");
    }

    private void moveShip() {
        addToGameLog("Player " + currentPlayer.getNickName() + " moved their ship.");
        statusLabel.setText("Ship moved. Choose next action.");
    }

    private void fireCannons() {
        addToGameLog("Player " + currentPlayer.getNickName() + " fired cannons.");
        statusLabel.setText("Cannons fired. Choose next action.");
    }

    private void endTurn() {
        addToGameLog("Player " + currentPlayer.getNickName() + " ended their turn.");
        statusLabel.setText("Waiting for other players...");

        // Reset UI for next turn
        rollDiceButton.setDisable(false);

        // In a real implementation, you would notify the server
        // For now, just simulate a turn change
        simulateNextTurn();
    }

    private void backToShipBuilding() {
        // Allow going back to ship building for testing
        stateManager.setState(TuiState.SHIP_BUILDING);
        // We need the connection parameters, use defaults for now
        application.showShipBuildingScene(
                currentPlayer != null ? currentPlayer.getNickName() : "Player",
                "socket", "localhost", 7777
        );
    }

    private void simulateNextTurn() {
        // This is a stub for demonstration
        // In a real implementation, the server would control turn order
        addToGameLog("Next player's turn...");
    }

    public void addToGameLog(String message) {
        Platform.runLater(() -> {
            gameLogArea.appendText(message + "\n");
            // Auto-scroll to bottom
            gameLogArea.setScrollTop(Double.MAX_VALUE);
        });
    }

    public Scene getScene() {
        return scene;
    }
}