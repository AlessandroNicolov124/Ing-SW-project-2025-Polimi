package it.polimi.ingsw.Components;

import it.polimi.ingsw.*;
import it.polimi.ingsw.Visitors.Visitor;

// Classe Engine
public class BatteryCompartment extends Component {
    private int amount; // numero di batterie
    private BatteryType type;
    private int chargeLevel; // carica totale

    public BatteryCompartment() {
        super();
    }

    public BatteryCompartment(int amount, BatteryType type, int chargeLevel) {
        super();
        this.amount = amount;
        this.type = type;
        this.chargeLevel = chargeLevel;
    }

    public void updateChargeLevel(int chargeLevel) {
        this.chargeLevel = chargeLevel;
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
    public BatteryType getType() {
        return type;
    }
    public void setType(BatteryType type) {
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
