package it.polimi.ingsw.Components;

import it.polimi.ingsw.Visitors.Visitor;

public class LifeSupportSystem extends Component {

    private LifeSupportSystemType type;

    public LifeSupportSystem(LifeSupportSystemType type) {
        super();
        this.type = type;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

}
