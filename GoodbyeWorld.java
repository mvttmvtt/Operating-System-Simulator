/**
 * Represents a Userland Process that prints "Goodbye World" infinitely
 * @author Matthew Welsh
 */
public class GoodbyeWorld extends UserlandProcess {

    /**
     * Prints "Goodbye World" infinitely, calls cooperate(),
     * and sleeps for 50 milliseconds after each iteration.
     */
    @Override
    public void main() {

        while (true) {
            System.out.println("Goodbye World");
            cooperate(); //cooperates with the operating system
            try {
                Thread.sleep(50); //sleeps for 50 ms
            } catch (Exception e) { }
        }
    }
}