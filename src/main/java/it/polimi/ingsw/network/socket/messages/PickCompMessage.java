package it.polimi.ingsw.network.socket.messages;

import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.Player;
import it.polimi.ingsw.network.socket.server.SocketClientHandler;

public class PickCompMessage implements ServerMessage {

    private int playerId;
    public PickCompMessage(int plID) {
        this.playerId = plID;
    }


    @Override
    public void handleServer(SocketClientHandler h) {
        System.out.println("picking component for player: " + this.playerId);
        Player p = h.getController().getPlayerById(this.playerId);
        if (p != null) {
            Component comp = h.getController().getCompFromDeck(p);
            h.sendUpdate(new CompPickedMessage(comp, true));
        }
        else{
            System.out.println("An error occured pickong component for " + this.playerId);
            h.sendUpdate(new CompPickedMessage(null, false));
        }

    }
}
