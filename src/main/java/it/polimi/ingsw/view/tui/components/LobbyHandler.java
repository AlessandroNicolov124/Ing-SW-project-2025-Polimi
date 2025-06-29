package it.polimi.ingsw.view.tui.components;

import it.polimi.ingsw.network.client.GeneralClientIf;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static it.polimi.ingsw.network.Difficulty.LEVEL_2;

public class LobbyHandler {
    private final GeneralClientIf simpleClient;
    private final ConsoleManager console;
    private final int playerId;
    private UUID currentMatchId;
    private boolean isHost;
    private String username;
    private List<String> availableMatches;
    private List<String> playersInCurrentMatch;
    private int readyPlayers;
    private final AtomicBoolean dirty = new AtomicBoolean(true);
    private final Set<UUID> handledCreations = new HashSet<>();

    public LobbyHandler(ConsoleManager console, GeneralClientIf simpleClient, int playerId, String username) {
        this.console = console;
        this.simpleClient = simpleClient;
        this.playerId = playerId;
        this.username = username;
        this.availableMatches = new ArrayList<>();
        this.playersInCurrentMatch = new ArrayList<>();
        
    }

    public boolean handleLobbyMenu() {
        String cmd = console.pollCommand(200);
        return (cmd == null) || executeCommand(cmd);
    }

    public boolean isInMatch() {
        return currentMatchId != null;
    }

    public UUID getCurrentMatchId() {
        return currentMatchId;
    }

    public int getPlayerId() {
        return playerId;
    }

    public List<String> getPlayersInCurrentMatch() {
        return new ArrayList<>(playersInCurrentMatch);
    }

    public void tick() {
        if (dirty.getAndSet(false)) {
            showLobbyMenu();
        }
    }

    private void showLobbyMenu() {
        console.printMessage("\n=== LOBBY MENU ===");
        if (currentMatchId == null) {
            console.printMessage("1. List available matches");
            console.printMessage("2. Create new match");
            console.printMessage("3. Join match");
            console.printMessage("quit. Exit application");
        } else {
            console.printMessage("Current Match ID: " + currentMatchId);
            console.printMessage("Role: " + (isHost ? "Host" : "Player"));
            console.printMessage("Players in match: " + playersInCurrentMatch);
            console.printMessage("1. Leave match");
            console.printMessage("2. Refresh menu");
            console.printMessage("3. Start match (requires at least 2 players)");
            console.printMessage("4. quit. Exit application");
        }
        console.printMessage("Enter command: ");
    }

    public boolean executeCommand(String command) {
        if (command == null || command.trim().isEmpty()) return true;
        try {
            return (currentMatchId == null) ? handleLobbyCommands(command) : handleMatchCommands(command);
        } catch (Exception e) {
            console.printError("Error executing command: " + e.getMessage());
            return true;
        }
    }

    private boolean handleLobbyCommands(String command) throws Exception {
        switch (command.toLowerCase().trim()) {
            case "1":
                listAvailableMatches();
                break;
            case "2":
                createNewMatch();
                break;
            case "3":
                joinMatch();
                break;
            case "help":
                showLobbyMenu();
                break;
            case "quit":
                simpleClient.close();
                return false;
            default:
                console.printError("Unknown command. Available commands: 1, 2, 3, help, quit");
        }
        return true;
    }

    private boolean handleMatchCommands(String command) throws Exception {
        switch (command.toLowerCase().trim()) {
            case "1":
                leaveMatch();
                break;
            case "2":
                showLobbyMenu();
                break;
            case "3":
                if (isHost) {
                    startMatch();
                } else {
                    console.printError("Only the host can start the match.");
                }
                break;
            case "quit":
                if (currentMatchId != null) {
                    leaveMatch();
                }
                simpleClient.close();
                return false;
            default:
                console.printError("Unknown command. Available commands: 1, 2, 3, quit");
        }
        return true;
    }

    private void listAvailableMatches() throws Exception {
        //console.printMessage("Requesting available matches...");
        simpleClient.getAvailableMatches();
    }

    private void createNewMatch() throws Exception {
        String matchName = console.askQuestion("Enter match name:");
        if (matchName.trim().isEmpty()) {
            console.printError("Match name cannot be empty.");
            return;
        }
        console.printMessage("Creating match: " + matchName);
        simpleClient.createMatch(matchName, playerId, username, LEVEL_2);
    }

    private void joinMatch() throws Exception {
        if (availableMatches == null || availableMatches.isEmpty()) {
            simpleClient.getAvailableMatches();
            
            // console.printMessage("Waiting for server response...");
            // try {
            //     Thread.sleep(1000);
            // } catch (InterruptedException e) {
            //     Thread.currentThread().interrupt();
            // }
            
            if (availableMatches == null || availableMatches.isEmpty()) {
                return;
            }
        }

        console.printMessage("Available matches:");
        for (int i = 0; i < availableMatches.size(); i++) {
            console.printMessage((i + 1) + ". " + availableMatches.get(i));
        }

        String input = console.askQuestion("Enter match number to join (or 'cancel'):");
        if ("cancel".equalsIgnoreCase(input.trim())) {
            return;
        }

        try {
            int matchIndex = Integer.parseInt(input.trim()) - 1;
            if (matchIndex >= 0 && matchIndex < availableMatches.size()) {
                String selectedMatch = availableMatches.get(matchIndex);
                String matchIdStr = selectedMatch.split(":")[0];
                UUID matchId = UUID.fromString(matchIdStr);
                
                console.printMessage("Joining match: " + selectedMatch);
                simpleClient.joinMatch(matchId, playerId, username);

            } else {
                console.printError("Invalid match number.");
            }
        } catch (NumberFormatException e) {
            console.printError("Invalid input. Please enter a number.");
        } catch (Exception e) {
            console.printError("Error joining match: " + e.getMessage());
        }
    }

