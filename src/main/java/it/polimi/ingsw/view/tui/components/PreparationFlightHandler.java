package it.polimi.ingsw.view.tui.components;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import it.polimi.ingsw.Player;
import it.polimi.ingsw.Position;
import it.polimi.ingsw.TokenType;
import it.polimi.ingsw.Components.*;
import it.polimi.ingsw.network.client.GeneralClientIf;

public class PreparationFlightHandler {

    private final GeneralClientIf client;
    private final ConsoleManager console;
    private final Player currentPlayer;
    
    private Map<Position, Component> shipComponents = new HashMap<>();
    private Map<Position, List<TokenType>> tokens = new HashMap<>();
    private boolean continuePreparation = true;

    public PreparationFlightHandler(GeneralClientIf client, ConsoleManager console, Player currentPlayer) {
        this.client = client;
        this.console = console;
        this.currentPlayer = currentPlayer;
    }

    public void managePreparationFlight() {
        console.printMessage("\n=== FLIGHT PREPARATION PHASE ===");
        console.printMessage("Loading your ship...");
        
        // Load ship data
        try {
            client.getPlayerShip(currentPlayer.getNickName());
            Thread.sleep(1000);
            client.getPlayerTokenList(currentPlayer.getNickName());
            Thread.sleep(1000);
        } catch (Exception e) {
            console.printError("Error loading data: " + e.getMessage());
        }

        while (continuePreparation) {
            displayShip();
            showMenu();
            
            String choice = console.readCommand().trim();
            
            switch (choice) {
                case "1": placeHuman(); break;
                case "2": placePurpleAlien(); break;
                case "3": placeBrownAlien(); break;
                case "4": placeBattery(); break;
                case "5": removeToken(); break;
                case "6": seeOtherPlayerShip(); break;
                case "7": readyToFly(); break;
                case "d": displayShip(); break;
                case "q": 
                    console.printMessage("Exiting preparation...");
                    continuePreparation = false;
                    break;
                default:
                    console.printError("Invalid choice!");
            }
        }
    }

    private void showMenu() {
        console.printMessage("\n--- Preparation Menu ---");
        console.printMessage("1. Place Human in Cabin");
        console.printMessage("2. Place Purple Alien in Cabin");
        console.printMessage("3. Place Brown Alien in Cabin");
        console.printMessage("4. Place Battery in Battery Compartment");
        console.printMessage("5. Remove Token");
        console.printMessage("6. See Other Player's Ship");
        console.printMessage("7. Ready to Fly");
        console.printMessage("D. Display Ship");
        console.printMessage("Q. Quit");
        console.printMessage("Choice: ");
    }

