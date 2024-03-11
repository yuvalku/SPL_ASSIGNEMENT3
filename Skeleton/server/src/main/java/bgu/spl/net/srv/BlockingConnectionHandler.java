package bgu.spl.net.srv;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.MessagingProtocol;
import bgu.spl.net.api.BidiMessagingProtocol;
import bgu.spl.net.impl.tftp.TftpProtocol;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

// Added
import java.util.Vector;

public class BlockingConnectionHandler<T> implements Runnable, ConnectionHandler<T> {

    private final BidiMessagingProtocol<T> protocol;
    private final MessageEncoderDecoder<T> encdec;
    private final Socket sock;
    private BufferedInputStream in;
    private BufferedOutputStream out;
    private volatile boolean connected = true;

    // Added
    private int id;
    private Connections<T> connections;
    private messageQueue<T> pendingMSG;

    public BlockingConnectionHandler(Socket sock, MessageEncoderDecoder<T> reader, BidiMessagingProtocol<T> protocol, int id, Connections<T> connections) {
        this.sock = sock;
        this.encdec = reader;
        this.protocol = protocol;
        this.id = id;
        this.connections = connections;
        pendingMSG = new messageQueue<>();
    }

    @Override
    public void run() {
        try (Socket sock = this.sock) { //just for automatic closing
            int read;
            
            // Added
            ((TftpProtocol)protocol).start(id, (Connections<byte[]>)connections); // Is this OK????????????????
            connections.connect(id, this);

            in = new BufferedInputStream(sock.getInputStream());
            out = new BufferedOutputStream(sock.getOutputStream());

            while (!protocol.shouldTerminate() && connected && (read = in.read()) >= 0) {
                T nextMessage = encdec.decodeNextByte((byte) read);
                if (nextMessage != null)
                    protocol.process(nextMessage);
                while (!pendingMSG.isEmpty()) {
                    T msg = pendingMSG.take();
                    out.write(encdec.encode(msg));
                    out.flush();
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void close() throws IOException {
        connected = false;
        sock.close();
    }

    @Override
    public void send(T msg) {
        pendingMSG.put(msg);
    }

    // Added
    public boolean isLoggedIn(){
        return ((TftpProtocol)protocol).isLoggedIn();
    }

    public String getUsername(){
        return ((TftpProtocol)protocol).getUsername();
    }
}

// Concurrent safe queue
class messageQueue<T> {

    private Vector<T> vec;

    public messageQueue(){
        vec = new Vector<>();
    }

    public synchronized void put(T msg){
        vec.add(msg);
    }

    public synchronized T take(){
        return vec.remove(0);
    }

    public synchronized boolean isEmpty(){
        return vec.size() == 0;
    }
}