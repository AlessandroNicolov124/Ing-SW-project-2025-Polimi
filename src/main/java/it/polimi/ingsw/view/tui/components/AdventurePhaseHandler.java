package it.polimi.ingsw.view.tui.components;

import it.polimi.ingsw.Player;
import it.polimi.ingsw.network.client.GeneralClientIf;
import it.polimi.ingsw.AdventureCards.AdventureCard;
import it.polimi.ingsw.AdventureCards.Planets;

import java.util.*;

public class AdventurePhaseHandler {
    
    private final ConsoleManager console;
    private final GeneralClientIf client;
    private final Player currentPlayer;
    private final GalaxyTruckerTUI parentTUI; // Reference to parent TUI for state checking
    
    private Map<Integer, Integer> currentPlayerPositions = new HashMap<>();
    private boolean isActive = false;
    
    // Planet selection state
    private boolean waitingForPlanetSelection = false;
    private List<Planets.Planet> currentAvailablePlanets = null;
    private int currentFlightDaysCost = 0;
    private int currentSelectingPlayerId = -1;
    private String currentSelectingPlayerName = "";
    
    public AdventurePhaseHandler(ConsoleManager console, GeneralClientIf client, Player currentPlayer, GalaxyTruckerTUI parentTUI) {
        this.console = console;
        this.client = client;
        this.currentPlayer = currentPlayer;
        this.parentTUI = parentTUI;
    }
    
    /**
     * Initialize the adventure phase with starting positions
     */
    public void initializePhase(Map<Integer, Integer> playerPositions, int firstPlayerToAct) {
        this.currentPlayerPositions = new HashMap<>(playerPositions);
        this.isActive = true;
        
        console.printMessage("\nALL PLAYERS POSITIONED! Starting Adventure Phase!");
        console.printMessage("=== STARTING POSITIONS (0-23, lower = better for winning) ===");
        
        // Sort players by position (best to worst for flight race)
        List<Map.Entry<Integer, Integer>> sortedEntries = new ArrayList<>(playerPositions.entrySet());
        sortedEntries.sort(Map.Entry.comparingByValue()); // Sort by position value (ascending)
        
        // Display flight race standings
        int rank = 1;
        for (Map.Entry<Integer, Integer> entry : sortedEntries) {
            int playerId = entry.getKey();
            int position = entry.getValue();
            String playerName = (currentPlayer != null && currentPlayer.getId() == playerId) ? 
                "You" : "Player " + playerId;
            
            String rankStr = getRankString(rank);
            console.printMessage(rankStr + " in flight race (pos " + position + "): " + playerName);
            rank++;
        }
        
        // Show who picks adventure cards (player with highest position number = best flight position)
        String cardPickerName = (currentPlayer != null && currentPlayer.getId() == firstPlayerToAct) ? 
            "You" : "Player " + firstPlayerToAct;
        
        // Find the position of the card picker
        int cardPickerPosition = playerPositions.getOrDefault(firstPlayerToAct, -1);
        
        console.printMessage("\n ADVENTURE CARD PICKER: " + cardPickerName + " (position " + cardPickerPosition + ")");
        
        console.printMessage("\n=============================================");
        console.printMessage("ADVENTURE CARD RULES:");
        console.printMessage("• LEADING player (highest position number) picks cards!");
        console.printMessage("• Flight day costs move you to LOWER position numbers (worse positions)");
        console.printMessage("• Multiple players losing flight days move in reverse order");
        console.printMessage("• Players cannot overlap - if a position is occupied, move to next available position");
        
        // Check if we are the card picker
        if (currentPlayer != null && currentPlayer.getId() == firstPlayerToAct) {
            console.printMessage("\n You are the adventure card picker! Type 'pick' to draw a card for all players.");
        } else {
            console.printMessage("\n Waiting for " + cardPickerName + " to pick an adventure card...");
        }
    }
    
