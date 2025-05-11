package it.polimi.ingsw.network.commands;

import it.polimi.ingsw.GameController;

import java.io.Serializable;

public interface Command extends Serializable {
    void execute(GameController controller);

}
