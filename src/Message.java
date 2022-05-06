import java.nio.ByteBuffer;
import java.util.Arrays;

public class Message {

    /*public static final int HEARTBEAT = 0xFF;
    public static final int DUMMY_MESSAGE = 0x0F;*/
    public static final int HELLO = 0x09;
    public static final int INITIALIZE =0x01;
    public static final int EXERCISE = 0x02;
    public static final int RESULT = 0x03;
    public static final int FAIL = 0x04;

    private int type;	// der typ der nachricht
    private int peerId; // die id des Peers

    public Message(int type, int peerId)
    {
        this.type = type;
        this.peerId = peerId;
    }

    public Message(byte [] data)
    {
        this.type = ByteBuffer.wrap(Arrays.copyOfRange(data, 0, 4)).getInt();
        this.peerId = ByteBuffer.wrap(Arrays.copyOfRange(data, 4, 8)).getInt();
    }

    public byte [] toByteArray()
    {
        ByteBuffer b =  ByteBuffer.allocate(getMessageSize());
        b.putInt(this.type);
        b.putInt(this.peerId);
        return b.array();
    }

    public static final int getMessageSize()
    {
        return 2*Integer.BYTES;
    }

    public int getType()
    {
        return type;
    }

    public int getPeerId()
    {
        return peerId;
    }
}
