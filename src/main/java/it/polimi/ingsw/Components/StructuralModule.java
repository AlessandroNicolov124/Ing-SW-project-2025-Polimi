package it.polimi.ingsw.Components;

import it.polimi.ingsw.*;

/**
 * Represents a structural module in the game.
 * Structural modules are the basic building blocks of a ship.
 * They can be connected to other components and can have various connections.
 */
public final class StructuralModule extends Component {

    public StructuralModule() {
        super();
    }

    @Override
    public String getComponentString() {
        String s = this.getClass().getSimpleName().toLowerCase() + "_";
        s += getConnectionsString();
        return s;
    }


}


