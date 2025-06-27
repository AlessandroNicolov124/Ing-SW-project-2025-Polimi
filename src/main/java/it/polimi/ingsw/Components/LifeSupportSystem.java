package it.polimi.ingsw.Components;

/** life support system is a component that can be connected to a ship. */
public final class LifeSupportSystem extends Component {

    private LifeSupportSystemType type;

    public LifeSupportSystem(LifeSupportSystemType type) {
        super();
        this.type = type;
    }

    public LifeSupportSystemType getType() {
        return type;
    }

    @Override
    public String getComponentString() {
        String s = this.getClass().getSimpleName().toLowerCase() + "_";
        s += type.toString().toLowerCase() + "_" + getConnectionsString();
        return s;
    }
}
