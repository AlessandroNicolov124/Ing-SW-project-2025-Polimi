package it.polimi.ingsw.GUI.Controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.Position;
import it.polimi.ingsw.TokenType;
import it.polimi.ingsw.GUI.AppContext;
import it.polimi.ingsw.GUI.GUIApp;
import it.polimi.ingsw.GUI.GameStateUpdateListener.PreparationFlightListener;

import java.util.Map;
import java.util.Objects;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static it.polimi.ingsw.GUI.Controllers.ShipBuilderController.COMP_IMAGE_MAP;

public class PlayerShipController extends GuiContoller implements PreparationFlightListener {
    @FXML 
    private AnchorPane anchorPane;
    
    @FXML 
    private Label statusLabel;

    private final Map<Position, ImageView> componentViews = new HashMap<>();
    private String targetPlayerName;
    private final Map<Position, List<Circle>> tokenViews = new HashMap<>();
    private Pane tokenLayer;
    
    // Usa la mappa statica da FlightPreparationController
    private final Map<TokenType, Color> TOKEN_COLOR = FlightPreparationController.TOKEN_COLOR;

    private static final int SQUARE_SIZE = 123;
    private static final double GRID_START_X = 38;
    private static final double GRID_START_Y = 31;

    public void setPlayerToView(String playerName) {
        this.targetPlayerName = playerName;
    }

    @FXML
    public void initialize() {
        setupBackground();
        tokenLayer = new Pane();
        tokenLayer.setPickOnBounds(false);
        anchorPane.getChildren().add(tokenLayer);
        tokenLayer.toFront();
        if (targetPlayerName != null) {
            statusLabel.setText("Loading ship for " + targetPlayerName + "…");
        }
    }

    private void setupBackground() {
        Image bg = new Image(getClass().getResource("/Images/cardboard/cardboard-1b.jpg").toExternalForm());
        anchorPane.setBackground(new javafx.scene.layout.Background(
            new javafx.scene.layout.BackgroundImage(
                bg,
                javafx.scene.layout.BackgroundRepeat.NO_REPEAT,
                javafx.scene.layout.BackgroundRepeat.NO_REPEAT,
                javafx.scene.layout.BackgroundPosition.CENTER,
                new javafx.scene.layout.BackgroundSize(
                    937, 679, false, false, true, false
                )
            )
        ));
    }

    @Override
    public void setContext(AppContext ctx) {
        this.context = ctx;
        ctx.addPreparationFlightListener(this);
        new Thread(() -> {
            try {
                context.getClient().getPlayerShip(targetPlayerName);
                // Corretto: solo username come parametro
                context.getClient().getPlayerTokenList(targetPlayerName);
            } catch (Exception e) {
                Platform.runLater(() ->
                    showErrorDialog("Load error", e.getMessage()));
            }
        }).start();
    }

    @Override
    public void getPlayerShipResponse(String playerName, Map<Position, Component> ship) {
        System.out.println("[Debug] Received ship response for: '" + playerName + "'");
        System.out.println("[Debug] Target player name: '" + targetPlayerName + "'");
        System.out.println("[Debug] Ship data: " + (ship != null ? ship.size() + " components" : "null"));
        
        if (!Objects.equals(playerName, targetPlayerName)) {
            System.out.println("[Debug] Player name mismatch, ignoring response");
            return;
        }

        Platform.runLater(() -> {
            componentViews.values().forEach(iv -> anchorPane.getChildren().remove(iv));
            componentViews.clear();

            if (ship == null || ship.isEmpty()) {
                statusLabel.setText("No ship data available for " + playerName);
                return;
            }

            ship.forEach((pos, comp) -> {
                String path = getComponentImgPath(comp);
                if (path == null) return;

                try {
                    Image img = new Image(getClass().getResource(path).toExternalForm());
                    ImageView iv = new ImageView(img);
                    iv.setFitWidth(SQUARE_SIZE);
                    iv.setFitHeight(SQUARE_SIZE);
                    iv.setLayoutX(GRID_START_X + pos.getX() * SQUARE_SIZE);
                    iv.setLayoutY(GRID_START_Y + pos.getY() * SQUARE_SIZE);

                    double angle = switch (comp.getComponentDirection()) {
                        case NORTH -> 0;
                        case EAST  -> 90;
                        case SOUTH -> 180;
                        case WEST  -> 270;
                    };
                    iv.setRotate(angle);

                    anchorPane.getChildren().add(iv);
                    componentViews.put(pos, iv);
                } catch (Exception e) {
                    System.err.println("Error loading image for component at " + pos + ": " + e.getMessage());
                    e.printStackTrace();
                }
            });

            statusLabel.setText("Ship for " + playerName + " loaded successfully.");
        });
    }

    private String getComponentImgPath(Component c) {
        if (c == null) return null;

        String key = c.getComponentString();
        if ("cabin_starting".equals(key)) {
            String color = context.getSelectedCabinColor();
            key = switch (color) {
                case "BLUE" -> "cabin_starting_4";
                case "GREEN" -> "cabin_starting_3";
                case "YELLOW" -> "cabin_starting_2";
                default -> "cabin_starting_1";
            };
        }
        return COMP_IMAGE_MAP.get(key);
    }

    @FXML
    private void previousView() {
        try {
            GUIApp.returnToPreviousPage();
        } catch (Exception e) {
            showErrorDialog("Navigation Error", "Failed to return to previous view: " + e.getMessage());
        }
    }

    @Override
    public void addTokenToComponent(String username, Position position, TokenType tokenType, boolean success, String errorMsg) {
        if (!success || !Objects.equals(username, targetPlayerName)){
                return;
        }

        Platform.runLater(() -> {
            Circle dot = new Circle(
                    GRID_START_X + position.getX()*SQUARE_SIZE + SQUARE_SIZE/2,
                    GRID_START_Y + position.getY()*SQUARE_SIZE + SQUARE_SIZE/2,
                    7, TOKEN_COLOR.get(tokenType));
            tokenLayer.getChildren().add(dot);
            tokenViews.computeIfAbsent(position, k -> new ArrayList<>()).add(dot);
        });
    }

    @Override
    public void getPlayerTokenList(String playerName, Map<Position, List<TokenType>> tokens) {
        if (!Objects.equals(playerName, targetPlayerName)) {
            return;
        }

        Platform.runLater(() -> {
            tokenViews.values().forEach(circles -> 
                circles.forEach(circle -> tokenLayer.getChildren().remove(circle)));
            tokenViews.clear();

            if (tokens != null) {
                tokens.forEach((pos, tokenList) -> {
                    List<Circle> circles = new ArrayList<>();
                    for (int i = 0; i < tokenList.size(); i++) {
                        TokenType tokenType = tokenList.get(i);
                        
                        double offsetX = (i % 3 - 1) * 10;
                        double offsetY = (i / 3) * 10;
                        
                        Circle dot = new Circle(
                                GRID_START_X + pos.getX()*SQUARE_SIZE + SQUARE_SIZE/2 + offsetX,
                                GRID_START_Y + pos.getY()*SQUARE_SIZE + SQUARE_SIZE/2 + offsetY,
                                7, 
                                TOKEN_COLOR.get(tokenType)
                        );
                        dot.setStroke(Color.BLACK);
                        dot.setStrokeWidth(0.5);
                        
                        tokenLayer.getChildren().add(dot);
                        circles.add(dot);
                    }
                    tokenViews.put(pos, circles);
                });
                
                tokenLayer.toFront();
            }
        });
    }

    @Override
    public void removeTokenFromComponent(String playerName, Position position, TokenType tokenType, boolean success, String error){
    
    }
    
}