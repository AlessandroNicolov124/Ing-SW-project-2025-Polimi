package it.polimi.ingsw.Components;

import it.polimi.ingsw.Visitors.Visitor;

public class ShieldGenerator extends Component {
    
    public ShieldGenerator() {
        super();
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

}

