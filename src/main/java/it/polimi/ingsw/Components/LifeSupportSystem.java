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
    protected String calculateComponentString() {
        String typeStr = (type == LifeSupportSystemType.BROWN) ? "brown" : "purple";
        return "lifesupportsystem_" + typeStr + "_" + getConnectionsString();
    }
}
