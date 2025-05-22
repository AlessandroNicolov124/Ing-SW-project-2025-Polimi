package it.polimi.ingsw.view.gui.scenes;

import it.polimi.ingsw.view.gui.GalaxyTruckerFX;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * The initial scene of the game showing the Galaxy Trucker banner
 */
public class IntroScene {

    private final GalaxyTruckerFX application;
    private Scene scene;

    public IntroScene(GalaxyTruckerFX application) {
        this.application = application;
        createScene();
    }

    private void createScene() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));

        // Game banner title
        Label titleLabel = new Label("GALAXY TRUCKER");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));

        // Game subtitle
        Label subtitleLabel = new Label("Build your ship and explore the galaxy!");
        subtitleLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 16));

        // Start button
        Button startButton = new Button("Start Game");
        startButton.setPrefWidth(150);
        startButton.setStyle("-fx-background-color: green; -fx-text-fill: white; -fx-font-weight: bold;");
        startButton.setOnAction(e -> application.showConnectionScene());

        // Quick test buttons for development
        HBox testButtonsBox = new HBox(10);
        testButtonsBox.setAlignment(Pos.CENTER);

        Button quickShipBuildingButton = new Button("Quick Ship Building");
        quickShipBuildingButton.setStyle("-fx-background-color: orange; -fx-text-fill: white;");
        quickShipBuildingButton.setOnAction(e -> {
            // Quick start for testing
            application.getController().addPlayer(new it.polimi.ingsw.Player(0, "TestPlayer", false));
            application.getStateManager().setCurrentPlayer(application.getController().getPlayers().get(0));
            application.showShipBuildingScene("TestPlayer", "socket", "localhost", 7777);
        });

        Button quickGameButton = new Button("Quick Game");
        quickGameButton.setStyle("-fx-background-color: blue; -fx-text-fill: white;");
        quickGameButton.setOnAction(e -> {
            // Quick start to game for testing
            application.getController().addPlayer(new it.polimi.ingsw.Player(0, "TestPlayer", true));
            application.getStateManager().setCurrentPlayer(application.getController().getPlayers().get(0));
            application.showGameScene();
        });

        testButtonsBox.getChildren().addAll(quickShipBuildingButton, quickGameButton);

        root.getChildren().addAll(titleLabel, subtitleLabel, startButton, testButtonsBox);
        scene = new Scene(root);
    }

    public Scene getScene() {
        return scene;
    }
}