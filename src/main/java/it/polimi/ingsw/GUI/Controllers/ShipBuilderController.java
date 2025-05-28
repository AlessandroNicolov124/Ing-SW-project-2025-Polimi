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
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
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

    private AppContext context;
    private Component component;
    private Position selectedPosition;
    private final Map<Position, ImageView> cellViews = new HashMap<>();

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

    private void updateButtonStates() {
        // implement logic to enable/disable buttons based on the current state
    }

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
        
        if (selectedPosition != null) {
            try {
                System.out.println("[ShipBuilder] Sending clockwise rotation request for position: " + selectedPosition);
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
        
        if (selectedPosition != null) {
            try {
                System.out.println("[ShipBuilder] Sending counter-clockwise rotation request for position: " + selectedPosition);
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

    /*private void addDraggablePiece(String imagePath, double x, double y) {
        ImageView piece = new ImageView(new Image(getClass().getResourceAsStream(imagePath)));
        piece.setFitWidth(64);
        piece.setFitHeight(64);
        piece.setLayoutX(x);
        piece.setLayoutY(y);

        // Enable drag-and-drop
        DragContext dragContext = new DragContext();
        piece.setOnMousePressed(event -> {
            dragContext.mouseAnchorX = event.getSceneX();
            dragContext.mouseAnchorY = event.getSceneY();
            dragContext.initialX = piece.getLayoutX();
            dragContext.initialY = piece.getLayoutY();
            piece.toFront(); // Bring the piece to front when dragging
        });

        piece.setOnMouseDragged(event -> {
            double offsetX = event.getSceneX() - dragContext.mouseAnchorX;
            double offsetY = event.getSceneY() - dragContext.mouseAnchorY;
            piece.setLayoutX(dragContext.initialX + offsetX);
            piece.setLayoutY(dragContext.initialY + offsetY);
        });

        anchorPane.getChildren().add(piece);
    }

    // Helper class to keep drag context per piece
    private static class DragContext {
        double mouseAnchorX;
        double mouseAnchorY;
        double initialX;
        double initialY;
    }*/

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