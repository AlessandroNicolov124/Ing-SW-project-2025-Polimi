package it.polimi.ingsw.Components;

import it.polimi.ingsw.*;

public final class BatteryCompartment extends Component {
    private int maxCapacity; // numero di batterie
    private BatteryType type;
    private int chargeLevel; // carica

    public BatteryCompartment() {
        super();
    }

    @Override
    public String getComponentString() {
        String s = this.getClass().getSimpleName().toLowerCase() + "_";
        s += type.toString().toLowerCase() + "_" + getConnectionsString();
        return s;
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

    public BatteryType getType() {
        return type;
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

//    public boolean useCharge(int requiredCharge) {
//        if (chargeLevel >= requiredCharge) {
//            chargeLevel -= requiredCharge;
//            return true;
//        }
//        return false;
//    }

    /**
     * Ricarica la batteria della quantità specificata.
     *
     * @param amount La quantità di energia da ricaricare
     * @return La quantità effettivamente ricaricata
     */
    public int recharge(int amount) {
        if (amount <= 0) {
            return 0;
        }

        int spaceLeft = maxCapacity - chargeLevel;
        int actualRecharge = Math.min(spaceLeft, amount);
        chargeLevel += actualRecharge;
        return actualRecharge;
    }

    /**
     * Scarica la batteria della quantità specificata.
     *
     * @param amount La quantità di energia da scaricare
     * @return La quantità effettivamente scaricata
     */
    public int discharge(int amount) {
        if (amount <= 0) {
            return 0;
        }

        int actualDischarge = Math.min(chargeLevel, amount);
        chargeLevel -= actualDischarge;
        return actualDischarge;
    }
}
