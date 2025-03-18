package it.polimi.ingsw;

public class Cargo {
    private int value;
    private int amount;
    private String Color;


    public void setValue(int value) {
        this.value = value;
    }
    public void setAmount(int amount) {
        this.amount = amount;
    }
    public void setColor(String Color) {
        this.Color = Color;
    }
    public void decrementAmount() {
        amount--;
    }
    public void decrementAmount(int quantity) {
        amount -= quantity;
    }

    public Cargo() {}
}

