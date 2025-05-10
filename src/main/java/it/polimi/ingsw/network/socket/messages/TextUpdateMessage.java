package it.polimi.ingsw.network.socket.messages;

import it.polimi.ingsw.network.socket.client.SocketClient;

public class TextUpdateMessage implements ClientMessage {

    private static final long serialVersionUID = 1L;

    private final String text;

    public TextUpdateMessage(String text) {
        this.text = text;
    }

    public String text() {
        return text;
    }

    @Override
    public void handleClient(SocketClient cli) {
        cli.showUpdate(text);
    }

}