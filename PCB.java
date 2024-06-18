/**
 * Manages the process from the kernel’s perspective
 * @author Matthew Welsh
 */
public class PCB {

    /**
     * Private static members to store the pid and
     * the Userland Process
     */
    private static int nextPid = 1;
    private int pid;
    private UserlandProcess up;
    private OS.Priority priority;

    /**
     *
     * @param up
     */
    public PCB (UserlandProcess up) {
        this(up, OS.Priority.INTERACTIVE);
    }

    /**
     * Creates thread and sets pid
     * @param up
     */
    public PCB (UserlandProcess up, OS.Priority priority) {
        this.pid = nextPid++;
        this.up = up;
        this.priority = priority;
    }

    /**
     * Calls Userlandprocess' stop and
     * Loops with Thread.sleep() until up.isStopped() is true
     */
    public void stop() {
        up.stop();

        while (!up.isStopped()) {
            try {
                Thread.sleep(100);
            } catch (Exception e) {}
        }
    }

    /**
     * Calls Userlandprocess' isDone()
     * @return up.isDone()
     */
    public boolean isDone() {
        return up.isDone();
    }

    /**
     * Calls Userlandprocess' Start()
     */
    public void run() {
        up.start();
    }
}
