package it.polimi.ingsw.GUI.Controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import it.polimi.ingsw.Direction;
import it.polimi.ingsw.Position;
import it.polimi.ingsw.TokenType;
import it.polimi.ingsw.Components.BatteryCompartment;
import it.polimi.ingsw.Components.Cabin;
import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.GUI.AppContext;
import it.polimi.ingsw.GUI.GUIApp;
import it.polimi.ingsw.GUI.GameStateUpdateListener.PreparationFlightListener;

import static it.polimi.ingsw.GUI.Controllers.ShipBuilderController.COMP_IMAGE_MAP;

public class FlightPreparationController extends GuiContoller implements PreparationFlightListener {
    
    @FXML 
    private AnchorPane anchorPane;
    
    @FXML 
    private Button placeHumanButton;
    
    @FXML 
    private Button placeBatteryButton;
    
    @FXML 
    private Button placeBrownAlienButton;
    
    @FXML 
    private Button placePurpleAlienButton;
    
    @FXML 
    private Button selectPlayerButton;
    
    @FXML 
    private Button seeOthersShipButton;
    
    @FXML 
    private Button readyButton;
    
    @FXML 
    private Label statusLabel;

    private Pane tokenLayer;
    
    private final Map<Position, List<Circle>> tokenViews = new HashMap<>();
    private final Map<Position, ImageView> componentViews = new HashMap<>();

    private enum PlacementMode { NONE, HUMAN, BATTERY, BROWN_ALIEN, PURPLE_ALIEN }
    private PlacementMode currentPlacementMode = PlacementMode.NONE;

    private final Map<Position, Component> shipData   = new HashMap<>();
    public static final Map<TokenType, Color> TOKEN_COLOR = Map.of(
            TokenType.HUMAN, Color.WHITE,
            TokenType.BATTERY, Color.GREEN,
            TokenType.BROWN_ALIEN, Color.BROWN,
            TokenType.PURPLE_ALIEN, Color.PURPLE,
            TokenType.BLUE_GOOD_BLOCK, Color.DEEPSKYBLUE,
            TokenType.RED_GOOD_BLOCK, Color.RED,
            TokenType.GREEN_GOOD_BLOCK, Color.LAWNGREEN,
            TokenType.YELLOW_GOOD_BLOCK, Color.GOLD
    );

    private static final int SQUARE_SIZE = 123;
    private static final int GRID_ROWS = 5;
    private static final int GRID_COLS = 7;
    private static final double GRID_START_X = 38;
    private static final double GRID_START_Y = 31;
    private static final double DOT_RADIUS = 8;
    private static final double DOT_MARGIN = 5;

    private static final boolean[][] VALID_SQUARES = {
        {false,false,true,false,true,false,false},
        {false,true,true,true,true,true,false},
        {true,true,true,true,true,true,true},
        {true,true,true,true,true,true,true},
        {true,true,true,false,true,true,true},
    };

    private String targetPlayerName;

    @FXML
    public void initialize() {
        setupBackground();
        setupGrid();

        tokenLayer = new Pane();
        tokenLayer.setPickOnBounds(false);
        anchorPane.getChildren().add(tokenLayer);
        tokenLayer.toFront();   

        setupClickHandlers();
        statusLabel.setText("Preparation phase: place resources on the grid");
    }

    private void setupBackground() {
        Image bg = new Image(getClass().getResource("/Images/cardboard/cardboard-1b.jpg").toExternalForm());

        anchorPane.setBackground(new Background(new BackgroundImage(bg, 
                                BackgroundRepeat.NO_REPEAT, 
                                BackgroundRepeat.NO_REPEAT,
                                BackgroundPosition.CENTER,
                                new BackgroundSize(937,679,false,false,true,false)
            )
        ));
    }

    private void setupGrid() {
        for (int r = 0; r < GRID_ROWS; r++) {
            for (int c = 0; c < GRID_COLS; c++) {
                if (VALID_SQUARES[r][c]) {
                    double x = GRID_START_X + c * SQUARE_SIZE;
                    double y = GRID_START_Y + r * SQUARE_SIZE;
                    Rectangle rect = new Rectangle(x, y, SQUARE_SIZE, SQUARE_SIZE);
                    rect.setFill(Color.TRANSPARENT);
                    rect.setStroke(null); 
                    
                    // rect.setMouseTransparent(true);
                    
                    Position pos = new Position(c, r);
                    rect.setOnMouseClicked(e -> handleGridClick(pos));
                    anchorPane.getChildren().add(rect);
                }
            }
        }
    }

