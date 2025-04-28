package it.polimi.ingsw.network.rmi;

// Just to test fastly

public class Controller {

    private final Model model = new Model();

    public synchronized int x() { 
        return model.increment(); 
    }

    public synchronized int y() { 
        model.reset(); return 0; 
    }

    public int state() { 
        return model.get(); 
    }
}