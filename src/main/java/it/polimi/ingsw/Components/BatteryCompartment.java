package it.polimi.ingsw.Components;

import it.polimi.ingsw.*;
import it.polimi.ingsw.Visitors.Visitor;

// Classe Engine
public class BatteryCompartment extends Component {
    private int amount;
    private String type;

    public BatteryCompartment() {
        super();
    }
    public BatteryCompartment(int amount, String type) {
        super();
        this.amount = amount;
        this.type = type;
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

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public int getAmount() {
        return amount;
    }
}
