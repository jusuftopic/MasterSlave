import java.net.Socket;

class AddSocket implements Runnable {
    private Socket sock;
    private Peer s;

    public AddSocket(Peer s, Socket sock) {
        this.sock = sock;
        this.s = s;
    }

    public void run() {
        s.addPeer(sock);
    }
}
