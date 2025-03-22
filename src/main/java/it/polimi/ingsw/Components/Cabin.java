package it.polimi.ingsw.Components;

import it.polimi.ingsw.*;
import it.polimi.ingsw.Visitors.Visitor;

import java.util.ArrayList;
import java.util.List;

// Classe Cabin
public class Cabin extends Component {
    private String type;
    private List<Crew> crew;

    public Cabin() {
        super();
        crew = new ArrayList<>();
    }

    public Cabin(String type) {
        super();
        this.type = type;
        this.crew = new ArrayList<>();
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

    public Crew getCrewByIndex(int index) {
        return crew.get(index);
    }
}
