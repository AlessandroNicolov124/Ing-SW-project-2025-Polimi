package it.polimi.ingsw.GUI.Controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import it.polimi.ingsw.GUI.AppContext;

public class FlightPreparationController extends GuiContoller {
    
    @FXML
    private Label titleLabel;
    
    @FXML
    private Label statusLabel;
    
    @FXML
    private Label infoLabel;
    
    @FXML
    private ListView<String> playersListView;
    
    @FXML
    private Button continueButton;
    
    private ObservableList<String> playersList = FXCollections.observableArrayList();
    
    @FXML
    public void initialize() {
        playersListView.setItems(playersList);
        continueButton.setOnAction(e -> handleContinue());
        
        // dummy initialization
        Platform.runLater(() -> {
            statusLabel.setText("Flight preparation complete!");
            infoLabel.setText("Ready to begin flight phase");
            continueButton.setDisable(false);
        });
    }
    
    @Override
    public void setContext(AppContext context) {
        super.setContext(context);
        
        
    }
    
    private void handleContinue() {
        statusLabel.setText("Starting flight phase...");
        continueButton.setDisable(true);
    }
}