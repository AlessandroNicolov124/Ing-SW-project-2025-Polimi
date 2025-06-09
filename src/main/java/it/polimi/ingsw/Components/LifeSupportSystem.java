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

    @Override
    public String getComponentString() {
        String s = this.getClass().getSimpleName().toLowerCase() + "_";
        s += type.toString().toLowerCase() + "_" + getConnectionsString();
        return s;
    }
}