    /**
     * Main loop for handling adventure phase commands
     */
    public void handleAdventurePhase() {
        console.printMessage("\n=== ADVENTURE CARD PHASE ===");
        console.printMessage("Only the LEADING player (highest position number) can pick adventure cards for everyone");
        console.printMessage("Wait for the card picker to pick or type 'status' to check current state");
        
        while (isActive && parentTUI.isInAdventurePhaseState()) {
            String cmd = console.pollCommand(1000); // Poll with timeout
            
            if (cmd != null) {
                if (!executeCommand(cmd.trim().toLowerCase())) {
                    break; // Exit requested
                }
            }
        }
    }
    
    /**
     * Execute a command during adventure phase
     */
    private boolean executeCommand(String cmd) {
        // Handle planet selection commands
        if (waitingForPlanetSelection && (currentPlayer != null && currentPlayer.getId() == currentSelectingPlayerId)) {
            if (cmd.matches("\\d+")) {
                // Player entered a planet number
                try {
                    int planetIndex = Integer.parseInt(cmd) - 1; // Convert from 1-based to 0-based index
                    if (planetIndex >= 0 && planetIndex < currentAvailablePlanets.size()) {
                        try {
                            client.selectPlanet(currentPlayer.getId(), planetIndex);
                            console.printMessage("Planet selection sent. Waiting for confirmation...");
                            return true;
                        } catch (Exception e) {
                            console.printError("Error sending planet selection: " + e.getMessage());
                        }
                    } else {
                        console.printMessage("Invalid planet number. Please choose 1-" + currentAvailablePlanets.size() + " or 'skip' to not land.");
                    }
                } catch (NumberFormatException e) {
                    console.printMessage("Invalid input. Please enter a number.");
                }
                return true;
            } else if (cmd.equals("skip") || cmd.equals("none") || cmd.equals("0")) {
                // Player chooses not to land
                try {
                    client.selectPlanet(currentPlayer.getId(), -1);
                    console.printMessage("Chose not to land. Waiting for confirmation...");
                } catch (Exception e) {
                    console.printError("Error sending planet selection: " + e.getMessage());
                }
                return true;
            }
        }
        
        // Regular adventure phase commands
        switch (cmd) {
            case "pick":
                // Try to pick an adventure card (only leader can)
                try {
                    client.pickAdventureCard(currentPlayer.getId());
                    console.printMessage("Adventure card pick request sent. Waiting for response...");
                } catch (Exception e) {
                    console.printError("Error picking adventure card: " + e.getMessage());
                }
                break;
            case "status":
                showStatus();
                break;
            case "help":
                showHelp();
                break;
            case "quit":
                console.printMessage("Exiting game...");
                isActive = false;
                return false; // Signal to exit
            default:
                if (!waitingForPlanetSelection) {
                    console.printMessage("Unknown command: " + cmd + ". Type 'help' for available commands.");
                } else {
                    console.printMessage("Invalid planet selection. Type 'help' for available commands.");
                }
                break;
        }
        return true; // Continue
    }
    
    /**
     * Show current status
     */
    private void showStatus() {
        if (currentPlayer != null) {
            console.printMessage("Player: " + currentPlayer.getNickName() + " (ID: " + currentPlayer.getId() + ")");
            
            if (waitingForPlanetSelection) {
                console.printMessage("Game state: Waiting for planet selection");
                console.printMessage("Current player choosing: " + currentSelectingPlayerName);
                if (currentPlayer.getId() == currentSelectingPlayerId) {
                    console.printMessage("It's your turn to choose a planet!");
                    displayPlanetOptions();
                }
            } else {
                console.printMessage("Game state: Adventure Phase - only leader can pick cards");
            }
        }
    }
    
    /**
     * Show available commands
     */
    private void showHelp() {
        if (waitingForPlanetSelection && (currentPlayer != null && currentPlayer.getId() == currentSelectingPlayerId)) {
            console.printMessage("Planet Selection Commands:");
            console.printMessage("  1-" + currentAvailablePlanets.size() + " - Select planet by number");
            console.printMessage("  skip   - Don't land on any planet");
            console.printMessage("  status - Show current planet options");
            console.printMessage("  help   - Show this help message");
            console.printMessage("  quit   - Exit the game");
        } else {
            console.printMessage("Available commands:");
            console.printMessage("  pick   - Pick an adventure card (only if you are the leader)");
            console.printMessage("  status - Show current game status");
            console.printMessage("  help   - Show this help message");
            console.printMessage("  quit   - Exit the game");
        }
    }
    
