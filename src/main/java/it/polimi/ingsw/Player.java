package it.polimi.ingsw;

import it.polimi.ingsw.AdventureCards.*;
import it.polimi.ingsw.Components.Component;

import java.io.Serializable;

public class Player implements Serializable {
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
        this.ship = new ShipBuilder(7, 5);
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

    public void takeTurn(){

    }

    public void pickCard(AdventureCard card) {
        if (card != null) {
            this.pickedCard = card; // Assegna la carta pescata al giocatore
            System.out.println(nickName + " ha pescato una carta: " + card.getClass().getSimpleName());
            // Note: Card effects are applied on the server side through GameController.applyAdventureCardEffect()
        } else {
            System.out.println("Nessuna carta disponibile da pescare.");
        }
    }

    public AdventureCard getPickedCard() {
        return pickedCard;
    }

    public boolean placeComponent(Component component, Position pos) {
        return ship.placeComponent(component, pos);
    }

    public boolean removeComponentAt(Position position) {
        return ship.removeComponentAt(position);
    }

    // public boolean removeComponent(Component component) {
    //     return ship.removeComponent(component);
    // }

    public boolean validateShip() {
        String errors = ship.validateShip();
        return errors == null;
    }

    public String getShipValidationErrors() {
        return ship.validateShip();
    }


}
