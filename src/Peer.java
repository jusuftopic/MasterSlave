import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class Peer implements Runnable {


  private int id;
  private int port;
  private CustomSocket listener; // der Serversocket zum Verbinden der Anderen Peers/Clients/Slaves
  private boolean run;
  private ArrayList<Socket> connectedPeers;
  private ArrayList<Socket> connectionToPeers;

  private boolean isCoordinator;
  private int[] slaves;
  private long coordinatorTTL; // in Sekunden
  Thread listenerThread;

  public Peer(boolean isCoordinator, long coordinatorTTL, int id, int port, int[] slaves) {
    this.id = id;
    this.port = port;
    run = true;
    this.listener = new CustomSocket(this, port);
    this.isCoordinator = isCoordinator;
    connectedPeers = new ArrayList<>();
    connectionToPeers = new ArrayList<>();
    this.slaves = slaves;
    System.out.println("Starting listener of peer: " + id);
    listenerThread = new Thread(listener);
    listenerThread.start();

  }

  public boolean isActive() {
    return run;
  }

  /*
   * Baue das Netzwerk - in unserem Fall ein Mesh
   */
  public void buildMesh() {
    for (int i = 0; i < slaves.length; i++) {
      if (slaves[i] != port) {
        try {
          this.debugMessage(" connecting to peer with port: " + slaves[i]);
          Socket clS = new Socket("localhost", slaves[i]);
          byte[] data = new byte[Message.getMessageSize()];
          (new DataInputStream(clS.getInputStream())).read(data);
          Message helloMsg = new Message(data);
          if (clS.isConnected() && helloMsg.getType() == Message.HELLO) {
            debugMessage(" received hello!");
            connectionToPeers.add(clS);
          } else {
            debugMessage(" could not connect to peer with port: " + slaves[i]);
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public void run() {
    try {
      Thread.sleep(1000);
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
    }

    // Falls wird der Koordinator sind, simuliere Knotenausfall
		if(isCoordinator)
		{
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				 	public void run() {
				 		stop();
				 	}
			}, coordinatorTTL*1000);
		}

    while (run) {
      // 1. Pr??fe periodisch ob der derzeitige Koordinator online ist - wenn wir nicht selbst der Koordinator sind
      // 2. Wenn dieser nicht Online ist initiiere den Bully-Algorithmus und bestimme aus den bestehenden Peers einen neuen Koordinator

      if (isCoordinator) {
        for (int i = 0; i < connectedPeers.size(); i++) {
          try {
            /* master collects messages from slaves */
            Message receivedMessage =
                MessageUtils.receiveMessage(connectedPeers.get(i).getInputStream());
            debugMessage(
                " got message with from peer with id " + receivedMessage.getPeerId() + " and type "
                    + receivedMessage.getType());
            handleMessage(connectedPeers.get(i), receivedMessage);
          } catch (IOException ioe) {
            ioe.printStackTrace();
          }
        }
      } else {
        // if we are not the coordinator send something - its the peer with the highest id/index
        sendInitializeMessageToMaster();
        listenToMasterMessages();
      }
    }
  }

  private void sendInitializeMessageToMaster() {
    try {
      MessageUtils.writeMessage(connectionToPeers.get(0).getOutputStream(), Message.INITIALIZE, id);
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }

  private void listenToMasterMessages() {
    try {
      Message message = MessageUtils.receiveMessage(
          connectionToPeers.get(0).getInputStream());
      debugMessage(" got message with from peer with id " + message.getPeerId() + " and type "
          + message.getType());
      handleMessage(connectedPeers.get(0), message);
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }

  public void handleMessage(Socket cl, Message msg) {
    switch (msg.getType()) {
      case Message.INITIALIZE:
        try {
          MessageUtils.writeMessage(cl.getOutputStream(), Message.EXERCISE, id);
        } catch (IOException e) {
          e.printStackTrace();
        }
        break;
      case Message.EXERCISE:
        try {
          /* slave's fail simulation */
          if (id == 2){
            MessageUtils.writeMessage(cl.getOutputStream(), Message.FAIL, id);
            this.stop();
            break;
          }
          MessageUtils.writeMessage(cl.getOutputStream(), Message.RESULT, id);
        } catch (IOException e) {
          e.printStackTrace();
        }
      case Message.RESULT:
        System.out.println("Got result message: " + msg.getType());
        break;

        /* if master receive failed message */
      case Message.FAIL:
        try {
          MessageUtils.writeMessage(cl.getOutputStream(), Message.EXERCISE, id);
        }
        catch (IOException ioe){
          ioe.printStackTrace();
        }

      default:
        debugMessage("unknown message type!");
    }

  }

  public int getId() {
    return id;
  }

  public boolean alreadyConnected(int remotePort) {

    return false;
  }

  public synchronized void addPeer(Socket clientSocketPeer) {
    debugMessage(
        "Peer " + id + " received connect of peer with IP: " + clientSocketPeer.getInetAddress()
            .toString() + " and port: " + clientSocketPeer.getPort());
    Message helloMsg = new Message(Message.HELLO, id);
    try {
      DataOutputStream os = new DataOutputStream(clientSocketPeer.getOutputStream());
      os.write(helloMsg.toByteArray());
      os.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
    connectedPeers.add(clientSocketPeer);
  }

  public void debugMessage(String text) {
    System.out.println(
        (isCoordinator ? "Coordinator (peer" + id + " )" : "Peer " + id + ":") + text);
  }

  public void stop() {
    run = false;
    debugMessage("dying.");
    try {

      listener.close();
      for (int i = 0; i < connectedPeers.size(); i++) connectedPeers.get(i).close();

    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
