package it.polimi.ingsw.Components;

import it.polimi.ingsw.*;
import it.polimi.ingsw.Visitors.Visitor;

import java.util.ArrayList;
import java.util.List;

// Classe Cabin
public class Cabin extends Component {
    private List<Crew> crew;

    @Override
    public void initializeConnectors() {
        // Cabina standard ha connettori universali su tutti i lati
        connections.add(new Connector(Direction.NORTH, ConnectorType.UNIVERSAL));
        connections.add(new Connector(Direction.EAST, ConnectorType.UNIVERSAL));
        connections.add(new Connector(Direction.SOUTH, ConnectorType.UNIVERSAL));
        connections.add(new Connector(Direction.WEST, ConnectorType.UNIVERSAL));
    }

    public Cabin() {
        super();
        this.crew = new ArrayList<>();
        initializeConnectors();
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public Crew getCrewByIndex(int index) {
        return crew.get(index);
    }

    public List<Crew> getCrew() {
        return crew;
    }
}
