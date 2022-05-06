import java.util.ArrayList;

public class Initializer {

    public static void main(String [] args)
    {
        int numOfPeers = 5; // Integer.valueOf(args[0]);
        int port = 8600;
        ArrayList<Peer> peers = new ArrayList<>();
        ArrayList<Thread> threads = new ArrayList<>();
        int [] ports = new int[numOfPeers];
        for(int i = 0; i < numOfPeers;i++){
            ports[i] = port + i;
        }
        for(int i  = 0; i < numOfPeers; i++)
        {
            /* we assume that the coordinator is the master, and therefore the first one should be started */
            peers.add(new Peer((i==0 ? true : false), 40, i, ports[i], ports));
            threads.add(new Thread(peers.get(i)));
        }

        for(int i = 0; i < numOfPeers;i++) peers.get(i).buildMesh();

        // starten der threads von den peers, diese sollten nun alle bereit sein
        for(int i = 0; i < numOfPeers;i++) threads.get(i).start();


        while(true){
            int inActive =0;
            for(int i = 0; i < numOfPeers; i++)
            {
                if(!peers.get(i).isActive()) inActive++;
            }
            if (inActive == numOfPeers) return;
        }
    }

}
