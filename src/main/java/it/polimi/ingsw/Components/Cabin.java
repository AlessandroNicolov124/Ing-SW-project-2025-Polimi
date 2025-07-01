package it.polimi.ingsw.Components;

import it.polimi.ingsw.*;

import java.util.*;

// Classe Cabin
public final class Cabin extends Component {
    private static final long serialVersionUID = 1L;
    private List<Crew> crew;
    private boolean isStartingCabin;
    private Set<LifeSupportSystemType> connectedLifeSupports;

    public void initializeConnectors() {
        // Cabina di partenza ha connettori universali su tutti i lati
        connections.add(new Connector(Direction.NORTH, new UniversalConnectorType()));
        connections.add(new Connector(Direction.EAST, new UniversalConnectorType()));
        connections.add(new Connector(Direction.SOUTH, new UniversalConnectorType()));
        connections.add(new Connector(Direction.WEST, new UniversalConnectorType()));
    }

    public Cabin() {
        this(false);
    }

    @Override
    protected String calculateComponentString() {
        if (isStartingCabin()) {
            return "cabin_starting";
        }
        return "cabin_" + getConnectionsString();
    }

    public Cabin(boolean isStartingCabin) {
        super();
        this.crew = new ArrayList<>();
        this.isStartingCabin = isStartingCabin;
        this.connectedLifeSupports = new HashSet<>();
        if (isStartingCabin) {
            initializeConnectors();
        }
    }

    public Crew getCrewByIndex(int index) {
        return crew.get(index);
    }

    public List<Crew> getCrew() {
        return crew;
    }

    public boolean removeCrew(Crew crew) {
        return this.crew.remove(crew);
    }

    public boolean isStartingCabin() {
        return isStartingCabin;
    }

    public void connectLifeSupport(LifeSupportSystemType type) {
        connectedLifeSupports.add(type);
    }

    public boolean hasConnectedLifeSupport(LifeSupportSystemType type) {
        return connectedLifeSupports.contains(type);
    }

    public Set<LifeSupportSystemType> getConnectedLifeSupports() {
        return new HashSet<>(connectedLifeSupports);
    }

    public boolean hasAnyConnectedLifeSupport() {
        return !connectedLifeSupports.isEmpty();
    }

    public boolean addCrew(Crew crewMember) {
        // Check if this is the starting cabin and the crew is alien
        if (isStartingCabin && crewMember.getType() != CrewType.HUMAN) {
            System.out.println("[Cabin] Starting cabin cannot host aliens");
            return false;
        }

        // Check current occupancy
        int currentOccupants = crew.size();
        
        // If cabin is empty, check based on crew type
        if (currentOccupants == 0) {
            if (crewMember.getType() != CrewType.HUMAN) {
                // Alien needs appropriate life support
                LifeSupportSystemType requiredType = switch (crewMember.getType()) {
                    case BROWN_ALIEN -> LifeSupportSystemType.BROWN;
                    case PURPLE_ALIEN -> LifeSupportSystemType.PURPLE;
                    default -> null;
                };
                
                if (requiredType != null && !hasConnectedLifeSupport(requiredType)) {
                    System.out.println("[Cabin] Cannot add " + crewMember.getType() + 
                                    ": no " + requiredType + " life support connected");
                    return false;
                }
            }
            crew.add(crewMember);
            return true;
        }
        
        // If cabin has 1 occupant
        if (currentOccupants == 1) {
            Crew existingCrew = crew.get(0);
            
            // If existing is alien, cabin is full (aliens take 2 spaces)
            if (existingCrew.getType() != CrewType.HUMAN) {
                System.out.println("[Cabin] Cabin with alien is full");
                return false;
            }
            
            // If existing is human and new is alien, reject (no mixing)
            if (crewMember.getType() != CrewType.HUMAN) {
                System.out.println("[Cabin] Cannot mix humans and aliens");
                return false;
            }
            
            // Both are humans - add the second human
            crew.add(crewMember);
            return true;
        }
        
        // Cabin already has 2 occupants
        System.out.println("[Cabin] Cabin is full");
        return false;
    }

    public boolean canAddAlien(CrewType alienType) {
        // Can't add aliens to the starting cabin
        if (isStartingCabin) {
            return false;
        }

        // Can't add if cabin is full
        if (crew.size() >= 2 || (crew.size() == 1 && crew.get(0).getType() != CrewType.HUMAN)) {
            return false;
        }

        // Check if we have appropriate life support for this alien type
        return switch (alienType) {
            case PURPLE_ALIEN -> connectedLifeSupports.contains(LifeSupportSystemType.PURPLE);
            case BROWN_ALIEN -> connectedLifeSupports.contains(LifeSupportSystemType.BROWN);
            default -> false; // Not an alien type
        };
    }

    public boolean initializeCrewStartingCabin() {
        if (isStartingCabin) {
            return addCrew(new HumanCrew()) &&
                    addCrew(new HumanCrew());
        }
        return false;
    }

    public boolean isOccuped() {
        return !crew.isEmpty();
    }

    public void disconnectLifeSupport(LifeSupportSystemType type) {
        connectedLifeSupports.remove(type);
    }
}

