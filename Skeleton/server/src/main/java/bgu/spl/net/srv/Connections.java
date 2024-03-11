package bgu.spl.net.srv;

import java.io.IOException;

public interface Connections<T> {

    void connect(int connectionId, BlockingConnectionHandler handler);

    boolean send(int connectionId, T msg);

    void disconnect(int connectionId);

    // Added
    void addName(String username);

    boolean containsName(String username);

    byte[] getFileNames();

    public void removeFile(String name);

    void addFile(String name);
}
