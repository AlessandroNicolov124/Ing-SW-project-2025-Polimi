package it.polimi.ingsw.Components;

public final class LifeSupportSystem extends Component {

    private LifeSupportSystemType type;

    public LifeSupportSystem(LifeSupportSystemType type) {
        super();
        this.type = type;
    }

    public LifeSupportSystemType getType() {
        return type;
    }

}
