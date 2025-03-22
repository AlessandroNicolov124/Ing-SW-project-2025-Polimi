package it.polimi.ingsw.Components;

import it.polimi.ingsw.*;
import it.polimi.ingsw.Visitors.Visitor;

import java.util.*;

public class CargoBay extends Component{

    private String type;
    private List<Cargo> cargos;

    public CargoBay() {
        super();
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public CargoBay(String type, List<Cargo> cargos) {
        this.type=type;
        this.cargos=cargos;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Cargo> getCargos() {
        return new ArrayList<>(cargos.stream().toList());
    }

    public void loadCargo(Cargo cargo) {
        cargos.add(cargo);
    }

    public void unloadCargo(Cargo cargo) {
        cargos.remove(cargo);
    }

    public void decrementCargo(int pos, int quantity) {
        cargos.get(pos).decrementAmount(quantity);
    }

    public Cargo getCargo(int pos){
        return cargos.get(pos);
    }
}
