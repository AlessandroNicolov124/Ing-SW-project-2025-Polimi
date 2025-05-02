package it.polimi.ingsw;

import it.polimi.ingsw.AdventureCards.*;
import it.polimi.ingsw.Components.Component;

public class Player {
    private int id;
    private String nickName;
    private boolean active;
    private ShipBuilder ship;
    private AdventureCard pickedCard; // Carta pescata dal giocatore
    private boolean readyToStart;

    public Player(int id, String nickName, boolean active) {
        this.id = id;
        this.nickName = nickName;
        this.active = active;
        this.ship = new ShipBuilder(10, 10);
        this.readyToStart = false;
    }

    public Player(int id, String nickName, boolean active, ShipBuilder ship) {
        this.id = id;
        this.nickName = nickName;
        this.active = active;
        this.ship = ship;
    }

    public int getId() {
        return id;
    }

    public String getNickName() {
        return nickName;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public ShipBuilder getShip() {
        return ship;
    }

    public void setShip(ShipBuilder ship) {
        this.ship = ship;
    }

    public void setReady(boolean ready)
    {
        this.readyToStart = ready;
    }

    public boolean getReady ()
    {
        return this.readyToStart;
    }

    public void assembleShip(){
        ship.setAssembled(true);
    }

    public void takeTurn(){

    }

    public void pickCard(AdventureCard card) {
        if (card != null) {
            this.pickedCard = card; // Assegna la carta pescata al giocatore
            System.out.println(nickName + " ha pescato una carta: " + card.getClass().getSimpleName());

            //  metodo per applicare l'effetto della carta, se necessario
            card.effect(); // Esempio di applicazione dell'effetto della carta
        } else {
            System.out.println("Nessuna carta disponibile da pescare.");
        }
    }

    public boolean placeComponent(Component component, Position pos) {
        return ship.placeComponent(component, pos);
    }

    public void removeComponent(Component component) {
        ship.removeComponent(component);
    }

    public boolean validateShip() {
        return ship.validateShip();
    }

    public void shipAssembled(){
        assembleShip();
    }
}
