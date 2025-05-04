package it.polimi.ingsw.network.commands;

import it.polimi.ingsw.GameController;
import it.polimi.ingsw.Player;

public class AddPlayerCommand implements Command {
    private final PlayerDTO playerData;

    public AddPlayerCommand(PlayerDTO player) {
        this.playerData = player;

    }

    @Override
    public void execute(GameController controller) {
        synchronized (controller) {
            Player pl = new Player(0, this.playerData.getPlayerName(), true);
            controller.addPlayer(pl);
            System.out.println("Added player " + this.playerData.getPlayerName());
            System.out.println("Controller: " + controller);
        }
    }
}