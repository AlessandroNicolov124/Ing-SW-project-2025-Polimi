package it.polimi.ingsw.Components;

import it.polimi.ingsw.*;
import it.polimi.ingsw.Visitors.Visitor;

public class StructuralModule extends Component {

    public StructuralModule() {
        super();
    }


    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

}