    /**
     * Handle adventure card picked response
     */
    public void handleAdventureCardPicked(int playerId, AdventureCard adventureCard, boolean success, 
                                        String message, int nextPlayerTurn, Map<Integer, Integer> updatedPositions) {
        if (success && adventureCard != null) {
            // Find player name for display
            String playerName = (currentPlayer != null && currentPlayer.getId() == playerId) ? 
                "You" : "Player " + playerId;
            
            console.printMessage("✓ The leading player " + playerName + " picked adventure card: " + adventureCard.getName());
            
            // Show detailed card information for everyone
            displayAdventureCard(adventureCard);
            // Note: The actual card effect is applied on the server side
            console.printMessage("Adventure card effects have been applied to the game state.");
            
            // Display updated positions if provided
            if (updatedPositions != null && !updatedPositions.isEmpty()) {
                Map<Integer, Integer> previousPositions = new HashMap<>(currentPlayerPositions);
                currentPlayerPositions = new HashMap<>(updatedPositions); // Make a defensive copy
                displayPositionUpdates(previousPositions, currentPlayerPositions);
            }
            
            // Check if there are more adventure cards to pick
            if (nextPlayerTurn == -2) { // Special value indicating deck is empty or game ended
                console.printMessage("Adventure card deck is empty. Waiting for game completion notification...");
            } else {
                console.printMessage("Waiting for the new leading player to pick the next adventure card...");
                console.printMessage("(The leader may have changed due to card effects)");
            }
        } else {
            console.printError("✗ Failed to pick adventure card: " + message);
        }
    }
    
    /**
     * Handle adventure phase completion
     */
    public void handlePhaseComplete(boolean gameComplete, String completionReason) {
        isActive = false;
        console.printMessage("\n=== ADVENTURE PHASE COMPLETE ===");
        console.printMessage("Reason: " + completionReason);
        
        if (gameComplete) {
            console.printMessage(" GAME OVER! ");
            console.printMessage("Final results will be calculated...");
        } else {
            console.printMessage("Adventure phase finished. Proceeding to main game...");
        }
    }
    
    /**
     * Displays detailed information about an adventure card
     */
    private void displayAdventureCard(AdventureCard adventureCard) {
        console.printMessage("\n╔══════════════════════════════════════════════════════════════════════╗");
        console.printMessage("║                        ADVENTURE CARD DRAWN                         ║");
        console.printMessage("╠══════════════════════════════════════════════════════════════════════╣");
        console.printMessage("║ Card Name: " + String.format("%-55s", adventureCard.getName()) + " ║");
        console.printMessage("║                                                                      ║");

        // Split description into multiple lines if it's too long
        String description = adventureCard.getDescription();
        String[] descriptionLines = wrapText(description, 68);
        console.printMessage("║ Description:                                                         ║");
        for (String line : descriptionLines) {
            console.printMessage("║ " + String.format("%-68s", line) + " ║");
        }

        console.printMessage("║                                                                      ║");

        // Show additional properties if available
        if (adventureCard.getDuration() > 1) {
            console.printMessage("║ Duration: " + String.format("%-59s", adventureCard.getDuration() + " rounds") + " ║");
        }
        if (adventureCard.getCost() > 0) {
            console.printMessage("║ Cost: " + String.format("%-63s", adventureCard.getCost() + " credits") + " ║");
        }

        console.printMessage("╚══════════════════════════════════════════════════════════════════════╝");
        console.printMessage("");
    }
    