    private void setupClickHandlers() {
        anchorPane.setOnMouseClicked(evt -> {
            if (evt.getTarget() == anchorPane) {
                currentPlacementMode = PlacementMode.NONE;
                statusLabel.setText("Ready");
                clearButtonStyles();
            }
        });
    }

    private void handleGridClick(Position pos) {
        System.out.println("[DEBUG] Grid clicked at position: " + pos);
        System.out.println("[DEBUG] Current placement mode: " + currentPlacementMode);
        
        if (currentPlacementMode == PlacementMode.NONE) {
            statusLabel.setText("Please select a token type first");
            return;
        }

        Component comp = shipData.get(pos);
        System.out.println("[DEBUG] Component at position: " + (comp != null ? comp.getClass().getSimpleName() : "null"));
        
        if (comp == null) {
            statusLabel.setText("No component in that square - cannot place token here");
            return;
        }

        TokenType tokenToPlace = switch (currentPlacementMode) {
            case HUMAN -> TokenType.HUMAN;
            case BATTERY -> TokenType.BATTERY;
            case BROWN_ALIEN -> TokenType.BROWN_ALIEN;
            case PURPLE_ALIEN -> TokenType.PURPLE_ALIEN;
            default -> null;
        };

        if (tokenToPlace == null) {
            statusLabel.setText("Invalid placement mode");
            return;
        }

        boolean allowed = switch (tokenToPlace) {
            case HUMAN, BROWN_ALIEN, PURPLE_ALIEN -> comp instanceof Cabin;
            case BATTERY -> comp instanceof BatteryCompartment;
            default -> false;
        };

        if (!allowed) {
            String compType = comp.getClass().getSimpleName();
            String tokenName = switch (tokenToPlace) {
                case HUMAN -> "Humans";
                case BATTERY -> "Batteries";
                case BROWN_ALIEN -> "Brown aliens";
                case PURPLE_ALIEN -> "Purple aliens";
                default -> "This token";
            };
            statusLabel.setText(tokenName + " cannot be placed on " + compType);
            return;
        }

        try {
            System.out.println("[DEBUG] Sending token placement request: " + tokenToPlace + " at " + pos);
            context.getClient().addTokenToComponent(context.getUsername(), pos, tokenToPlace);
            statusLabel.setText("Sending token placement request…");
        } catch (Exception e) {
            System.err.println("[DEBUG] Error sending token placement request: " + e.getMessage());
            showErrorDialog("Network error", e.getMessage());
            statusLabel.setText("Failed to send request");
        }
    }

    // private void placeToken(Position pos) {
    //     List<Circle> list = tokenViews.computeIfAbsent(pos, k -> new ArrayList<>());
    //     double baseX = GRID_START_X + pos.getX() * SQUARE_SIZE;
    //     double baseY = GRID_START_Y + pos.getY() * SQUARE_SIZE;
    //     int index = list.size();
    //     double x = baseX + SQUARE_SIZE / 2;
    //     double y = baseY + SQUARE_SIZE / 2;
    //     Circle dot = new Circle(x, y, DOT_RADIUS);
    //     switch (currentPlacementMode) {
    //         case HUMAN -> dot.setFill(Color.WHITE);
    //         case BATTERY -> dot.setFill(Color.GREEN);
    //         case BROWN_ALIEN -> dot.setFill(Color.BROWN);
    //         case PURPLE_ALIEN -> dot.setFill(Color.PURPLE);
    //         default -> dot.setFill(Color.GRAY);
    //     }
    //     dot.setOnMouseClicked(e -> removeToken(pos, dot, currentPlacementMode == PlacementMode.HUMAN ? TokenType.HUMAN :
    //             currentPlacementMode == PlacementMode.BATTERY ? TokenType.BATTERY :
    //             currentPlacementMode == PlacementMode.BROWN_ALIEN ? TokenType.BROWN_ALIEN :
    //             currentPlacementMode == PlacementMode.PURPLE_ALIEN ? TokenType.PURPLE_ALIEN : null));
    //     tokenLayer.getChildren().add(dot);
    //     //dot.toFront();
    //     list.add(dot);
    //     statusLabel.setText("Resource placed: " + currentPlacementMode + " (" + list.size() + ")");
    //     currentPlacementMode = PlacementMode.NONE;
    //     clearButtonStyles();
    // }

