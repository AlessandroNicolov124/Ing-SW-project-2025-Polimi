package it.polimi.ingsw.GUI.Controllers;

import it.polimi.ingsw.GUI.AppContext;
import it.polimi.ingsw.GUI.GUIApp;
import it.polimi.ingsw.GUI.GameStateUpdateListener.LobbyListener;
import it.polimi.ingsw.network.Difficulty;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Pair;

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
    private boolean colorChosen = false;

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
        
        Dialog<Pair<String, Difficulty>> dialog = new Dialog<>();
        dialog.setTitle("Create Match");
        dialog.setHeaderText("Create a new match");
        
        VBox content = new VBox(10);
        TextField nameField = new TextField();
        nameField.setPromptText("Match name");
        
        ComboBox<Difficulty> difficultyBox = new ComboBox<>();
        difficultyBox.getItems().addAll(Difficulty.values());
        difficultyBox.setValue(Difficulty.LEVEL_1);
        
        content.getChildren().addAll(
            new Label("Match name:"), nameField,
            new Label("Difficulty:"), difficultyBox
        );
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return new Pair<>(nameField.getText(), difficultyBox.getValue());
            }
            return null;
        });
        
        Optional<Pair<String, Difficulty>> result = dialog.showAndWait();
        if (result.isPresent()) {
            String name = result.get().getKey();
            Difficulty difficulty = result.get().getValue();
            String finalName = name.isBlank() ? "Match-" + context.getUsername() : name;

            try {
                context.getClient().createMatch(finalName, getPlayerId(), context.getUsername(), difficulty);
            } catch (Exception e) {
                showErrorDialog("Create failed", e.getMessage());
            }
        }
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
    public void matchCreated(UUID id, String name, String playerName, int creatorId, Difficulty difficulty) {
        Platform.runLater(() -> {
            String row = id + ": " + name +
                        " (Host: " + playerName +
                        ", Difficulty: " + difficulty +
                        ", Players: 1/4)";

            this.isHost = (creatorId == getPlayerId());
            
            if (!isHost) {
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
            
            colorChosen = false;
            context.setSelectedCabinColor(null);
            
            requestColorSelection();
        });
    }

    @Override
    public void playerJoinedMatch(UUID id, String playerName) {
        Platform.runLater(() -> {
            if (Objects.equals(currentMatchId, id) && !playersInCurrentMatch.contains(playerName)) {

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
                colorChosen = false;
                playersInCurrentMatch.clear();
                playerList.clear();
                phase = Phase.NONE;
                context.setSelectedCabinColor(null);
                statusLabel.setText("");
                System.out.println("[LobbyController] Local player left match - full state reset");
            }
            
            updateUiButtons();
            askServerForMatches();
        });
    }

    @Override
    public void playersInMatch(UUID id, List<String> names, int hostId) {
        Platform.runLater(() -> {
            playersInCurrentMatch.clear();
            playersInCurrentMatch.addAll(names);
            playerList.setAll(playersInCurrentMatch);

            if (!names.contains(context.getUsername())) {
                return;
            }

            currentMatchId = id;
            isHost = (hostId == getPlayerId());
            updateUiButtons();

            askServerForMatches();

            if (!isHost && context.getSelectedCabinColor() == null && !colorChosen) {
                requestColorSelection();
            }
        });
    }

    private void requestColorSelection() {
        if (colorChosen || context.getSelectedCabinColor() != null) 
            return;
        showColorSelectionDialog(color -> {
            try {
                context.getClient().selectCabinColor(color, getPlayerId());
            } catch (Exception e) {
                showErrorDialog("Selec­tion failed", e.getMessage());
                requestColorSelection();
            }
        });
    }

    @Override
    public void cabinColorSelected(String color) {
        Platform.runLater(() -> {
            this.colorChosen = true;
            statusLabel.setText("Chosen color: " + color);
        });
    }

    @Override
    public void cabinColorSelectionFailed(String errorMsg) {
        Platform.runLater(() -> {
            showErrorDialog("Failed to select color", errorMsg);
            requestColorSelection();
        });
    }


    @Override
    public void updateHost(UUID id, int hostId, String hostName) {
        if (!Objects.equals(currentMatchId, id)) {
            return;
        }
        isHost = (hostId == getPlayerId());
        updateUiButtons();
    }


    @Override
    public void matchStarted(UUID m, String hostName) {
        if (!Objects.equals(currentMatchId, m)) {
            return;
        }

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
            try {
                GUIApp.showShipBuilder(); 
            }
            catch (Exception ex) { 
                showErrorDialog("Navigation", "Cannot open builder"); 
            }
        });
    }

    @Override
    public void matchCreatedFailed(String message) {
        Platform.runLater(() -> {
            statusLabel.setText("Failed to create match");
            updateUiButtons();
        });
    }

    @Override
    public void playerJoinedMatchFailed(String message) {
        Platform.runLater(() -> {
            statusLabel.setText("Failed to join match");
            updateUiButtons();
        });
    }

    @Override
    public void playerLeftMatchFailed(String message) {
        Platform.runLater(() -> {
            statusLabel.setText("Failed to leave match");
        });
    }

    @Override
    public void updateAvailableMatchesFailed(String message) {
        Platform.runLater(() -> {
            statusLabel.setText("Failed to refresh matches");
        });
    }

    @Override
    public void updateHostFailed(String message) {
        Platform.runLater(() -> {
            statusLabel.setText("Failed to update host");
        });
    }

    @Override
    public void matchStartedFailed(String message) {
        Platform.runLater(() -> {
            statusLabel.setText("Failed to start match");
            updateUiButtons();
        });
    }

    @Override
    public void loadingAckResponseFailed(String message) {
        Platform.runLater(() -> {
            statusLabel.setText("Loading acknowledgment failed");
        });
    }

    @Override
    public void loadingStatusFailed(String message) {
        Platform.runLater(() -> {
            statusLabel.setText("Loading status update failed");
        });
    }

    @Override
    public void playersInMatchFailed(String message) {
        Platform.runLater(() -> {
            statusLabel.setText("Failed to get players in match");
        });
    }

    @Override
    public void playerReadyFailed(String message) {
        Platform.runLater(() -> {
            statusLabel.setText("Failed to set ready status");
            readyButton.setDisable(false);
            updateUiButtons();
        });
    }

    @Override
    public void shipBuildingStartFailed(String message) {
        Platform.runLater(() -> {
            statusLabel.setText("Failed to start ship building");
            phase = Phase.NONE;
            updateUiButtons();
        });
    }

}