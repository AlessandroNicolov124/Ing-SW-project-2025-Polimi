package it.polimi.ingsw.Components;

import it.polimi.ingsw.*;
import it.polimi.ingsw.Visitors.Visitor;

// Classe Engine
public class Engine extends Component {
    private String type;

    public Engine() {
        super();
    }
    public Engine(String type) {
        super();
        this.type = type;
    }

    @Override
    public void connectTo(Component c, Direction dir) {
        connections.add(new Connector(dir, c));
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

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
