package it.polimi.ingsw;

import it.polimi.ingsw.Components.*;
import it.polimi.ingsw.Visitors.Visitor;

public class DiagnosticVisitor implements Visitor {
    @Override
    public void visit(Engine engine) {
        System.out.println("Performing diagnostic on Engine of type: " + engine.getType());
        // Add specific diagnostic logic for engines
    }

    @Override
    public void visit(Cabin cabin) {
        System.out.println("Performing diagnostic on Cabin of type: " + cabin.getType());
        // Add specific diagnostic logic for cabins
    }

    @Override
    public void visit(Cannon cannon) {
        System.out.println("Performing diagnostic on Cannon of type: " + cannon.getType());
        // Add specific diagnostic logic for cannons
    }

    @Override
    public void visit(BatteryCompartment batteryCompartment) {
        System.out.println("Performing diagnostic on Battery Compartment");
        // Add specific diagnostic logic
    }

    @Override
    public void visit(CargoHolds cargoBay) {
        System.out.println("Performing diagnostic on Cargo Bay of type: " + cargoBay.getType());
        System.out.println("Cargo items: " + cargoBay.getCargos().size());
        // Add specific diagnostic logic
    }

    @Override
    public void visit(LifeSupportSystem lifeSupportSystem) {
        System.out.println("Performing diagnostic on Life Support System");
        // Add specific diagnostic logic
    }

    @Override
    public void visit(ShieldGenerator shieldGenerator) {
        System.out.println("Performing diagnostic on Shield Generator");
        // Add specific diagnostic logic
    }

    @Override
    public void visit(StructuralModule structuralModule) {
        System.out.println("Performing diagnostic on Structural Module");
        // Add specific diagnostic logic
    }
}
