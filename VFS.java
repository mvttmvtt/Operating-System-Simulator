import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.io.IOException;

/**
 * Represents a Virtual File System (VFS) that maps calls to other devices and IDs.
 * It maintains an array of devices and an array of IDs to map VFS IDs to (Device/ID) combinations.
 * Implements the Device interface.
 * @author Matthew Welsh
 */
public class VFS implements Device {

    private Device[] devices;
    private int[] deviceIds;
    private Random random;
    private static File swapFile;

    /**
     * Constructs a VFS with the specified maximum size.
     * @param maxSize The maximum size of the VFS.
     */
    public VFS(int maxSize) {
        devices = new Device[maxSize];
        deviceIds = new int[maxSize];
    }

    public VFS() {
        this.random = new Random();
    }

    // Perform operations to open the swap file
    public void openSwapFile() {

        String swapFilePath = "Swapfile.txt";

        // Create a File object for the swap file
        swapFile = new File(swapFilePath);

        // Check if the swap file exists
        if (!swapFile.exists()) {
            try {
                // Create a new swap file if it doesn't exist
                if (swapFile.createNewFile()) {
                    System.out.println("Swap file created successfully.");
                } else {
                    System.out.println("Failed to create swap file.");
                }
            } catch (IOException e) {
                System.out.println("An error occurred while creating the swap file: " + e.getMessage());
            }
        } else {
            System.out.println("Swap file already exists.");
        }
    }

    /**
     * Opens a file/device and returns a VFS ID.
     * @param s The filename or identifier (not used in this implementation).
     * @return The VFS ID or -1 if no available slot is found.
     */
    @Override
    public int Open(String s) {
        // Find an available slot in the VFS
        for (int i = 0; i < devices.length; i++) {
            if (devices[i] == null) {
                return i; // Return the VFS ID
            }
        }
        return -1; // Return -1 if no available slot is found
    }

    /**
     * Closes the device associated with the given VFS ID.
     * @param id The VFS ID.
     */
    @Override
    public void Close(int id) {
        // Close the device associated with the given VFS ID
        if (id >= 0 && id < devices.length) {
            devices[id] = null;
            deviceIds[id] = 0;
        }
    }

    /**
     * Reads data from the device associated with the given VFS ID.
     * @param id The VFS ID.
     * @param size The size of data to read.
     * @return The bytes read from the device or an empty byte array if not found or closed.
     */
    @Override
    public byte[] Read(int id, int size) {
        // Read data from the device associated with the given VFS ID
        if (id >= 0 && id < devices.length && devices[id] != null) {
            int deviceIndex = deviceIds[id];
            return devices[id].Read(deviceIndex, size);
        }
        return new byte[0]; // Return an empty byte array if the device is not found or closed
    }

    /**
     * Moves the read/write pointer to the specified position on the device.
     * @param id The VFS ID.
     * @param to The position to seek.
     */
    @Override
    public void Seek(int id, int to) {
        if (id >= 0 && id < devices.length && devices[id] != null) {
            int deviceIndex = deviceIds[id];
            devices[id].Seek(deviceIndex, to);
        }
    }

    /**
     * Writes data to the device associated with the given VFS ID.
     * @param id The VFS ID.
     * @param data The data to write.
     * @return The number of bytes written or 0 if not found or closed.
     */
    @Override
    public int Write(int id, byte[] data) {
        if (id >= 0 && id < devices.length && devices[id] != null) {
            int deviceIndex = deviceIds[id];
            return devices[id].Write(deviceIndex, data);
        }
        return 0;
    }

    /**
     * Additional method to associate a device with a VFS ID
     */
    public void MountDevice(Device device, int vfsId) {
        if (vfsId >= 0 && vfsId < devices.length) {
            devices[vfsId] = device;
            deviceIds[vfsId] = device.Open(null);
        }
    }
}
