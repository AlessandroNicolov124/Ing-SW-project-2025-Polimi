package it.polimi.ingsw.Visitors;

import it.polimi.ingsw.Components.*;

// 1. Ship value calculator visitor
public class ShipValueVisitor implements Visitor {
    private int totalValue = 0;

    public int getTotalValue() {
        return totalValue;
    }

    @Override
    public void visit(Engine engine) {
        // Engines are valuable components
//        if ("Fusion".equals(engine.getType())) {
//            totalValue += 300;
//        } else if ("Ion".equals(engine.getType())) {
//            totalValue += 200;
//        } else {
//            totalValue += 100;  // Standard engine
//      }
    }

    @Override
    public void visit(Cabin cabin) {
        // Cabins house crew
        if ("Command".equals(cabin.getType())) {
            totalValue += 150;  // Command cabins are more valuable
        } else {
            totalValue += 100;
        }
    }

    @Override
    public void visit(Cannon cannon) {
        // Cannons provide defense
//        if ("Heavy".equals(cannon.getType())) {
//            totalValue += 200;
//        } else if ("Double".equals(cannon.getType())) {
//            totalValue += 180;
//        } else {
//            totalValue += 100;
//        }
    }

    @Override
    public void visit(BatteryCompartment batteryCompartment) {
        // Value based on battery capacity
        totalValue += 50 + (batteryCompartment.getAmount() * 10);
    }

    @Override
    public void visit(CargoHolds cargoBay) {
        // Value based on cargo capacity and content
        totalValue += 75 + (cargoBay.getCargos().size() * 25);
    }

    @Override
    public void visit(LifeSupportSystem lifeSupportSystem) {
        // Essential component
        totalValue += 150;
    }

    @Override
    public void visit(ShieldGenerator shieldGenerator) {
        // Defensive component
        totalValue += 200;
    }

    @Override
    public void visit(StructuralModule structuralModule) {
        // Basic component
        totalValue += 50;
    }
}
