import java.util.*;
import java.time.Clock;
import java.time.Instant;

/**
 * Manages the scheduling of the Userland Process
 * @author Matthew Welsh
 */
public class Scheduler {

    private static Clock clock = Clock.systemDefaultZone();

    /**
     * Private static members to store the list of scheduler queues, the timer instance,
     * the currently running process, and the Process ID counter
     */
    private static LinkedList<UserlandProcess> highPriorityQueue;
    private static LinkedList<UserlandProcess> interactivePriorityQueue;
    private static LinkedList<UserlandProcess> lowPriorityQueue;
    private static LinkedList<UserlandProcess> sleepProcesses;
    private static LinkedList<UserlandProcess> readyProcesses;

    private Kernel kernel;
    private Random random;
    private static Timer timer;
    private static UserlandProcess currentProcess;
    private static OS.Priority currentPriority;
    private static int nextPid = 1; //Static Process ID counter
    private Map<Integer, UserlandProcess> processes;


    /**
     * Constructor that initializes the Scheduler and
     * schedules to interrupt every 250ms
     */
    public Scheduler() {

        // Queues for processes with different priorities
        highPriorityQueue = new LinkedList<>();
        interactivePriorityQueue = new LinkedList<>();
        lowPriorityQueue = new LinkedList<>();

        // Queues for sleeping and ready processes
        sleepProcesses = new LinkedList<>();
        readyProcesses = new LinkedList<>();

        random = new Random();
        timer = new Timer();
        //currentProcess = null;

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (currentProcess != null) {
                    currentProcess.requestStop();
                }
            }
        }, 0, 250);
    }

    //Constructs a Scheduler with a reference to the Kernel.
    public Scheduler(Kernel kernel) {
        this.kernel = kernel;
    }

    //Checks if a process has ended and closes its associated devices if it has.
    public void checkProcessEnd(UserlandProcess process) {
        if (process.isDone()) {
            kernel.closeDevicesForProcess(process);
        }
    }

    // Method to get the current process's PID
    public int getPid() {
        return kernel.getMyScheduler().getCurrentProcess().getPid();
    }

    // Method to get the PID of a process by name
    public int getPidByName(String name) {
        for (Map.Entry<Integer, UserlandProcess> entry : processes.entrySet()) {
            UserlandProcess process = entry.getValue();
            if (process.getName().equals(name)) {
                return entry.getKey(); // Return the PID if the name matches
            }
        }
        return -1; // Return -1 if no process with the given name is found
    }

    /**
     * Get the current time as an Instant.
     * @return The current time.
     */
    private static Instant getCurrentTime() {
        return clock.instant();
    }

    /**
     * Adds a userland process to the list and starts it if nothing else is running.
     * @param up - The userland process to add
     * @return The size of the scheduler
     */
    public int CreateProcess(UserlandProcess up) {
        return CreateProcess(up, OS.Priority.INTERACTIVE);
    }

    /**
     * Creates a new Userland process of a specific priority
     * @param up
     * @param priority
     * @return The pid
     */
    public int CreateProcess(UserlandProcess up, OS.Priority priority) {

        currentProcess = up;
        currentPriority = priority;
//        //up.setState(UserlandProcess.ProcessState.RUNNING);
//        up.setPid(nextPid++);

        switch (priority) {
            case HIGH:
                highPriorityQueue.add(up);
                break;
            case INTERACTIVE:
                interactivePriorityQueue.add(up);
                break;
            case LOW:
                lowPriorityQueue.add(up);
                break;
            default:
                break;
        }

        if (currentProcess != null) {
            SwitchProcess();
        }
        return nextPid;
    }

    /**
     * Takes the currently running process and puts it at the end,
     * then starts the next process
     */
    public void SwitchProcess() {

        if (currentProcess != null) {
            switch (currentPriority) {
                case HIGH:
                    highPriorityQueue.addLast(currentProcess);
                    break;
                case INTERACTIVE:
                    interactivePriorityQueue.addLast(currentProcess);
                    break;
                case LOW:
                    lowPriorityQueue.addLast(currentProcess);
                    break;
                default:
                    break;
            }
        }

        int QueueIndex = random.nextInt(3);
        LinkedList<UserlandProcess> selectedQueue = null;
        switch (QueueIndex) {
            case 0:
                    //System.out.println("high");
                    selectedQueue = highPriorityQueue;
                break;
            case 1:
                    //System.out.println("medium");
                    selectedQueue = interactivePriorityQueue;
                break;
            case 2:
                    //System.out.println("low");
                    selectedQueue = lowPriorityQueue;
                break;
            default:
                break;
        }

        // Start the next process from the selected queue
        if (selectedQueue != null && !selectedQueue.isEmpty()) {
            currentProcess = selectedQueue.poll();
        }

        if (currentProcess != null && !currentProcess.isDone()) {
            currentProcess.start();
        }

//        wakeupSleepingProcesses();
    }

    //DELETE SOON!

//    /**
//     * An accessor used to retrieve the high Priority queue
//     * @return highPriorityQueue - The high Priority queue
//     */
//    public LinkedList<UserlandProcess> getHighPriorityQueue() {
//        return highPriorityQueue;
//    }
//
//    /**
//     * An accessor used to retrieve the medium Priority queue
//     * @return mediumPriorityQueue - The medium Priority queue
//     */
//    public LinkedList<UserlandProcess> getInteractivePriorityQueue() {
//        return interactivePriorityQueue;
//    }
//
//    /**
//     * An accessor used to retrieve the low Priority queue
//     * @return lowPriorityQueue - The low Priority queue
//     */
//    public LinkedList<UserlandProcess> getLowPriorityQueue() {
//        return lowPriorityQueue;
//    }

    /**
     * An accessor used to retrieve the currently running process
     * @return currentProcess - The currently running process
     */
    public static UserlandProcess getCurrentProcess() {
        return currentProcess;
    }

    /**
     * Puts the current process to sleep for the specified duration.
     * @param milliseconds The duration to sleep in milliseconds.
     */
    public void sleep(int milliseconds) {

        UserlandProcess process = getCurrentProcess(); // Obtain the current process
        Instant currentTime = Instant.now(); // Get the current time as an Instant

        Instant wakeupTime = currentTime.plusMillis(milliseconds);

        process.setWakeupTime(wakeupTime);
        process.setState(UserlandProcess.ProcessState.SLEEPING);

        sleepProcesses.add(process);

        // Switch to the next process
        SwitchProcess();
    }

    //Wakes up sleeping processes whose wakeup time has passed
    public void wakeupSleepingProcesses() {
        Instant currentTime = getCurrentTime(); // Get current system time
        while (!sleepProcesses.isEmpty() && sleepProcesses.peek().getWakeupTime().isBefore(currentTime)) {
            UserlandProcess process = sleepProcesses.poll();
            process.setState(UserlandProcess.ProcessState.READY); // Transition the process to the ready state
            readyProcesses.add(process); // Add the process to the ready queue
        }
    }
}
