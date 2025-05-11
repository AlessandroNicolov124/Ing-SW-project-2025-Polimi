package it.polimi.ingsw.network.commands;

import it.polimi.ingsw.GameController;
import it.polimi.ingsw.Player;

public class PlayerReadyCommand implements Command{
    private String nickName;
    public PlayerReadyCommand(PlayerDTO player) {
        this.nickName = player.getPlayerName();
    }

    @Override
    public void execute(GameController controller) {
        synchronized (controller) {
            Player pl = controller.getPlayerByNickName(nickName);
            if (pl != null) {
                controller.setPlayerReady(pl);
                System.out.println("Player " + this.nickName + " is ready");
                System.out.println("Player state: " + controller.getPlayerByNickName(nickName).getReady());
            }
        }
    }
}
