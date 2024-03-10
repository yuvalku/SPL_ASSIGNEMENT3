package bgu.spl.net.srv;

import java.io.IOException;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.io.File;;

public class ConnectionsImpl implements Connections<byte[]> {

    private ConcurrentHashMap<Integer, BlockingConnectionHandler> clientsHandlers;
    private Vector<String> usernames;
    private Vector<String> files;

    // constructor
    public ConnectionsImpl(){
        
        clientsHandlers = new ConcurrentHashMap<Integer, BlockingConnectionHandler>();
        usernames = new Vector<String>();
        files = new Vector<>();
        
        //add the exisiting files in Files folder 
        File folder = new File("Files");
        File[] f = folder.listFiles();
        for(File file : f){
            files.add(file.getName());
        }
        
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

    public byte[] getFileNames(){

        if (files.isEmpty())
            return new byte[0];

        Vector<byte[]> filebytes = new Vector<>();
        int length = 0;

        for (String file : files){
            byte[] toAdd = file.getBytes();
            filebytes.add(toAdd);
            length += toAdd.length;
        }

        byte[] output = new byte[length + filebytes.size() - 1];
        int index = 0;
        for (byte[] arr : filebytes){
            for (int i = 0; i < arr.length; i++){
                output[index] = arr[i];
                index++;
            }
            if (index < output.length) {
                output[index] = (byte)0;
                index++;
            }
        }

        return output;
    }

    public void removeFile(String name){
        files.remove(name);
    }
    
}
