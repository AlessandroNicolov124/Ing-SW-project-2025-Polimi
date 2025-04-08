package it.polimi.ingsw.Components;

public class BatteryCompartment extends Component {
    private int maxCapacity;
    private BatteryType type;
    private int chargeLevel;

    public BatteryCompartment() {
        super();
    }

    public BatteryCompartment(int maxCapacity, BatteryType type, int chargeLevel) {
        super();
        this.maxCapacity = maxCapacity;
        this.type = type;
        this.chargeLevel = chargeLevel;
    }

    public void updateChargeLevel(int chargeLevel) {
        this.chargeLevel = chargeLevel;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
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

    public void setChargeLevel(int chargeLevel) {
        this.chargeLevel = chargeLevel;
    }

    // Metodo per ricaricare la batteria
    public void recharge() {
        chargeLevel = maxCapacity;
    }

    public boolean useCharge(int requiredCharge) {
        if (chargeLevel >= requiredCharge) {
            chargeLevel -= requiredCharge;
            return true;
        }
        return false;
    }
}
