package bgu.spl.net.srv;

import java.io.IOException;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionsImpl implements Connections<byte[]> {

    private ConcurrentHashMap<Integer, BlockingConnectionHandler> clientsHandlers;
    private Vector<String> usernames;

    // constructor
    public ConnectionsImpl(){
        clientsHandlers = new ConcurrentHashMap<Integer, BlockingConnectionHandler>();
        usernames = new Vector<String>();
    }

    // add a new handler and id to the hash map, called when socket connected
    public void connect(int connectionId, BlockingConnectionHandler handler){
        clientsHandlers.put(connectionId, handler);
    }

    // check if the client is logged in and send the message if it does, return if message sent successfully
    public boolean send(int connectionId, byte[] msg){
        BlockingConnectionHandler handler = clientsHandlers.get(connectionId);
        boolean isLoggedIn = handler.isLoggedIn();
        if (isLoggedIn)
            handler.send(msg);
        return isLoggedIn;
    }

    // remove the client from the hash map and if needed from the usernames
    public void disconnect(int connectionId){
        BlockingConnectionHandler handler = clientsHandlers.get(connectionId);
        usernames.remove(handler.getUsername());
        clientsHandlers.remove(connectionId);
    }

    // Added

    // add a new username
    public void addName(String username){
        usernames.add(username);
    }

    // check if a username exists
    public boolean containsName(String username){
        return usernames.contains(username);
    }
    
}
