package it.polimi.ingsw;

public abstract class Crew {
    private int count;

    public Crew(int count) {
        this.count = count;
    }
    public Crew(){
        this.count = 0;
    }

    public int getCount() {
        return count;
    }

}
