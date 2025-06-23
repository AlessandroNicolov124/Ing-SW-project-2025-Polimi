package it.polimi.ingsw.GUI.Controllers;

import it.polimi.ingsw.GUI.AppContext;
import it.polimi.ingsw.GUI.GUIApp;
import it.polimi.ingsw.GUI.GameStateUpdateListener.LobbyListener;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.*;

public class LobbyController extends GuiContoller implements LobbyListener {

    @FXML private Label statusLabel;
    @FXML private Label welcomeLabel;
    @FXML private ListView<String> matchesListView;
    @FXML private ListView<String> playersListView;

    @FXML
    private Button createMatchButton, joinMatchButton, leaveMatchButton, startMatchButton, refreshButton, readyButton;

    private final ObservableList<String> matchList = FXCollections.observableArrayList();
    private final ObservableList<String> playerList = FXCollections.observableArrayList();

    private final List<String> availableMatches = new ArrayList<>();
    private final List<String> playersInCurrentMatch = new ArrayList<>();

    private UUID currentMatchId = null;
    private boolean isHost = false;

    private enum Phase { NONE, LOADING, WAIT_READY }
    private Phase phase = Phase.NONE;

    private int totalPlayers = 0;
    private int loadedPlayers = 0;
    private int readyPlayers  = 0;


    @FXML
    public void initialize() {
        matchesListView.setItems(matchList);
        playersListView.setItems(playerList);

        refreshButton.setOnAction(e -> askServerForMatches());
        createMatchButton.setOnAction(e -> createMatchDialog());
        joinMatchButton.setOnAction(e -> joinSelectedMatch());
        leaveMatchButton.setOnAction(e -> leaveCurrentMatch());
        startMatchButton.setOnAction(e -> startMatch());
        readyButton.setOnAction(e -> setPlayerReady());

        updateUiButtons();

        Platform.runLater(this::askServerForMatches);

    }

    @Override
    public void setContext(AppContext context) {
        super.setContext(context);
        context.addLobbyListener(this);
        welcomeLabel.setText("Welcome, " + context.getUsername());
    }

    private int getPlayerId() {
        return context.getPlayerId();
    }

    private void askServerForMatches() {
        try { 
            context.getClient().getAvailableMatches(); 
        }
        catch (Exception e) { 
            showErrorDialog("Com-error", e.getMessage()); 
        }
    }

    private void createMatchDialog() {
        if (currentMatchId != null) {
            showErrorDialog("Create Match","Leave the current match before creating a new one.");
            return;
        }
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create Match");
        dialog.setHeaderText("Enter a match name");
        dialog.setContentText("Name:");
        Optional<String> res = dialog.showAndWait();
        res.ifPresent(name -> {
            String finalName = name.isBlank() ? "Match-" + context.getUsername() : name;
            try {
                context.getClient().createMatch(finalName, getPlayerId(), context.getUsername());
            } catch (Exception e) { 
                showErrorDialog("Create failed", e.getMessage()); 
            }
        });
    }

    private void joinSelectedMatch() {
        String raw = matchesListView.getSelectionModel().getSelectedItem();
        if (raw == null) { 
            showErrorDialog("Join", "Select a match first"); 
            return; 
        }

        if (currentMatchId != null) { 
            showErrorDialog("Join", "Leave current match first"); 
            return; 
        }

        UUID id = UUID.fromString(raw.split(":")[0]);
        try {
            context.getClient().joinMatch(id, getPlayerId(), context.getUsername());
        } catch (Exception e) { 
            showErrorDialog("Join failed", e.getMessage()); 
        }
    }

    private void leaveCurrentMatch() {
        if (currentMatchId == null) 
            return;
        try {
            context.getClient().leaveMatch(currentMatchId, getPlayerId(), context.getUsername());
        } catch (Exception e) { 
            showErrorDialog("Leave failed", e.getMessage()); 
        }
    }

    private void startMatch() {
        if (!isHost) { 
            showErrorDialog("Start", "Only host can start"); 
            return; 
        }
        if (playersInCurrentMatch.size() < 2) {
            showErrorDialog("Start", "Need at least 2 players");
            return;
        }
        try {
            context.getClient().startMatch(currentMatchId, getPlayerId(), context.getUsername());
        } catch (Exception e) { 
            showErrorDialog("Start failed", e.getMessage()); 
        }
    }

    private void setPlayerReady() {
        if (phase != Phase.WAIT_READY) 
            return;          
        try {
            context.getClient().playerReady(currentMatchId, getPlayerId());
            readyButton.setDisable(true);
            statusLabel.setText("Ready sent – waiting others…");
        } catch (Exception e) {
            showErrorDialog("Ready failed", e.getMessage());
        }
    }

