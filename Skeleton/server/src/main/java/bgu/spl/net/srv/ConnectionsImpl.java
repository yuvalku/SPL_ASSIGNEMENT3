package bgu.spl.net.srv;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionsImpl implements Connections<byte[]> {

    private ConcurrentHashMap<Integer, ConnectionHandler<byte[]>> clientsHandlers;

    // constructor
    public ConnectionsImpl(){
        clientsHandlers = new ConcurrentHashMap<Integer, ConnectionHandler<byte[]>>();
    }

    // add a new handler and id to the hash map, called when socket connected
    public void connect(int connectionId, ConnectionHandler<byte[]> handler){
        clientsHandlers.put(connectionId, handler);
    }

    // check if the client is logged in and send the message if it does, return if message sent successfully
    public boolean send(int connectionId, byte[] msg){
        ConnectionHandler<byte[]> handler = clientsHandlers.get(connectionId);
        boolean isLoggedIn = ((BlockingConnectionHandler<byte[]>)handler).isLoggedIn();
        if (isLoggedIn)
            handler.send(msg);
        return isLoggedIn;
    }

    // remove the client from the hash map
    public void disconnect(int connectionId){
        clientsHandlers.remove(connectionId);
    }
    
}
