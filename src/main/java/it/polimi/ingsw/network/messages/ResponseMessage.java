package it.polimi.ingsw.network.messages;

import it.polimi.ingsw.network.client.listeners.GameListener;

public interface ResponseMessage extends CommonMessage {
    
    void handleClient(GameListener listener);

}