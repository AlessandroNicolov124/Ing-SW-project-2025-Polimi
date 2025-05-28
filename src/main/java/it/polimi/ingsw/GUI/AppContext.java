package it.polimi.ingsw.GUI;

import it.polimi.ingsw.Components.Component;
import it.polimi.ingsw.Player;
import it.polimi.ingsw.Position;
import it.polimi.ingsw.network.client.GeneralClientIf;
import it.polimi.ingsw.network.client.listeners.GameListener;
import it.polimi.ingsw.network.socket.messages.RotationType;

import java.util.List;

public class AppContext implements GameListener {
    private String username;
    private String connectionType;
    private String serverAddress;
    private Player localPlayerReference;
    private int serverPort;
    private int playerId;

    private GeneralClientIf client;

    public AppContext() {
        username = "";
        connectionType = "";
        serverAddress = "";
        serverPort = 0;
        playerId = 0;

        client = null;
    }

    public Player getLocalPlayerReference() {
        return localPlayerReference;
    }

    public void setLocalPlayerReference(Player localPlayerReference) {
        this.localPlayerReference = localPlayerReference;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(String connectionType) {
        this.connectionType = connectionType;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public GeneralClientIf getClient() {
        return client;
    }

    public void setClient(GeneralClientIf client) {
        this.client = client;
    }


    @Override
    public void displayRefresh() {

    }

    @Override
    public void componentPlaced(Component component, Position position) {

    }

    @Override
    public void componentRemoved(Position position) {

    }

    @Override
    public void componentRotated(Position position, RotationType rotationType) {

    }

    @Override
    public void shipValidated(boolean isValid, String message) {

    }

    @Override
    public void showAvailablePositions(List<Integer> positions) {

    }

    @Override
    public boolean notifyNewPlayerPosition(int playerId, int position) {
        return false;
    }

    @Override
    public void printTextMsg(String message) {

    }
}