    /**
     * Display position changes after adventure card effects
     */
    private void displayPositionUpdates(Map<Integer, Integer> previousPositions, Map<Integer, Integer> currentPositions) {
        console.printMessage("\n=== FLIGHT BOARD POSITIONS (0-23, higher = better) ===");
        
        // Track if any position actually changed
        boolean hasChanges = false;
        List<String> changeDetails = new ArrayList<>();
        
        // Find all players and their positions
        Map<Integer, String> playerNames = new HashMap<>(); // position -> player names at that position
        for (Map.Entry<Integer, Integer> entry : currentPositions.entrySet()) {
            int playerId = entry.getKey();
            int position = entry.getValue();
            
            String playerName = (currentPlayer != null && currentPlayer.getId() == playerId) ? 
                "You" : "Player " + playerId;
            
            // Check if position changed
            Integer previousPos = previousPositions.get(playerId);
            if (previousPos != null && !previousPos.equals(position)) {
                hasChanges = true;
                if (position > previousPos) {
                    // Higher position number = better position = moved forward
                    int improvement = position - previousPos;
                    playerName += " (↑" + improvement + ")";
                    changeDetails.add(playerName.replace(" (↑" + improvement + ")", "") + " moved forward " + improvement + " positions");
                } else {
                    // Lower position number = worse position = moved backward
                    int decline = previousPos - position;
                    playerName += " (↓" + decline + ")";
                    changeDetails.add(playerName.replace(" (↓" + decline + ")", "") + " moved backward " + decline + " positions");
                }
            } else if (previousPos == null) {
                // Player wasn't tracked before - this shouldn't happen during adventure phase
                changeDetails.add(playerName + " position tracking started");
            }
            
            playerNames.put(position, playerNames.getOrDefault(position, "") + 
                (playerNames.containsKey(position) ? ", " : "") + playerName);
        }
        
        // Display positions from best to worst (highest to lowest position numbers)
        List<Integer> sortedPositions = new ArrayList<>(playerNames.keySet());
        sortedPositions.sort(Collections.reverseOrder()); // Descending order (23, 22, 21, ... 0)
        
        // Create rank based on the sorted order
        int rank = 1;
        for (int position : sortedPositions) {
            String playersAtPosition = playerNames.get(position);
            String positionLabel = getPositionLabel(rank);
            console.printMessage("Position " + position + " " + positionLabel + ": " + playersAtPosition);
            rank++;
        }
        
        // Show position changes summary
        if (hasChanges) {
            console.printMessage("\nPosition changes from this card:");
            for (String change : changeDetails) {
                console.printMessage("  • " + change);
            }
        } else {
            // Check if positions are identical
            boolean identicalMaps = previousPositions.equals(currentPositions);
            if (identicalMaps) {
                console.printMessage("(No position changes from this card)");
            } else {
                console.printMessage("(Position data updated but no player movements detected)");
            }
        }
        
        console.printMessage("================================================\n");
    }
    
    /**
     * Get a descriptive label for a rank
     */
    private String getPositionLabel(int rank) {
        switch (rank) {
            case 1: return "(1st place)";
            case 2: return "(2nd place)";
            case 3: return "(3rd place)";
            case 4: return "(4th place)";
            default: return "(" + rank + "th place)";
        }
    }
    
    /**
     * Convert rank number to ordinal string
     */
    private String getRankString(int rank) {
        switch (rank) {
            case 1: return "1st";
            case 2: return "2nd";
            case 3: return "3rd";
            case 4: return "4th";
            default: return rank + "th";
        }
    }
    
