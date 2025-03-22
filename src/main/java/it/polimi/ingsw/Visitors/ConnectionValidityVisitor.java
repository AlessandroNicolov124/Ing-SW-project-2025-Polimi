package it.polimi.ingsw.Visitors;

import it.polimi.ingsw.Components.*;
import it.polimi.ingsw.Components.Connector;

import java.util.List;

// 4. Connection validity visitor (checks if ship components are properly connected)
public class ConnectionValidityVisitor implements Visitor {
    private boolean isValid = true;
    private StringBuilder validationReport = new StringBuilder();

    public boolean isValid() {
        return isValid;
    }

    public String getValidationReport() {
        return validationReport.toString();
    }

    @Override
    public void visit(Engine engine) {
        validateConnections(engine, "Engine");
    }

    @Override
    public void visit(Cabin cabin) {
        validateConnections(cabin, "Cabin");
    }

    @Override
    public void visit(Cannon cannon) {
        validateConnections(cannon, "Cannon");
    }

    @Override
    public void visit(BatteryCompartment batteryCompartment) {
        validateConnections(batteryCompartment, "Battery Compartment");
    }

    @Override
    public void visit(CargoHolds cargoBay) {
        validateConnections(cargoBay, "Cargo Bay");
    }

    @Override
    public void visit(LifeSupportSystem lifeSupportSystem) {
        validateConnections(lifeSupportSystem, "Life SupportSystem");
    }

    @Override
    public void visit(ShieldGenerator shieldGenerator) {
        validateConnections(shieldGenerator, "Shield Generator");
    }

    @Override
    public void visit(StructuralModule structuralModule) {
        validateConnections(structuralModule, "Structural Module");
    }

    private void validateConnections(Component component, String componentType) {
        List<Connector> connections = component.getConnections();

        if (connections.isEmpty()) {
            isValid = false;
            validationReport.append(componentType).append(" has no connections! ");
        }

        // Additional validation logic based on Galaxy Trucker rules could go here
        // For example: Engines must be on the edge of the ship, cabins need life support nearby, etc.
    }
}
