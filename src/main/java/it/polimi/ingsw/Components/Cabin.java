package it.polimi.ingsw.Components;

import it.polimi.ingsw.*;
import it.polimi.ingsw.Visitors.Visitor;

// Classe Cabin
public class Cabin extends Component {
    private String type;

    public Cabin() {
        super();
    }
    public Cabin(String type) {
        super();
        this.type = type;
    }

    @Override
    public void connectTo(Component c, Direction dir) {
        // Logica per connettere la cabina a un altro componente
        connections.add(new Connector(dir, this));
        System.out.println("Cabina connessa a " + c.getClass().getSimpleName() + " nella direzione " + dir);
    }

    @Override
    public void disconnect() {
        // Logica per disconnettere la cabina
        connections.clear();
        System.out.println("Cabina disconnessa.");
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
