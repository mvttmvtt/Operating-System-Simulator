import java.util.Timer;
import java.util.TimerTask;

/**
 * The starting point of the Operating System
 * @author Matthew Welsh
 */
public class Main {

    public static void main(String[] args) throws Exception {

//        OS.Startup(new HelloWorld()); //Starts the operating system with HelloWorld
//        OS.CreateProcess(new GoodbyeWorld()); //Creates a process for GoodbyeWorld
//        new PCB(new RealTimeProcess(), OS.Priority.HIGH);

//        OS.Startup(new PingProcess()); // Create and start a PingProcess
//        OS.CreateProcess(new PongProcess()); // Create and start a PongProcess
//
//        //A timer task that switches the process every 100 ms
//        Timer timer = new Timer(true);
//        timer.scheduleAtFixedRate(new TimerTask() {
//
//            @Override
//            public void run() {
//                Scheduler.getCurrentProcess().requestStop();
//            }
//        },0,100);

        AllocateAndFreeMemory();
        ReadAndWriteMemory();

    }

    //Tests Allocating and Freeing Memory
    public static void AllocateAndFreeMemory() {

        int size = 1024;

        IdleProcess idleProcess = new IdleProcess();
        OS.Startup(idleProcess);

        int startVirtualAddress = OS.allocateMemory(size);

        if (startVirtualAddress == -1) {
            System.out.println("Memory allocation failed");
        }

        byte valueToWrite = 42;
        UserlandProcess currentProcess = OS.getInstance().getMyScheduler().getCurrentProcess();
        currentProcess.Write(startVirtualAddress, valueToWrite);
        byte readValue = currentProcess.Read(startVirtualAddress);
        if (readValue != valueToWrite) {
            System.out.println("Memory read/write failed");
            return;
        }

        boolean freed = OS.freeMemory(startVirtualAddress, size);

        if (!freed) {
            System.out.println("Memory freeing failed");
            return;
        }

        int newVirtualAddress = OS.allocateMemory(size);

        if (newVirtualAddress != startVirtualAddress) {
            System.out.println("Memory reallocation failed");
            return;
        }
    }

    //Tests Reading and Writing Memory
    public static void ReadAndWriteMemory() {
        int size = 1024;

        IdleProcess idleProcess = new IdleProcess();
        OS.Startup(idleProcess);

        int allocatedAddress = OS.allocateMemory(size);

        byte testValue = 42;
        System.out.println("Allocated address: " + allocatedAddress);
        byte readValue = idleProcess.Read(allocatedAddress);

        if (readValue != testValue) {
            System.out.println("Memory read/write failed");
            return;
        }

        boolean freed = OS.freeMemory(allocatedAddress, size);

        if (!freed) {
            System.out.println("Memory freeing failed");
            return;
        }
    }
}