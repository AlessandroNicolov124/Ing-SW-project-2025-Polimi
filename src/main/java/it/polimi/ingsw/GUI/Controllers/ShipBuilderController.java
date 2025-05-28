package it.polimi.ingsw.GUI.Controllers;

import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.GUI.AppContext;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.Map;
import java.util.Objects;

public class ShipBuilderController {
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private ImageView cardImage;

    private AppContext context;
    private Component component;


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
        this.context = context;
    }

    public void rotateCardClockwise(){

    }

    public void rotateCardAntiClockwise(){

    }

    public void pickComponent(){
        try{

            this.component = context.getClient().pickComponentCard();
            String path = getComponentImgPath(this.component);
            Image compImg = new Image(Objects.requireNonNull(getClass().getResourceAsStream(path)));
            this.cardImage.setImage(compImg);
        }
        catch(Exception e){
            final Stage dialog = new Stage();
            VBox dialogVbox = new VBox(20);
            dialogVbox.getChildren().add(new Text("Something went wrong, please try again."));
            Scene dialogScene = new Scene(dialogVbox, 150, 150);
            dialog.setScene(dialogScene);
            dialog.show();

            e.printStackTrace();
        }
    }

    private String getComponentImgPath(Component c){
        return COMP_IMAGE_MAP.get(c.getClass().getSimpleName());
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
}
