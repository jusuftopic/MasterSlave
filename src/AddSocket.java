import java.net.Socket;

class AddSocket implements Runnable {
    private Socket sock;
    private Slave s;

    public AddSocket(Slave s, Socket sock) {
        this.sock = sock;
        this.s = s;
    }

    public void run() {
        s.addPeer(sock);
    }
}
