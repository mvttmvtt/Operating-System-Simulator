import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

/**
 * Represents a Userland Process in the Operating System
 * @author Matthew Welsh
 */
public abstract class UserlandProcess implements Runnable {

    /**
     * Private members to store the thread, the semaphore,
     * the quantum, and the Process ID
     */
    private Thread thread;
    private Semaphore mySemaphore = new Semaphore(0);
    private boolean quantum;
    private static int pid; //Process ID

    private Instant wakeupTime;
    private ProcessState state;
    private String name;

    private ArrayList<Integer> openDevices = new ArrayList<>();

    private static final int PAGE_SIZE = 1024;

    /**
     * A constructor that initializes the members
     */
    public UserlandProcess() {
        this.thread = new Thread(this);
        this.quantum = false;
        this.thread.start();
        this.name = name;
    }

    //
    public byte Read(int address) {

        int physicalAddress = getPhysicalAddress(address);
        return Kernel.ReadMemory(physicalAddress);
    }

    //
    public void Write(int address, byte value) {
        int physicalAddress = getPhysicalAddress(address);
        Kernel.WriteMemory(physicalAddress, value);
    }

    //
    private int getPhysicalAddress(int virtualAddress) {
        int virtualPage = virtualAddress / PAGE_SIZE;
        int pageOffset = virtualAddress % PAGE_SIZE;
        int physicalPage = KernelandProcess.GetMapping(virtualPage);
        return physicalPage * PAGE_SIZE + pageOffset;
    }


    public String getName() {
        return name;
    }

    /**
     * Enum representing the state of the process
     */
    public enum ProcessState {
        READY, SLEEPING, RUNNING
    }

    public ProcessState getState() {
        return state;
    }

    /**
     * Sets the wakeup time for the process.
     * @param wakeupTime The wakeup time to set.
     */
    public void setWakeupTime(Instant wakeupTime) {
        this.wakeupTime = wakeupTime;
    }

    /**
     * Retrieves the wakeup time of the process.
     * @return The wakeup time of the process.
     */
    public Instant getWakeupTime() {
        return wakeupTime;
    }

    /**
     * Returns a list of device IDs that are currently open for this process.
     * @return A list of open device IDs.
      */
    public ArrayList<Integer> getOpenDevices() {
        return openDevices;
    }

    /**
     * Sets the state of the process
     * @param state
     */
    public void setState(ProcessState state) {
        this.state = state;
    }


    /**
     * sets the boolean indicating that this process’ quantum has expired
     */
    public void requestStop() {
        quantum = true;
    }

    /**
     * represents the main of our “program”
     */
    abstract void main();

    /**
     * indicates if the semaphore is 0
     * @return true if semaphore is 0
     * @return false if it isn't
     */
    public boolean isStopped() {
        if (mySemaphore.availablePermits() == 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns true when the Java thread is not alive
     * @return true when the Java thread is not alive
     */
    public boolean isDone() {
        if (!thread.isAlive()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * releases (increments) the semaphore, allowing this thread to run
     */
    public void start() {
        mySemaphore.release();
    }

    /**
     * acquires (decrements) the semaphore, stopping this thread from running
     */
    public void stop() {
        try {
            mySemaphore.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * acquires the semaphore, then call main
     */
    public void run() {
        try {
            mySemaphore.acquire();
            main();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * if the boolean is true, set the boolean to false and call OS.switchProcess()
     */
    public void cooperate() {
        if (quantum == true) {
            quantum = false;
            OS.SwitchProcess();
        }
    }

    /**
     * Accessor used to retrieve the Process ID
     * @return pid - Process ID
     */
    public static int getPid() {
        return pid;
    }

    /**
     * Initializes the Process ID
     * @param pid - Process ID
     */
    public void setPid(int pid) {
        this.pid = pid;
    }
    
}
