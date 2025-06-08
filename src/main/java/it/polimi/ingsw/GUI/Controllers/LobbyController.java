package it.polimi.ingsw.GUI.Controllers;

import it.polimi.ingsw.GUI.MatchRowDisplay;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.Match;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.util.List;
import java.util.UUID;

public class LobbyController extends GuiContoller {

    @FXML
    private Label welcomeLabel;
    @FXML
    private ListView<String> matchesListView;
    @FXML
    private ListView<String> playersListView;

    @FXML
    private Button createMatchButton, joinMatchButton, startMatchButton, refreshButton;

    // Suppose you have a Match class with a nice toString()
    private static ObservableList<String> matchList = FXCollections.observableArrayList();
    private static ObservableList<String> playerList = FXCollections.observableArrayList();

    public void initialize() {
        matchesListView.setItems(matchList);
        playersListView.setItems(playerList);
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
                super.context.getClient().createMatch("GUI-CreatedMatch-" + context.getUsername() , context.getPlayerId(), context.getUsername());
            } catch (Exception e) {
                showErrorDialog("COM-error", e.getMessage());
            }
        }
    }

    private void joinSelectedMatch() {
        String[] selected = matchesListView.getSelectionModel().getSelectedItem().split(":");
        String matchID = selected[0];

        if (selected != null) {
            if (super.context.getClient() != null){
                try {
                    super.context.getClient().joinMatch(UUID.fromString(matchID), context.getPlayerId(), context.getUsername());
                    super.context.setCurrentMatchId(UUID.fromString(matchID));
                } catch (Exception e) {
                    showErrorDialog("COM-error", e.getMessage());
                }
            }
        }
    }

    private void startSelectedMatch() {
        String selected = matchesListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                super.context.getClient().startMatch(UUID.fromString(selected), context.getPlayerId(), context.getUsername());
            } catch (Exception e) {
                showErrorDialog("Impossible to start match:", e.getMessage());
            }
        }
    }

    public static void matchCreatedListener(String newMatch) {
        Platform.runLater(new Runnable(){
            @Override
            public void run() {
                matchList.add(newMatch);
            }
        });
    }

    public static void updateMatchListListener(List<String> newMatches) {
        Platform.runLater(new Runnable(){
            @Override
            public void run() {
                matchList.clear();
                matchList.addAll(newMatches);
                if (newMatches.isEmpty())
                {
                    showErrorDialog("Matches", "No Matches found");
                }
            }
        });
    }

    public static void playerJoinedMatchNotification(List<String> playersInMatch) {
        Platform.runLater(new Runnable(){
            @Override
            public void run() {
                playerList.clear();
                playerList.addAll(playersInMatch);
            }
        });
    }
}