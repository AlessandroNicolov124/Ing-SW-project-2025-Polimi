package it.polimi.ingsw.view.tui.components;


import it.polimi.ingsw.network.client.GeneralClientIf;

public class TuiController {
    private final GeneralClientIf client;
    private final ConsoleManager console;
    private final int playerId;

    public TuiController(GeneralClientIf client, ConsoleManager console, int playerId) {
        this.client = client;
        this.console = console;
        this.playerId = playerId;
    }


    public boolean executeCommand(String command) {
        try {
            switch (command.toLowerCase()) {
//                case "x":
//                    if (client instanceof RmiClient) {
//                        ((RmiClient) client).sendX();
//                    } else if (client instanceof SocketClient) {
//                        ((SocketClient) client).sendX();
//                    }
//                    break;
//                case "y":
//                    if (client instanceof RmiClient) {
//                        ((RmiClient) client).sendY();
//                    } else if (client instanceof SocketClient) {
//                        ((SocketClient) client).sendY();
//                    }
//                    break;
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