import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class CustomSocket implements Runnable {

    private int listenPort;
    private ServerSocket socket;
    private Peer hostClass;
    private boolean run = false;

    public CustomSocket(Peer hostClass, int port) {
        try {
            this.socket = new ServerSocket(port);
            this.hostClass = hostClass;
            run = true;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {

        while (run) {
            try {
                this.hostClass.debugMessage(" listener waiting for incoming connections.");
                Socket sock = socket.accept();
                this.hostClass.debugMessage(" connection from " + sock.getPort());
                new Thread(new AddSocket(this.hostClass, sock)).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void close() throws IOException {
        socket.close();
        run = false;
    }

}