package it.polimi.ingsw.Components;

import it.polimi.ingsw.*;
import it.polimi.ingsw.Visitors.Visitor;

// Classe Engine
public class BatteryCompartment extends Component {
    private int amount;
    private String type;
    private int chargeLevel;

    public BatteryCompartment() {
        super();
    }
    public BatteryCompartment(int amount, String type, int chargeLevel) {
        super();
        this.amount = amount;
        this.type = type;
        this.chargeLevel = chargeLevel;
    }

    public void setType(String type) {
        this.type = type;
    }
    public void updateChargeLevel(int chargeLevel) {
        this.chargeLevel = chargeLevel;
        //this.chargeLevel += charge;
    }
    public void setAmount(int amount) {
        this.amount = amount;
    }


    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public int getAmount() {
        return amount;
    }
    public String getType() {
        return type;
    }
    public int getChargeLevel() {
        return chargeLevel;
    }
}
