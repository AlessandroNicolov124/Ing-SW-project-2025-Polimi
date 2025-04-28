package it.polimi.ingsw.network.rmi;

import java.util.concurrent.atomic.AtomicInteger;


// Just to test fastly

public class Model {

    private final AtomicInteger value = new AtomicInteger();

    public int increment() { 
        return value.incrementAndGet(); 
    }

    public void reset() { 
        value.set(0); 
    }

    public int get() { 
        return value.get(); 
    }
}