package it.polimi.ingsw.view.tui.components;

import it.polimi.ingsw.network.rmi.RmiClient;
import it.polimi.ingsw.network.socket.client.SocketClient;


public class CommandExecutor {
    private final Object client;
    private final ConsoleManager console;

    public CommandExecutor(Object client, ConsoleManager console) {
        this.client = client;
        this.console = console;
    }


    // Find a better way to handle commands avoding to use instanceof
    public boolean executeCommand(String command) {
        try {
            switch (command.toLowerCase()) {
                case "x":
                    if (client instanceof RmiClient) {
                        ((RmiClient) client).sendX();
                    } else if (client instanceof SocketClient) {
                        ((SocketClient) client).sendX();
                    }
                    break;
                case "y":
                    if (client instanceof RmiClient) {
                        ((RmiClient) client).sendY();
                    } else if (client instanceof SocketClient) {
                        ((SocketClient) client).sendY();
                    }
                    break;
                case "help":
                    console.printHelp();
                    break;
                case "quit":
                    if (client instanceof AutoCloseable) {
                        ((AutoCloseable) client).close();
                    }
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