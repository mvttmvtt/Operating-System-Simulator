/**
 * Represents a process that sends pong messages to another process.
 * @author Matthew Welsh
 */
public class PongProcess extends UserlandProcess {

    @Override
    void main() {

        int pingPid = OS.GetPidByName("PingProcess");
        if (pingPid != -1) {
            System.out.println("I am PONG, ping = " + pingPid);

            for (int i=0; i < 15; i++) {
                try {
                    Thread.sleep(100); //sleeps for 100 ms
                } catch (Exception e) { }
                KernelMessage message = Kernel.WaitForMessage();
                System.out.println("  PONG: from: " + pingPid + " to: " + message.getSenderPid() + " what: " + message.getMessageType());
            }
        } else {
            System.out.println("PING not found");
        }
    }
}
