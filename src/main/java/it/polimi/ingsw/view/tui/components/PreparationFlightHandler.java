package it.polimi.ingsw.view.tui.components;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import it.polimi.ingsw.Direction;
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
        
        try {
            client.getPlayerShip(currentPlayer.getNickName());
            client.getPlayerTokenList(currentPlayer.getNickName());
        } catch (Exception e) {
            console.printError("Error loading data: " + e.getMessage());
        }

        while (continuePreparation) {
            displayShip();
            showMenu();
            String choice = console.readCommand().trim().toLowerCase();
            switch (choice) {
                case "1": placeHuman(); break;
                case "2": placePurpleAlien(); break;
                case "3": placeBrownAlien(); break;
                case "4": placeBattery(); break;
                case "5": removeToken(); break;
                case "6": seeOtherPlayerShip(); break;
                case "7": readyToFly(); break;
                case "d": break;
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
        synchronized (console) {
            console.printMessage("\n--- Your Ship ---");
            int width  = 7;
            int height = 5;
            int gh = height * 3, gw = width * 3;
            String[][] grid = new String[gh][gw];
            for (int i = 0; i < gh; i++)
                for (int j = 0; j < gw; j++)
                    grid[i][j] = "    ";

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    Position pos = new Position(x, y);
                    Component comp = shipComponents.get(pos);
                    int by = y * 3, bx = x * 3;
                    if (comp != null) {
                        grid[by+1][bx+1] = "[" + getComponentSymbol(comp) + "]";
                        for (Direction d : Direction.values()) {
                            Connector conn = comp.getConnectorByDirection(d);
                            if (conn != null && !(conn.getConnectorType() instanceof NullConnectorType)) {
                                String sym = "[" + getConnectionSymbol(conn) + "]";
                                switch (d) {
                                    case NORTH -> grid[by  ][bx+1] = sym;
                                    case SOUTH -> grid[by+2][bx+1] = sym;
                                    case EAST  -> grid[by+1][bx+2] = sym;
                                    case WEST  -> grid[by+1][bx  ] = sym;
                                }
                            }
                        }
                    }
                    // verifica token e se presente sovrascrive centro
                    List<TokenType> cellTokens = tokens.get(pos);
                    if (cellTokens != null && !cellTokens.isEmpty()) {
                        grid[by+1][bx+1] = "[" + getTokenSymbol(cellTokens.get(0)) + "]";
                    }
                }
            }

            // intestazione X
            StringBuilder hdr = new StringBuilder("      ");
            for (int x = 0; x < width; x++) hdr.append("     ").append(x).append("     ");
            console.printMessage(hdr.toString());

            // stampa righe con etichetta Y solo sulla riga centrale di ogni cella
            for (int i = 0; i < gh; i++) {
                StringBuilder row = new StringBuilder();
                if (i % 3 == 1) row.append("Y:").append(i/3).append("   ");
                else           row.append("      ");
                for (int j = 0; j < gw; j++) row.append(grid[i][j]);
                console.printMessage(row.toString());
            }
            console.printMessage("------------------------------------");
        }
    }

    private String getConnectionSymbol(Connector connector) {
        if (connector == null) {
            return "  "; // Empty for no connector
        }

        return switch (connector.getConnectorType()) {
            case SingleConnectorType s -> "S-";
            case DoubleConnectorType d -> "D=";
            case UniversalConnectorType u -> "U≡";
            case NullConnectorType n -> "  ";
            default -> "??";
        };
    }


    private String getTokenSymbol(TokenType t) {
        switch (t) {
            case HUMAN: return "H";
            case PURPLE_ALIEN: return "P";
            case BROWN_ALIEN: return "B";
            case BATTERY: return "E";
            default: return "?";
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