    /**
     * Wraps text to fit within specified width
     */
    private String[] wrapText(String text, int maxWidth) {
        if (text.length() <= maxWidth) {
            return new String[]{text};
        }

        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            if (currentLine.length() + word.length() + 1 <= maxWidth) {
                if (currentLine.length() > 0) {
                    currentLine.append(" ");
                }
                currentLine.append(word);
            } else {
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                } else {
                    // Word is longer than maxWidth, just add it anyway
                    lines.add(word);
                }
            }
        }

        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        return lines.toArray(new String[0]);
    }
    
    /**
     * Check if the handler is currently active
     */
    public boolean isActive() {
        return isActive;
    }
    
    /**
     * Deactivate the handler
     */
    public void deactivate() {
        isActive = false;
    }

    /**
     * Handle planet choice request
     */
    public void handlePlanetChoiceRequested(UUID matchId, List<Planets.Planet> availablePlanets, int flightDaysCost, int currentPlayerId, String currentPlayerName) {
        this.waitingForPlanetSelection = true;
        this.currentAvailablePlanets = new ArrayList<>(availablePlanets);
        this.currentFlightDaysCost = flightDaysCost;
        this.currentSelectingPlayerId = currentPlayerId;
        this.currentSelectingPlayerName = currentPlayerName;
        
        console.printMessage("\n=== PLANET SELECTION PHASE ===");
        console.printMessage("A Planets card has been drawn!");
        console.printMessage("Flight days cost: " + flightDaysCost);
        console.printMessage("Current player choosing: " + currentPlayerName);
        
        if (currentPlayer != null && currentPlayer.getId() == currentPlayerId) {
            console.printMessage("\n*** IT'S YOUR TURN TO CHOOSE A PLANET! ***");
            displayPlanetOptions();
            console.printMessage("\nEnter the number of the planet you want (1-" + availablePlanets.size() + ") or 'skip' to not land:");
        } else {
            console.printMessage("\nWaiting for " + currentPlayerName + " to make their selection...");
        }
    }
    
    /**
     * Handle planet selection result
     */
    public void handlePlanetSelected(int playerId, String playerName, int planetIndex, String planetName, boolean success, String message) {
        if (success) {
            if (planetIndex >= 0) {
                console.printMessage("✓ " + playerName + " selected planet: " + planetName);
                
                // Remove selected planet from available list if it was taken
                if (currentAvailablePlanets != null) {
                    currentAvailablePlanets.removeIf(planet -> planet.getName().equals(planetName));
                }
            } else {
                console.printMessage("✓ " + playerName + " chose not to land on any planet");
            }
            
            // Check if this was our selection
            if (currentPlayer != null && currentPlayer.getId() == playerId) {
                waitingForPlanetSelection = false;
                console.printMessage("Your selection has been confirmed.");
            }
        } else {
            console.printError("✗ Planet selection failed for " + playerName + ": " + message);
            
            // If it was our failed selection, keep waiting for a valid choice
            if (currentPlayer != null && currentPlayer.getId() == playerId) {
                console.printMessage("Please try selecting a planet again:");
                displayPlanetOptions();
            }
        }
    }
    
    /**
     * Display the current planet options to the player
     */
    private void displayPlanetOptions() {
        if (currentAvailablePlanets == null || currentAvailablePlanets.isEmpty()) {
            console.printMessage("No planets available for selection.");
            return;
        }
        
        console.printMessage("\n╔══════════════════════════════════════════════════════════════════════╗");
        console.printMessage("║                        AVAILABLE PLANETS                            ║");
        console.printMessage("║                     Cost: " + String.format("%-2d", currentFlightDaysCost) + " flight days                               ║");
        console.printMessage("╠══════════════════════════════════════════════════════════════════════╣");
        
        for (int i = 0; i < currentAvailablePlanets.size(); i++) {
            Planets.Planet planet = currentAvailablePlanets.get(i);
            String planetInfo = (i + 1) + ". " + planet.getName() + 
                              " (Cargo: " + planet.getCargoCount() + 
                              " units, Value: " + planet.getTotalCargoValue() + ")";
            console.printMessage("║ " + String.format("%-68s", planetInfo) + " ║");
        }
        
        console.printMessage("║                                                                      ║");
        console.printMessage("║ Options:                                                             ║");
        console.printMessage("║   • Enter planet number (1-" + String.format("%-2d", currentAvailablePlanets.size()) + ") to land and collect cargo      ║");
        console.printMessage("║   • Type 'skip' to not land on any planet                           ║");
        console.printMessage("╚══════════════════════════════════════════════════════════════════════╝");
    }
} 