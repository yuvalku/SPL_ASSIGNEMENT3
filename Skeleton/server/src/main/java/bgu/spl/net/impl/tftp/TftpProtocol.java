package bgu.spl.net.impl.tftp;

import bgu.spl.net.api.BidiMessagingProtocol;
import bgu.spl.net.srv.Connections;

// Added
import java.util.concurrent.ConcurrentHashMap;
import java.nio.charset.StandardCharsets;


// class holder{
//     static ConcurrentHashMap<Integer, Boolean> ids_login = new ConcurrentHashMap<>();
// }

public class TftpProtocol implements BidiMessagingProtocol<byte[]>  {

    private boolean shouldTerminate;
    private int connectionId;
    private Connections<byte[]> connections;
    private String username;
    private boolean loggedIn;

    @Override
    public void start(int connectionId, Connections<byte[]> connections) {
        
        this.shouldTerminate = false;
        this.connectionId = connectionId;
        this.connections = connections;
        //holder.ids_login.put(connectionId, false);
        this.loggedIn = false;
    }

    @Override
    public void process(byte[] message) {
        
        short opCode = (short)(((short)message[0]) << 8 | (short)(message[1]));

        if (opCode != 7 && !loggedIn)
            connections.send(connectionId, createError((byte)6, "User not logged in"));

        // RRQ
        if (opCode == 1){

        }

        // WRQ
        else if (opCode == 2){

        }

        // DATA
        else if (opCode == 3){
            
        }

        else if (opCode == 4){
            
        }

        else if (opCode == 5){
            
        }

        else if (opCode == 6){
            
        }

        // LOGRQ
        else if (opCode == 7){

            // extract username from bytes
            username = new String(message, 2, message.length - 2, StandardCharsets.UTF_8);

            // if username is taken
            if (connections.containsName(username))
                connections.send(connectionId, createError((byte)7, "User already logged in"));
            
            // if client already logged in
            else if (loggedIn)
                connections.send(connectionId, createError((byte)0, "Current client already logged in"));
            
            //if there is no active client with this username, register it
            else {
                loggedIn = true;
                byte[] ack = {0, 4, 0, 0};
                connections.send(connectionId, ack);
            }

        }

        else if (opCode == 8){
            
        }

        else if (opCode == 9){
            
        }

        else{
            
        }

    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    } 

    // Added

    public boolean isLoggedIn(){
        return loggedIn;
    }

    public String getUsername(){
        return username;
    }

    // create the relevant bytes array for the given error
    private byte[] createError(byte errorCode, String message){
        byte[] errorMessage = message.getBytes();
        byte[] opC = {0, 5, 0, errorCode};
        byte[] output = new byte[errorMessage.length + opC.length];
        for (int i = 0; i < output.length; i++){
            if (i < 4)
                output[i] = opC[i];
            else
                output[i] = errorMessage[i - 4];
        }
        return output;
    }
    
}
