package it.polimi.ingsw.network.commands;

import java.io.Serializable;

public class PlayerDTO implements Serializable {

    private final String playerName;
    //private final int credits;

    public PlayerDTO(String playerName) {
        this.playerName = playerName;
    }

    public String getPlayerName() {
        return playerName;
    }

}