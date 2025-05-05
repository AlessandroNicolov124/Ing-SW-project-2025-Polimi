package it.polimi.ingsw.view.tui.components;

import it.polimi.ingsw.network.client.GameClient;


public class TuiController {
    private final GameClient client;
    private final ConsoleManager console;
    private final int playerId;

    public TuiController(GameClient client, ConsoleManager console, int playerId) {
        this.client = client;
        this.console = console;
        this.playerId = playerId;
    }


    public boolean executeCommand(String command) {
        try {
            switch (command.toLowerCase()) {
                case "help":
                    console.printHelp();
                    break;
                case "quit":
                    client.close();
                    return false;
                default:
                    console.printError("Unknown command. Type 'help' for available commands.");
            }
        } catch (Exception e) {
            console.printError(e.getMessage());
        }
        return true;
    }
}