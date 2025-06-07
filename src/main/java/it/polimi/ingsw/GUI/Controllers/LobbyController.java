package it.polimi.ingsw.GUI.Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.Match;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

public class LobbyController extends GuiContoller {

    @FXML
    private Label welcomeLabel;
    @FXML
    private ListView<Match> matchesListView;
    @FXML
    private Button createMatchButton, joinMatchButton, startMatchButton, refreshButton;

    // Suppose you have a Match class with a nice toString()
    private ObservableList<Match> matchList = FXCollections.observableArrayList();

    public void initialize() {
        matchesListView.setItems(matchList);
        // Load matches from server
        refreshButton.setOnAction(e -> loadMatchesFromServer());
        createMatchButton.setOnAction(e -> showCreateMatchDialog());
        joinMatchButton.setOnAction(e -> joinSelectedMatch());
        startMatchButton.setOnAction(e -> startSelectedMatch());
    }

    private void loadMatchesFromServer() {
        if (super.context.getClient() != null){
            try {
                super.context.getClient().getAvailableMatches();
            } catch (Exception e) {
                showErrorDialog("COM-error", e.getMessage());
            }
        }
    }

    private void showCreateMatchDialog() {
        if (super.context.getClient() != null){
            try {
                super.context.getClient().createMatch("11", context.getPlayerId(), context.getUsername());
            } catch (Exception e) {
                showErrorDialog("COM-error", e.getMessage());
            }
        }
    }

    private void joinSelectedMatch() {
        Match selected = matchesListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (super.context.getClient() != null){
                try {
                    //super.context.getClient().joinMatch()
                } catch (Exception e) {
                    showErrorDialog("COM-error", e.getMessage());
                }
            }
        }
    }

    private void startSelectedMatch() {
        Match selected = matchesListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // Send start request to server (only if user is host/owner)
        }
    }
}