import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.Semaphore;

/**
 * Represents the core of the Operating system
 * Manages system calls, process scheduling, and execution
 * @author Matthew Welsh
 */
public class Kernel implements Runnable, Device{

    /**
     * private members to store the scheduler, the thread, and the semaphore
     */
    private static Scheduler myScheduler;
    private Thread thread;
    private Semaphore mySemaphore;
    private static HashMap<Integer, KernelandProcess> processMap = new HashMap<>();
    private static LinkedList<KernelMessage> messageQueue = new LinkedList<>();
    private static HashMap<Integer, UserlandProcess> waitingProcesses = new HashMap<>();

    // TLB array with virtual to physical mappings
    private static final int[][] TLB = new int[2][2];

    // Physical memory array
    private static final byte[] memory = new byte[1024 * 1024]; // 1MB of memory

    private KernelandProcess kap;
    private VFS vfs;

    private static int nextPageToWriteOut;
    private static VFS swapFileSystem;

    //Initializes the virtual file system
    public Kernel(VFS vfs) {
        this.vfs = vfs;
    }

    /**
     * A constructor that initializes the scheduler, thread, and semaphore
     */
    public Kernel() {
        this.myScheduler = new Scheduler();
        this.thread = new Thread(this);
        this.mySemaphore = new Semaphore(0);
        this.thread.start();
    }

    public static void main(String[] args) {
        // Open swap file on startup
        swapFileSystem = new VFS();
        swapFileSystem.openSwapFile();

        nextPageToWriteOut = 0;
    }

    // Reads a byte from physical memory
    public static byte ReadMemory(int physicalAddress) {
        return memory[physicalAddress];
    }

    //
    public static void WriteMemory(int physicalAddress, byte value) {

        if (physicalAddress >= 0 && physicalAddress < memory.length) {
            memory[physicalAddress] = value;
        } else {
            System.err.println("Invalid physical address: " + physicalAddress);
        }
    }

    //Accessor used to retrieve the scheduler instance/
    public Scheduler getMyScheduler() {
        return myScheduler;
    }

    // Method to add a process to the scheduler
    public static void addProcess(int pid, KernelandProcess process) {
        processMap.put(pid, process);
    }

    // Method to get the current process's PID
    public static int getPid() {
        return myScheduler.getCurrentProcess().getPid();
    }

    // Method to get the PID of a process by name
    public int getPidByName(String name) {
        for (Map.Entry<Integer, KernelandProcess> entry : processMap.entrySet()) {
            KernelandProcess process = entry.getValue();
            if (process.getName().equals(name)) {
                return entry.getKey(); // Return the PID if the name matches
            }
        }
        return -1; // Return -1 if no process with the given name is found
    }

    //
    public static void SendMessage(KernelMessage message) {
        // Make a copy of the message using the copy constructor
        KernelMessage copiedMessage = new KernelMessage(message);

        //sets sending pid
        copiedMessage.setSenderPid(message.getSenderPid());

        KernelandProcess targetProcess = processMap.get(message.getTargetPid());

        if (targetProcess != null) {
            getMessageQueue().add(copiedMessage);

            if (targetProcess.isWaitingForMessage()) {
                waitingProcesses.remove(targetProcess.getPid());
                targetProcess.setState(UserlandProcess.ProcessState.READY);
            }
        }
    }

    public static KernelMessage WaitForMessage() {

        // Check if the process has any pending messages
        if (!messageQueue.isEmpty()) {
            // If there are messages, remove the first message from the queue and return it
            return messageQueue.removeFirst();
        } else {
            // If there are no messages, suspend the process until a message is received
            PingProcess process = new PingProcess();
            process.setState(UserlandProcess.ProcessState.SLEEPING);

            waitingProcesses.put(getPid(), myScheduler.getCurrentProcess());
            OS.SwitchProcess();
            System.out.println(waitingProcesses.get(0).getName());
            return null;
//            return messageQueue.removeFirst(); // Return the received message
        }
    }

    //
    public static LinkedList<KernelMessage> getMessageQueue() {
        return messageQueue;
    }

    /**
     * The run method of the Kernel thread
     */
    @Override
    public void run() {

        while (true) {
            try {
                //System.out.println("at run in Kernel");
                mySemaphore.acquire(); //Acquires the semaphore to allow kernel execution

                switch (OS.getCurrentCall()) {
                    case create_process:
                        //creates a new process based on parameters
                        myScheduler.CreateProcess((UserlandProcess) OS.getParameters().get(0));
                        break;

                    case switch_process:
                        //switches to next process
                        myScheduler.SwitchProcess();
                        break;

                    case sleep:
                        //Calls sleep() in the scheduler
                        myScheduler.sleep((Integer) OS.getParameters().get(0));
                        break;

                    case allocate_memory:
                        int startAddress = OS.allocateMemory(1024); //debug here
                        System.out.println(startAddress);
                        OS.getParameters().add(startAddress);
                        break;

                    case free_memory:
                        int pointer = (Integer) OS.getParameters().get(0);
                        int freeSize = (Integer) OS.getParameters().get(1);
                        boolean success = OS.freeMemory(pointer, freeSize);
                        System.out.println("Free memory success status: " + success + " --Kernel");
                        OS.getParameters().add(success);
                        break;
                }
//                if (myScheduler.getCurrentProcess().getState() == UserlandProcess.ProcessState.RUNNING) {
//                    myScheduler.getCurrentProcess().start(); //executes the current process
//                }
                myScheduler.getCurrentProcess().start();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Starts the kernel by releasing the semaphore
     */
    public void start() {
        mySemaphore.release();
    }

    /**
     * Opens a file in the VFS and associates it with the currently running process.
     * @param file The name of the file to open.
     * @return The VFS ID associated with the opened file or -1 if failed.
     */
    @Override
    public int Open(String file) {
        UserlandProcess currentProcess = myScheduler.getCurrentProcess();

        int emptyIndex = kap.findEmptyIndex();

        if (emptyIndex == -1) {
            return -1;
        }

        int vfsId = vfs.Open(file);

        if (vfsId == -1) {
            return -1;
        }

        kap.setData(emptyIndex, vfsId);
        return emptyIndex;
    }

    /**
     * Closes a file associated with the provided VFS ID.
     * @param id The VFS ID associated with the file.
     */
    @Override
    public void Close(int id) {
        int vfsId = kap.getData(id);
        kap.setData(id, -1);
        vfs.Close(vfsId);
    }

    /**
     * Reads data from a file associated with the provided VFS ID.
     * @param id The VFS ID associated with the file.
     * @param size The size of data to read.
     * @return The read data.
     */
    @Override
    public byte[] Read(int id, int size) {
        int vfsId = kap.getData(id);
        return vfs.Read(vfsId, size);
    }

    /**
     * Seeks to a position in a file associated with the provided VFS ID.
     * @param id The VFS ID associated with the file.
     * @param to The position to seek to.
     */
    @Override
    public void Seek(int id, int to) {
        int vfsId = kap.getData(id);
        vfs.Seek(vfsId, to);
    }

    /**
     * Writes data to a file associated with the provided VFS ID.
     * @param id The VFS ID associated with the file.
     * @param data The data to write.
     * @return The number of bytes written.
     */
    @Override
    public int Write(int id, byte[] data) {
        int vfsId = kap.getData(id);
        return vfs.Write(vfsId, data);
    }

    /**
     * Closes all devices associated with a specific UserlandProcess.
     * @param process The UserlandProcess whose devices need to be closed.
     */
    public void closeDevicesForProcess(UserlandProcess process) {
        for (int id : process.getOpenDevices()) {
            vfs.Close(id);
        }
    }

}
