package it.polimi.ingsw.Components;

import it.polimi.ingsw.*;
import it.polimi.ingsw.Visitors.Visitor;

public class LifeSupport extends Component {

    public LifeSupport() {
        super();
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

}
