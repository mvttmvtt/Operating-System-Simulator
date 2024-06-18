import java.util.Random;

/**
 * Represents a device that generates random data.
 * @author Matthew Welsh
 */
public class RandomDevice implements Device {

    //Array of Random instances to generate random data.
    private Random[] randomDevices;

    //Initializes the RandomDevice with an array of Random instances.
    //Default size of the array is 10.
    public RandomDevice() {
        randomDevices = new Random[10];
    }

    //Opens a new Random device with an optional seed.
    @Override
    public int Open(String s) {
        for (int i = 0; i < randomDevices.length; i++) {
            if (randomDevices[i] == null) {
                if (s != null && !s.isEmpty()) {
                    // If seed is provided, use it to create Random instance
                    int seed = Integer.parseInt(s);
                    randomDevices[i] = new Random(seed);
                } else {
                    // Create Random instance without seed
                    randomDevices[i] = new Random();
                }
                return i; // Return the index as the device id
            }
        }
        return -1;
    }

    //Closes the Random device with the specified ID.
    @Override
    public void Close(int id) {
        if (id >= 0 && id < randomDevices.length) {
            randomDevices[id] = null;
        }
    }

    //Reads random data from the Random device with the specified ID.
    @Override
    public byte[] Read(int id, int size) {
        if (id >= 0 && id < randomDevices.length && randomDevices[id] != null && size > 0) {
            byte[] randomBytes = new byte[size];
            randomDevices[id].nextBytes(randomBytes);
            return randomBytes;
        }
        return null;
    }

    //Performs a seek operation on the Random device with the specified ID.
    @Override
    public void Seek(int id, int to) {
        if (id >= 0 && id < randomDevices.length && randomDevices[id] != null) {
            byte[] randomBytes = new byte[to];
            randomDevices[id].nextBytes(randomBytes);
        }
    }

    //Writes data to the Random device with the specified ID (not supported).
    @Override
    public int Write(int id, byte[] data) {
        return 0;
    }
}
