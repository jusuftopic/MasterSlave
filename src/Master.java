import java.util.ArrayList;

public class Master {

    public static void main(String [] args)
    {
        int slaves = 5; // Integer.valueOf(args[0]);
        int port = 8600;
        ArrayList<Slave> mySlaves = new ArrayList<>();
        ArrayList<Thread> threads = new ArrayList<>();
        int [] ports = new int[slaves];
        for(int i = 0; i < slaves;i++) ports[i] = port + i;
        for(int i  = 0; i < slaves; i++)
        {
            mySlaves.add(new Slave((i==(slaves-1)? true: false), 40, i, ports[i], ports));
            threads.add(new Thread(mySlaves.get(i)));
        }

        for(int i = 0; i < slaves;i++) mySlaves.get(i).buildMesh();

        // starten der threads von den peers, diese sollten nun alle bereit sein
        for(int i = 0; i < slaves;i++) threads.get(i).start();


        while(true){
            int inActive =0;
            for(int i = 0; i < slaves; i++)
            {
                if(!mySlaves.get(i).isActive()) inActive++;
            }
            if (inActive == slaves) return;
        }
    }

}
