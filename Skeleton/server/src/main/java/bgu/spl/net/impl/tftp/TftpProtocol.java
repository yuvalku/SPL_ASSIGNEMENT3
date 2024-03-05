package bgu.spl.net.impl.tftp;

import bgu.spl.net.api.BidiMessagingProtocol;
import bgu.spl.net.srv.Connections;

// Added
import java.util.concurrent.ConcurrentHashMap;
import java.nio.charset.StandardCharsets;


class holder{
    static ConcurrentHashMap<Integer, Boolean> ids_login = new ConcurrentHashMap<>();
}

public class TftpProtocol implements BidiMessagingProtocol<byte[]>  {

    private boolean shouldTerminate;
    private int connectionId;
    private Connections<byte[]> connections;
    private String username;

    @Override
    public void start(int connectionId, Connections<byte[]> connections) {
        
        this.shouldTerminate = false;
        this.connectionId = connectionId;
        this.connections = connections;
        holder.ids_login.put(connectionId, false);
    }

    @Override
    public void process(byte[] message) {
        
        short opCode = (short)(((short)message[0]) << 8 | (short)(message[1]));

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
            if (!holder.ids_login.get(connectionId)) {
                username = new String(message, 2, message.length - 1, StandardCharsets.UTF_8);
                holder.ids_login.replace(connectionId, true);
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


    
}
