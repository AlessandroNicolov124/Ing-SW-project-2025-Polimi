package it.polimi.ingsw.GUI.Controllers;

import it.polimi.ingsw.Position;
import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.GUI.AppContext;
import it.polimi.ingsw.GUI.GameStateUpdateListener.GameStateUpdateListener;
import it.polimi.ingsw.network.messages.RotationType;
import javafx.application.Platform;
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
import java.util.UUID;

public class ShipBuilderController extends GuiContoller implements GameStateUpdateListener {
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

    // Component management
    private Component currentComponent = null;
    private boolean isRemoveMode = false;

    // Visual elements tracking
    private ImageView selectedImageView = null;
    //private ImageView imageViewToBeRemoved = null;
    private Rectangle selectedHighlight = null;
    private final Map<Position, ImageView> cellViews = new HashMap<>();
    private final Map<Position, Component> shipComponents = new HashMap<>();

    private boolean isReadyingForFlight = false;

    // Board configuration
    private static final int SQUARE_SIZE = 123;
    private static final int GRID_ROWS = 5;
    private static final int GRID_COLS = 7;
    private static final double GRID_START_X = 38;
    private static final double GRID_START_Y = 31;

    private static final int STASH_CARDS = 2;
    private static final double GRIST_STASH_CARDS_START_X = GRID_START_X + (5*SQUARE_SIZE) + 20;
    private static final double GRIST_STASH_CARDS_START_Y = GRID_START_Y - 8;;


    private static final boolean[][] VALID_SQUARES = {
            {false, false, true,  false, true,  false, false},
            {false, true,  true,  true,  true,  true,  false},
            {true,  true,  true,  true,  true,  true,  true },
            {true,  true,  true,  true,  true,  true,  true },
            {true, true,  true,  false, true,  true,  true },
    };