    private void removeToken(Position pos, Circle dot, TokenType tokenType) {
        Boolean isProcessing = (Boolean) dot.getUserData();
        if (isProcessing != null && isProcessing) {
            return;
        }
        
        try {
            dot.setUserData(true);
            dot.setDisable(true);
            dot.setOpacity(0.5);
            dot.setOnMouseClicked(null);
            
            statusLabel.setText("Removing token...");
            
            context.getClient().removeTokenFromComponent(context.getUsername(), pos, tokenType);
            
        } catch (Exception e) {
            dot.setUserData(false);
            dot.setDisable(false);
            dot.setOpacity(1.0);
            
            dot.setOnMouseClicked(ev -> {
                ev.consume();
                removeToken(pos, dot, tokenType);
            });
            
            showErrorDialog("Error removing token", e.getMessage());
        }
    }

    private void clearButtonStyles() {
        placeHumanButton.setStyle("");
        placeBatteryButton.setStyle("");
        placeBrownAlienButton.setStyle("");
        placePurpleAlienButton.setStyle("");
    }

    private void highlightButton(Button btn) {
        clearButtonStyles();
        btn.setStyle("-fx-background-color:#4CAF50; -fx-text-fill:white;");
    }

    @FXML private void placeHuman() {
        currentPlacementMode = PlacementMode.HUMAN;
        highlightButton(placeHumanButton);
        statusLabel.setText("Select a square to place the resource");
    }

    @FXML private void placeBattery() {
        currentPlacementMode = PlacementMode.BATTERY;
        highlightButton(placeBatteryButton);
        statusLabel.setText("Select a square to place the resource");
    }

    @FXML private void placeBrownAlien() {
        currentPlacementMode = PlacementMode.BROWN_ALIEN;
        highlightButton(placeBrownAlienButton);
        statusLabel.setText("Select a square to place the resource");
    }

    @FXML private void placePurpleAlien() {
        currentPlacementMode = PlacementMode.PURPLE_ALIEN;
        highlightButton(placePurpleAlienButton);
        statusLabel.setText("Select a square to place the resource");
    }


    @FXML 
    private void seeOthersShip() {
        String selectedPlayerName = showPlayerSelectionDialog();
        
        if (selectedPlayerName != null && !selectedPlayerName.isEmpty()) {
            try {
                GUIApp.showPlayerShip(selectedPlayerName);
            } catch (Exception e) {
                showErrorDialog("Error", "Failed to load player ship: " + e.getMessage());
            }
        }
    }

    @FXML private void startGamePhase() {
        // TODO: implement
    }

