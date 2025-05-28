package it.polimi.ingsw.network.socket.messages;

import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.network.socket.client.SocketClient;

public class CompPickedMessage implements ClientMessage{
    private final Component component;
    private final boolean success;

    public CompPickedMessage(Component c, boolean s)
    {
        this.component = c;
        this.success = s;
    }

    public Component getComponent(){
        return this.component;
    }

    public boolean getSuccess(){
        return this.success;
    }

    @Override
    public void handleClient(SocketClient client) {
        if (success) {
            client.showUpdate("Received component from server : " + component);
        }
    }
}
