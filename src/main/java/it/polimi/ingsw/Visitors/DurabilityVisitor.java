package it.polimi.ingsw.Visitors;

import it.polimi.ingsw.Components.*;

// 2. Ship durability visitor
public class DurabilityVisitor implements Visitor {
    private int structuralIntegrity = 0;
    private int shieldStrength = 0;

    public int getStructuralIntegrity() {
        return structuralIntegrity;
    }

    public int getShieldStrength() {
        return shieldStrength;
    }

    public int getTotalDurability() {
        return structuralIntegrity + shieldStrength;
    }

    @Override
    public void visit(Engine engine) {
        structuralIntegrity += 2;  // Engines provide some structure
    }

    @Override
    public void visit(Cabin cabin) {
        structuralIntegrity += 3;  // Cabins are sturdy
    }

    @Override
    public void visit(Cannon cannon) {
        structuralIntegrity += 1;  // Cannons are somewhat fragile
    }

    @Override
    public void visit(BatteryCompartment batteryCompartment) {
        structuralIntegrity += 2;
    }

    @Override
    public void visit(CargoBay cargoBay) {
        structuralIntegrity += 4;  // Cargo bays are reinforced
    }

    @Override
    public void visit(LifeSupport lifeSupport) {
        structuralIntegrity += 2;
    }

    @Override
    public void visit(ShieldGenerator shieldGenerator) {
        structuralIntegrity += 2;
        shieldStrength += 8;  // Major shield contribution
    }

    @Override
    public void visit(StructuralModule structuralModule) {
        structuralIntegrity += 5;  // Major structural contribution
    }
}
