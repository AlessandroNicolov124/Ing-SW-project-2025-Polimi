package it.polimi.ingsw.Components;

import it.polimi.ingsw.*;
import it.polimi.ingsw.Visitors.Visitor;

public class StructuralModule extends Component {

    public StructuralModule() {
        super();
    }

    @Override
    public void connectTo(Component c, Direction dir) {
        connections.add(new Connector(dir, this));
        System.out.println("Motore connesso a " + c.getClass().getSimpleName() + " nella direzione " + dir);
    }

    @Override
    public void disconnect() {
        connections.clear();
        System.out.println("Motore disconnesso.");
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

}


