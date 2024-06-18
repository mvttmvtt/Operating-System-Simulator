/**
 * Represents a Userland Process that runs for a long time
 * @author Matthew Welsh
 */
public class RealTimeProcess extends UserlandProcess {

    /**
     * Runs for a long time, calls cooperate(),
     * and sleeps for 50 milliseconds after each iteration.
     */
    @Override
    public void main() {

        while (true) {
            System.out.println("Realtime");
            cooperate(); //cooperates with the operating system
            try {
                Thread.sleep(50); //sleeps for 50 ms
            } catch (Exception e) { }
        }
    }
}
