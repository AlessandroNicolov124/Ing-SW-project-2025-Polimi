package it.polimi.ingsw.Components;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

public class StructuralModuleTest {

    private StructuralModule structuralModule;

    @BeforeEach
    public void setUp() {
        structuralModule = new StructuralModule();
        structuralModule.initializeConnectors(new UniversalConnectorType(), new NullConnectorType(), new NullConnectorType(), new UniversalConnectorType());
    }

    // ===== CONSTRUCTION TESTS =====

    @Test
    @DisplayName("StructuralModule should initialize with default Component values")
    public void testDefaultConstruction() {
        assertNotNull(structuralModule);
        assertFalse(structuralModule.isDamaged()); // Inherited from Component
    }

    @Test
    @DisplayName("StructuralModule should extend Component")
    public void testInheritance() {
        assertTrue(structuralModule instanceof Component);
    }

    // ===== COMPONENT STRING TESTS =====

    @Test
    @DisplayName("getComponentString should return correct format")
    public void testGetComponentString() {
        String componentString = structuralModule.getComponentString();

        assertNotNull(componentString);
        assertTrue(componentString.startsWith("structuralmodule_"));

        // The component string should contain the class name in lowercase followed by underscore
        // and then the connections string from the parent Component class
        String expectedPrefix = "structuralmodule_";
        assertTrue(componentString.startsWith(expectedPrefix));
    }

    @Test
    @DisplayName("getComponentString should use class simple name in lowercase")
    public void testGetComponentStringFormat() {
        String componentString = structuralModule.getComponentString();
        String className = structuralModule.getClass().getSimpleName().toLowerCase();

        assertTrue(componentString.startsWith(className + "_"));
    }

    @Test
    @DisplayName("Should inherit damage functionality from Component")
    public void testInheritedDamageFunctionality() {
        // Test initial state
        assertFalse(structuralModule.isDamaged());

        // Test setting damaged state
        structuralModule.setDamaged(true);
        assertTrue(structuralModule.isDamaged());

        // Test resetting damaged state
        structuralModule.setDamaged(false);
        assertFalse(structuralModule.isDamaged());
    }

    @Test
    @DisplayName("Should have access to inherited Component methods")
    public void testInheritedMethods() {
        // Test that methods from Component are accessible
        assertDoesNotThrow(() -> {
            structuralModule.isDamaged();
            structuralModule.setDamaged(true);
            structuralModule.setDamaged(false);
            structuralModule.getComponentString();
        });
    }


}