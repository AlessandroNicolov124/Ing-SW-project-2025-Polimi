package it.polimi.ingsw;

public abstract class Crew {
    private String type;
    private int count;

    public Crew(String type, int count) {
        this.type = type;
        this.count = count;
    }
    public Crew(){
        this.type = "";
        this.count = 0;
    }

    public String getType() {
        return type;
    }
    public int getCount() {
        return count;
    }

}
