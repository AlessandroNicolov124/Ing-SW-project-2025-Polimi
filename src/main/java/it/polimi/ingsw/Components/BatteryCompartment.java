package it.polimi.ingsw.Components;

import it.polimi.ingsw.*;
import it.polimi.ingsw.Visitors.Visitor;

// Classe Engine
public class BatteryCompartment extends Component {
    private int amount; // numero di batterie
    private String type; // tipo doppio o triplo
    private int chargeLevel; // carica totale

    public BatteryCompartment() {
        super();
    }

    public BatteryCompartment(int amount, String type, int chargeLevel) {
        super();
        this.amount = amount;
        this.type = type;
        this.chargeLevel = chargeLevel;
    }

    public void initializeConnectors() {
        connections.add(new Connector(Direction.NORTH, ConnectorType.UNIVERSAL));
        connections.add(new Connector(Direction.EAST, ConnectorType.UNIVERSAL));
        connections.add(new Connector(Direction.SOUTH, ConnectorType.UNIVERSAL));
        connections.add(new Connector(Direction.WEST, ConnectorType.UNIVERSAL));
    }

    public void updateChargeLevel(int chargeLevel) {
        this.chargeLevel = chargeLevel;
        //this.chargeLevel += charge;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public int getChargeLevel() {
        return chargeLevel;
    }

    public boolean useCharge(int value) {
        if (amount >= value) {
            amount -= value;
            return true;
        }
        return false;
    }
}