    private void displayShip() {
        console.printMessage("\n--- Your Ship ---");
        
        if (shipComponents.isEmpty()) {
            console.printMessage("No ship data loaded yet.");
            return;
        }

        // Simple grid display
        for (int y = 0; y < 5; y++) {
            StringBuilder row = new StringBuilder();
            for (int x = 0; x < 7; x++) {
                Position pos = new Position(x, y);
                Component comp = shipComponents.get(pos);
                
                if (comp != null) {
                    String symbol = getComponentSymbol(comp);
                    List<TokenType> posTokens = tokens.get(pos);
                    
                    if (posTokens != null && !posTokens.isEmpty()) {
                        // Show component with token count
                        row.append("[" + symbol + ":" + posTokens.size() + "] ");
                    } else {
                        row.append("[" + symbol + "]  ");
                    }
                } else {
                    row.append("     ");
                }
            }
            console.printMessage(row.toString());
        }
        
        // Show token details
        console.printMessage("\nTokens placed:");
        for (Map.Entry<Position, List<TokenType>> entry : tokens.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                Position pos = entry.getKey();
                console.printMessage("  (" + pos.getX() + "," + pos.getY() + "): " + entry.getValue());
            }
        }
    }

    private String getComponentSymbol(Component comp) {
        if (comp instanceof Cabin) return "CA";
        if (comp instanceof BatteryCompartment) return "BC";
        if (comp instanceof SingleCannon) return "C1";
        if (comp instanceof DoubleCannon) return "C2";
        if (comp instanceof SingleEngine) return "E1";
        if (comp instanceof DoubleEngine) return "E2";
        if (comp instanceof CargoHolds) return "CH";
        if (comp instanceof ShieldGenerator) return "SG";
        if (comp instanceof LifeSupportSystem) return "LS";
        if (comp instanceof StructuralModule) return "SM";
        return "??";
    }

    private void placeHuman() {
        placeToken(TokenType.HUMAN, "Human");
    }

    private void placePurpleAlien() {
        placeToken(TokenType.PURPLE_ALIEN, "Purple Alien");
    }

    private void placeBrownAlien() {
        placeToken(TokenType.BROWN_ALIEN, "Brown Alien");
    }

    private void placeBattery() {
        placeToken(TokenType.BATTERY, "Battery");
    }

    private void placeToken(TokenType tokenType, String name) {
        console.printMessage("\n--- Place " + name + " ---");
        
        try {
            String xStr = console.askQuestion("Enter X coordinate:");
            String yStr = console.askQuestion("Enter Y coordinate:");
            
            int x = Integer.parseInt(xStr);
            int y = Integer.parseInt(yStr);
            Position pos = new Position(x, y);
            
            Component comp = shipComponents.get(pos);
            if (comp == null) {
                console.printError("No component at that position!");
                return;
            }
            
            // Check if valid placement
            boolean valid = false;
            if (tokenType == TokenType.BATTERY && comp instanceof BatteryCompartment) {
                valid = true;
            } else if (tokenType != TokenType.BATTERY && comp instanceof Cabin) {
                valid = true;
            }
            
            if (!valid) {
                console.printError("Cannot place " + name + " on " + comp.getClass().getSimpleName());
                return;
            }
            
            console.printMessage("Placing " + name + "...");
            client.addTokenToComponent(currentPlayer.getNickName(), pos, tokenType);
            
        } catch (Exception e) {
            console.printError("Error: " + e.getMessage());
        }
    }

    private void removeToken() {
        console.printMessage("\n--- Remove Token ---");
        
        if (tokens.isEmpty()) {
            console.printMessage("No tokens placed yet!");
            return;
        }
        
        try {
            String xStr = console.askQuestion("Enter X coordinate:");
            String yStr = console.askQuestion("Enter Y coordinate:");
            
            int x = Integer.parseInt(xStr);
            int y = Integer.parseInt(yStr);
            Position pos = new Position(x, y);
            
            List<TokenType> posTokens = tokens.get(pos);
            if (posTokens == null || posTokens.isEmpty()) {
                console.printError("No tokens at that position!");
                return;
            }
            
            TokenType lastToken = posTokens.get(posTokens.size() - 1);
            console.printMessage("Removing " + lastToken + "...");
            
            client.removeTokenFromComponent(currentPlayer.getNickName(), pos, lastToken);
            
        } catch (Exception e) {
            console.printError("Error: " + e.getMessage());
        }
    }

    private void seeOtherPlayerShip() {
        String playerName = console.askQuestion("Enter player name:");
        
        try {
            console.printMessage("Requesting ship data...");
            client.getPlayerShip(playerName);
        } catch (Exception e) {
            console.printError("Error: " + e.getMessage());
        }
    }

    private void readyToFly() {
        String confirm = console.askQuestion("Ready to fly? (yes/no):");
        
        if ("yes".equalsIgnoreCase(confirm)) {
            console.printMessage("Marking as ready...");
            continuePreparation = false;
            // Note: The actual ready call should be handled by the parent TUI
        }
    }

    // Methods for callbacks
    public void updateShipComponents(Map<Position, Component> components) {
        this.shipComponents.clear();
        this.shipComponents.putAll(components);
    }

    public void updateTokens(Map<Position, List<TokenType>> newTokens) {
        this.tokens.clear();
        this.tokens.putAll(newTokens);
    }

    public void tokenPlaced(Position pos, TokenType token, boolean success) {
        if (success) {
            tokens.computeIfAbsent(pos, k -> new ArrayList<>()).add(token);
            console.printMessage("✓ Token placed successfully!");
        } else {
            console.printError("✗ Failed to place token!");
        }
    }

    public void tokenRemoved(Position pos, TokenType token, boolean success) {
        if (success) {
            List<TokenType> list = tokens.get(pos);
            if (list != null) {
                list.remove(token);
                if (list.isEmpty()) {
                    tokens.remove(pos);
                }
            }
            console.printMessage("✓ Token removed successfully!");
        } else {
            console.printError("✗ Failed to remove token!");
        }
    }

    public void displayOtherPlayerShip(String playerName, Map<Position, Component> otherShip) {
        console.printMessage("\n=== " + playerName + "'s Ship ===");
        
        for (int y = 0; y < 5; y++) {
            StringBuilder row = new StringBuilder();
            for (int x = 0; x < 7; x++) {
                Position pos = new Position(x, y);
                Component comp = otherShip.get(pos);
                
                if (comp != null) {
                    row.append("[" + getComponentSymbol(comp) + "] ");
                } else {
                    row.append("    ");
                }
            }
            console.printMessage(row.toString());
        }
        
        console.printMessage("\nPress Enter to continue...");
        console.readCommand();
    }
}