package bgu.spl.net.srv;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.MessagingProtocol;
import bgu.spl.net.impl.tftp.TftpProtocol;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Supplier;
import bgu.spl.net.impl.tftp.TftpProtocol;
import bgu.spl.net.impl.tftp.TftpEncoderDecoder;

public abstract class BaseServer<T> implements Server<T> {

    private final int port;
    private final Supplier<TftpProtocol> protocolFactory;
    private final Supplier<TftpEncoderDecoder> encdecFactory;
    private ServerSocket sock;

    //need to add a id counter
    //need to add connections

    public BaseServer(
            int port,
            Supplier<TftpProtocol> protocolFactory,
            Supplier<TftpEncoderDecoder> encdecFactory) {

        this.port = port;
        this.protocolFactory = protocolFactory;
        this.encdecFactory = encdecFactory;
		this.sock = null;
    }

    @Override
    public void serve() {

        try (ServerSocket serverSock = new ServerSocket(port)) {
			System.out.println("Server started");

            this.sock = serverSock; //just to be able to close

            while (!Thread.currentThread().isInterrupted()) {

                Socket clientSock = serverSock.accept();

                BlockingConnectionHandler<T> handler = new BlockingConnectionHandler<>(
                        clientSock,
                        encdecFactory.get(),
                        protocolFactory.get());

                execute(handler);
            }
        } catch (IOException ex) {
        }

        System.out.println("server closed!!!");
    }

    @Override
    public void close() throws IOException {
		if (sock != null)
			sock.close();
    }

    protected abstract void execute(BlockingConnectionHandler<T>  handler);
    // handler.setid(counter++)
    // hander.start
    // close handler at the end
}