    @Override
    public void getPlayerShipResponse(String playerName, Map<Position, Component> ship) {
        
        if (targetPlayerName != null && !playerName.equals(targetPlayerName)) {
            System.out.println("Ignoring ship data for " + playerName + ", waiting for " + targetPlayerName);
            return;
        }

        Platform.runLater(() -> {
            if (statusLabel != null) {
                statusLabel.setText("Loading ship for " + playerName + "...");
            }

            componentViews.values().forEach(iv -> anchorPane.getChildren().remove(iv));
            componentViews.clear();
            
            shipData.clear();
            
            if (ship == null || ship.isEmpty()) {
                System.err.println("ERROR: Ship is null or empty!");
                statusLabel.setText("Error: No ship data received");
                return;
            }

            shipData.putAll(ship);
            System.out.println("[DEBUG] Updated shipData with " + shipData.size() + " components");

            ship.forEach((pos, comp) -> {
                String path = getComponentImgPath(comp);
                
                if (path == null) {
                    System.err.println("No image path found for component: " + comp.getClass().getSimpleName() + " at " + pos);
                    return;
                }

                try {
                    Image img = new Image(getClass().getResource(path).toExternalForm());
                    ImageView iv = new ImageView(img);
                    iv.setFitWidth(SQUARE_SIZE);
                    iv.setFitHeight(SQUARE_SIZE);
                    iv.setPreserveRatio(true);
                    iv.setLayoutX(GRID_START_X + pos.getX() * SQUARE_SIZE);
                    iv.setLayoutY(GRID_START_Y + pos.getY() * SQUARE_SIZE);
                    iv.setMouseTransparent(true);

                    Direction dir = comp.getComponentDirection();
                    double angle = switch (dir) {
                        case NORTH -> 0;
                        case EAST  -> 90;
                        case SOUTH -> 180;
                        case WEST  -> 270;
                    };

                    iv.setRotate(angle);
                    
                    System.out.println("Component at " + pos + " rendered with rotation: " + angle + " degrees");

                    iv.setMouseTransparent(true);
                    anchorPane.getChildren().add(iv);
                    componentViews.put(pos, iv);

            if (tokenLayer != null) {
                tokenLayer.toFront();
                tokenLayer.setPickOnBounds(false);
                tokenLayer.setMouseTransparent(false);
            }
                    
                } catch (Exception e) {
                    System.err.println("Error loading image for path: " + path);
                    e.printStackTrace();
                }
            });
            
            statusLabel.setText("Ship loaded with " + componentViews.size() + " components - Ready to place tokens");
            
            System.out.println("[DEBUG] shipData contents:");
            shipData.forEach((pos, comp) -> 
                System.out.println("  " + pos + " -> " + comp.getClass().getSimpleName()));
        });
    }


    @Override
    public void setContext(AppContext ctx) {
        this.context = ctx;
        ctx.addPreparationFlightListener(this);
        
        new Thread(() -> {
            try {
                context.getClient().getPlayerShip(context.getUsername());
                Thread.sleep(500);
                context.getClient().getPlayerTokenList(context.getUsername());
            } catch (Exception e) {
                Platform.runLater(() ->
                    showErrorDialog("Error fetching ship", e.getMessage()));
            }
        }).start();
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
        
        System.out.println("Component: " + c.getClass().getSimpleName() + ", Key: " + key + ", Direction: " + c.getComponentDirection());
        
        return COMP_IMAGE_MAP.get(key);
    }


    @Override
    public void addTokenToComponent(String username, Position position, TokenType tokenType, boolean success, String errorMsg) {      
        Platform.runLater(() -> {
            if (!success) {
                statusLabel.setText("Error: " + errorMsg);
                currentPlacementMode = PlacementMode.NONE;
                clearButtonStyles();
                return;
            }
            
            if (Objects.equals(username, context.getUsername())) {
                List<Circle> list = tokenViews.computeIfAbsent(position, k -> new ArrayList<>());
                
                double offsetX = (list.size() % 3 - 1) * 25;  
                double offsetY = (list.size() / 3) * 25;
                
                Circle dot = new Circle(
                        GRID_START_X + position.getX()*SQUARE_SIZE + SQUARE_SIZE/2 + offsetX,
                        GRID_START_Y + position.getY()*SQUARE_SIZE + SQUARE_SIZE/2 + offsetY,
                        DOT_RADIUS,
                        TOKEN_COLOR.getOrDefault(tokenType, Color.GRAY)
                );
                
                dot.setStroke(Color.BLACK);
                dot.setStrokeWidth(1);
                
                dot.setUserData(false);
                
                dot.setOnMouseClicked(e -> {
                    e.consume();
                    removeToken(position, dot, tokenType);
                });
                
                dot.setOnMouseEntered(e -> {
                    Boolean isProcessing = (Boolean) dot.getUserData();
                    if (!isProcessing) {
                        dot.setStroke(Color.RED);
                        dot.setStrokeWidth(2);
                        dot.setCursor(javafx.scene.Cursor.HAND);
                    }
                });
                
                dot.setOnMouseExited(e -> {
                    Boolean isProcessing = (Boolean) dot.getUserData();
                    if (!isProcessing) {
                        dot.setStroke(Color.BLACK);
                        dot.setStrokeWidth(1);
                        dot.setCursor(javafx.scene.Cursor.DEFAULT);
                    }
                });
                
                tokenLayer.getChildren().add(dot);
                list.add(dot);
                
                statusLabel.setText("Token placed successfully! Click on token to remove it.");
                currentPlacementMode = PlacementMode.NONE;
                clearButtonStyles();
            }
        });
    }