    private void updateUiButtons() {
        boolean inMatch = currentMatchId != null;

        createMatchButton.setDisable(inMatch);
        joinMatchButton.setDisable(inMatch);
        leaveMatchButton.setDisable(!inMatch || phase == Phase.LOADING);
        startMatchButton.setDisable(!inMatch || !isHost || phase != Phase.NONE);
        readyButton.setDisable(phase != Phase.WAIT_READY);
    }


    @Override
    public void availableMatches(List<String> matches) {
        Platform.runLater(() -> {
            availableMatches.clear();
            
            if (matches != null) {
                availableMatches.addAll(matches);
            }

            matchList.setAll(availableMatches);
        });
    }

    @Override
    public void matchCreated(UUID id, String name, String playerName, int creatorId) {
        Platform.runLater(() -> {
            String row = id + ": " + name;
            if (creatorId != getPlayerId()) {
                if (!availableMatches.contains(row)) {
                    availableMatches.add(row);
                    matchList.setAll(availableMatches);
                }
                return;
                }

            currentMatchId = id;
            isHost = true;
            playersInCurrentMatch.clear();
            playersInCurrentMatch.add(playerName);
            playerList.setAll(playersInCurrentMatch);

            if (!availableMatches.contains(row)) {
                availableMatches.add(row);
            }
            matchList.setAll(availableMatches);

            updateUiButtons();

        });
    }

    @Override
    public void playerJoinedMatch(UUID id, String playerName) {
        if (!Objects.equals(currentMatchId, id)) 
            return;
            
        Platform.runLater(() -> {
            if (!playersInCurrentMatch.contains(playerName)) {
                playersInCurrentMatch.add(playerName);
                playerList.setAll(playersInCurrentMatch);
            }
        });
    }

    @Override
    public void playerLeftMatch(UUID id, int pId, String pName) {
        if (!Objects.equals(currentMatchId, id)) 
            return;

        Platform.runLater(() -> {
            playersInCurrentMatch.remove(pName);
            playerList.setAll(playersInCurrentMatch);
            if (pId == getPlayerId()) {
                currentMatchId = null;
                isHost = false;
                playersInCurrentMatch.clear();
                playerList.clear();
            }
            updateUiButtons();
        });
    }

    @Override
    public void playersInMatch(UUID id, List<String> names, int hostId) {
        Platform.runLater(() -> {
            currentMatchId = id;
            playersInCurrentMatch.clear();
            playersInCurrentMatch.addAll(names);
            playerList.setAll(playersInCurrentMatch);
            isHost = (hostId == getPlayerId());
            updateUiButtons();
        });
    }

    @Override
    public void updateHost(UUID id, int hostId, String hostName) {
        if (!Objects.equals(currentMatchId, id)) 
            return;
        isHost = (hostId == getPlayerId());
        updateUiButtons();
    }


    @Override
    public void matchStarted(UUID m, String hostName) {
        if (!Objects.equals(currentMatchId, m)) 
            return;

        Platform.runLater(() -> {
            phase = Phase.LOADING;
            loadedPlayers = 0;
            readyPlayers = 0;
            totalPlayers = playersInCurrentMatch.size();

            statusLabel.setText("Loading resources…");
            updateUiButtons();
        });
    }

    @Override
    public void loadingStatus(UUID m, int ready, int total) {
        if (!Objects.equals(currentMatchId, m)) 
            return;

        Platform.runLater(() -> {
            loadedPlayers = ready;
            totalPlayers  = total;
            statusLabel.setText("Loaded: " + ready + " / " + total);

            
            if (ready == total) {
                phase = Phase.WAIT_READY;
                statusLabel.setText("All loaded! Press READY");
            }
            updateUiButtons();
        });
    }

    @Override
    public void playerReady(UUID m, int ready, int total) {
        if (!Objects.equals(currentMatchId, m)) 
            return;

        Platform.runLater(() -> {
            readyPlayers = ready;
            totalPlayers = total;
            statusLabel.setText("Players ready: " + ready + " / " + total);
        });
    }

    @Override
    public void shipBuildingStart(UUID m, int readyPlayers) {
        if (!Objects.equals(currentMatchId, m)) 
            return;

        Platform.runLater(() -> {
            phase = Phase.NONE;
            statusLabel.setText("");
            try { GUIApp.showShipBuilder(); }
            catch (Exception ex) { showErrorDialog("Navigation", "Cannot open builder"); }
        });
    }

}