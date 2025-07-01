package it.polimi.ingsw.Components;

import it.polimi.ingsw.*;

public final class StructuralModule extends Component {
    private static final long serialVersionUID = 1L;

    public StructuralModule() {
        super();
    }

    @Override
    protected String calculateComponentString() {
        String s = this.getClass().getSimpleName().toLowerCase() + "_";
        s += getConnectionsString();
        return s;
    }


}


