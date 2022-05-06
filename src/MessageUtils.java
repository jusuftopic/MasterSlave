import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MessageUtils {

  /* read peer's message */
  public static Message receiveMessage(InputStream is){
    Message message = null;
    try {
      byte[] data = new byte[Message.getMessageSize()];
      DataInputStream dataInputStream = new DataInputStream(is);
      int bread = dataInputStream.read(data);
      System.out.println("Size: " + bread);

      message = new Message(data);
    }
    catch (IOException e){
      e.printStackTrace();
    }

    return message;
  }

  /* write message to peers */
  public static void writeMessage(OutputStream os, int msgType, int peerId){
    try {
      DataOutputStream dataOutputStream = new DataOutputStream(os);
      Message msg = new Message(msgType, peerId);
      dataOutputStream.write(msg.toByteArray());
      dataOutputStream.flush();
      Thread.sleep(1000);
    } catch (InterruptedException e1) {
      e1.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
