package bgu.spl.net.impl.tftp;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Vector;
import java.nio.charset.StandardCharsets;

import bgu.spl.net.api.MessagingProtocol;

public class TftpProtocolClient implements MessagingProtocol<byte[]>  {

    private boolean shouldTerminate;
    short flag = -1;
    private Queue<byte[]> uploadingFile = new LinkedList<>();
    private int UFsize = 0;
    private String uploadingFileName;
    private final String directory = "Skeleton\\client";
    private sendingFile toSend = null;
    private Path pathToNewFile;

    
    public byte[] process(byte[] msg) {

        short opCode = (short)(((short)msg[0]) << 8 | (short)(msg[1]) & 0x00ff);
        byte[] output = null;
        
        // DATA
        if (opCode == 3){
            // add data packet to queue
            uploadingFile.add(msg);

            // send acknowledgment
            output = ack(msg[4], msg[5]);

            // check data's size
            short packetSize = (short)(((short)msg[2]) << 8 | (short)(msg[3]) & 0x00ff);
            UFsize += packetSize;


            if (packetSize < 512) {
                 
                // RRQ
                if (flag == 1){
                    byte[] file = buildFileBytes(uploadingFile);
                    addNewFile(file);
                    System.out.println("RRQ " + uploadingFileName + " complete");
                    flag = -1;
                }
                
                //DIRQ
                else {
                    Vector<String> files = buildFileNames(uploadingFile);
                    System.out.println();
                    System.out.println("Directory Files:");
                    for (String s : files){
                        System.out.println(s);
                    }
                    System.out.println();
                    flag = -1;
                }
            }
        }

        // ACK
        else if(opCode == 4){

            short blockNum = (short)(((short)msg[2]) << 8 | (short)(msg[3]) & 0x00ff);
            System.out.println("ACK " + blockNum);

            // handle DATA packet if needed
            if (flag == 2) {
                if (toSend.isFinished()){
                    flag = -1;
                    System.out.println("WRQ " + uploadingFileName + " complete");
                }
                else
                    output = toSend.generatePacket();              
            }

            // handle disconnect
            else if (flag == 10)
                shouldTerminate = true;

            // check if need to reset flag
            if (blockNum == 0 & flag != 2)
                flag = -1;

        }

        // BCAST
        else if (opCode == 9){

            String added;
            if (msg[2] == (byte)1)
                added = "Added";
            else
                added = "Deleted";
            
            String toPrint = new String(msg, 3, msg.length - 1, StandardCharsets.UTF_8);
            System.out.println("BCAST " + added + " " + toPrint);
        }

        // Error
        else {

            // print error
            short errorNum = (short)(((short)msg[2]) << 8 | (short)(msg[3]) & 0x00ff);
            String errorMsg = new String(msg, 4, msg.length - 1, StandardCharsets.UTF_8);
            System.out.println("Error " + errorNum + " " + errorMsg);

            // RRQ ERROR
            if (flag == 1){

                // delete file from directory
                File tempFile = new File(directory + "\\" + uploadingFileName);
                tempFile.delete();

                // reset uploading file data
                while (!uploadingFile.isEmpty())
                    uploadingFile.poll();
                UFsize = 0;
            }

            // DISC ERROR
            if (flag == 10)
                shouldTerminate = true;

            // Reset flag
            flag = -1;
        }

        return output;
    }

    public byte[] keyboardProcess(String str){

        byte[] output = null;
        String[] input = str.split("\\s+");
        String cmd = input[0];

        if (cmd == "LOGRQ"){

            flag = 7;

            // extract name and create logrq packet
            String name = input[1];
            byte[] bytesName = name.getBytes();
            output = new byte[bytesName.length + 2];
            output[0] = (byte)0;
            output[1] = (byte)7;
            for (int i = 0; i < bytesName.length; i++)
                output[i + 2] = bytesName[i];
        }

        else if (cmd == "DELRQ"){

            flag = 8;

            // extract name and create delrq packet
            String file = input[1];
            byte[] fileName = file.getBytes();
            output = new byte[fileName.length + 2];
            output[0] = (byte)0;
            output[1] = (byte)8;
            for (int i = 0; i < fileName.length; i++)
                output[i + 2] = fileName[i];
        }

        else if (cmd == "RRQ"){

            // extract name and create rrq packet if doesn't exists here
            uploadingFileName = input[1];
            if (fileExists(directory + "\\" + uploadingFileName))
                System.out.println("file already exists");

            else {

                // set flag
                flag = 1;
                
                // create path
                pathToNewFile = Paths.get(directory, uploadingFileName);

                byte[] fileName = uploadingFileName.getBytes();
                output = new byte[fileName.length + 2];
                output[0] = (byte)0;
                output[1] = (byte)1;
                for (int i = 0; i < fileName.length; i++)
                    output[i + 2] = fileName[i];
            }
        }

        else if (cmd == "WRQ"){

            String file = input[1];
            if (!fileExists(directory + "\\" + file))
                System.out.println("file does not exists");

            else {

                // set flag
                flag = 2;

                // set toSend
                toSend = new sendingFile(extractFile(directory + "\\" + file));

                byte[] fileName = file.getBytes();
                output = new byte[fileName.length + 2];
                output[0] = (byte)0;
                output[1] = (byte)1;
                for (int i = 0; i < fileName.length; i++)
                    output[i + 2] = fileName[i];
            }
        }

        else if (cmd == "DIRQ"){
            output = new byte[2];
            output[0] = (byte)0;
            output[1] = (byte)6;
        }

        else if (cmd == "DISC"){
            output = new byte[2];
            output[0] = (byte)0;
            output[1] = (byte)10;
        }

        else
            System.out.println("Illegal input, try again");

        return output;
    }


    public boolean shouldTerminate() {
        return shouldTerminate;
    } 

    private byte[] ack(byte first, byte second){
        byte[] output = new byte[4];
        output[0] = (byte)0;
        output[1] = (byte)4;
        output[2] = first;
        output[3] = second;
        return output;
    }


    private byte[] buildFileBytes(Queue<byte[]> queue){

        byte[] file = new byte[UFsize];
        int index = 0;
        while (!queue.isEmpty()){
            byte[] curr = queue.poll();
            for (int i = 6; i < curr.length; i++){
                file[index] = curr[i];
                index++;
            }
        }

        UFsize = 0;
        return file;
    }

    private boolean addNewFile(byte[] file){

        try {
            Files.write(pathToNewFile, file);
        } catch (IOException e) { return false;}
        return true;
    }

    private Vector<String> buildFileNames(Queue<byte[]> queue){

        Vector<String> output = new Vector<>();
        
        byte[] allbytes = new byte[UFsize];
        int index = 0;
        while (!queue.isEmpty()){
            byte[] curr = queue.poll();
            for (int i = 6; i < curr.length; i++){
                allbytes[index] = curr[i];
                index++;
            }
        }
        UFsize = 0;

        index = 0;
        int start = 0;

        while (index < allbytes.length) {

            if (allbytes[index] == (byte)0){
                output.add(new String(allbytes, start, index - 1, StandardCharsets.UTF_8));
                start = index + 1;
            }
            index++;
        }

        output.add(new String(allbytes, start, allbytes.length - 1, StandardCharsets.UTF_8));

        return output;
    }

    private static boolean fileExists(String filePath) {
        Path path = Paths.get(filePath);
        return Files.exists(path);
    }

    private static byte[] extractFile(String path){
        
        try {
            Path file = Paths.get(path);
            byte[] output = Files.readAllBytes(file);
            return output;      
        } catch (IOException e) {e.printStackTrace(); return null;}

    }
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