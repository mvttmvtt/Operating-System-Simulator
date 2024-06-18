import java.util.Arrays;

/**
 * Represents a message sent between processes in the kernel.
 * Contains information such as sender PID, target PID, message type, and message data.
 * @author Matthew Welsh
 */
public class KernelMessage {

    // Variables to store message information
    private int senderPid;
    private int targetPid;
    private int messageType;
    private byte[] data;

    //Constructs a new KernelMessage object with the given parameters.
    public KernelMessage(int senderPid, int targetPid, int messageType, byte[] data) {
        this.senderPid = senderPid;
        this.targetPid = targetPid;
        this.messageType = messageType;
        this.data = data.clone();
    }

    //Constructs a new KernelMessage object by copying another KernelMessage.
    public KernelMessage(KernelMessage message) {
        this.targetPid = message.getTargetPid();
        this.messageType = message.getMessageType();
        this.data = message.getData().clone();
    }

    //Retrieves the PID of the sender process.
    public int getSenderPid() {
        return senderPid;
    }

    //Sets the PID of the sender process.
    public void setSenderPid(int senderPid) {
        this.senderPid = senderPid;
    }

    //Retrieves the PID of the target process.
    public int getTargetPid() {
        return targetPid;
    }

    //Sets the PID of the target process.
    public void setTargetPid(int TargetPid) {
        this.targetPid = TargetPid;
    }

    //Retrieves the type of the message.
    public int getMessageType() {
        return messageType;
    }

    //Retrieves the data associated with the message.
    public byte[] getData() {
        return data;
    }

    //Returns a string representation of the KernelMessage object.
    @Override
    public String toString() {
        return "KernelMessage{" +
                "senderPid: " + senderPid +
                ", targetPid: " + targetPid +
                ", messageType: " + messageType +
                ", data: " + Arrays.toString(data) +
                '}';
    }
}
