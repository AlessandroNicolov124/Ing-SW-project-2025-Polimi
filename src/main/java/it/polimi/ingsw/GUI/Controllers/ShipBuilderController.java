package it.polimi.ingsw.GUI.Controllers;

import it.polimi.ingsw.Position;
import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.GUI.AppContext;
import it.polimi.ingsw.GUI.GameStateUpdateListener.GameStateUpdateListener;
import it.polimi.ingsw.network.socket.messages.RotationType;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShipBuilderController implements GameStateUpdateListener {
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private ImageView cardImage;
    @FXML
    private Button pickComponentButton;
    @FXML
    private Button rotateClockwiseButton;
    @FXML
    private Button rotateCounterClockwiseButton;
    @FXML
    private Button validateShipButton;
    @FXML
    private Label statusLabel;


    private ImageView selectedImageView = null;
    private Rectangle selectedHighlight = null; // For visual feedback

    private AppContext context;
    private Component component;
    private Position selectedPosition;
    private final Map<Position, ImageView> cellViews = new HashMap<>();

    // level 2 board related measures [px]
    private static final int SQUARE_SIZE = 123; // width/height of a square (pixels)
    private static final int GRID_ROWS = 5;    // number of rows
    private static final int GRID_COLS = 7;    // number of columns
    private static final double GRID_START_X = 38; // pixel position where leftmost valid square starts
    private static final double GRID_START_Y = 31; // pixel position where topmost valid square starts

    // true means valid square, false means no square
    private static final boolean[][] VALID_SQUARES = {
            {false, false, true,  false, true,  false, false},
            {false, true,  true,  true,  true,  true,  false},
            {true,  true,  true,  true,  true,  true,  true },
            {true,  true,  true,  true,  true,  true,  true },
            {true, true,  true,  false, true,  true,  true },
    };

    private final Map<String, String> COMP_IMAGE_MAP = Map.ofEntries(
            Map.entry("BatteryCompartment", "/Images/tiles/GT-new_tiles_16_for web34.jpg"),
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
        // Set background image (replace with your path)
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

        anchorPane.setOnMouseClicked(event -> {
            // Only clear selection if clicking NOT on an image
            if (event.getTarget() == anchorPane) {
                if (selectedHighlight != null) {
                    anchorPane.getChildren().remove(selectedHighlight);
                    selectedHighlight = null;
                }
                selectedImageView = null;
            }
        });

        // Draw only valid squares
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

        // Drag-and-drop logic
        cardImage.setOnDragDetected(event -> {
            if (cardImage.getImage() != null) {
                Dragboard db = cardImage.startDragAndDrop(TransferMode.COPY);
                ClipboardContent content = new ClipboardContent();
                content.putImage(cardImage.getImage());
                db.setContent(content);
            }
            event.consume();
        });

        anchorPane.setOnDragOver(event -> {
            if (event.getGestureSource() != anchorPane && event.getDragboard().hasImage()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });

        anchorPane.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasImage()) {
                // Snap to nearest grid square
                int col = (int) ((event.getX() - GRID_START_X) / SQUARE_SIZE);
                int row = (int) ((event.getY() - GRID_START_Y) / SQUARE_SIZE);

                // Check bounds
                if (row >= 0 && row < GRID_ROWS && col >= 0 && col < GRID_COLS && VALID_SQUARES[row][col]) {
                    double snappedX = GRID_START_X + col * SQUARE_SIZE;
                    double snappedY = GRID_START_Y + row * SQUARE_SIZE;

                    ImageView dropped = new ImageView(db.getImage());
                    dropped.setFitWidth(SQUARE_SIZE);
                    dropped.setFitHeight(SQUARE_SIZE);
                    dropped.setLayoutX(snappedX);
                    dropped.setLayoutY(snappedY);
                    dropped.getProperties().put("row", row);
                    dropped.getProperties().put("col", col);
                    dropped.setOnMouseClicked(mouseclick -> {
                        selectImageView(dropped);
                    });
                    anchorPane.getChildren().add(dropped);


                    Position newPosition = new Position(row, col);
                    try {
                        context.getClient().placeComponent(this.component, newPosition);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        this.showErrorDialog("Error", "Communicatiion error, try again");
                    }
                    this.component = null;
                    //cardImage = null;
                    success = true;
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    private void showErrorDialog(String title, String message) {
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
            System.err.println("Failed to show error dialog: " + e.getMessage());
        }
    }

    private void selectImageView(ImageView imageView) {
        if (selectedHighlight != null) {
            anchorPane.getChildren().remove(selectedHighlight);
        }
        selectedImageView = imageView;
        selectedHighlight = new Rectangle(
                imageView.getLayoutX(), imageView.getLayoutY(),
                SQUARE_SIZE, SQUARE_SIZE
        );
        selectedHighlight.setFill(Color.TRANSPARENT);
        selectedHighlight.setStroke(Color.YELLOWGREEN);
        selectedHighlight.setStrokeWidth(7);
        anchorPane.getChildren().add(selectedHighlight);
        selectedHighlight.toBack(); // so it doesn't cover the image
    }

    private void updateButtonStates() {
        // implement logic to enable/disable buttons based on the current state
    }

    public void setContext(AppContext context) {
        if (this.context != null){
            this.context.removeGameStateUpdateListener(this);
        }

        this.context = context;

        if (context != null) {
            context.addGameStateUpdateListener(this);
        }
    }

    @FXML
    public void rotateCardClockwise(){
        System.out.println("[ShipBuilder] Rotate clockwise button clicked");
        
        if (context == null) {
            System.err.println("[ShipBuilder] Context is null!");
            statusLabel.setText("Error: No connection to server");
            return;
        }
        
        if (context.getClient() == null) {
            System.err.println("[ShipBuilder] Client is null!");
            statusLabel.setText("Error: No client connection");
            return;
        }

        if (selectedImageView != null)
        {
            int row = (int) selectedImageView.getProperties().get("row");
            int col = (int) selectedImageView.getProperties().get("col");
            selectedPosition = new Position(row, col);
        }
        
        if (selectedPosition != null) {
            try {
                System.out.println("[ShipBuilder] Sending clockwise rotation request for position: " + selectedPosition);
                selectedImageView.setRotate(selectedImageView.getRotate() + 90);
                context.getClient().rotateComponent(selectedPosition, RotationType.CLOCKWISE);
                context.getClient().rotateComponent(selectedPosition, RotationType.CLOCKWISE);
                statusLabel.setText("Rotating component clockwise...");
            } catch (Exception e) {
                System.err.println("[ShipBuilder] Error during clockwise rotation: " + e.getMessage());
                showErrorDialog("Rotation Failed", "Failed to rotate component: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("[ShipBuilder] No position selected for rotation");
            statusLabel.setText("Please select a component on the ship first");
        }
    }

    @FXML
    public void rotateCardAntiClockwise(){
        System.out.println("[ShipBuilder] Rotate counter-clockwise button clicked");
        
        if (context == null) {
            System.err.println("[ShipBuilder] Context is null!");
            statusLabel.setText("Error: No connection to server");
            return;
        }
        
        if (context.getClient() == null) {
            System.err.println("[ShipBuilder] Client is null!");
            statusLabel.setText("Error: No client connection");
            return;
        }

        if (selectedImageView != null)
        {
            int row = (int) selectedImageView.getProperties().get("row");
            int col = (int) selectedImageView.getProperties().get("col");
            selectedPosition = new Position(row, col);
        }

            if (selectedPosition != null) {
            try {
                System.out.println("[ShipBuilder] Sending counter-clockwise rotation request for position: " + selectedPosition);
                selectedImageView.setRotate(selectedImageView.getRotate() - 90);
                context.getClient().rotateComponent(selectedPosition, RotationType.ANTI_CLOCKWISE);

                statusLabel.setText("Rotating component counter-clockwise...");
            } catch (Exception e) {
                System.err.println("[ShipBuilder] Error during counter-clockwise rotation: " + e.getMessage());
                showErrorDialog("Rotation Failed", "Failed to rotate component: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("[ShipBuilder] No position selected for rotation");
            statusLabel.setText("Please select a component on the ship first");
        }
    }

    @FXML
    public void pickComponent() {
        System.out.println("[ShipBuilder] Pick component button clicked");
        
        if (context == null) {
            System.err.println("[ShipBuilder] Context is null!");
            showErrorDialog("Component pick failed", "No connection context available");
            return;
        }
        
        if (context.getClient() == null) {
            System.err.println("[ShipBuilder] Client is null!");
            showErrorDialog("Component pick failed", "No client connection available");
            return;
        }
        
        try {
            System.out.println("[ShipBuilder] Requesting component from server...");
            this.component = context.getClient().pickComponentCard();
            
            if (this.component == null) {
                throw new IllegalStateException("Server returned null component");
            }
            
            System.out.println("[ShipBuilder] Received component: " + this.component.getClass().getSimpleName());

            String path = getComponentImgPath(this.component);
            if (path == null) {
                System.err.println("[ShipBuilder] No image mapping found for: " + this.component.getClass().getSimpleName());
                statusLabel.setText("Picked: " + this.component.getClass().getSimpleName() + " (no image)");
                updateButtonStates();
                return;
            }

            InputStream is = getClass().getResourceAsStream(path);
            if (is == null) {
                System.err.println("[ShipBuilder] Image not found in resources: " + path);
                statusLabel.setText("Picked: " + this.component.getClass().getSimpleName() + " (image missing)");
                updateButtonStates();
                return;
            }

            cardImage.setImage(new Image(is));
            statusLabel.setText("Picked: " + this.component.getClass().getSimpleName());
            updateButtonStates();
            
            System.out.println("[ShipBuilder] Component picked successfully");
            
        } catch (Exception e) {
            System.err.println("[ShipBuilder] Error picking component: " + e.getMessage());
            e.printStackTrace();
            showErrorDialog("Component pick failed", "Error: " + e.getMessage());
            
            this.component = null;
            if (cardImage != null) {
                cardImage.setImage(null);
            }
            statusLabel.setText("Failed to pick component");
        }
    }

    @FXML
    public void validateShip() {
        System.out.println("[ShipBuilder] Validate ship button clicked");
        
        if (context == null || context.getClient() == null) {
            showErrorDialog("Validation Failed", "No connection to server");
            return;
        }
        
        try {
            context.getClient().validateShip();
            statusLabel.setText("Validating ship...");
        } catch (Exception e) {
            System.err.println("[ShipBuilder] Error during ship validation: " + e.getMessage());
            showErrorDialog("Validation Failed", "Failed to validate ship: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void placeComponentAt(Position position) {
        if (component != null) {
            try {
                context.getClient().placeComponent(component, position);
                statusLabel.setText("Placing component at (" + position.getX() + ", " + position.getY() + ")...");
            } catch (Exception e) {
                showErrorDialog("Placement Failed", "Failed to place component: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            statusLabel.setText("Please pick a component first");
        }
    }

    public void removeComponentAt(Position position) {
        try {
            // implement method on client 
            statusLabel.setText("Removing component at (" + position.getX() + ", " + position.getY() + ")...");
        } catch (Exception e) {
            showErrorDialog("Removal Failed", "Failed to remove component: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String getComponentImgPath(Component c){
        if (c == null) return null;
        return COMP_IMAGE_MAP.get(c.getClass().getSimpleName());
    }

    private ImageView createImageViewFor(Component component) {
        String path = getComponentImgPath(component);
        if (path != null) {
            try {
                InputStream is = getClass().getResourceAsStream(path);
                if (is != null) {
                    ImageView iv = new ImageView(new Image(is));
                    iv.setFitWidth(50);
                    iv.setFitHeight(50);
                    return iv;
                }
            } catch (Exception e) {
                System.err.println("Error creating ImageView: " + e.getMessage());
            }
        }
        
        ImageView iv = new ImageView();
        iv.setFitWidth(50);
        iv.setFitHeight(50);
        return iv;
    }

    public ShipBuilderController getController() {
        return this;
    }

    @Override
    public void gameStateUpdate() {
        System.out.println("[ShipBuilder] Game state updated");
        updateButtonStates();
    }

    @Override
    public void gameComponentPlaced(Component component, Position position) {
        System.out.println("[ShipBuilder] Component placed: " + component.getClass().getSimpleName() + " at " + position);
        statusLabel.setText("Component placed successfully!");
        
        if (component.equals(this.component)) {
            this.component = null;
            cardImage.setImage(null);
            ImageView iv = createImageViewFor(component);
            anchorPane.getChildren().add(iv);
            cellViews.put(position, iv);
            updateButtonStates();
        }
        
        refreshShipDisplay();
    }

    @Override
    public void gameComponentRemoved(Position position) {
        System.out.println("[ShipBuilder] Component removed from: " + position);
        statusLabel.setText("Component removed successfully!");
        
        ImageView iv = cellViews.remove(position);
        if (iv != null) {
            anchorPane.getChildren().remove(iv);
        }
        
        refreshShipDisplay();
    }

    @Override
    public void gameComponentRotated(Position position, RotationType rotationType) {
        System.out.println("[ShipBuilder] Component rotated at position: " + position + " - " + rotationType);
        
        ImageView iv = cellViews.get(position);
        if (iv != null) {
            double delta = switch (rotationType) {
                case CLOCKWISE ->  90;
                case ANTI_CLOCKWISE -> -90;
                case OPPOSITE -> 180;
            };
            iv.setRotate(iv.getRotate() + delta);
            System.out.println("[ShipBuilder] Visual rotation applied: " + delta + " degrees");
        } else {
            System.out.println("[ShipBuilder] No ImageView found for position: " + position);
        }
        
        statusLabel.setText("Component rotated " + rotationType.toString().toLowerCase().replace("_", " "));
    }

    @Override
    public void gameShipValidated(boolean isValid, String message) {
        System.out.println("[ShipBuilder] Ship validation: " + isValid + " - " + message);
        if (isValid) {
            statusLabel.setText("Ship is valid! " + message);
        } else {
            statusLabel.setText("Ship validation failed: " + message);
        }
    }

    @Override
    public void gameAvailablePositionsUpdate(List<Integer> positions) {
        System.out.println("[ShipBuilder] Available positions updated: " + positions);
        // implement logic to update available positions in the UI if needed
    }

    private void refreshShipDisplay() {
        System.out.println("[ShipBuilder] Refreshing ship display");
        // implement logic to refresh the ship display
    }

    public void setSelectedPosition(Position position) {
        this.selectedPosition = position;
        updateButtonStates();
        if (position != null) {
            System.out.println("[ShipBuilder] Selected position: " + position);
            statusLabel.setText("Selected position: (" + position.getX() + ", " + position.getY() + ")");
        } else {
            System.out.println("[ShipBuilder] Position deselected");
            statusLabel.setText("No position selected");
        }
    }
}