    private void leaveMatch() throws Exception {
        if (currentMatchId != null) {
            console.printMessage("Leaving match...");
            simpleClient.leaveMatch(currentMatchId, playerId, username);
        }
    }

    private void startMatch() throws Exception {
        if (currentMatchId != null && isHost) {
            int playerCount;
            if (playersInCurrentMatch != null) {
                playerCount = playersInCurrentMatch.size();
            } else {
                console.printError("No players in the match. Cannot start.");
                return;
            }
            if (playerCount < 2) {
                console.printError("Cannot start match: need at least 2 players (current: " + playerCount + ")");
                return;
            }
            
            console.printMessage("Starting match with " + playerCount + " players...");
            simpleClient.startMatch(currentMatchId, playerId, username);
        } else {
            console.printError("Cannot start match: not the host or no active match.");
        }
    }

    public void matchCreatedCallback(String matchName, UUID matchId, boolean isHost, String playerName, int playerId) {
        String matchNameParsed = matchId.toString() + ": " + matchName;  

        if (this.playerId != playerId) {
            if (!this.availableMatches.contains(matchNameParsed)) {
                this.availableMatches.add(matchNameParsed);
            }
            return;
        }

        this.currentMatchId = matchId;
        this.isHost = isHost;
        this.playersInCurrentMatch.clear();
        this.playersInCurrentMatch.add(playerName);

        console.printMessage("Match created successfully: " + matchName);
        console.printMessage("You are now in match: " + matchName + (isHost ? " (as Host)" : ""));
        dirty.set(true);   
    }


    public void setAvailableMatches(List<String> matches) {
        if (matches == null || matches.isEmpty()) {
            console.printMessage("No available matches found.");
            this.availableMatches.clear();
        } else {
            this.availableMatches = new ArrayList<>(matches);
            console.printMessage("Available matches:");
            for (int i = 0; i < availableMatches.size(); i++) {
                console.printMessage((i + 1) + ". " + availableMatches.get(i));
            }
        }
        dirty.set(true);
    }
    
    public void playerJoinedMatchCallback(UUID matchId, String playerName) {
        if (currentMatchId == null) {
            currentMatchId = matchId;
            playersInCurrentMatch.clear();
        }
        if (!currentMatchId.equals(matchId)) {
            return;
        }

        if (!playersInCurrentMatch.contains(playerName)) {
            playersInCurrentMatch.add(playerName);
        }
        console.printMessage("Player " + playerName + " has joined the match.");
        dirty.set(true);
    }


    public void playerLeftMatchCallback(UUID matchId, int playerId, String playerName) {
        if (currentMatchId == null || !currentMatchId.equals(matchId)) {
            return;
        }
        boolean me = (playerId == this.playerId);
        playersInCurrentMatch.remove(playerName);
        console.printMessage("Player " + playerName + " has left the match.");

        if (me) {
            currentMatchId = null;
            isHost = false;
            playersInCurrentMatch.clear();
        }

        dirty.set(true);
    }   

    public void updateHostCallback(UUID matchId, int newHostId, String newHostName) {
        if (currentMatchId == null || !currentMatchId.equals(matchId)) {
            return;
        }
        isHost = (newHostId == this.playerId);
        console.printMessage(
            "The new host is now: " + newHostName + (isHost ? " — you are now the host." : ""));

        dirty.set(true);
    }

    public void playersInMatchCallback(UUID matchId, List<String> playerNames, int hostId) {
        currentMatchId = matchId;
        playersInCurrentMatch.clear();
        playersInCurrentMatch.addAll(playerNames);
        isHost = (matchId != null && hostId == playerId);
        dirty.set(true);
    }

    public void matchStartedCallback(UUID matchId, String username) {
        if (currentMatchId == null || !currentMatchId.equals(matchId)) {
            return;
        }
        console.printMessage("Loading match... Please wait.");
    }

    public void loadingStatusCallback(UUID matchId, int playersReady, int totalPlayers) {
        if (currentMatchId == null || !currentMatchId.equals(matchId)) {
            return;
        }
        
    }

    public void playerReadyCallback(UUID matchId, int readyPlayers, int totalPlayers /*, String username */) {
        if (currentMatchId == null || !currentMatchId.equals(matchId)) {
            return;
        }
        console.printMessage("Player " + readyPlayers + " out of " + totalPlayers + " are ready for the match.");
    }

    public void shipBuildingStartCallback(UUID matchId, int readyPlayers) {
        if (currentMatchId == null || !currentMatchId.equals(matchId)) {
            return;
        }
        console.printMessage("Ship building phase started with " + readyPlayers + " players ready.");
    }



}