package it.polimi.ingsw.Components;

public class LifeSupportSystem extends Component {

    private LifeSupportSystemType type;

    public LifeSupportSystem(LifeSupportSystemType type) {
        super();
        this.type = type;
    }

    public LifeSupportSystemType getType() {
        return type;
    }

    public void setType(LifeSupportSystemType type) {
        this.type = type;
    }


}
