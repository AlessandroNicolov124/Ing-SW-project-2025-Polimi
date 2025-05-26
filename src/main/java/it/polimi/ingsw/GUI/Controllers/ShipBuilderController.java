package it.polimi.ingsw.GUI.Controllers;

import it.polimi.ingsw.GUI.AppContext;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

public class ShipBuilderController {
    @FXML
    private AnchorPane anchorPane;

    private AppContext context;

    @FXML

    public void setContext(AppContext context) {
        this.context = context;
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