    private final Map<String, String> COMP_IMAGE_MAP = Map.ofEntries(
            Map.entry("cabin_starting_1", "/Images/tiles/Cabin_Starting_1.jpg"), // RED
            Map.entry("cabin_starting_2", "/Images/tiles/Cabin_Starting_2.jpg"), // YELLOW
            Map.entry("cabin_starting_3", "/Images/tiles/Cabin_Starting_3.jpg"), // GREEN
            Map.entry("cabin_starting_4", "/Images/tiles/Cabin_Starting_4.jpg"), // BLUE

            Map.entry("lifesupportsystem_brown_DSNS", "/Images/tiles/lifesupportsystem_brown_DSNS.jpg"),
            Map.entry("lifesupportsystem_brown_NDNU", "/Images/tiles/lifesupportsystem_brown_NDNU.jpg"),
            Map.entry("lifesupportsystem_brown_NNNU", "/Images/tiles/lifesupportsystem_brown_NNNU.jpg"),
            Map.entry("lifesupportsystem_brown_NNSD", "/Images/tiles/lifesupportsystem_brown_NNSD.jpg"),
            Map.entry("lifesupportsystem_brown_SNNU", "/Images/tiles/lifesupportsystem_brown_SNNU.jpg"),
            Map.entry("lifesupportsystem_brown_SSNS", "/Images/tiles/lifesupportsystem_brown_SSNS.jpg"),

            Map.entry("lifesupportsystem_purple_DDND", "/Images/tiles/lifesupportsystem_purple_DDND.jpg"),
            Map.entry("lifesupportsystem_purple_DNNU", "/Images/tiles/lifesupportsystem_purple_DNNU.jpg"),
            Map.entry("lifesupportsystem_purple_NNDU", "/Images/tiles/lifesupportsystem_purple_NNDU.jpg"),
            Map.entry("lifesupportsystem_purple_NNNU", "/Images/tiles/lifesupportsystem_purple_NNNU.jpg"),
            Map.entry("lifesupportsystem_purple_NSNU", "/Images/tiles/lifesupportsystem_purple_NSNU.jpg"),
            Map.entry("lifesupportsystem_purple_SDND", "/Images/tiles/lifesupportsystem_purple_SDND.jpg"),


            Map.entry("cabin_DDNU", "/Images/tiles/cabin_DDNU.jpg"),
            Map.entry("cabin_DNDU", "/Images/tiles/cabin_DNDU.jpg"),
            Map.entry("cabin_DNNS", "/Images/tiles/cabin_DNNS.jpg"),
            Map.entry("cabin_DSDD", "/Images/tiles/cabin_DSDD.jpg"),
            Map.entry("cabin_DSDS", "/Images/tiles/cabin_DSDS.jpg"),
            Map.entry("cabin_DSNS", "/Images/tiles/cabin_DSNS.jpg"),
            Map.entry("cabin_NDNU", "/Images/tiles/cabin_NDNU.jpg"),
            Map.entry("cabin_NNUS", "/Images/tiles/cabin_NNUS.jpg"),
            Map.entry("cabin_NSNU", "/Images/tiles/cabin_NSNU.jpg"),
            Map.entry("cabin_NSSU", "/Images/tiles/cabin_NSSU.jpg"),
            Map.entry("cabin_NSUD", "/Images/tiles/cabin_NSUD.jpg"),
            Map.entry("cabin_SDDS", "/Images/tiles/cabin_SDDS.jpg"),
            Map.entry("cabin_SDND", "/Images/tiles/cabin_SDND.jpg"),
            Map.entry("cabin_SDSS", "/Images/tiles/cabin_SDSS.jpg"),
            Map.entry("cabin_SNDU", "/Images/tiles/cabin_SNDU.jpg"),
            Map.entry("cabin_SNSU", "/Images/tiles/cabin_SNSU.jpg"),
            Map.entry("cabin_UNND", "/Images/tiles/cabin_UNND.jpg"),

            Map.entry("batterycompartment_double_DSDS", "/Images/tiles/batterycompartment_double_DSDS.jpg"),
            Map.entry("batterycompartment_double_NNNU", "/Images/tiles/batterycompartment_double_NNNU.jpg"),
            Map.entry("batterycompartment_double_NNNU_2","/Images/tiles/batterycompartment_double_NNNU_2.jpg"),
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

            Map.entry("normalcargoholds_double_DSUN", "/Images/tiles/normalcargoholds_double_DSUN.jpg"),
            Map.entry("normalcargoholds_double_DSUS", "/Images/tiles/normalcargoholds_double_DSUS.jpg"),
            Map.entry("normalcargoholds_double_DUNS", "/Images/tiles/normalcargoholds_double_DUNS.jpg"),
            Map.entry("normalcargoholds_double_NNNU", "/Images/tiles/normalcargoholds_double_NNNU.jpg"),
            Map.entry("normalcargoholds_double_NNNU_2", "/Images/tiles/normalcargoholds_double_NNNU_2.jpg"),
            Map.entry("normalcargoholds_double_NSNU", "/Images/tiles/normalcargoholds_double_NSNU.jpg"),
            Map.entry("normalcargoholds_double_NDNU", "/Images/tiles/normalcargoholds_double_NDNU.jpg"),
            Map.entry("normalcargoholds_double_DSDU", "/Images/tiles/normalcargoholds_double_DSDU.jpg"),
            Map.entry("normalcargoholds_double_UNNU", "/Images/tiles/normalcargoholds_double_UNNU.jpg"),
            Map.entry("normalcargoholds_triple_NNNS", "/Images/tiles/normalcargoholds_triple_NNNS.jpg"),
            Map.entry("normalcargoholds_triple_SNSN", "/Images/tiles/normalcargoholds_triple_SNSN.jpg"),
            Map.entry("normalcargoholds_triple_NNND", "/Images/tiles/normalcargoholds_triple_NNND.jpg"),
            Map.entry("normalcargoholds_triple_DNDN", "/Images/tiles/normalcargoholds_triple_DNDN.jpg"),
            Map.entry("normalcargoholds_triple_SNDS", "/Images/tiles/normalcargoholds_triple_SNDS.jpg"),
            Map.entry("normalcargoholds_triple_DNSD", "/Images/tiles/normalcargoholds_triple_DNSD.jpg"),

            Map.entry("specialcargoholds_single_DDDU", "/Images/tiles/specialcargoholds_single_DDDU.jpg"),
            Map.entry("specialcargoholds_single_DSNU", "/Images/tiles/specialcargoholds_single_DSNU.jpg"),
            Map.entry("specialcargoholds_single_NDSU", "/Images/tiles/specialcargoholds_single_NDSU.jpg"),
            Map.entry("specialcargoholds_single_NUNU", "/Images/tiles/specialcargoholds_single_NUNU.jpg"),
            Map.entry("specialcargoholds_single_UNNU", "/Images/tiles/specialcargoholds_single_UNNU.jpg"),
            Map.entry("specialcargoholds_single_SSSU", "/Images/tiles/specialcargoholds_single_SSSU.jpg"),
            Map.entry("specialcargoholds_double_NDNS", "/Images/tiles/specialcargoholds_double_NDNS.jpg"),
            Map.entry("specialcargoholds_double_NNND", "/Images/tiles/specialcargoholds_double_NNND.jpg"),
            Map.entry("specialcargoholds_double_NNNS", "/Images/tiles/specialcargoholds_double_NNNS.jpg"),

            Map.entry("structuralmodule_SUDU", "/Images/tiles/structuralmodule_SUDU.jpg"),
            Map.entry("structuralmodule_SUNU", "/Images/tiles/structuralmodule_SUNU.jpg"),
            Map.entry("structuralmodule_SUSU", "/Images/tiles/structuralmodule_SUSU.jpg"),
            Map.entry("structuralmodule_UDDU", "/Images/tiles/structuralmodule_UDDU.jpg"),
            Map.entry("structuralmodule_UDNU", "/Images/tiles/structuralmodule_UDNU.jpg"),
            Map.entry("structuralmodule_USDU", "/Images/tiles/structuralmodule_USDU.jpg"),
            Map.entry("structuralmodule_UUNS", "/Images/tiles/structuralmodule_UUNS.jpg"),

            Map.entry("shieldgenerator_NSUS", "/Images/tiles/shieldgenerator_NSUS.jpg"),
            Map.entry("shieldgenerator_DSDS", "/Images/tiles/shieldgenerator_DSDS.jpg"),
            Map.entry("shieldgenerator_NDDD", "/Images/tiles/shieldgenerator_NDDD.jpg"),
            Map.entry("shieldgenerator_NDDU", "/Images/tiles/shieldgenerator_NDDU.jpg"),
            Map.entry("shieldgenerator_NNSU", "/Images/tiles/shieldgenerator_NNSU.jpg"),
            Map.entry("shieldgenerator_NNUD", "/Images/tiles/shieldgenerator_NNUD.jpg"),
            Map.entry("shieldgenerator_SDSD", "/Images/tiles/shieldgenerator_SDSD.jpg"),
            Map.entry("shieldgenerator_SNSS", "/Images/tiles/shieldgenerator_SNSS.jpg"),

            Map.entry("doubleengine_DDND", "/Images/tiles/doubleengine_DDND.jpg"),
            Map.entry("doubleengine_DNNN", "/Images/tiles/doubleengine_DNNN.jpg"),
            Map.entry("doubleengine_DNNU", "/Images/tiles/doubleengine_DNNU.jpg"),
            Map.entry("doubleengine_NUNU", "/Images/tiles/doubleengine_NUNU.jpg"),
            Map.entry("doubleengine_SSNS", "/Images/tiles/doubleengine_SSNS.jpg"),
            Map.entry("doubleengine_SNNN", "/Images/tiles/doubleengine_SNNN.jpg"),
            Map.entry("doubleengine_SUNN", "/Images/tiles/doubleengine_SUNN.jpg"),
            Map.entry("doubleengine_UNND", "/Images/tiles/doubleengine_UNND.jpg"),
            Map.entry("doubleengine_USNN", "/Images/tiles/doubleengine_USNN.jpg"),

            Map.entry("singleengine_DNND", "/Images/tiles/singleengine_DNND.jpg"),
            Map.entry("singleengine_DNNN", "/Images/tiles/singleengine_DNNN.jpg"),
            Map.entry("singleengine_DNNN_2", "/Images/tiles/singleengine_DNNN_2.jpg"),
            Map.entry("singleengine_DSNS", "/Images/tiles/singleengine_DSNS.jpg"),
            Map.entry("singleengine_DUNN", "/Images/tiles/singleengine_DUNN.jpg"),
            Map.entry("singleengine_NDNU", "/Images/tiles/singleengine_NDNU.jpg"),
            Map.entry("singleengine_NNNU", "/Images/tiles/singleengine_NNNU.jpg"),
            Map.entry("singleengine_NNNU_2", "/Images/tiles/singleengine_NNNU_2.jpg"),
            Map.entry("singleengine_NSND", "/Images/tiles/singleengine_NSND.jpg"),
            Map.entry("singleengine_NUNN", "/Images/tiles/singleengine_NUNN.jpg"),
            Map.entry("singleengine_NUNN_2", "/Images/tiles/singleengine_NUNN_2.jpg"),
            Map.entry("singleengine_NUNS", "/Images/tiles/singleengine_NUNS.jpg"),
            Map.entry("singleengine_SDND", "/Images/tiles/singleengine_SDND.jpg"),
            Map.entry("singleengine_SNNN", "/Images/tiles/singleengine_SNNN.jpg"),
            Map.entry("singleengine_SNNN_2", "/Images/tiles/singleengine_SNNN_2.jpg"),
            Map.entry("singleengine_SNNU", "/Images/tiles/singleengine_SNNU.jpg"),
            Map.entry("singleengine_SSNN", "/Images/tiles/singleengine_SSNN.jpg"),
            Map.entry("singleengine_UDNN", "/Images/tiles/singleengine_UDNN.jpg"),
            Map.entry("singleengine_UDNS", "/Images/tiles/singleengine_UDNS.jpg"),
            Map.entry("singleengine_UNNS", "/Images/tiles/singleengine_UNNS.jpg"),
            Map.entry("singleengine_USND", "/Images/tiles/singleengine_USND.jpg"),


            Map.entry("doublecannon_NDNS", "/Images/tiles/doublecannon_NDNS.jpg"),
            Map.entry("doublecannon_NDSD", "/Images/tiles/doublecannon_NDSD.jpg"),
            Map.entry("doublecannon_NNDN", "/Images/tiles/doublecannon_NNDN.jpg"),
            Map.entry("doublecannon_NNNU", "/Images/tiles/doublecannon_NNNU.jpg"),
            Map.entry("doublecannon_NNSN", "/Images/tiles/doublecannon_NNSN.jpg"),
            Map.entry("doublecannon_NNSU", "/Images/tiles/doublecannon_NNSU.jpg"),
            Map.entry("doublecannon_NNUD", "/Images/tiles/doublecannon_NNUD.jpg"),
            Map.entry("doublecannon_NSDS", "/Images/tiles/doublecannon_NSDS.jpg"),
            Map.entry("doublecannon_NSUN", "/Images/tiles/doublecannon_NSUN.jpg"),
            Map.entry("doublecannon_NUDN", "/Images/tiles/doublecannon_NUDN.jpg"),
            Map.entry("doublecannon_NUNN", "/Images/tiles/doublecannon_NUNN.jpg"),

            Map.entry("singlecannon_NDSN", "/Images/tiles/singlecannon_NDSN.jpg"),
            Map.entry("singlecannon_NUSN", "/Images/tiles/singlecannon_NUSN.jpg"),
            Map.entry("singlecannon_NUNS", "/Images/tiles/singlecannon_NUNS.jpg"),
            Map.entry("singlecannon_NDSN_2", "/Images/tiles/singlecannon_NDSN.jpg"),
            Map.entry("singlecannon_NNDN", "/Images/tiles/singlecannon_NNDN.jpg"),
            Map.entry("singlecannon_NNSN_2", "/Images/tiles/singlecannon_NNSN.jpg"),
            Map.entry("singlecannon_NDDD", "/Images/tiles/singlecannon_NDDD.jpg"),
            Map.entry("singlecannon_NDNN", "/Images/tiles/singlecannon_NDNN.jpg"),
            Map.entry("singlecannon_NDNU", "/Images/tiles/singlecannon_NDNU.jpg"),
            Map.entry("singlecannon_NDUN", "/Images/tiles/singlecannon_NDUN.jpg"),
            Map.entry("singlecannon_NDUS", "/Images/tiles/singlecannon_NDUS.jpg"),
            Map.entry("singlecannon_NNDN_2", "/Images/tiles/singlecannon_NNDN.jpg"),
            Map.entry("singlecannon_NNDS", "/Images/tiles/singlecannon_NNDS.jpg"),
            Map.entry("singlecannon_NNDU", "/Images/tiles/singlecannon_NNDU.jpg"),
            Map.entry("singlecannon_NNND", "/Images/tiles/singlecannon_NNND.jpg"),
            Map.entry("singlecannon_NNNS", "/Images/tiles/singlecannon_NNNS.jpg"),
            Map.entry("singlecannon_NNSD", "/Images/tiles/singlecannon_NNSD.jpg"),
            Map.entry("singlecannon_NNSN", "/Images/tiles/singlecannon_NNSN.jpg"),
            Map.entry("singlecannon_NNUD", "/Images/tiles/singlecannon_NNUD.jpg"),
            Map.entry("singlecannon_NNUS", "/Images/tiles/singlecannon_NNUS.jpg"),
            Map.entry("singlecannon_NSDN", "/Images/tiles/singlecannon_NSDN.jpg"),
            Map.entry("singlecannon_NSND", "/Images/tiles/singlecannon_NSND.jpg"),
            Map.entry("singlecannon_NSNN", "/Images/tiles/singlecannon_NSNN.jpg"),
            Map.entry("singlecannon_NSSS", "/Images/tiles/singlecannon_NSSS.jpg"),
            Map.entry("singlecannon_NSUD", "/Images/tiles/singlecannon_NSUD.jpg"),
            Map.entry("singlecannon_NSUN", "/Images/tiles/singlecannon_NSUN.jpg")
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
        double x;
        double y;
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                if (VALID_SQUARES[row][col]) {
                    x = GRID_START_X + col * SQUARE_SIZE;
                    y = GRID_START_Y + row * SQUARE_SIZE;
                    Rectangle rect = new Rectangle(x, y, SQUARE_SIZE, SQUARE_SIZE);
                    rect.setFill(Color.TRANSPARENT);
                    rect.setStroke(Color.WHITE);
                    rect.setStrokeWidth(2.0);
                    anchorPane.getChildren().add(rect);
                }
            }
        }

        for (int col = 0; col < STASH_CARDS; col++) {

            x = GRIST_STASH_CARDS_START_X + col * SQUARE_SIZE;
            y = GRIST_STASH_CARDS_START_Y;
            Rectangle rect = new Rectangle(x, y, SQUARE_SIZE, SQUARE_SIZE);
            rect.setFill(Color.TRANSPARENT);
            rect.setStroke(Color.WHITE);
            rect.setStrokeWidth(2.0);
            anchorPane.getChildren().add(rect);

        }

    }

    private void setupDragAndDrop() {
        cardImage.setOnDragDetected(event -> {
            if (cardImage.getImage() != null && currentComponent != null && !isRemoveMode) {
                Dragboard db = cardImage.startDragAndDrop(TransferMode.COPY);
                ClipboardContent content = new ClipboardContent();
                
                content.putImage(cardImage.getImage());
                //javafx.scene.image.WritableImage snapshot = cardImage.snapshot(null, null);
                //content.putImage(snapshot);
                
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
            statusLabel.setText("Error: No connection available");
            return;
        }
        // Reset rotazione logica e grafica ogni volta che si pesca un nuovo componente
        //cardImage.setRotate(0);
        //if (currentComponent != null) {
        //    if(currentComponent.getComponentDirection()!=Direction.NORTH){
        //        //currentComponent.setComponentDirection(Direction.NORTH);
        //        // Riporta anche i connettori alla posizione originale
        //        System.out.println("ERRORE COMPONENT RUOTATO IMPLICITAMENTE");
        //    }

        try {
            statusLabel.setText("Requesting component...");
            pickComponentButton.setDisable(true);
            context.getClient().pickComponentCard(context.getPlayerId());

        } catch (Exception e) {
            pickComponentButton.setDisable(false);
            statusLabel.setText("Pick failed: " + e.getMessage());
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
                statusLabel.setText("Picked: " + component.getClass().getSimpleName() + " (no image)" + component.getComponentString());
            }
        } else {
            statusLabel.setText("Picked: " + component.getClass().getSimpleName() + " (unmapped) : " + component.getComponentString());
        }
    }

    @FXML
    public void rotateCardClockwise() {
        if (currentComponent == null) {
            statusLabel.setText("Devi prima pescare un componente");
            return;
        }
         //Rotazione logica
        currentComponent.rotate(RotationType.CLOCKWISE);
        //currentComponent.setComponentDirection(currentComponent.getComponentDirection().getClockwise());
        //currentComponent.getConnections().forEach(conn -> conn.setDirection(conn.getDirection().getClockwise()));
        //Rotazione grafica
        cardImage.setRotate(cardImage.getRotate() + 90);
        statusLabel.setText("Ruotato in senso orario (solo prima del piazzamento)");


//        if (selectedImageView == null) {
//            statusLabel.setText("Select a component on the ship first");
//            return;
//        }
//
//        Position componentPosition = null;
//        for (Map.Entry<Position, ImageView> entry : cellViews.entrySet()) {
//            if (entry.getValue() == selectedImageView) {
//                componentPosition = entry.getKey();
//                break;
//            }
//        }
//
//        if (componentPosition != null) {
//            try {
//                context.getClient().rotateComponent(context.getPlayerId(), componentPosition, RotationType.CLOCKWISE);
//                statusLabel.setText("Rotating component...");
//            } catch (Exception e) {
//                statusLabel.setText("Rotation failed: " + e.getMessage());
//            }
//        }
    }

    @FXML
    public void rotateCardAntiClockwise() {
        if (currentComponent == null) {
            statusLabel.setText("Devi prima pescare un componente");
            return;
        }
        // Rotazione logica
        currentComponent.rotate(RotationType.ANTI_CLOCKWISE);
        //currentComponent.setComponentDirection(currentComponent.getComponentDirection().getCounterClockwise());
        //currentComponent.getConnections().forEach(conn -> conn.setDirection(conn.getDirection().getCounterClockwise()));
        // Rotazione grafica
        cardImage.setRotate(cardImage.getRotate() - 90);
        statusLabel.setText("Ruotato in senso antiorario (solo prima del piazzamento)");


//        if (selectedImageView == null) {
//            statusLabel.setText("Select a component on the ship first");
//            return;
//        }
//
//        Position componentPosition = null;
//        for (Map.Entry<Position, ImageView> entry : cellViews.entrySet()) {
//            if (entry.getValue() == selectedImageView) {
//                componentPosition = entry.getKey();
//                break;
//            }
//        }
//
//        if (componentPosition != null) {
//            try {
//                context.getClient().rotateComponent(context.getPlayerId(), componentPosition, RotationType.ANTI_CLOCKWISE);
//                statusLabel.setText("Rotating component...");
//            } catch (Exception e) {
//                statusLabel.setText("Rotation failed: " + e.getMessage());
//            }
//        }
    }

    @FXML
    public void validateShip() {
        try {
            context.getClient().validateShip(context.getPlayerId());
            statusLabel.setText("Validating ship...");
            validateShipButton.setDisable(true);
        } catch (Exception e) {
            statusLabel.setText("Validation failed: " + e.getMessage());
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

    @Override
    public void gameStateUpdate() {
        updateButtonStates();
    }

    @Override
    public void gameComponentPlaced(Component component, Position position) {
        System.out.println("[DEBUG] gameComponentPlaced chiamato: " + component + " in posizione " + position);
        String imagePath = getComponentImgPath(component);
        if (imagePath == null) {
            System.out.println("[DEBUG] imagePath nullo per componente: " + component);
            return;
        }

        InputStream imageStream = getClass().getResourceAsStream(imagePath);
        if (imageStream == null) {
            System.out.println("[DEBUG] imageStream nullo per path: " + imagePath);
            return;
        }

        ImageView placed = new ImageView(new Image(imageStream));
        placed.setFitWidth(SQUARE_SIZE);
        placed.setFitHeight(SQUARE_SIZE);
        placed.setLayoutX(GRID_START_X + position.getX() * SQUARE_SIZE);
        placed.setLayoutY(GRID_START_Y + position.getY() * SQUARE_SIZE);
        placed.setOnMouseClicked(event -> handleComponentClick(placed, position));

        // Rotazione coerente con la logica del componente
        double rotation = 0;
        switch (component.getComponentDirection()) {
            case NORTH -> rotation = 0;
            case EAST -> rotation = 90;
            case SOUTH -> rotation = 180;
            case WEST -> rotation = 270;
        }
        placed.setRotate(rotation);

        anchorPane.getChildren().add(placed);
        cellViews.put(position, placed);
        shipComponents.put(position, component);

        statusLabel.setText("Component placed successfully!");
        currentComponent = null;
        cardImage.setImage(null);
        System.out.println("[DEBUG] Componente piazzato: " + component.getComponentString() + " con direzione " + component.getComponentDirection());
        //System.out.println("[DEBUG] Componente piazzato e GUI aggiornata");
    }

    @Override
    public void gameComponentRemoved(Position position) {
        ImageView removed = cellViews.get(position);
        if (removed != null) {
            removed.setImage(null);
            cellViews.remove(position);
            //anchorPane.getChildren().remove(removed);
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

//        Platform.runLater(() -> {
//            ImageView imageView = cellViews.get(position);
//            if (imageView != null) {
//                double rotation = switch (rotationType) {
//                    case CLOCKWISE -> 90;
//                    case ANTI_CLOCKWISE -> -90;
//                    case OPPOSITE -> 180;
//                };
//
//                double currentRotation = imageView.getRotate();
//                imageView.setRotate(currentRotation + rotation);
//
//                statusLabel.setText("Component rotated successfully");
//
//                if (selectedImageView == imageView && selectedHighlight != null) {
//                    selectedHighlight.setRotate(currentRotation + rotation);
//                }
//            } else {
//                System.err.println("[GUI] No ImageView found at position " + position);
//            }
//        });
    }

    @Override
    public void gameShipValidated(boolean isValid, String message) {
        Platform.runLater(() -> {
            validateShipButton.setDisable(false);

            if (isValid) {
                statusLabel.setText("Ship validated! Ready for flight");

                validateShipButton.setText("Ready for Flight");
                validateShipButton.setOnAction(e -> readyForFlight());
            } else {
                statusLabel.setText("Validation failed");

                showValidationErrorDialog(message);

                validateShipButton.setText("Validate Ship");
                validateShipButton.setStyle("");
                validateShipButton.setOnAction(e -> validateShip());
            }
        });
    }

    public void readyForFlight() {
        if (isReadyingForFlight) {
            statusLabel.setText("Already processing ready for flight...");
            return;
        }

        isReadyingForFlight = true;
        validateShipButton.setDisable(true);

        try {
            UUID matchId = context.getCurrentMatchId();
            if (matchId == null) {
                statusLabel.setText("No active match found");
                isReadyingForFlight = false;
                validateShipButton.setDisable(false);
                return;
            }

            validateShipButton.setText("Waiting for others...");
            validateShipButton.setDisable(true);
            statusLabel.setText("Ready for flight - waiting for other players");

            context.getClient().readyForFlight(matchId, context.getPlayerId());

        } catch (Exception e) {
            statusLabel.setText("Ready for flight failed: " + e.getMessage());
            validateShipButton.setDisable(false);
            isReadyingForFlight = false;
            validateShipButton.setText("Ready for Flight");
        }
    }

    private void updateButtonStates() {
        pickComponentButton.setDisable(currentComponent != null);
        validateShipButton.setDisable(shipComponents.isEmpty());
        rotateClockwiseButton.setDisable(selectedImageView == null);
        rotateCounterClockwiseButton.setDisable(selectedImageView == null);
    }

    @Override
    public void setContext(AppContext ctx) {
        if (this.context != null) {
            this.context.removeGameStateUpdateListener(this);
        }
        this.context = ctx;
        ctx.addGameStateUpdateListener(this);
    }


    public ShipBuilderController getController() {
        return this;
    }

    @Override
    public void gameComponentPickedFailed(String message) {
        Platform.runLater(() -> {
            pickComponentButton.setDisable(false);
            statusLabel.setText("Pick failed: " + message);
            statusLabel.setTextFill(javafx.scene.paint.Color.RED);
        });
    }

    @Override
    public void gameComponentPlacedFailed(String message) {
        Platform.runLater(() -> {
            statusLabel.setText("Placement failed: " + message);
            statusLabel.setTextFill(javafx.scene.paint.Color.RED);
        });
    }

    @Override
    public void gameComponentRemovedFailed(String message) {
        Platform.runLater(() -> {
            statusLabel.setText("Removal failed: " + message);
            statusLabel.setTextFill(javafx.scene.paint.Color.RED);
            clearSelection();
        });
    }

    @Override
    public void gameComponentRotatedFailed(String message) {
        Platform.runLater(() -> {
            statusLabel.setText("Rotation failed: " + message);
            statusLabel.setTextFill(javafx.scene.paint.Color.RED);
        });
    }

    @Override
    public void gameShipValidationFailed(String message) {
        Platform.runLater(() -> {
            validateShipButton.setDisable(false);
            statusLabel.setText("Validation failed");
            statusLabel.setTextFill(javafx.scene.paint.Color.RED);

            showValidationErrorDialog(message);

            validateShipButton.setText("Validate Ship");
            validateShipButton.setStyle("");
            validateShipButton.setOnAction(e -> validateShip());
        });
    }

    @Override
    public void gamePlayerReadyForFlightSuccess(String msg) {
        Platform.runLater(() -> {
            isReadyingForFlight = false;
            validateShipButton.setText("Ready! Waiting for others...");
            validateShipButton.setDisable(true);
            statusLabel.setText(msg);
        });
    }

    @Override
    public void gamePlayerReadyForFlightFailed(String message) {
        Platform.runLater(() -> {
            isReadyingForFlight = false;
            validateShipButton.setDisable(false);
            validateShipButton.setText("Ready for Flight");
            statusLabel.setText("Ready for flight failed: " + message);

            showErrorDialog("Ready for Flight Failed", message);
        });
    }

}
