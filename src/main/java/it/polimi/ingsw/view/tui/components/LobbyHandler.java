package it.polimi.ingsw.view.tui.components;

import it.polimi.ingsw.network.client.GeneralClientIf;
import java.util.List;
import java.util.UUID;

public class LobbyHandler {
    private final GeneralClientIf simpleClient;
    private final ConsoleManager console;
    private final int playerId;
    private UUID currentMatchId;
    private boolean isHost;
    private List<String> availableMatches;
    private List<String> playersInCurrentMatch;

    public LobbyHandler(ConsoleManager console, GeneralClientIf simpleClient, int playerId) {
        this.console = console;
        this.simpleClient = simpleClient;
        this.playerId = playerId;
    }

    public boolean handleLobbyMenu() {
        try {
            showLobbyMenu();
            String command = console.readCommand();
            return executeCommand(command);
        } catch (Exception e) {
            console.printError("Error in lobby: " + e.getMessage());
            return true;
        }
    }

    public boolean isInMatch() {
        return currentMatchId != null;
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
            if (playersInCurrentMatch != null) {
                console.printMessage("Players in match: " + playersInCurrentMatch);
            }
            console.printMessage("1. Leave match");
            if (isHost) {
                console.printMessage("2. Start match (requires at least 2 players)");
            }
            console.printMessage("quit. Exit application");
        }
        console.printMessage("Enter command: ");
    }

    public boolean executeCommand(String command) {
        try {
            if (currentMatchId == null) {
                return handleLobbyCommands(command);
            } else {
                return handleMatchCommands(command);
            }
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
                if (isHost) {
                    startMatch();
                } else {
                    console.printError("Only the host can start the match.");
                }
                break;
            case "help":
                showLobbyMenu();
                break;
            case "quit":
                if (currentMatchId != null) {
                    leaveMatch();
                }
                simpleClient.close();
                return false;
            default:
                console.printError("Unknown command. Available commands: 1" + (isHost ? ", 2" : "") + ", help, quit");
        }
        return true;
    }

    private void listAvailableMatches() throws Exception {
        console.printMessage("Requesting available matches...");
        availableMatches = simpleClient.getAvailableMatches();
        if (availableMatches == null || availableMatches.isEmpty()) {
            console.printMessage("No available matches found.");
            availableMatches = null;
        } else {
            console.printMessage("Available matches:");

            for (String match : availableMatches) {
                console.printMessage(match);
            }
        }
    }

    private void createNewMatch() throws Exception {
        String matchName = console.askQuestion("Enter match name:");
        if (matchName.trim().isEmpty()) {
            console.printError("Match name cannot be empty.");
            return;
        }
        console.printMessage("Creating match: " + matchName);
        UUID matchId = simpleClient.createMatch(matchName);
        this.currentMatchId = matchId;
        this.isHost = simpleClient.isPlayerHostById(matchId); 
        this.playersInCurrentMatch = simpleClient.getPlayersInMatch(matchId);
    }

    private void joinMatch() throws Exception {
        if (availableMatches == null || availableMatches.isEmpty()) {
            console.printMessage("No available matches");
            return;
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
                simpleClient.joinMatch(matchId);
                this.currentMatchId = matchId;
                this.isHost = simpleClient.isPlayerHostById(matchId);
                this.playersInCurrentMatch = simpleClient.getPlayersInMatch(matchId);

            } else {
                console.printError("Invalid match number.");
            }
        } catch (NumberFormatException e) {
            console.printError("Invalid input. Please enter a number.");
        } catch (Exception e) {
            console.printError("Error joining match: " + e.getMessage());
        }
    }

    //TODO: fix logic to handle leaving the match, remember to update the host correctly
    private void leaveMatch() throws Exception {
        if (currentMatchId != null) {
            console.printMessage("Leaving match...");
            simpleClient.leaveMatch(currentMatchId);
            currentMatchId = null;
            isHost = simpleClient.isPlayerHostById(currentMatchId);
            playersInCurrentMatch = null;
            console.printMessage("Left the match. Returning to lobby menu.");
        }
    }

    // TODO: modify logic to handle starting the match
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
            simpleClient.startMatch(currentMatchId);
        } else {
            console.printError("Cannot start match: not the host or no active match.");
        }
    }

    public void updateAvailableMatches() throws Exception {
        availableMatches = simpleClient.getAvailableMatches();
    }
    
    public void updatePlayersInMatch() throws Exception {
        if (currentMatchId != null) {
            playersInCurrentMatch = simpleClient.getPlayersInMatch(currentMatchId);
            console.printMessage("Updated players in match: " + playersInCurrentMatch);
        } 
    }


}