import java.util.LinkedList;
import java.util.Random;

/**
 *
 * @author Matthew Welsh
 */
public class KernelandProcess {

    private int[] data;
    private String name;
    private UserlandProcess ulp;
    private int pid;
    UserlandProcess.ProcessState state;
    private static int[] pageTable;
    private static Random random = new Random();
    private VirtualToPhysicalMapping[] virtualToPhysicalMappings;

    // Constructor that initializes the page table with -1 values
    public KernelandProcess() {
        this.data = new int[10];

        this.pageTable = new int[100]; // Assuming 100 virtual pages
        for (int i = 0; i < pageTable.length; i++) {
            pageTable[i] = -1; // Set all entries to -1 (no mapping)
        }

        this.virtualToPhysicalMappings = new VirtualToPhysicalMapping[100];
        for (int i = 0; i < virtualToPhysicalMappings.length; i++) {
            virtualToPhysicalMappings[i] = new VirtualToPhysicalMapping();
        }
    }

    //Initializes a KernelandProcess object with a specified UserlandProcess and process ID.
    public KernelandProcess(UserlandProcess ulp, int pid) {
        this.ulp = ulp;
        this.name = ulp.getClass().getSimpleName();
        this.pid = pid;
    }

    public int getPhysicalPageNumber(int virtualPageNumber) {
        return virtualToPhysicalMappings[virtualPageNumber].getPhysicalPageNumber();
    }

    public void setPhysicalPageNumber(int virtualPageNumber, int physicalPageNumber) {
        virtualToPhysicalMappings[virtualPageNumber].setPhysicalPageNumber(physicalPageNumber);
    }

    public int getDiskPageNumber(int virtualPageNumber) {
        return virtualToPhysicalMappings[virtualPageNumber].getDiskPageNumber();
    }

    public void setDiskPageNumber(int virtualPageNumber, int diskPageNumber) {
        virtualToPhysicalMappings[virtualPageNumber].setDiskPageNumber(diskPageNumber);
    }

    // Method to update TLB and try to find a mapping
    public static int GetMapping(int virtualPageNumber) {
        int tlbIndexToUpdate = random.nextInt(2);
        int physicalPageNumber = random.nextInt(1024);
        pageTable[virtualPageNumber] = physicalPageNumber;

        return tlbIndexToUpdate;
    }

    //Retrieves the process ID associated with this KernelandProcess.
    public int getPid() {
        return pid;
    }

    /**
     * Gets the process name.
     * @return The process name.
     */
    public String getName() {
        return name;
    }

    /**
     * Retrieves the data stored at the specified index in the data array.
     * @param index The index of the data element to retrieve.
     * @return The data value stored at the specified index.
     * @throws IllegalArgumentException
     */
    public int getData(int index) {
        if (index >= 0 && index < data.length) {
            return data[index];
        } else {
            throw new IllegalArgumentException("Index out of bounds");
        }
    }

    /**
     * Sets the data at the specified index in the data array.
     * @param index - where the data will be stored.
     * @param value The value to be stored at the specified index.
     * @throws IllegalArgumentException
     */
    public void setData(int index, int value) {
        if (index >= 0 && index < data.length) {
            data[index] = value;
        } else {
            throw new IllegalArgumentException("Index out of bounds");
        }
    }

    /**
     * Finds an empty index in the data array.
     * @return The index of the first empty slot found, or -1 if no empty slot is available.
     */
    public int findEmptyIndex() {
        for (int i = 0; i < data.length; i++) {
            if (data[i] == -1) {
                return i;
            }
        }
        return -1; // No empty index found
    }

    public UserlandProcess.ProcessState getState() {
        return ulp.getState();
    }

    public void setState(UserlandProcess.ProcessState state) {
        this.state = state;
    }

    public boolean isWaitingForMessage() {
        return true;
    }
}