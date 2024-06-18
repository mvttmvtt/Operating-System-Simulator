/**
 * Represents a mapping between a virtual page and a physical page,
 * along with its corresponding disk page number.
 * @author Matthew Welsh
 */
public class VirtualToPhysicalMapping {

    private int physicalPageNumber;
    private int diskPageNumber;

    //Constructs a new VirtualToPhysicalMapping object with default values.
    //Initializes physicalPageNumber and diskPageNumber to -1.
    public VirtualToPhysicalMapping() {
        this.physicalPageNumber = -1;
        this.diskPageNumber = -1;
    }

    //Gets the physical page number associated with this mapping.
    public int getPhysicalPageNumber() {
        return physicalPageNumber;
    }

    //Sets the physical page number associated with this mapping.
    public void setPhysicalPageNumber(int physicalPageNumber) {
        this.physicalPageNumber = physicalPageNumber;
    }

    //Gets the disk page number associated with this mapping.
    public int getDiskPageNumber() {
        return diskPageNumber;
    }

    //Sets the disk page number associated with this mapping.
    public void setDiskPageNumber(int diskPageNumber) {
        this.diskPageNumber = diskPageNumber;
    }

}
