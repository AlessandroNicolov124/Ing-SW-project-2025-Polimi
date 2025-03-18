package it.polimi.ingsw;

import it.polimi.ingsw.Components.*;
import it.polimi.ingsw.AdventureCards.*;

import java.util.*;

public class ShipBuilder {

    private Position position;
    private Map<Component, Position> components;
    private boolean isAssembled;

    public ShipBuilder() {
        this.position = new Position(0, 0); // Posizione iniziale
        this.components = new HashMap<>();
        this.isAssembled = false;
    }

    public ShipBuilder(Position position, Map<Component, Position> components) {
        this.position = position;
        this.components = components;
        this.isAssembled = false;
    }

    // Convalida la posizione per assicurarsi che non ci siano sovrapposizioni
    private boolean validatePosition(Position pos) {
        for (Position existingPos : components.values()) {
            if (existingPos.getX() == pos.getX() && existingPos.getY() == pos.getY()) {
                return false; // Posizione già occupata
            }
        }
        return true; // Posizione valida
    }

    public void addComponent(Component component, Position pos) {
        if (validatePosition(pos)) {
            components.put(component, pos);
            System.out.println("Componente aggiunto: " + component.getClass().getSimpleName() + " alla posizione " + pos.getX() + ", " + pos.getY());
        } else {
            System.out.println("Posizione non valida per il componente.");
        }
    }

    public Component getCompByPosition(Position pos){
        return null;
    }

    // Rimuove un componente dalla nave
    public void removeComponent(Component component) {
        if (components.containsKey(component)) {
            components.remove(component);
            System.out.println("Componente rimosso: " + component.getClass().getSimpleName());
        } else {
            System.out.println("Componente non trovato nella nave.");
        }
    }

    // Convalida la nave per assicurarsi che tutti i componenti necessari siano presenti
    public boolean validateShip() {
        // Esempio di validazione: controlla se ci sono almeno un motore e una cabina
        boolean hasEngine = components.keySet().stream().anyMatch(c -> c instanceof Engine);
        boolean hasCabin = components.keySet().stream().anyMatch(c -> c instanceof Cabin);

        isAssembled = hasEngine && hasCabin; // La nave è assemblata se ha almeno un motore e una cabina
        return isAssembled;
    }


    // Restituisce la posizione della nave
    public Position getPosition() {
        return position;
    }

    // Imposta la posizione della nave
    public void setPosition(Position position) {
        this.position = position;
    }

    // Restituisce i componenti della nave
    public Map<Component, Position> getComponents() {
        return components;
    }

    // Restituisce lo stato di assemblaggio della nave
    public boolean isAssembled() {
        return isAssembled;
    }


}