    @Override
    public void getPlayerTokenList(String playerName, Map<Position, List<TokenType>> tokens) {
        if (!Objects.equals(playerName, context.getUsername())) {
            return;
        }

        Platform.runLater(() -> {
            tokenViews.values().forEach(circles -> 
                circles.forEach(circle -> tokenLayer.getChildren().remove(circle)));
            tokenViews.clear();

            if (tokens != null) {
                tokens.forEach((pos, tokenList) -> {
                    for (int i = 0; i < tokenList.size(); i++) {
                        TokenType tokenType = tokenList.get(i);
                        
                        List<Circle> list = tokenViews.computeIfAbsent(pos, k -> new ArrayList<>());
                        
                        double offsetX = (i % 3 - 1) * 15;
                        double offsetY = (i / 3) * 15;
                        
                        Circle dot = new Circle(
                                GRID_START_X + pos.getX()*SQUARE_SIZE + SQUARE_SIZE/2 + offsetX,
                                GRID_START_Y + pos.getY()*SQUARE_SIZE + SQUARE_SIZE/2 + offsetY,
                                DOT_RADIUS,
                                TOKEN_COLOR.getOrDefault(tokenType, Color.GRAY)
                        );
                        
                        dot.setStroke(Color.BLACK);
                        dot.setStrokeWidth(1);
                        
                        dot.setOnMouseClicked(e -> {
                            e.consume();
                            removeToken(pos, dot, tokenType);
                        });
                        
                        dot.setOnMouseEntered(e -> {
                            dot.setStroke(Color.RED);
                            dot.setStrokeWidth(2);
                            dot.setCursor(javafx.scene.Cursor.HAND);
                        });
                        
                        dot.setOnMouseExited(e -> {
                            dot.setStroke(Color.BLACK);
                            dot.setStrokeWidth(1);
                            dot.setCursor(javafx.scene.Cursor.DEFAULT);
                        });
                        
                        tokenLayer.getChildren().add(dot);
                        list.add(dot);
                    }
                });
                
                statusLabel.setText("Loaded " + tokenViews.size() + " token positions");
            }
        });
    }

    @Override
    public void removeTokenFromComponent(String playerName, Position position, TokenType tokenType, boolean success, String errorMessage) {
        Platform.runLater(() -> {
            if (!success) {
                statusLabel.setText("Error removing token: " + errorMessage);
                showErrorDialog("Remove Token Failed", errorMessage);
                
                List<Circle> circles = tokenViews.get(position);
                if (circles != null) {
                    circles.forEach(circle -> {
                        if (circle.isDisabled()) {
                            circle.setDisable(false);
                            circle.setOpacity(1.0);
                            circle.setUserData(false);
                            
                            circle.setOnMouseClicked(e -> {
                                e.consume();
                                removeToken(position, circle, null);
                            });
                        }
                    });
                }
                return;
            }
            
            if (Objects.equals(playerName, context.getUsername())) {
                List<Circle> circles = tokenViews.get(position);
                
                if (circles != null && !circles.isEmpty()) {
                    Circle toRemove = circles.stream()
                        .filter(Circle::isDisabled)
                        .findFirst()
                        .orElse(circles.get(circles.size() - 1));
                    
                    circles.remove(toRemove);
                    tokenLayer.getChildren().remove(toRemove);
                    
                    for (int i = 0; i < circles.size(); i++) {
                        Circle c = circles.get(i);
                        double offsetX = (i % 3 - 1) * 25;
                        double offsetY = (i / 3) * 25;
                        c.setCenterX(GRID_START_X + position.getX() * SQUARE_SIZE + SQUARE_SIZE/2 + offsetX);
                        c.setCenterY(GRID_START_Y + position.getY() * SQUARE_SIZE + SQUARE_SIZE/2 + offsetY);
                    }
                    
                    if (circles.isEmpty()) {
                        tokenViews.remove(position);
                    }
                    
                    statusLabel.setText("Token removed successfully from position " + position);
                }
            }
        });
    }
}