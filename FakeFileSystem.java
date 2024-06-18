import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

public class FakeFileSystem {

    private Map<String, RandomAccessFile> fileMap;

    public FakeFileSystem() {
        this.fileMap = new HashMap<>();
    }

    /**
     * Opens a file and records it in the internal array.
     * @param filename The name of the file to open.
     * @throws IllegalArgumentException if the filename is empty or null.
     * @throws IOException if an I/O error occurs.
     */
    public void open(String filename) throws IOException {
        if (filename == null || filename.isEmpty()) {
            throw new IllegalArgumentException("Filename cannot be empty or null");
        }

        RandomAccessFile file = new RandomAccessFile(filename, "rw");
        fileMap.put(filename, file);
    }

    /**
     * Reads from the specified file.
     * @param filename The name of the file to read from.
     * @param position The position in the file to start reading from.
     * @param length The number of bytes to read.
     * @return The bytes read from the file.
     * @throws IOException if an I/O error occurs.
     */
    public byte[] read(String filename, long position, int length) throws IOException {
        RandomAccessFile file = fileMap.get(filename);
        if (file == null) {
            throw new IllegalArgumentException("File not found: " + filename);
        }

        byte[] buffer = new byte[length];
        file.seek(position);
        file.read(buffer);
        return buffer;
    }

    /**
     * Writes to the specified file.
     * @param filename The name of the file to write to.
     * @param position The position in the file to start writing to.
     * @param data The bytes to write to the file.
     * @throws IOException if an I/O error occurs.
     */
    public void write(String filename, long position, byte[] data) throws IOException {
        RandomAccessFile file = fileMap.get(filename);
        if (file == null) {
            throw new IllegalArgumentException("File not found: " + filename);
        }

        file.seek(position);
        file.write(data);
    }

    /**
     * Moves the file pointer to the specified position in the file.
     * @param filename The name of the file to seek.
     * @param position The position in the file to move to.
     * @throws IOException if an I/O error occurs.
     */
    public void seek(String filename, long position) throws IOException {
        RandomAccessFile file = fileMap.get(filename);
        if (file == null) {
            throw new IllegalArgumentException("File not found: " + filename);
        }

        file.seek(position);
    }

    /**
     * Closes the specified file and removes it from the internal array.
     * @param filename The name of the file to close.
     * @throws IOException if an I/O error occurs.
     */
    public void close(String filename) throws IOException {
        RandomAccessFile file = fileMap.remove(filename);
        if (file != null) {
            file.close();
        }
    }
}
