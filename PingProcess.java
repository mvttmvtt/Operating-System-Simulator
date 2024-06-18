/**
 * Represents a process that sends ping messages to another process.
 * @author Matthew Welsh
 */
public class PingProcess extends UserlandProcess {

    String messageContent = "Ping";
    byte[] messageData = messageContent.getBytes();

    @Override
    void main() {

        int senderID = 2;
        int pongPid = OS.GetPidByName("PongProcess");
        if (pongPid != -1) {
            System.out.println("I am PING, pong = " + pongPid);

            for (int i=0; i < 15; i++) {
                try {
                    Thread.sleep(100); //sleeps for 100 ms
                } catch (Exception e) { }
                KernelMessage message = new KernelMessage(senderID, pongPid, i, messageData);
                Kernel.SendMessage(message);
                System.out.println("  PING: from: " + pongPid + " to: " + senderID + " what: " + i);
            }
        } else {
            System.out.println("PONG not found");
        }
    }

}
