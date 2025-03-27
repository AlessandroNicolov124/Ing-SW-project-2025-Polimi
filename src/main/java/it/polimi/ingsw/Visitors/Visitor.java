package it.polimi.ingsw.Visitors;

import it.polimi.ingsw.Components.*;

public interface Visitor {
    void visit(Engine engine);
    void visit(Cabin cabin);
    void visit(Cannon cannon);
    void visit(BatteryCompartment batteryCompartment);
    void visit(CargoHolds cargoBay);
    void visit(LifeSupportSystem lifeSupportSystem);
    void visit(ShieldGenerator shieldGenerator);
    void visit(StructuralModule structuralModule);
    // Add more visit methods for other components as needed
}

