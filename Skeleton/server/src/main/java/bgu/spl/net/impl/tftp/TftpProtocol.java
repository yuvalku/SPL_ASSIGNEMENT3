package bgu.spl.net.impl.tftp;

import bgu.spl.net.api.BidiMessagingProtocol;
import bgu.spl.net.srv.Connections;

// Added
import java.util.concurrent.ConcurrentHashMap;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.io.FileOutputStream;


class holder{
    static ConcurrentHashMap<Integer, Boolean> ids_login = new ConcurrentHashMap<>();
}

class sendingFile{

    private int curr;
    private short blockNum;
    private byte[] file;

    public sendingFile(byte[] file){
        this.curr = 0;
        this.blockNum = 0;
        this.file = file;
    }

    public byte[] generatePacket(){
        int size = file.length - curr;
        if (size > 512)
            size = 512;
        blockNum++;

        short packetSize = (short)size;
        byte[] output = new byte[size + 6];
        
        output[0] = (byte)0;
        output[1] = (byte)3;
        output[2] = (byte)((packetSize >> 8) & 0xFF);
        output[3] = (byte)(packetSize & 0xFF);
        output[4] = (byte)((blockNum >> 8) & 0xFF);
        output[5] = (byte)(blockNum & 0xFF);
        

        for (int i = 6; i < size + 6; i++){
            output[i] = file[curr];
            curr++;
        }
        
        return output;
    }

    public boolean isFinished(){
        return curr == file.length;
    }
}

public class TftpProtocol implements BidiMessagingProtocol<byte[]>  {

    private boolean shouldTerminate;
    private int connectionId;
    private Connections<byte[]> connections;
    private String username;
    //private boolean loggedIn;
    private sendingFile toSend;
    // LinkedList<byte[]> pendingFiles;

    // private byte[] receivingFile;
    // private int RFindex;

    @Override
    public void start(int connectionId, Connections<byte[]> connections) {
        
        this.shouldTerminate = false;
        this.connectionId = connectionId;
        this.connections = connections;
        holder.ids_login.put(connectionId, false);
        toSend = null;
        // pendingFiles = new LinkedList<>();
        // receivingFile = null;
        // RFindex = 0;
    }

    @Override
    public void process(byte[] message) {
        
        short opCode = (short)(((short)message[0]) << 8 | (short)(message[1]) & 0x00ff);

        if (opCode != 7 && !holder.ids_login.get(connectionId))
            connections.send(connectionId, createError((byte)6, "User not logged in"));

        // RRQ
        if (opCode == 1){

            // extract file name and check if exists
            String fileName = new String(message, 2, message.length - 2, StandardCharsets.UTF_8);
            byte[] file = extractFile("Files/" + fileName);
            if (file != null){

                // if file exists, start sending it / add to pending files
                // if (toSend == null) {
                    toSend = new sendingFile(file);
                    connections.send(connectionId, toSend.generatePacket());
                // }
                // else
                //     pendingFiles.addLast(file);

            }
            //send an ERROR packet
            else{ 
                connections.send(connectionId, createError((byte)1, "File not found"));
            }
        }

        // WRQ
        else if (opCode == 2){

            // extract file name and check if exists
            String fileName = new String(message, 2, message.length - 2, StandardCharsets.UTF_8);
            if (fileExists("Files/" + fileName)){
                connections.send(connectionId, createError((byte)5, "File already exists"));
            }
            else{
                connections.send(connectionId, ack((short)0));
            }

        }

        // DATA
        else if (opCode == 3){
            
        }

        // ACK
        else if (opCode == 4){
            
            // WHAT TO DO WITH BLOCKNUM??????????????????
            if (toSend.isFinished()){
                toSend = null;
            }
            else{
                connections.send(connectionId, toSend.generatePacket());
            }

        }

        // DIRQ
        else if (opCode == 6){
            
            byte[] fileNames = connections.getFileNames();

            // if (toSend == null){
                toSend = new sendingFile(fileNames);
                connections.send(connectionId, toSend.generatePacket());
            // }
            // else
            //     pendingFiles.addLast(fileNames);
        }

        // LOGRQ
        else if (opCode == 7){

            // extract username from bytes
            username = new String(message, 2, message.length - 2, StandardCharsets.UTF_8);

            // if username is taken
            if (connections.containsName(username))
                connections.send(connectionId, createError((byte)7, "User already logged in"));
            
            // if client already logged in
            else if (holder.ids_login.get(connectionId))
                connections.send(connectionId, createError((byte)0, "Current client already logged in"));
            
            //if there is no active client with this username, register it
            else {
                holder.ids_login.put(connectionId, true);
                connections.send(connectionId, ack((short)0));
            }

        }

        // DELRQ
        else if (opCode == 8){

            // extract fileName from bytes
            String fileName = new String(message, 2, message.length - 2, StandardCharsets.UTF_8);

            // if file exists
            if (fileExists("Files/" + fileName)){
                connections.removeFile(fileName);
                File tempFile = new File("Files/" + fileName);
                tempFile.delete(); // check if really working
                connections.send(connectionId, ack((short)0));
            }

            // if file doesn't exists
            else{
                connections.send(connectionId, createError((byte)1, " File not found"));
            }
        }

        // Disc
        else{
            
            // if logged in send ack
            if (holder.ids_login.get(connectionId)){
                connections.send(connectionId, ack((short)0));
            }

            // if not logged in send error
            else
                connections.send(connectionId, createError((byte)6, "User no logged in"));

            // remove from ids_login and from connections
            holder.ids_login.remove(connectionId);
            connections.disconnect(connectionId);
            shouldTerminate = true;
        }

    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    } 

    // Added

    public boolean isLoggedIn(){
        return holder.ids_login.get(connectionId);
    }

    public String getUsername(){
        return username;
    }

    // create the relevant bytes array for the given error
    private static byte[] createError(byte errorCode, String message){
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

    //BCAST
    private void broadCast(byte[] msg){
        holder.ids_login.forEach((key, logged) -> {
            if (logged == true){
                connections.send(key, msg);
            }
        });
    }

    private static boolean fileExists(String filePath) {
        Path path = Paths.get(filePath);
        return Files.exists(path) && Files.isRegularFile(path);
    }


    private static byte[] extractFile(String path){

        try (FileInputStream fis = new FileInputStream(path)) {
            long fileSize = fis.available();

            byte[] fileBytes = new byte[(int) fileSize];

            fis.read(fileBytes);

            return fileBytes;
        } catch (IOException e) {return null;} 
    }

    private static void createFileFromBytes(byte[] fileData, String fileName) {
        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            fos.write(fileData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] ack(short blockNum){
        byte[] output = new byte[4];
        output[0] = (byte)0;
        output[1] = (byte)4;
        output[2] = (byte)((blockNum >> 8) & 0xFF);
        output[3] = (byte)(blockNum & 0xFF);
        return output;
    }
}
