package it.polimi.ingsw.Components;

import it.polimi.ingsw.*;

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


