import jdk.nashorn.internal.codegen.CompilerConstants;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Serves as the interface between the Userland process and the kernel
 * @author Matthew Welsh
 */
public class OS {

    //Private static members to store the parameters, the kernel instance, the return value, and the current call type
    private static ArrayList<Object> parameters = new ArrayList<>();
    private static Kernel instance = new Kernel();
    private static Object returnVal = 0;
    private static CallType currentCall;

    //Map to store process names and their corresponding PIDs
    private static HashMap<String, Integer> processNameToPidMap = new HashMap<>();

    // HashMap to track allocated memory blocks
    private static HashMap<Integer, Integer> allocatedMemory = new HashMap<>();

    //An accessor used to retrieve the parameters list
    public static ArrayList<Object> getParameters() {
        return parameters;
    }

    //Retrieves the PID of the current process.
    public static int GetPid() {
        return processNameToPidMap.getOrDefault("",-1);
    }

    //Retrieves the PID of a process by its name.
    public static int GetPidByName(String name) {
        return processNameToPidMap.getOrDefault(name, -1);
    }

    //Adds a process name and its corresponding PID to the map.
    public static void addProcessName(String name, int pid) {
        processNameToPidMap.put(name, pid);
    }


    //An accessor used to retrieve the current call type
    public static CallType getCurrentCall() {
        return currentCall;
    }

    //An accessor used to retrieve the kernel instance
    public static Kernel getInstance() {
        return instance;
    }

    //An enum used to represent the different system calls
    public enum CallType {
        create_process, switch_process,
        sleep, allocate_memory, free_memory
    }

    //An enum used to represent the different priorities
    public enum Priority {
        HIGH, INTERACTIVE, LOW
    }


    /**
     * Creates a new Userland Process
     * @param up
     * @return returnVal - casted return val
     */
    public static int CreateProcess(UserlandProcess up) {
        return CreateProcess(up, Priority.INTERACTIVE);
    }

    /**
     * Creates a new Userland process of a specific priority
     * @param up
     * @param priority
     * @return - The pid
     */
    public static int CreateProcess(UserlandProcess up, Priority priority) {
        parameters.clear();
        parameters.add(up);
        parameters.add(priority); // Add priority parameter

        //populates the hashmap
        if (up instanceof PingProcess) {
            int pongPid = 3;
            KernelandProcess kp = new KernelandProcess(up, pongPid);
            addProcessName("PongProcess", pongPid);
            Kernel.addProcess(kp.getPid(), kp);
        }

        if (up instanceof PongProcess) {
            int pingPid = 2;
            KernelandProcess kp = new KernelandProcess(up, pingPid);
            addProcessName("PingProcess", pingPid);
            Kernel.addProcess(kp.getPid(), kp);
        }

        currentCall = CallType.create_process;
        new PCB(up, priority);
        switchToKernel();
        return (int)returnVal;
    }

    /**
     * Starts the Operating System
     * @param init
     */
    public static void Startup(UserlandProcess init) {

        //instance = new Kernel();
        CreateProcess(init);
        CreateProcess(new IdleProcess());
    }

    //Switches to the Kernel
    public static void switchToKernel() {

        instance.start();

        //Retrieves the currently running process
        UserlandProcess currentlyRunning = instance.getMyScheduler().getCurrentProcess();

        //Stops a process if it is running
        if (currentlyRunning != null) {
            currentlyRunning.stop();
        } else {

            boolean processAvailable = false;
            while (!processAvailable) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e){
                    e.printStackTrace();
                }
                // Check if a process has become available
                if (instance.getMyScheduler().getCurrentProcess() != null) {
                    processAvailable = true;
                }
            }
        }
    }

    //Sets the current call type and switches the processes
    public static void SwitchProcess() {
        currentCall = CallType.switch_process;
        switchToKernel();
    }

    //Method to allocate memory
    public static int allocateMemory(int size) {
        if (size % 1024 != 0) {
            System.out.println("Size must be a multiple of 1024 bytes.");
            return -1;
        }

        parameters.clear();
        parameters.add(size);
        currentCall = CallType.allocate_memory;
        switchToKernel(); //debug here
        System.out.println("Parameter Size: " + parameters.size());
        System.out.println("Parameters: " + parameters);
        if (parameters.size() >= 1) {
            Integer allocatedAddress = (Integer) parameters.get(0);
            System.out.println("Allocated Address: " + allocatedAddress);
            return allocatedAddress != null ? allocatedAddress: -1;
        }
        else {
            System.out.println("No address was allocated.");
            return -1;
        }
    }

    public static boolean freeMemory(int pointer, int size) {
        if (pointer % 1024 != 0 || size % 1024 != 0) {
            System.out.println("Size must be a multiple of 1024 bytes.");
            return false;
        }
        parameters.clear();
        parameters.add(pointer);
        parameters.add(size);
        currentCall = CallType.free_memory;
        switchToKernel();
        System.out.println(parameters);
        boolean success = (Boolean) parameters.get(2);
        System.out.println("Free memory success status: " + success + " --OS");
        return Boolean.TRUE.equals(success);
    }

    //Puts the current process to sleep for the specified duration.
    public static void sleep(int milliseconds) {
        parameters.clear();
        parameters.add(milliseconds);
        currentCall = CallType.sleep;
        switchToKernel();
    }

}

