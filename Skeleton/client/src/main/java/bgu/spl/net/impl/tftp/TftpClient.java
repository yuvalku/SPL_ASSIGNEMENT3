package bgu.spl.net.impl.tftp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;
import java.io.IOException;

public class TftpClient {

    private static Thread listeningThread;
    private static Thread keyboardThread;
    private static TftpEncoderDecoderClient encdec;
    private static TftpProtocolClient protocol;

    public static void main(String[] args) throws IOException{

        String serverIP = args[0];
        int serverPort =  Integer.parseInt(args[1]);

        Socket sock = new Socket(serverIP, serverPort);

        Scanner keyboard = new Scanner(System.in);

        BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));

        int read;

        listeningThread = new Thread(()->{
            while((read = in.read())>= 0){ 
                byte[] nextMessage = encdec.decodeNextByte((byte) read);
                if (nextMessage != null) 
                    protocol.process(nextMessage);
            }
        }

        
        
        );

        keyboardThread = new Thread(()-> {

            while (!protocol.shouldTerminate()){
                String input = keyboard.nextLine();
                byte[] toEx = protocol.keyboardProcess(input);
            }
        }
        
        );


        while((read = in.read()) >= 0){

        }
   
    }
}
