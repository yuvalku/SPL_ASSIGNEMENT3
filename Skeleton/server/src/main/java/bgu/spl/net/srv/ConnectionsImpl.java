package bgu.spl.net.srv;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionsImpl<T> implements Connections<T> {

    private ConcurrentHashMap<Integer, ConnectionHandler<T>> clientsHandlers;

    // constructor
    public ConnectionsImpl(){
        clientsHandlers = new ConcurrentHashMap<Integer, ConnectionHandler<T>>();
    }

    // add a new handler and id to the hash map, called when socket connected
    @Override
    public void connect(int connectionId, ConnectionHandler<T> handler){
        clientsHandlers.put(connectionId, handler);
    }

    // check if the client is logged in and send the message if it does, return if message sent successfully
    @Override
    public boolean send(int connectionId, T msg){
        ConnectionHandler<T> handler = clientsHandlers.get(connectionId);
        boolean isLoggedIn = ((BlockingConnectionHandler<T>)handler).isLoggedIn();
        if (isLoggedIn)
            handler.send(msg);
        return isLoggedIn;
    }

    // remove the client from the hash map
    @Override
    public void disconnect(int connectionId){
        clientsHandlers.remove(connectionId);
    }
    
}
