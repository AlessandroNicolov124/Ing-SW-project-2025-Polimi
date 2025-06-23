package it.polimi.ingsw.GUI.Controllers;

import it.polimi.ingsw.Position;
import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.GUI.AppContext;
import it.polimi.ingsw.GUI.GameStateUpdateListener.GameStateUpdateListener;
import it.polimi.ingsw.network.messages.RotationType;
import javafx.fxml.FXML;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BackgroundPosition;

import javafx.scene.paint.Color;

import javafx.scene.shape.Rectangle;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShipBuilderController extends GuiContoller implements GameStateUpdateListener {
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private ImageView cardImage;
    @FXML
    private Button pickComponentButton;
    @FXML
    private Button pickFistComponentButton;
    @FXML
    private Button rotateClockwiseButton;
    @FXML
    private Button rotateCounterClockwiseButton;
    @FXML
    private Button validateShipButton;
    @FXML
    private Label statusLabel;

    // Component management
    private Component currentComponent = null;
    private boolean isRemoveMode = false;
    
    // Visual elements tracking
    private ImageView selectedImageView = null;
    private Rectangle selectedHighlight = null;
    private final Map<Position, ImageView> cellViews = new HashMap<>();
    private final Map<Position, Component> shipComponents = new HashMap<>();

    // Board configuration
    private static final int SQUARE_SIZE = 123;
    private static final int GRID_ROWS = 5;
    private static final int GRID_COLS = 7;
    private static final double GRID_START_X = 38;
    private static final double GRID_START_Y = 31;

    private static final boolean[][] VALID_SQUARES = {
            {false, false, true,  false, true,  false, false},
            {false, true,  true,  true,  true,  true,  false},
            {true,  true,  true,  true,  true,  true,  true },
            {true,  true,  true,  true,  true,  true,  true },
            {true, true,  true,  false, true,  true,  true },
    };

    private final Map<String, String> COMP_IMAGE_MAP = Map.ofEntries(
            Map.entry("cabin_starting", "/Images/tiles/Cabin_Starting_1.jpg"),
            Map.entry("batterycompartment_double_DSDS", "/Images/tiles/batterycompartment_double_DSDS.jpg"),
            Map.entry("batterycompartment_double_NNNU", "/Images/tiles/batterycompartment_double_NNNU.jpg"),
            Map.entry("batterycompartment_double_NNNU_2", "/Images/tiles/batterycompartment_double_NNNU_2.jpg"),
            Map.entry("batterycompartment_double_NUNU", "/Images/tiles/batterycompartment_double_NUNU.jpg"),
            Map.entry("batterycompartment_double_UDDD", "/Images/tiles/batterycompartment_double_UDDD.jpg"),
            Map.entry("batterycompartment_double_UDNN", "/Images/tiles/batterycompartment_double_UDNN.jpg"),
            Map.entry("batterycompartment_double_UDSN", "/Images/tiles/batterycompartment_double_UDSN.jpg"),
            Map.entry("batterycompartment_double_UNNS", "/Images/tiles/batterycompartment_double_UNNS.jpg"),
            Map.entry("batterycompartment_double_UNNU", "/Images/tiles/batterycompartment_double_UNNU.jpg"),
            Map.entry("batterycompartment_double_USDN", "/Images/tiles/batterycompartment_double_USDN.jpg"),
            Map.entry("batterycompartment_double_USSS", "/Images/tiles/batterycompartment_double_USSS.jpg"),
            Map.entry("batterycompartment_triple_DDSN", "/Images/tiles/batterycompartment_triple_DDSN.jpg"),
            Map.entry("batterycompartment_triple_DNNN", "/Images/tiles/batterycompartment_triple_DNNN.jpg"),
            Map.entry("batterycompartment_triple_DSNN", "/Images/tiles/batterycompartment_triple_DSNN.jpg"),
            Map.entry("batterycompartment_triple_NNNS", "/Images/tiles/batterycompartment_triple_NNNS.jpg"),
            Map.entry("batterycompartment_triple_SNDN", "/Images/tiles/batterycompartment_triple_SNDN.jpg"),
            Map.entry("batterycompartment_triple_SSDN", "/Images/tiles/batterycompartment_triple_SSDN.jpg"),
            Map.entry("BatteryType", "/Images/tiles/GT-new_tiles_16_for web35.jpg"),
            Map.entry("BlueCargo", "/Images/tiles/GT-new_tiles_16_for web36.jpg"),
            Map.entry("Cabin", "/Images/tiles/GT-new_tiles_16_for web37.jpg"),
            Map.entry("Cannon", "/Images/tiles/GT-new_tiles_16_for web38.jpg"),
            Map.entry("Cargo", "/Images/tiles/GT-new_tiles_16_for web39.jpg"),
            Map.entry("CargoHolds", "/Images/tiles/GT-new_tiles_16_for web40.jpg"),
            Map.entry("CargoHoldsSize", "/Images/tiles/GT-new_tiles_16_for web41.jpg"),
            Map.entry("Component", "/Images/tiles/GT-new_tiles_16_for web42.jpg"),
            Map.entry("Connector", "/Images/tiles/GT-new_tiles_16_for web43.jpg"),
            Map.entry("ConnectorType", "/Images/tiles/GT-new_tiles_16_for web44.jpg"),
            Map.entry("DoubleCannon", "/Images/tiles/GT-new_tiles_16_for web45.jpg"),
            Map.entry("DoubleConnectorType", "/Images/tiles/GT-new_tiles_16_for web46.jpg"),
            Map.entry("DoubleEngine", "/Images/tiles/GT-new_tiles_16_for web47.jpg"),
            Map.entry("Engine", "/Images/tiles/GT-new_tiles_16_for web48.jpg"),
            Map.entry("GreenCargo", "/Images/tiles/GT-new_tiles_16_for web49.jpg"),
            Map.entry("LifeSupportSystem", "/Images/tiles/GT-new_tiles_16_for web50.jpg"),
            Map.entry("LifeSupportSystemType", "/Images/tiles/GT-new_tiles_16_for web51.jpg"),
            Map.entry("NormalCargoHolds", "/Images/tiles/GT-new_tiles_16_for web52.jpg"),
            Map.entry("NullConnectorType", "/Images/tiles/GT-new_tiles_16_for web53.jpg"),
            Map.entry("RedCargo", "/Images/tiles/GT-new_tiles_16_for web54.jpg"),
            Map.entry("ShieldGenerator", "/Images/tiles/GT-new_tiles_16_for web55.jpg"),
            Map.entry("SingleCannon", "/Images/tiles/GT-new_tiles_16_for web56.jpg"),
            Map.entry("SingleConnectorType", "/Images/tiles/GT-new_tiles_16_for web57.jpg"),
            Map.entry("SingleEngine", "/Images/tiles/GT-new_tiles_16_for web58.jpg"),
            Map.entry("SpecialCargoHolds", "/Images/tiles/GT-new_tiles_16_for web59.jpg"),
            Map.entry("StructuralModule", "/Images/tiles/GT-new_tiles_16_for web60.jpg"),
            Map.entry("UniversalConnectorType", "/Images/tiles/GT-new_tiles_16_for web61.jpg"),
            Map.entry("YellowCargo", "/Images/tiles/GT-new_tiles_16_for web62.jpg")
    );

    @FXML
    public void initialize() {
        setupBackground();
        setupGrid();
        setupDragAndDrop();
        setupClickHandlers();
        setupRemoveButton();
    }

    private void setupBackground() {
        Image bgImage = new Image(getClass().getResource("/Images/cardboard/cardboard-1b.jpg").toExternalForm());
        anchorPane.setBackground(new javafx.scene.layout.Background(
                new javafx.scene.layout.BackgroundImage(
                        bgImage,
                        javafx.scene.layout.BackgroundRepeat.NO_REPEAT,
                        javafx.scene.layout.BackgroundRepeat.NO_REPEAT,
                        BackgroundPosition.CENTER,
                        new javafx.scene.layout.BackgroundSize(
                                937, 679, false, false, true, false
                        )
                )
        ));
    }

    private void setupGrid() {
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                if (VALID_SQUARES[row][col]) {
                    double x = GRID_START_X + col * SQUARE_SIZE;
                    double y = GRID_START_Y + row * SQUARE_SIZE;
                    Rectangle rect = new Rectangle(x, y, SQUARE_SIZE, SQUARE_SIZE);
                    rect.setFill(Color.TRANSPARENT);
                    rect.setStroke(Color.WHITE);
                    rect.setStrokeWidth(2.0);
                    anchorPane.getChildren().add(rect);
                }
            }
        }
    }

    private void setupDragAndDrop() {
        cardImage.setOnDragDetected(event -> {
            if (cardImage.getImage() != null && currentComponent != null && !isRemoveMode) {
                Dragboard db = cardImage.startDragAndDrop(TransferMode.COPY);
                ClipboardContent content = new ClipboardContent();
                content.putImage(cardImage.getImage());
                db.setContent(content);
                event.consume();
            }
        });

        cardImage.setOnDragDone(event -> {
            if (event.getTransferMode() == TransferMode.COPY) {
                cardImage.setImage(null);
                currentComponent = null;
            }
            event.consume();
        });

        anchorPane.setOnDragOver(event -> {
            if (event.getGestureSource() != anchorPane && event.getDragboard().hasImage()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        anchorPane.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            
            if (db.hasImage() && currentComponent != null) {
                Position dropPosition = calculateGridPosition(event.getX(), event.getY());
                
                if (isValidDropPosition(dropPosition)) {
                    try {
                        context.getClient().placeComponent(currentComponent, dropPosition, context.getPlayerId());
                        success = true;
                        statusLabel.setText("Placing component...");
                    } catch (Exception e) {
                        statusLabel.setText("Failed to place: " + e.getMessage());
                    }
                } else {
                    statusLabel.setText("Invalid position or position occupied!");
                }
            }
            
            event.setDropCompleted(success);
            event.consume();
        });
    }

    private void setupClickHandlers() {
        anchorPane.setOnMouseClicked(event -> {
            if (event.getTarget() == anchorPane) {
                clearSelection();
            }
        });
    }

    private void setupRemoveButton() {
        Button removeButton = new Button("Remove Mode");
        removeButton.setLayoutX(10);
        removeButton.setLayoutY(10);
        removeButton.setPrefWidth(120);
        
        removeButton.setOnAction(event -> {
            isRemoveMode = !isRemoveMode;
            if (isRemoveMode) {
                removeButton.setText("Exit Remove Mode");
                removeButton.setStyle("-fx-background-color: #ff6666;");
                statusLabel.setText("Remove mode: Click a component to remove it");
                clearSelection();
            } else {
                removeButton.setText("Remove Mode");
                removeButton.setStyle("");
                statusLabel.setText("Ready");
            }
        });
        
        anchorPane.getChildren().add(removeButton);
    }

    @FXML
    public void pickComponent() {
        if (context == null || context.getClient() == null) {
            showErrorDialog("Error", "No connection available");
            return;
        }

        if (currentComponent != null) {
            statusLabel.setText("You already have a component. Place it first!");
            return;
        }

        try {
            statusLabel.setText("Requesting component...");
            pickComponentButton.setDisable(true);
            
            Component picked = context.getClient().pickComponentCard(context.getPlayerId());
            
            
        } catch (Exception e) {
            pickComponentButton.setDisable(false);
            statusLabel.setText("Failed: " + e.getMessage());
            showErrorDialog("Pick Failed", e.getMessage());
        }
    }

    @Override
    public void gameComponentPicked(Component component) {
        pickComponentButton.setDisable(false);
        
        if (component == null) {
            statusLabel.setText("No components available");
            return;
        }

        currentComponent = component;
        String imagePath = getComponentImgPath(component);
        
        if (imagePath != null) {
            InputStream imageStream = getClass().getResourceAsStream(imagePath);
            if (imageStream != null) {
                cardImage.setImage(new Image(imageStream));
                statusLabel.setText("Picked: " + component.getClass().getSimpleName() + " - Drag to place");
            } else {
                statusLabel.setText("Picked: " + component.getClass().getSimpleName() + " (no image)");
            }
        } else {
            statusLabel.setText("Picked: " + component.getClass().getSimpleName() + " (unmapped)");
        }
    }

    @FXML
    public void rotateCardClockwise() {
        if (selectedImageView == null) {
            statusLabel.setText("Select a component on the ship first");
            return;
        }

        selectedImageView.setRotate(selectedImageView.getRotate() + 90);
        statusLabel.setText("Rotated clockwise (visual only)");
    }

    @FXML
    public void rotateCardAntiClockwise() {
        if (selectedImageView == null) {
            statusLabel.setText("Select a component on the ship first");
            return;
        }

        selectedImageView.setRotate(selectedImageView.getRotate() - 90);
        statusLabel.setText("Rotated counter-clockwise (visual only)");
    }

    @FXML
    public void validateShip() {
        try {
            context.getClient().validateShip();
            statusLabel.setText("Validating ship...");
        } catch (Exception e) {
            showErrorDialog("Validation Failed", e.getMessage());
        }
    }

    private Position calculateGridPosition(double x, double y) {
        int col = (int) ((x - GRID_START_X) / SQUARE_SIZE);
        int row = (int) ((y - GRID_START_Y) / SQUARE_SIZE);
        return new Position(col, row);
    }

    private boolean isValidDropPosition(Position pos) {
        return pos.getX() >= 0 && pos.getX() < GRID_COLS &&
               pos.getY() >= 0 && pos.getY() < GRID_ROWS &&
               VALID_SQUARES[pos.getY()][pos.getX()] &&
               !cellViews.containsKey(pos);
    }

    private void selectImageView(ImageView imageView) {
        clearSelection();
        
        selectedImageView = imageView;
        selectedHighlight = new Rectangle(
                imageView.getLayoutX(), imageView.getLayoutY(),
                SQUARE_SIZE, SQUARE_SIZE
        );
        selectedHighlight.setFill(Color.TRANSPARENT);
        selectedHighlight.setStroke(Color.YELLOWGREEN);
        selectedHighlight.setStrokeWidth(7);
        selectedHighlight.setMouseTransparent(true);
        
        anchorPane.getChildren().add(selectedHighlight);
        imageView.toFront();
    }

    private void clearSelection() {
        if (selectedHighlight != null) {
            anchorPane.getChildren().remove(selectedHighlight);
            selectedHighlight = null;
        }
        selectedImageView = null;
    }

    private void handleComponentClick(ImageView imageView, Position position) {
        if (isRemoveMode) {
            try {
                context.getClient().removeComponent(position, context.getPlayerId());
                statusLabel.setText("Removing component...");
            } catch (Exception e) {
                statusLabel.setText("Remove failed: " + e.getMessage());
            }
        } else {
            selectImageView(imageView);
        }
    }

    private String getComponentImgPath(Component c) {
        if (c == null) return null;
        return COMP_IMAGE_MAP.get(c.getComponentString());
    }

    @Override
    public void gameStateUpdate() {
        updateButtonStates();
    }

    @Override
    public void gameComponentPlaced(Component component, Position position) {
        String imagePath = getComponentImgPath(component);
        if (imagePath == null) return;

        InputStream imageStream = getClass().getResourceAsStream(imagePath);
        if (imageStream == null) return;

        ImageView placed = new ImageView(new Image(imageStream));
        placed.setFitWidth(SQUARE_SIZE);
        placed.setFitHeight(SQUARE_SIZE);
        placed.setLayoutX(GRID_START_X + position.getX() * SQUARE_SIZE);
        placed.setLayoutY(GRID_START_Y + position.getY() * SQUARE_SIZE);
        placed.setOnMouseClicked(event -> handleComponentClick(placed, position));

        anchorPane.getChildren().add(placed);
        cellViews.put(position, placed);
        shipComponents.put(position, component);

        statusLabel.setText("Component placed successfully!");
        currentComponent = null;
        cardImage.setImage(null);
    }

    @Override
    public void gameComponentRemoved(Position position) {
        ImageView removed = cellViews.remove(position);
        if (removed != null) {
            anchorPane.getChildren().remove(removed);
            shipComponents.remove(position);
            clearSelection();
            statusLabel.setText("Component removed!");
        }
    }

    @Override
    public void gameComponentRotated(Position position, RotationType rotationType) {
        ImageView imageView = cellViews.get(position);
        if (imageView != null) {
            double delta = switch (rotationType) {
                case CLOCKWISE -> 90;
                case ANTI_CLOCKWISE -> -90;
                case OPPOSITE -> 180;
            };
            imageView.setRotate(imageView.getRotate() + delta);
            statusLabel.setText("Component rotated");
        }
    }

    @Override
    public void gameShipValidated(boolean isValid, String message) {
        statusLabel.setText(isValid ? "Ship valid! " + message : "Invalid: " + message);
    }

    @Override
    public void gameAvailablePositionsUpdate(List<Integer> positions) {

    }

    private void updateButtonStates() {
        pickComponentButton.setDisable(currentComponent != null);
        validateShipButton.setDisable(shipComponents.isEmpty());
        rotateClockwiseButton.setDisable(selectedImageView == null);
        rotateCounterClockwiseButton.setDisable(selectedImageView == null);
    }

    public void setContext(AppContext context) {
        if (this.context != null) {
            this.context.removeGameStateUpdateListener(this);
        }

        this.context = context;

        if (context != null) {
            context.addGameStateUpdateListener(this);
        }
    }

    public ShipBuilderController getController() {
        return this;
    }
}